package com.example.erp.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginService {

    private static final String ERPNEXT_LOGIN_URL = "http://erpnext.localhost:8000/api/method/login";

    @Autowired
    private RestTemplate restTemplate;

    public String login(String username, String password) {
        // Prepare request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("usr", username);
        requestBody.put("pwd", password);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HTTP entity
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        // Make API call
        try {
            return restTemplate.postForObject(ERPNEXT_LOGIN_URL, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to login to ERPNext: " + e.getMessage());
        }
    }
}