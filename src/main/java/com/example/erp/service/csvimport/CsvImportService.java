package com.example.erp.service.csvimport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvImportService {

    private static final String ERPNEXT_IMPORT_CSV_URL = "%s/api/method/erpnext.import_csv.csv_import.import_csv_data";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.url}")
    private String erpNextApiUrl;
    @Value("${erpnext.api.key}")
    private String apiKey;
    @Value("${erpnext.api.secret}")
    private String apiSecret;

    public Map<String, Object> importCsv(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        try {
            // Parse CSV
            List<Map<String, String>> records = parseCsv(file);
            if (records.isEmpty()) {
                return Map.of(
                    "success", false,
                    "message", "CSV file is empty",
                    "errors", List.of("No data found in CSV")
                );
            }

            // Générer item_code à partir de item_name si absent
            for (Map<String, String> record : records) {
                if (!record.containsKey("item_code") || record.get("item_code") == null || record.get("item_code").trim().isEmpty()) {
                    String itemName = record.get("item_name");
                    if (itemName != null && !itemName.trim().isEmpty()) {
                        record.put("item_code", itemName.replace(" ", "_").toUpperCase().substring(0, Math.min(itemName.length(), 50)));
                    }
                }
            }

            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("data", records);
            payload.put("doctype", "Material Request Import");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Call ERP Next API
            Map<String, Object> response = restTemplate.exchange(
                String.format(ERPNEXT_IMPORT_CSV_URL, erpNextApiUrl),
                HttpMethod.POST,
                request,
                Map.class
            ).getBody();

            if (response != null && response.containsKey("message")) {
                return (Map<String, Object>) response.get("message");
            }
            throw new RuntimeException("No valid response from ERP Next");

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                "success", false,
                "message", "Failed to import CSV to ERP Next",
                "errors", List.of(e.getMessage())
            );
        }
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws Exception {
        List<Map<String, String>> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readLine().split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(",");
                Map<String, String> record = new HashMap<>();
                for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                    record.put(headers[i].trim(), values[i].trim());
                }
                records.add(record);
            }
        }
        return records;
    }
}