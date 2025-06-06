package com.example.erp.service.salary;

import com.example.erp.config.ErpNextConfig;
import com.example.erp.dto.salary.SalarySlipResponse;
import com.example.erp.dto.salary.SingleSalarySlipResponse;
import com.example.erp.entity.salary.SalarySlip;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class SalarySlipService {

    private static final Logger logger = LoggerFactory.getLogger(SalarySlipService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<SalarySlip> getAllSalarySlips(String employee, String startDate, String endDate, String status) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList(
            "name", "posting_date", "start_date", "end_date", "status",
            "total_deduction", "total_earnings", "net_pay", "gross_pay"
        );

        List<List<String>> filters = new ArrayList<>();
        if (employee != null && !employee.isEmpty()) {
            filters.add(Arrays.asList("employee", "=", employee));
        }
        if (startDate != null && !startDate.isEmpty()) {
            filters.add(Arrays.asList("start_date", "=", startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            filters.add(Arrays.asList("end_date", "=", endDate));
        }
        if (status != null && !status.isEmpty()) {
            filters.add(Arrays.asList("status", "=", status));
        }

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson);

            if (!filters.isEmpty()) {
                String filtersJson = objectMapper.writeValueAsString(filters);
                builder.queryParam("filters", filtersJson);
            }

            String finalUrl = builder.build(false).toUriString();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SalarySlipResponse> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, SalarySlipResponse.class);
            return response.getBody() != null ? response.getBody().getData() : new ArrayList<>();
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON for ERPNext Salary Slip API call", e);
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        }
    }

    public List<SalarySlip> getSalarySlipsByEmployeeAndDate(String employee, String payslipDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList(
            "name", "posting_date", "start_date", "end_date", "status",
            "total_deduction", "total_earnings", "net_pay", "gross_pay", "employee"
        );

        List<List<String>> filters = new ArrayList<>();
        
        if (employee != null && !employee.isEmpty()) {
            filters.add(Arrays.asList("employee", "=", employee));
        }
        
        if (payslipDate != null && !payslipDate.isEmpty()) {
            filters.add(Arrays.asList("posting_date", "=", payslipDate));
        }

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson)
                    .queryParam("order_by", "posting_date desc");

            if (!filters.isEmpty()) {
                String filtersJson = objectMapper.writeValueAsString(filters);
                builder.queryParam("filters", filtersJson);
            }

            String finalUrl = builder.build(false).toUriString();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SalarySlipResponse> response = restTemplate.exchange(
                finalUrl, HttpMethod.GET, entity, SalarySlipResponse.class);
            
            return response.getBody() != null ? response.getBody().getData() : new ArrayList<>();
            
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON for ERPNext Salary Slip API call", e);
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        } catch (Exception e) {
            logger.error("Error fetching salary slips for employee: {} on date: {}", employee, payslipDate, e);
            throw new RuntimeException("Error fetching salary slips for employee: " + employee + " on date: " + payslipDate, e);
        }
    }

    public SalarySlip getSalarySlipByName(String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");
        System.out.println("Atooooooooooo tafiditra");
    
        List<String> fieldsList = Arrays.asList(
            "name", "posting_date", "start_date", "end_date", "status",
            "total_deduction", "total_earnings", "net_pay", "gross_pay"
        );
    
        try {
            System.out.println("Ato am tryyyy");
    
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
    
            // Construction manuelle de l'URL sans encoder le path (le nom contient des espaces)
            String baseUrl = ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL + "/" + name;
            System.out.println("Url ==> " + baseUrl);
    
            // UriComponentsBuilder va encoder uniquement les query params (ce qu'on veut)
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("fields", fieldsJson);
    
            String finalUrl = builder.build(false).toUriString(); // false → ne pas encoder le chemin
    
            System.out.println("finalUrl ==> " + finalUrl);
    
            logger.debug("Fetching salary slip from ERP Next: {}", finalUrl);
    
            HttpEntity<String> entity = new HttpEntity<>(headers);
    
            ResponseEntity<SingleSalarySlipResponse> response = restTemplate.exchange(
                finalUrl, HttpMethod.GET, entity, SingleSalarySlipResponse.class
            );
    
            if (response.getBody() != null && response.getBody().getData() != null) {
                logger.debug("Salary slip retrieved: {}", response.getBody().getData().getName());
                return response.getBody().getData();
            } else {
                logger.warn("No salary slip found for name: {}", name);
                return null;
            }
    
        } catch (HttpClientErrorException e) {
            logger.error("Client error fetching salary slip for name: {}, status: {}", name, e.getStatusCode(), e);
            return null;
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON for ERPNext Salary Slip API call: {}", name, e);
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching salary slip for name: {}", name, e);
            throw new RuntimeException("Failed to fetch salary slip", e);
        }
    }
    public List<SalarySlip> getSalarySlipsByEmployeeAndMonthYear(String employee, String month, String year) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");
    
        // Define fields to retrieve from Salary Slip
        List<String> fieldsList = Arrays.asList(
                "name", "posting_date", "start_date", "end_date", "status",
                "total_deduction", "total_earnings", "net_pay", "gross_pay", "employee"
        );
    
        // Build filters dynamically
        List<List<String>> filters = new ArrayList<>();
    
        // Add employee filter if provided
        if (employee != null && !employee.isEmpty()) {
            filters.add(Arrays.asList("employee", "=", employee));
        }
    
        // Add date-based filters based on provided month and/or year
        if (month != null && !month.isEmpty() && year != null && !year.isEmpty()) {
            // Case 1: Both month and year provided
            String startDate = year + "-" + month + "-01";
            int lastDay = getLastDayOfMonth(Integer.parseInt(month), Integer.parseInt(year));
            String endDate = year + "-" + month + "-" + String.format("%02d", lastDay);
            filters.add(Arrays.asList("posting_date", ">=", startDate));
            filters.add(Arrays.asList("posting_date", "<=", endDate));
        } else if (month != null && !month.isEmpty()) {
            // Case 2: Only month provided - use BETWEEN with current year
            int currentYear = LocalDate.now().getYear();
            String startDate = currentYear + "-" + month + "-01";
            int lastDay = getLastDayOfMonth(Integer.parseInt(month), currentYear);
            String endDate = currentYear + "-" + month + "-" + String.format("%02d", lastDay);
            filters.add(Arrays.asList("posting_date", ">=", startDate));
            filters.add(Arrays.asList("posting_date", "<=", endDate));
        } else if (year != null && !year.isEmpty()) {
            // Case 3: Only year provided - use BETWEEN with full year
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            filters.add(Arrays.asList("posting_date", ">=", startDate));
            filters.add(Arrays.asList("posting_date", "<=", endDate));
        }
    
        try {
            // Convert fields to JSON for API query
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson)
                    .queryParam("order_by", "posting_date desc");
    
            // Add filters to query if any exist
            if (!filters.isEmpty()) {
                String filtersJson = objectMapper.writeValueAsString(filters);
                builder.queryParam("filters", filtersJson);
            }
    
            String finalUrl = builder.build(false).toUriString();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SalarySlipResponse> response = restTemplate.exchange(
                    finalUrl, HttpMethod.GET, entity, SalarySlipResponse.class);
    
            return response.getBody() != null ? response.getBody().getData() : new ArrayList<>();
    
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON for ERPNext Salary Slip API call", e);
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        } catch (Exception e) {
            logger.error("Error fetching salary slips for employee: {} for month: {}/{}", employee, month, year, e);
            throw new RuntimeException("Error fetching salary slips for employee: " + employee + " for month: " + month + "/" + year, e);
        }
    }
    // Utility method to calculate the last day of a given month and year
    private int getLastDayOfMonth(int month, int year) {
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                return 31;
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                    return 29;
                } else {
                    return 28;
                }
            default:
                return 31; // Fallback for invalid month
        }
    }    
}