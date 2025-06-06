package com.example.erp.service;

import com.example.erp.config.ErpNextConfig;
import com.example.erp.entity.HrmsCsvImportRequest;
import com.example.erp.entity.HrmsCsvImportResponse;
import com.example.erp.entity.HrmsResetResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
public class HrmsCsvImportService {

    private static final Logger logger = LoggerFactory.getLogger(HrmsCsvImportService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Base API URL from configuration
    private final String baseApiUrl = ErpNextConfig.ERP_NEXT_API_BASE_URL;

    public HrmsCsvImportResponse importCsvFiles(MultipartFile employeesCsv, MultipartFile salaryStructureCsv,
                                               MultipartFile payrollCsv) throws Exception {
        // Prepare headers with API key and secret
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        // Prepare request body
        HrmsCsvImportRequest request = new HrmsCsvImportRequest();
        if (employeesCsv != null && !employeesCsv.isEmpty()) {
            request.setEmployeesCsv(Base64.getEncoder().encodeToString(employeesCsv.getBytes()));
            logger.debug("employeesCsv encoded, size: {}", employeesCsv.getSize());
        }
        if (salaryStructureCsv != null && !salaryStructureCsv.isEmpty()) {
            request.setSalaryStructureCsv(Base64.getEncoder().encodeToString(salaryStructureCsv.getBytes()));
            logger.debug("salaryStructureCsv encoded, size: {}", salaryStructureCsv.getSize());
        }
        if (payrollCsv != null && !payrollCsv.isEmpty()) {
            request.setPayrollCsv(Base64.getEncoder().encodeToString(payrollCsv.getBytes()));
            logger.debug("payrollCsv encoded, size: {}", payrollCsv.getSize());
        }

        // Define API endpoint
        String url = baseApiUrl + "/hrms.controllers.hrms_controller.import_csvs_from_json";
        System.out.println("Chemin==>"+url);

        try {
            // Serialize request to JSON
            String requestBody = objectMapper.writeValueAsString(request);
            logger.debug("Sending request to {} with body: {}", url, requestBody);

            // Make API call
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<HrmsCsvImportResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, HrmsCsvImportResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Received response: {}", response.getBody());
                return response.getBody();
            }

            logger.warn("Received null or unsuccessful response: {}", response);
            HrmsCsvImportResponse errorResponse = new HrmsCsvImportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to import CSV files");
            return errorResponse;

        } catch (Exception e) {
            logger.error("Error importing CSV files", e);
            HrmsCsvImportResponse errorResponse = new HrmsCsvImportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            return errorResponse;
        }
    }

    public HrmsResetResponse resetHrmsData() throws Exception {
        // Prepare headers with API key and secret
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        // Define API endpoint
        String url = baseApiUrl + "/hrms.controllers.hrms_reset_controller.reset_hrms_data";

        try {
            logger.debug("Sending reset request to {}", url);

            // Make API call
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<HrmsResetResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, HrmsResetResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Received reset response: {}", response.getBody());
                return response.getBody();
            }

            logger.warn("Received null or unsuccessful response: {}", response);
            HrmsResetResponse errorResponse = new HrmsResetResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to reset HRMS data");
            return errorResponse;
        } catch (Exception e) {
            logger.error("Error resetting HRMS data", e);
            HrmsResetResponse errorResponse = new HrmsResetResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            return errorResponse;
        }
    }
}