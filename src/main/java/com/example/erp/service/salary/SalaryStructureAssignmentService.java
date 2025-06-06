package com.example.erp.service.salary;


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
import com.example.erp.dto.salary.SalaryStructureAssignmentResponse;
import com.example.erp.entity.salary.SalaryStructureAssignment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SalaryStructureAssignmentService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<SalaryStructureAssignment> getAllSalaryStructureAssignments(String employee) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList("base", "variable");

        List<List<String>> filters = new ArrayList<>();
        if (employee != null && !employee.isEmpty()) {
            filters.add(Arrays.asList("employee", "=", employee));
        }

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_STRUCTURE_ASSIGNMENT_URL;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson);

            if (!filters.isEmpty()) {
                String filtersJson = objectMapper.writeValueAsString(filters);
                builder.queryParam("filters", filtersJson);
            }

            String finalUrl = builder.build(false).toUriString() + ErpNextConfig.PAGINATION_PARAM_FILTRE;
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SalaryStructureAssignmentResponse> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, SalaryStructureAssignmentResponse.class);
            return response.getBody().getData();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Salary Structure Assignment API call", e);
        }
    }
}