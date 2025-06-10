package com.example.erp.service.salary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public int updateBaseSalaries(double newBaseSalary, String salaryComponent, String comparisonOperator, double threshold) {
        if (!"greater".equals(comparisonOperator) && !"less".equals(comparisonOperator)) {
            throw new IllegalArgumentException("Opérateur de comparaison invalide: " + comparisonOperator);
        }

        List<SalarySlip> salarySlips = getSalarySlipsWithComponents();
        List<String> listEmpMarina = new ArrayList<>();

        for (SalarySlip slip : salarySlips) {
            boolean marina = false;
            if (slip.getDeductions() != null) {
                for (SalaryDetail deduction : slip.getDeductions()) {
                    if (salaryComponent.equals(deduction.getSalaryComponent())) {
                        if (("greater".equals(comparisonOperator) && deduction.getAmount() > threshold) ||
                            ("less".equals(comparisonOperator) && deduction.getAmount() < threshold)) {
                            marina = true;
                            break;
                        }
                    }
                }
            }
            if (!marina && slip.getEarnings() != null) {
                for (SalaryDetail earning : slip.getEarnings()) {
                    if (salaryComponent.equals(earning.getSalaryComponent())) {
                        if (("greater".equals(comparisonOperator) && earning.getAmount() > threshold) ||
                            ("less".equals(comparisonOperator) && earning.getAmount() < threshold)) {
                            marina = true;
                            break;
                        }
                    }
                }
            }
            if (marina) {
                listEmpMarina.add(slip.getEmployee());
            }
        }

        if (listEmpMarina.isEmpty()) {
            throw new IllegalStateException("Aucun emp trouve");
        }

        int updatedCount = 0;
        for (String employee : listEmpMarina) {
            try {
                updateBaseSalaryForEmployee(employee, newBaseSalary);
                updatedCount++;
            } catch (RestClientException e) {
            }
        }

        return updatedCount;
    }

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

            ResponseEntity<SalarySlipResponse> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                entity,
                SalarySlipResponse.class
            );

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            } else {
                return new ArrayList<>();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error", e);
        } catch (RestClientException e) {
            throw new RuntimeException("Error " + e.getMessage(), e);
        }
    }

    private void updateBaseSalaryForEmployee(String employee, double newBaseSalary) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<List<Object>> filters = Arrays.asList(
            Arrays.asList("employee", "=", employee),
            Arrays.asList("docstatus", "=", 1) 
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

            ResponseEntity<Map> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                entity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("data") || ((List<?>) responseBody.get("data")).isEmpty()) {
                return;
            }

            Map<String, Object> assignment = ((List<Map<String, Object>>) responseBody.get("data")).get(0);
            String assignmentName = (String) assignment.get("name");

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("base", newBaseSalary);

            HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(updateData, headers);
            String updateUrl = ErpNextConfig.ERP_NEXT_API_URL + "Salary Structure Assignment/" + assignmentName;

            restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error", e);
        } catch (RestClientException e) {
            throw new RuntimeException("Error  " + e.getMessage(), e);
        }
    }
}