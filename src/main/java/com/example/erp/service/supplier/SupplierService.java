package com.example.erp.service.supplier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.erp.entity.supplier.Supplier;

@Service
public class SupplierService {

    private static final String ERPNEXT_SUPPLIER_RESOURCE_URL = "http://erpnext.localhost:8000/api/resource/Supplier?fields=[\"name\",\"supplier_name\",\"supplier_group\",\"country\"]";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.key}")
    private String apiKey;

    @Value("${erpnext.api.secret}")
    private String apiSecret;

    public List<Supplier> getSuppliers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            Map<String, Object> response = restTemplate.exchange(ERPNEXT_SUPPLIER_RESOURCE_URL, HttpMethod.GET, request, Map.class).getBody();
            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> suppliers = (List<Map<String, Object>>) response.get("data");
                return suppliers.stream().map(supplier -> new Supplier(
                        (String) supplier.get("name"),
                        (String) supplier.get("supplier_name"),
                        (String) supplier.get("supplier_group"),
                        (String) supplier.get("country")
                )).collect(Collectors.toList());
            }
            throw new RuntimeException("No suppliers found in response");
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch suppliers from ERPNext: " + e.getMessage());
        }
    }
}