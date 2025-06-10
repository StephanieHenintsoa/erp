package com.example.erp.service.employee;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.erp.config.ErpNextConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmployeeSalaryService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ERP_NEXT_API_URL = "http://erpnext.localhost:8000/api/resource/";
    private static final Map<String, Integer> MONTHS = new HashMap<>();

    static {
        MONTHS.put("Janvier", 1);
        MONTHS.put("Février", 2);
        MONTHS.put("Mars", 3);
        MONTHS.put("Avril", 4);
        MONTHS.put("Mai", 5);
        MONTHS.put("Juin", 6);
        MONTHS.put("Juillet", 7);
        MONTHS.put("Août", 8);
        MONTHS.put("Septembre", 9);
        MONTHS.put("Octobre", 10);
        MONTHS.put("Novembre", 11);
        MONTHS.put("Décembre", 12);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json"); 
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public List<Map<String, Object>> getAllEmployees() {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<String> fields = Arrays.asList("name", "employee_name");
        try {
            String fieldsJson = objectMapper.writeValueAsString(fields);
            String url = ERP_NEXT_API_URL + "Employee";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson);

            ResponseEntity<Map> response = restTemplate.exchange(
                builder.build(false).toUriString(),
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                return (List<Map<String, Object>>) responseBody.get("data");
            } else {
                throw new RuntimeException("Invalid response format from ERPNext API");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Employee API call", e);
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling ERPNext Employee API: " + e.getMessage(), e);
        }
    }

    public void generateSalaries(String employee, String moisDebut, int anneeDebut, String moisFin, int anneeFin) {
        YearMonth start = YearMonth.of(anneeDebut, MONTHS.get(moisDebut));
        YearMonth end = YearMonth.of(anneeFin, MONTHS.get(moisFin));

        if (start.isAfter(end)) {
            throw new IllegalStateException("La date de début doit être antérieure ou égale à la date de fin.");
        }

        Map<String, Object> referenceSalary = getReferenceSalary(employee, start);
        if (referenceSalary == null) {
            throw new IllegalStateException("Aucun salaire de référence trouvé avant " + moisDebut + " " + anneeDebut);
        }

        YearMonth current = start;
        while (!current.isAfter(end)) {
            if (!hasSalaryForMonth(employee, current)) {
                createSalaryRecord(employee, current, referenceSalary);
            }
            current = current.plusMonths(1);
        }
    }

    private Map<String, Object> getReferenceSalary(String employee, YearMonth start) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<List<String>> filters = new ArrayList<>();
        filters.add(Arrays.asList("employee", "=", employee));
        filters.add(Arrays.asList("posting_date", "<", start.atDay(1).toString()));
        
        List<String> fields = Arrays.asList("name", "gross_pay");

        try {
            String filtersJson = objectMapper.writeValueAsString(filters);
            String fieldsJson = objectMapper.writeValueAsString(fields);
            String url = ERP_NEXT_API_URL + "Salary Slip";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("filters", filtersJson)
                    .queryParam("fields", fieldsJson)
                    .queryParam("order_by", "posting_date desc")
                    .queryParam("limit_page_length", "1");

            // Use build(false) to avoid double encoding
            String finalUrl = builder.build(false).toUriString();
            System.out.println("Reference salary URL: " + finalUrl);

            ResponseEntity<Map> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                List<Map<String, Object>> salaries = (List<Map<String, Object>>) responseBody.get("data");
                return salaries.isEmpty() ? null : salaries.get(0);
            } else {
                return null;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling ERPNext Salary Slip API: " + e.getMessage(), e);
        }
    }

    private boolean hasSalaryForMonth(String employee, YearMonth month) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<List<Object>> filters = new ArrayList<>();
        filters.add(Arrays.asList("employee", "=", employee));
        filters.add(Arrays.asList("month", "=", month.getMonthValue())); 
        filters.add(Arrays.asList("year", "=", month.getYear())); 

        try {
            String filtersJson = objectMapper.writeValueAsString(filters);
            String url = ERP_NEXT_API_URL + "Salary Slip";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("filters", filtersJson);

            String finalUrl = builder.build(false).toUriString();
            System.out.println("Checking salary for month URL: " + finalUrl);

            ResponseEntity<Map> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                List<Map<String, Object>> salaries = (List<Map<String, Object>>) responseBody.get("data");
                return !salaries.isEmpty();
            } else {
                return false;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        } catch (RestClientException e) {
            System.err.println("RestClientException in hasSalaryForMonth: " + e.getMessage());
            return false;
        }
    }

    private void createSalaryRecord(String employee, YearMonth month, Map<String, Object> referenceSalary) {
        HttpHeaders headers = createHeaders();
        Map<String, Object> salaryData = new HashMap<>();
        salaryData.put("employee", employee);
        salaryData.put("month", month.getMonthValue()); 
        salaryData.put("year", month.getYear()); 
        salaryData.put("gross_pay", referenceSalary.get("gross_pay"));
        
        salaryData.put("start_date", month.atDay(1).toString());
        salaryData.put("end_date", month.atEndOfMonth().toString());
        salaryData.put("posting_date", month.atDay(1).toString());

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(salaryData, headers);
            String url = ERP_NEXT_API_URL + "Salary Slip";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        } catch (RestClientException e) {
            System.err.println("Error creating salary record: " + e.getMessage());
            throw new RuntimeException("Error creating salary record in ERPNext: " + e.getMessage(), e);
        }
    }
}