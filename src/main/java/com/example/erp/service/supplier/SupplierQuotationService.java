package com.example.erp.service.supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.example.erp.entity.supplier.SupplierQuotationItem;

@Service
public class SupplierQuotationService {

    private static final String ERPNEXT_SUPPLIER_QUOTATION_API_URL = "http://erpnext.localhost:8000/api/method/erpnext.supplier.supplier_quotation_api_controller.get_supplier_quotations?supplier_name=";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.key}")
    private String apiKey;

    @Value("${erpnext.api.secret}")
    private String apiSecret;

    // Cache to store fetched quotations
    private List<SupplierQuotation> cachedQuotations = new ArrayList<>();
    private String lastFetchedSupplier = null;

    @SuppressWarnings("unchecked")
    public List<SupplierQuotation> getSupplierQuotations(String supplierName) {
        // Return cached results if available for the same supplier
        if (supplierName.equals(lastFetchedSupplier) && !cachedQuotations.isEmpty()) {
            return cachedQuotations;
        }

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
                        List<SupplierQuotation> result = quotations.stream().map(quotation -> {
                            List<Map<String, Object>> items = (List<Map<String, Object>>) quotation.get("items");
                            List<SupplierQuotationItem> quotationItems = items != null ? items.stream().map(item -> new SupplierQuotationItem(
                                (String) item.getOrDefault("item_name", null),
                                ((Number) item.get("qty")).doubleValue(),
                                ((Number) item.get("rate")).doubleValue(),
                                ((Number) item.get("amount")).doubleValue()
                            )).collect(Collectors.toList()) : new ArrayList<>();
                            return new SupplierQuotation(
                                (String) quotation.get("name"),
                                (String) quotation.get("supplier"),
                                (String) quotation.get("transaction_date"),
                                ((Number) quotation.get("grand_total")).doubleValue(),
                                (String) quotation.get("status"),
                                quotationItems
                            );
                        }).collect(Collectors.toList());
                        
                        // Cache the results
                        this.cachedQuotations = result;
                        this.lastFetchedSupplier = supplierName;
                        
                        return result;
                    }
                }
            }
            throw new RuntimeException("No supplier quotations found in response");
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch supplier quotations from ERPNext: " + e.getMessage());
        }
    }
    
    //getAll getSupplierQuotation et filtrer par quotationName
    public SupplierQuotation getSupplierQuotationByName(String quotationName) {
        Optional<SupplierQuotation> cachedQuotation = cachedQuotations.stream()
                .filter(q -> q.getName().equals(quotationName))
                .findFirst();
        
        if (cachedQuotation.isPresent()) {
            return cachedQuotation.get();
        }

        throw new RuntimeException("Quotation not found. Please navigate through the supplier's quotations first.");
    }

    public String getSupplierNameByQuotation(String quotationName) {
        try {
            SupplierQuotation quotation = getSupplierQuotationByName(quotationName);
            return quotation != null ? quotation.getSupplier() : "";
        } catch (Exception e) {
            return "";
        }
    }
}