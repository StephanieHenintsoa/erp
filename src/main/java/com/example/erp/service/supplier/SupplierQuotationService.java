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

import com.example.erp.entity.supplier.SupplierQuotation;

@Service
public class SupplierQuotationService {

    private static final String ERPNEXT_SUPPLIER_QUOTATION_API_URL = "http://erpnext.localhost:8000/api/method/erpnext.supplier.supplier_quotation_api_controller.get_supplier_quotations?supplier_name=";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.key}")
    private String apiKey;

    @Value("${erpnext.api.secret}")
    private String apiSecret;

    @SuppressWarnings("unchecked")
    public List<SupplierQuotation> getSupplierQuotations(String supplierName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            Map<String, Object> response = restTemplate.exchange(
                ERPNEXT_SUPPLIER_QUOTATION_API_URL + supplierName,
                HttpMethod.GET,
                request,
                Map.class
            ).getBody();

            if (response != null && response.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) response.get("message");
                if (message.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) message.get("data");
                    if (data.containsKey("quotations")) {
                        List<Map<String, Object>> quotations = (List<Map<String, Object>>) data.get("quotations");
                        return quotations.stream().map(quotation -> new SupplierQuotation(
                            (String) quotation.get("name"),
                            (String) quotation.get("supplier"),
                            (String) quotation.get("transaction_date"),
                            ((Number) quotation.get("grand_total")).doubleValue(),
                            (String) quotation.get("status")
                        )).collect(Collectors.toList());
                    }
                }
            }
            throw new RuntimeException("No supplier quotations found in response");
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch supplier quotations from ERPNext: " + e.getMessage());
        }
    }
}