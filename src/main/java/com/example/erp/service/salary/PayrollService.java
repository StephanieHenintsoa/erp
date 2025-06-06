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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.erp.config.ErpNextConfig;
import com.example.erp.entity.Employee;
import com.example.erp.entity.salary.PayrollComponentsResponse;
import com.example.erp.entity.salary.SalaryEvolutionResponse;
import com.example.erp.entity.salary.SalarySlip;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PayrollService {

    private static final Logger logger = LoggerFactory.getLogger(PayrollService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Employee> getAllEmployees() {
        HttpHeaders headers = createAuthHeaders();
        List<String> fieldsList = Arrays.asList(
                "name", "employee_name", "first_name", "company", "date_of_joining",
                "status", "department", "designation", "gender", "date_of_birth"
        );

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = UriComponentsBuilder.fromHttpUrl(ErpNextConfig.ERP_NEXT_API_EMPLOYEE_URL)
                    .queryParam("fields", fieldsJson)
                    .build(false)
                    .toUriString();

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url + ErpNextConfig.PAGINATION_PARAM_FILTRE, HttpMethod.GET, entity, Map.class);
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            List<Employee> employees = new ArrayList<>();
            for (Map<String, Object> item : data) {
                Employee employee = new Employee();
                employee.setName((String) item.get("name"));
                employee.setEmployeeName((String) item.get("employee_name"));
                employee.setFirstName((String) item.get("first_name"));
                employee.setCompany((String) item.get("company"));
                employee.setDateOfJoining((String) item.get("date_of_joining"));
                employee.setStatus((String) item.get("status"));
                employee.setDepartment((String) item.get("department"));
                employee.setDesignation((String) item.get("designation"));
                employee.setGender((String) item.get("gender"));
                employee.setDateOfBirth((String) item.get("date_of_birth"));
                employees.add(employee);
            }
            return employees;
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON for ERPNext Employee API call", e);
            throw new RuntimeException("Error processing JSON for ERPNext Employee API call", e);
        }
    }

    public List<SalarySlip> getFilteredSalarySlips(String month, String year) {
        HttpHeaders headers = createAuthHeaders();
        List<String> fieldsList = Arrays.asList(
                "name", "posting_date", "start_date", "end_date", "status",
                "total_deduction", "total_earnings", "net_pay", "gross_pay", "employee"
        );

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson)
                    .queryParam("order_by", "posting_date desc");

            List<List<String>> filters = new ArrayList<>();
            if (month != null && !month.isEmpty() && year != null && !year.isEmpty()) {
                String startDate = year + "-" + String.format("%02d", Integer.parseInt(month)) + "-01";
                int lastDay = getLastDayOfMonth(Integer.parseInt(month), Integer.parseInt(year));
                String endDate = year + "-" + String.format("%02d", Integer.parseInt(month)) + "-" + String.format("%02d", lastDay);
                filters.add(Arrays.asList("posting_date", ">=", startDate));
                filters.add(Arrays.asList("posting_date", "<=", endDate));
            } else if (year != null && !year.isEmpty()) {
                filters.add(Arrays.asList("posting_date", ">=", year + "-01-01"));
                filters.add(Arrays.asList("posting_date", "<=", year + "-12-31"));
            }

            if (!filters.isEmpty()) {
                String filtersJson = objectMapper.writeValueAsString(filters);
                builder.queryParam("filters", filtersJson);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(builder.build(false).toUriString() + ErpNextConfig.PAGINATION_PARAM_FILTRE, HttpMethod.GET, entity, Map.class);
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            List<SalarySlip> salarySlips = new ArrayList<>();
            for (Map<String, Object> item : data) {
                SalarySlip slip = new SalarySlip();
                slip.setName((String) item.get("name"));
                slip.setEmployee((String) item.get("employee"));
                slip.setStartDate((String) item.get("start_date"));
                slip.setEndDate((String) item.get("end_date"));
                slip.setPostingDate((String) item.get("posting_date"));
                slip.setStatus((String) item.get("status"));
                slip.setGrossPay(item.get("gross_pay") != null ? ((Number) item.get("gross_pay")).doubleValue() : 0.0);
                slip.setNetPay(item.get("net_pay") != null ? ((Number) item.get("net_pay")).doubleValue() : 0.0);
                slip.setTotalEarnings(item.get("total_earnings") != null ? ((Number) item.get("total_earnings")).doubleValue() : 0.0);
                slip.setTotalDeduction(item.get("total_deduction") != null ? ((Number) item.get("total_deduction")).doubleValue() : 0.0);
                salarySlips.add(slip);
            }
            return salarySlips;
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON for ERPNext Salary Slip API call", e);
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        } catch (Exception e) {
            logger.error("Error fetching salary slips: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching salary slips: " + e.getMessage(), e);
        }
    }

    public List<SalarySlip> getMonthlyAggregatedSalarySlips(String month, String year) {
        List<SalarySlip> salarySlips = getFilteredSalarySlips(null, year);
        Map<String, SalarySlip> aggregatedSlips = new HashMap<>();

        for (SalarySlip slip : salarySlips) {
            String[] dateParts = slip.getPostingDate().split("-");
            String yearMonth = dateParts[0] + "-" + dateParts[1];
            String displayMonth = getMonthName(dateParts[1]) + " " + dateParts[0];

            aggregatedSlips.computeIfAbsent(yearMonth, k -> {
                SalarySlip newSlip = new SalarySlip();
                newSlip.setPostingDate(displayMonth);
                newSlip.setGrossPay(0.0);
                newSlip.setNetPay(0.0);
                return newSlip;
            });

            SalarySlip aggSlip = aggregatedSlips.get(yearMonth);
            aggSlip.setGrossPay(aggSlip.getGrossPay() + slip.getGrossPay());
            aggSlip.setNetPay(aggSlip.getNetPay() + slip.getNetPay());
        }

        return new ArrayList<>(aggregatedSlips.values());
    }

    public PayrollComponentsResponse getPayrollComponents(String year, String month) {
        HttpHeaders headers = createAuthHeaders();
        String yearMonth = year + "-" + month;
        String url = UriComponentsBuilder.fromHttpUrl("http://erpnext.localhost:8000/api/method/hrms.controllers.payroll_controller.get_payroll_components")
                .queryParam("year_month", yearMonth)
                .build(false)
                .toUriString();

        try {
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url + ErpNextConfig.PAGINATION_PARAM_FILTRE, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("message");

            PayrollComponentsResponse components = objectMapper.convertValue(data, PayrollComponentsResponse.class);
            components.setYear(year); // Set year for display
            return components;
        } catch (Exception e) {
            logger.error("Error fetching payroll components for year_month {}: {}", yearMonth, e.getMessage(), e);
            throw new RuntimeException("Error fetching payroll components: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");
        return headers;
    }

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
                return 31;
        }
    }

    private String getMonthName(String monthNumber) {
        if (monthNumber == null || monthNumber.isEmpty()) {
            return "tous les mois";
        }
        switch (monthNumber) {
            case "01": return "Janvier";
            case "02": return "Février";
            case "03": return "Mars";
            case "04": return "Avril";
            case "05": return "Mai";
            case "06": return "Juin";
            case "07": return "Juillet";
            case "08": return "Août";
            case "09": return "Septembre";
            case "10": return "Octobre";
            case "11": return "Novembre";
            case "12": return "Décembre";
            default: return "mois inconnu";
        }
    }

    public SalaryEvolutionResponse getSalaryEvolutionData(String year) {
        HttpHeaders headers = createAuthHeaders();
        String url = UriComponentsBuilder.fromHttpUrl("http://erpnext.localhost:8000/api/method/hrms.controllers.payroll_controller.get_salary_evolution_data")
                .queryParam("year", year)
                .build(false)
                .toUriString();
        
        try {
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url + ErpNextConfig.PAGINATION_PARAM_FILTRE, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("message");
            SalaryEvolutionResponse evolutionData = objectMapper.convertValue(data, SalaryEvolutionResponse.class);
            return evolutionData;
        } catch (Exception e) {
            logger.error("Error fetching salary evolution data for year {}: {}", year, e.getMessage(), e);
            throw new RuntimeException("Error fetching salary evolution data: " + e.getMessage(), e);
        }
    }
}