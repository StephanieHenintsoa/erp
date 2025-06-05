package com.example.erp.service.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.erp.entity.CsvImportError;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ErpNextApiService {

    @Value("${erpnext.api.url}")
    private String erpNextUrl;

    @Value("${erpnext.api.username}")
    private String username;

    @Value("${erpnext.api.password}")
    private String password;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;
    
    // Store session cookies for subsequent requests
    private String sessionCookies;
    private long lastLoginTime = 0;
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    @Autowired
    public ErpNextApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Login and get session cookies
    private boolean loginAndGetSession() {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("usr", username);
            requestBody.put("pwd", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                erpNextUrl + "/api/method/login",
                HttpMethod.POST,
                request,
                String.class
            );

            // Extract cookies from response
            List<String> cookies = response.getHeaders().get("Set-Cookie");
            if (cookies != null && !cookies.isEmpty()) {
                sessionCookies = String.join("; ", cookies);
                lastLoginTime = System.currentTimeMillis();
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    // Check if session is valid and login if necessary
    private boolean ensureAuthenticated() {
        long currentTime = System.currentTimeMillis();
        
        // If no session or session expired, login again
        if (sessionCookies == null || (currentTime - lastLoginTime) > SESSION_TIMEOUT) {
            return loginAndGetSession();
        }
        return true;
    }

    // Create authenticated headers with session cookies
    private HttpHeaders createAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (sessionCookies != null) {
            headers.set("Cookie", sessionCookies);
        }
        
        return headers;
    }

    // Create a record in ERPNext
    public boolean createRecord(String docType, Map<String, Object> data, List<CsvImportError> errors) {
        try {
            if (!ensureAuthenticated()) {
                errors.add(new CsvImportError(docType + ".csv", 0, "Failed to authenticate with ERPNext", LocalDateTime.now()));
                return false;
            }

            HttpHeaders headers = createAuthenticatedHeaders();
            String json = objectMapper.writeValueAsString(data);
            HttpEntity<String> request = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                erpNextUrl + "/api/resource/" + docType,
                HttpMethod.POST,
                request,
                String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // If we get 403, try to re-login once
            if (e.getStatusCode().value() == 403) {
                sessionCookies = null; // Force re-login
                if (ensureAuthenticated()) {
                    return createRecord(docType, data, errors); // Retry once
                }
            }
            
            String errorMessage = e.getResponseBodyAsString();
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(errorMessage, Map.class);
                String serverMessage = errorResponse.getOrDefault("_error_message", e.getMessage()).toString();
                errors.add(new CsvImportError(docType + ".csv", 0, "API error creating record: " + serverMessage, LocalDateTime.now()));
            } catch (Exception ex) {
                errors.add(new CsvImportError(docType + ".csv", 0, "API error creating record: " + e.getStatusCode() + " - " + e.getMessage(), LocalDateTime.now()));
            }
            return false;
        } catch (Exception e) {
            errors.add(new CsvImportError(docType + ".csv", 0, "API error creating record: " + e.getMessage(), LocalDateTime.now()));
            return false;
        }
    }

    // Retrieve a record from ERPNext
    public Map<String, Object> getRecord(String docType, String name, List<CsvImportError> errors) {
        try {
            if (!ensureAuthenticated()) {
                errors.add(new CsvImportError(docType + ".csv", 0, "Failed to authenticate with ERPNext", LocalDateTime.now()));
                return null;
            }

            HttpHeaders headers = createAuthenticatedHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                erpNextUrl + "/api/resource/" + docType + "/" + name,
                HttpMethod.GET,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            errors.add(new CsvImportError(docType + ".csv", 0, "Failed to retrieve record: " + name, LocalDateTime.now()));
            return null;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                errors.add(new CsvImportError(docType + ".csv", 0, "Record not found: " + name, LocalDateTime.now()));
                return null;
            }
            if (e.getStatusCode().value() == 403) {
                sessionCookies = null; // Force re-login
                if (ensureAuthenticated()) {
                    return getRecord(docType, name, errors); // Retry once
                }
            }
            String errorMessage = e.getResponseBodyAsString();
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(errorMessage, Map.class);
                String serverMessage = errorResponse.getOrDefault("_error_message", e.getMessage()).toString();
                errors.add(new CsvImportError(docType + ".csv", 0, "API error retrieving record: " + serverMessage, LocalDateTime.now()));
            } catch (Exception ex) {
                errors.add(new CsvImportError(docType + ".csv", 0, "API error retrieving record: " + e.getStatusCode() + " - " + e.getMessage(), LocalDateTime.now()));
            }
            return null;
        } catch (Exception e) {
            errors.add(new CsvImportError(docType + ".csv", 0, "API error retrieving record: " + e.getMessage(), LocalDateTime.now()));
            return null;
        }
    }

    // Update a record in ERPNext
    public boolean updateRecord(String docType, String name, Map<String, Object> data, List<CsvImportError> errors) {
        try {
            if (!ensureAuthenticated()) {
                errors.add(new CsvImportError(docType + ".csv", 0, "Failed to authenticate with ERPNext", LocalDateTime.now()));
                return false;
            }

            HttpHeaders headers = createAuthenticatedHeaders();
            String json = objectMapper.writeValueAsString(data);
            HttpEntity<String> request = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                erpNextUrl + "/api/resource/" + docType + "/" + name,
                HttpMethod.PUT,
                request,
                String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            if (e.getStatusCode().value() == 403) {
                sessionCookies = null; // Force re-login
                if (ensureAuthenticated()) {
                    return updateRecord(docType, name, data, errors); // Retry once
                }
            }
            String errorMessage = e.getResponseBodyAsString();
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(errorMessage, Map.class);
                String serverMessage = errorResponse.getOrDefault("_error_message", e.getMessage()).toString();
                errors.add(new CsvImportError(docType + ".csv", 0, "API error updating record: " + serverMessage, LocalDateTime.now()));
            } catch (Exception ex) {
                errors.add(new CsvImportError(docType + ".csv", 0, "API error updating record: " + e.getStatusCode() + " - " + e.getMessage(), LocalDateTime.now()));
            }
            return false;
        } catch (Exception e) {
            errors.add(new CsvImportError(docType + ".csv", 0, "API error updating record: " + e.getMessage(), LocalDateTime.now()));
            return false;
        }
    }

    // Call custom bulk import API
    public Map<String, Object> bulkImport(List<Map<String, Object>> employees, List<Map<String, Object>> salaryStructures, List<Map<String, Object>> salarySlips, List<CsvImportError> errors) {
        try {
            if (!ensureAuthenticated()) {
                errors.add(new CsvImportError("general", 0, "Failed to authenticate with ERPNext", LocalDateTime.now()));
                return new HashMap<>();
            }

            HttpHeaders headers = createAuthenticatedHeaders();
            Map<String, List<Map<String, Object>>> data = new HashMap<>();
            data.put("employees", employees);
            data.put("salary_structures", salaryStructures);
            data.put("salary_slips", salarySlips);

            String json = objectMapper.writeValueAsString(data);
            HttpEntity<String> request = new HttpEntity<>(json, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                erpNextUrl + "/api/method/hrms.api.csv_import_api.bulk_import",
                HttpMethod.POST,
                request,
                Map.class
            );

            return response.getBody() != null ? response.getBody() : new HashMap<>();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Handle 403 with re-login
            if (e.getStatusCode().value() == 403) {
                sessionCookies = null; // Force re-login
                if (ensureAuthenticated()) {
                    return bulkImport(employees, salaryStructures, salarySlips, errors); // Retry
                }
            }
            String errorMessage = e.getResponseBodyAsString();
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(errorMessage, Map.class);
                String serverMessage = errorResponse.getOrDefault("_error_message", e.getMessage()).toString();
                errors.add(new CsvImportError("general", 0, "Bulk import API error: " + serverMessage, LocalDateTime.now()));
            } catch (Exception ex) {
                errors.add(new CsvImportError("general", 0, "Bulk import API error: " + e.getStatusCode() + " - " + e.getMessage(), LocalDateTime.now()));
            }
            return new HashMap<>();
        } catch (Exception e) {
            errors.add(new CsvImportError("general", 0, "Bulk import API error: " + e.getMessage(), LocalDateTime.now()));
            return new HashMap<>();
        }
    }

    // Check if a record exists with better error handling
    public boolean checkRecordExists(String docType, String name, List<CsvImportError> errors) {
        try {
            if (!ensureAuthenticated()) {
                errors.add(new CsvImportError(docType + ".csv", 0, "Failed to authenticate with ERPNext", LocalDateTime.now()));
                return false;
            }

            HttpHeaders headers = createAuthenticatedHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                erpNextUrl + "/api/resource/" + docType + "/" + name,
                HttpMethod.GET,
                request,
                String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                // Record doesn't exist, which is not an error
                return false;
            }
            
            // If we get 403, try to re-login once
            if (e.getStatusCode().value() == 403) {
                sessionCookies = null; // Force re-login
                if (ensureAuthenticated()) {
                    return checkRecordExists(docType, name, errors); // Retry once
                }
            }
            
            String errorMsg = "API error checking record existence: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
            errors.add(new CsvImportError(docType + ".csv", 0, errorMsg, LocalDateTime.now()));
            return false;
        } catch (Exception e) {
            errors.add(new CsvImportError(docType + ".csv", 0, "API error checking record existence: " + e.getMessage(), LocalDateTime.now()));
            return false;
        }
    }

    // Method to manually refresh session (useful for long-running operations)
    public boolean refreshSession() {
        sessionCookies = null;
        return ensureAuthenticated();
    }
}