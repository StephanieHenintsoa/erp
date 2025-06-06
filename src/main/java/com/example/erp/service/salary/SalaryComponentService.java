package com.example.erp.service.salary;

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
import com.example.erp.dto.salary.SalaryComponentResponse;
import com.example.erp.entity.salary.SalaryComponent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SalaryComponentService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<SalaryComponent> getAllSalaryComponents() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList("name", "salary_component_abbr", "amount");

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_COMPONENT_URL + ErpNextConfig.PAGINATION_PARAM_FILTRE; 
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson);

            String finalUrl = builder.build(false).toUriString();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SalaryComponentResponse> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, SalaryComponentResponse.class);
            return response.getBody().getData();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Salary Component API call", e);
        }
    }

    public SalaryComponent getSalaryComponentByName(String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList("name", "salary_component_abbr", "amount");

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_COMPONENT_URL + "/" + name;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson);

            String finalUrl = builder.build(false).toUriString() + ErpNextConfig.PAGINATION_PARAM_FILTRE;
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SalaryComponentResponse> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, SalaryComponentResponse.class);
            return response.getBody().getData().get(0); // Assumes single record for specific name
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Salary Component API call", e);
        }
    }
}