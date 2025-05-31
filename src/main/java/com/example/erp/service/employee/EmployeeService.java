package com.example.erp.service.employee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.erp.config.ErpNextConfig;
import com.example.erp.dto.DepartmentResponse;
import com.example.erp.dto.DesignationResponse;
import com.example.erp.dto.EmployeeResponse;
import com.example.erp.dto.SingleEmployeeResponse;
import com.example.erp.entity.Department;
import com.example.erp.entity.Designation;
import com.example.erp.entity.Employee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmployeeService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    public List<Employee> getAllEmployees(String minDate, String maxDate, String status, String designation, String department) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList(
            "name", "employee_name", "first_name", "company", "date_of_joining", 
            "status", "department", "designation", "gender", "date_of_birth"
        );

        List<List<String>> filters = new ArrayList<>();
        if (minDate != null && !minDate.isEmpty()) {
            filters.add(Arrays.asList("date_of_joining", ">=", minDate));
        }
        if (maxDate != null && !maxDate.isEmpty()) {
            filters.add(Arrays.asList("date_of_joining", "<=", maxDate));
        }
        if (status != null && !status.isEmpty()) {
            filters.add(Arrays.asList("status", "=", status));
        }
        if (designation != null && !designation.isEmpty()) {
            filters.add(Arrays.asList("designation", "=", designation));
        }
        if (department != null && !department.isEmpty()) {
            filters.add(Arrays.asList("department", "=", department));
        }

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_EMPLOYEE_URL;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson);

            if (!filters.isEmpty()) {
                String filtersJson = objectMapper.writeValueAsString(filters);
                builder.queryParam("filters", filtersJson);
            }

            String finalUrl = builder.build(false).toUriString();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<EmployeeResponse> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, EmployeeResponse.class);
            return response.getBody().getData();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext API call", e);
        }
    }

    public Employee getEmployeeByName(String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList(
            "name", "employee_name", "first_name", "company", "date_of_joining", 
            "status", "department", "designation", "gender", "date_of_birth"
        );

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_EMPLOYEE_URL + "/" + name;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson);

            String finalUrl = builder.build(false).toUriString();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> rawResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, String.class);
            System.out.println("Raw API response: " + rawResponse.getBody());
            ResponseEntity<SingleEmployeeResponse> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, SingleEmployeeResponse.class);
            Employee employee = response.getBody().getData();
            System.out.println("Deserialized employee name: " + (employee != null ? employee.getName() : "null"));
            return employee;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Employee API call", e);
        }
    }

    public List<Designation> getAllDesignations() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList("name");
        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = UriComponentsBuilder.fromHttpUrl(ErpNextConfig.ERP_NEXT_API_DESIGNATION_URL)
                    .queryParam("fields", fieldsJson)
                    .build(false)
                    .toUriString();
                    
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<DesignationResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, DesignationResponse.class);
            return response.getBody().getData();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for Designation API call", e);
        }
    }

    public List<Department> getAllDepartments() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList("name");
        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = UriComponentsBuilder.fromHttpUrl(ErpNextConfig.ERP_NEXT_API_DEPARTMENT_URL)
                    .queryParam("fields", fieldsJson)
                    .build(false)
                    .toUriString();
                    
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<DepartmentResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, DepartmentResponse.class);
            return response.getBody().getData();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for Department API call", e);
        }
    }
}