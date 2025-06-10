package com.example.erp.service.salary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.erp.config.ErpNextConfig;
import com.example.erp.dto.salary.SalarySlipResponse;
import com.example.erp.entity.salary.SalaryDetail;
import com.example.erp.entity.salary.SalarySlip;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UpdateBaseSalaryService {

    private static final Logger logger = LoggerFactory.getLogger(UpdateBaseSalaryService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));
        return headers;
    }

    // update base salaries for employees meeting the criteria
    public int updateBaseSalaries(double newBaseSalary, String salaryComponent, String comparisonOperator, double threshold) {
        // validate comparison operator
        if (!"greater".equals(comparisonOperator) && !"less".equals(comparisonOperator)) {
            throw new IllegalArgumentException("Opérateur de comparaison invalide: " + comparisonOperator);
        }

        // fetch salary slips with deductions and earnings
        List<SalarySlip> salarySlips = getSalarySlipsWithComponents();
        List<String> eligibleEmployees = new ArrayList<>();

        // identify employees meeting the criteria
        for (SalarySlip slip : salarySlips) {
            boolean meetsCriteria = false;
            // check deductions
            if (slip.getDeductions() != null) {
                for (SalaryDetail deduction : slip.getDeductions()) {
                    if (salaryComponent.equals(deduction.getSalaryComponent())) {
                        if (("greater".equals(comparisonOperator) && deduction.getAmount() > threshold) ||
                            ("less".equals(comparisonOperator) && deduction.getAmount() < threshold)) {
                            meetsCriteria = true;
                            break;
                        }
                    }
                }
            }
            // check earnings if not found in deductions
            if (!meetsCriteria && slip.getEarnings() != null) {
                for (SalaryDetail earning : slip.getEarnings()) {
                    if (salaryComponent.equals(earning.getSalaryComponent())) {
                        if (("greater".equals(comparisonOperator) && earning.getAmount() > threshold) ||
                            ("less".equals(comparisonOperator) && earning.getAmount() < threshold)) {
                            meetsCriteria = true;
                            break;
                        }
                    }
                }
            }
            if (meetsCriteria) {
                eligibleEmployees.add(slip.getEmployee());
            }
        }

        if (eligibleEmployees.isEmpty()) {
            throw new IllegalStateException("Aucun employé ne correspond aux critères spécifiés.");
        }

        // update base salary for each eligible employee
        int updatedCount = 0;
        for (String employee : eligibleEmployees) {
            try {
                updateBaseSalaryForEmployee(employee, newBaseSalary);
                updatedCount++;
            } catch (RestClientException e) {
                logger.error("Failed to update base salary for employee {}: {}", employee, e.getMessage());
            }
        }

        return updatedCount;
    }

    // fetch salary slips with earnings and deductions
    private List<SalarySlip> getSalarySlipsWithComponents() {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<String> fields = Arrays.asList("name", "employee", "earnings", "deductions");
        try {
            String fieldsJson = objectMapper.writeValueAsString(fields);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson);

            String finalUrl = builder.build(false).toUriString();
            logger.debug("Fetching salary slips from ERP Next: {}", finalUrl);

            ResponseEntity<SalarySlipResponse> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                entity,
                SalarySlipResponse.class
            );

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            } else {
                logger.warn("No salary slips found in ERPNext");
                return new ArrayList<>();
            }
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON for ERPNext Salary Slip API call", e);
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        } catch (RestClientException e) {
            logger.error("Error calling ERPNext Salary Slip API: {}", e.getMessage());
            throw new RuntimeException("Error calling ERPNext Salary Slip API: " + e.getMessage(), e);
        }
    }

    // update base salary in Salary Structure Assignment
    private void updateBaseSalaryForEmployee(String employee, double newBaseSalary) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // fetch the latest salary structure assignment
        List<List<Object>> filters = Arrays.asList(
            Arrays.asList("employee", "=", employee),
            Arrays.asList("docstatus", "=", 1) // only submitted assignments
        );
        List<String> fields = Arrays.asList("name", "base");

        try {
            String filtersJson = objectMapper.writeValueAsString(filters);
            String fieldsJson = objectMapper.writeValueAsString(fields);
            String url = ErpNextConfig.ERP_NEXT_API_URL + "Salary Structure Assignment";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("filters", filtersJson)
                    .queryParam("fields", fieldsJson)
                    .queryParam("order_by", "creation desc")
                    .queryParam("limit_page_length", "1");

            String finalUrl = builder.build(false).toUriString();
            logger.debug("Fetching salary structure assignment for employee {}: {}", employee, finalUrl);

            ResponseEntity<Map> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                entity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("data") || ((List<?>) responseBody.get("data")).isEmpty()) {
                logger.warn("No salary structure assignment found for employee: {}", employee);
                return;
            }

            Map<String, Object> assignment = ((List<Map<String, Object>>) responseBody.get("data")).get(0);
            String assignmentName = (String) assignment.get("name");

            // update the base salary
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("base", newBaseSalary);

            HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(updateData, headers);
            String updateUrl = ErpNextConfig.ERP_NEXT_API_URL + "Salary Structure Assignment/" + assignmentName;
            logger.debug("Updating base salary for employee {}: {}", employee, updateUrl);

            restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, Map.class);
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON for ERPNext Salary Structure Assignment API call", e);
            throw new RuntimeException("Error processing JSON for ERPNext Salary Structure Assignment API call", e);
        } catch (RestClientException e) {
            logger.error("Error calling ERPNext Salary Structure Assignment API: {}", e.getMessage());
            throw new RuntimeException("Error calling ERPNext Salary Structure Assignment API: " + e.getMessage(), e);
        }
    }
}