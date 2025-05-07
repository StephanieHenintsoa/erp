package com.example.erp.service.supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.erp.entity.request.RequestForQuotation;
import com.example.erp.entity.request.RequestForQuotationItem;
import com.example.erp.entity.supplier.SupplierQuotation;
import com.example.erp.entity.supplier.SupplierQuotationItem;

@Service
public class SupplierQuotationService {

    private static final String ERPNEXT_SUPPLIER_QUOTATION_API_URL = "http://erpnext.localhost:8000/api/method/erpnext.supplier.supplier_quotation_api_controller.get_supplier_quotations?supplier_name=";
    private static final String ERPNEXT_QUOTATION_BY_NAME_API_URL = "http://erpnext.localhost:8000/api/method/erpnext.supplier.supplier_quotation_api_controller.get_quotation_by_name?quotation_name=";
    private static final String ERPNEXT_SUPPLIER_QUOTATION_CREATION_API_URL = "http://erpnext.localhost:8000/api/resource/Supplier Quotation";

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
                        
                        return result;
                    }
                }
            }

            throw new RuntimeException("No supplier quotations found in response");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch supplier quotations from ERPNext: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public SupplierQuotation getSupplierQuotationByName(String quotationName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            Map<String, Object> response = restTemplate.exchange(
                ERPNEXT_QUOTATION_BY_NAME_API_URL + quotationName,
                HttpMethod.GET,
                request,
                Map.class
            ).getBody();

            if (response != null && response.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) response.get("message");
                if (message.containsKey("data")) {
                    Map<String, Object> quotation = (Map<String, Object>) message.get("data");
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
                }
            }

            throw new RuntimeException("Quotation not found for name: " + quotationName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch quotation from ERPNext: " + e.getMessage());
        }
    }

    public String getSupplierNameByQuotation(String quotationName) {
        try {
            SupplierQuotation quotation = getSupplierQuotationByName(quotationName);
            return quotation.getSupplier();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving quotation: " + e.getMessage());
        }
    }

    public String createSupplierQuotation(String supplier, RequestForQuotation rfq, Map<String, String> itemRates) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        JSONObject requestBody = buildRequestBody(supplier, rfq, itemRates);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                ERPNEXT_SUPPLIER_QUOTATION_CREATION_API_URL,
                request,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                return (String) data.get("name");
            }
            
            throw new RuntimeException("Failed to create supplier quotation: Invalid response format");
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            e.printStackTrace();
            throw new RuntimeException("Failed to create supplier quotation: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create supplier quotation: " + e.getMessage());
        }
    }

    /**
     * Builds the JSON request body for the API call
     */
    private JSONObject buildRequestBody(String supplier, RequestForQuotation rfq, Map<String, String> itemRates) {
        JSONObject requestBody = new JSONObject();
        
        // Add main quotation details
        requestBody.put("supplier", supplier);
        requestBody.put("transaction_date", rfq.getTransactionDate());
        requestBody.put("company", rfq.getCompany());
        
        // Process items from the form data
        JSONArray itemsArray = new JSONArray();
        List<RequestForQuotationItem> rfqItems = rfq.getItems();
        
        for (int i = 0; i < rfqItems.size(); i++) {
            RequestForQuotationItem rfqItem = rfqItems.get(i);
            String rateKey = "items[" + i + "].rate";
            
            if (itemRates.containsKey(rateKey)) {
                try {
                    double rate = Double.parseDouble(itemRates.get(rateKey));
                    
                    JSONObject item = new JSONObject();
                    item.put("item_code", rfqItem.getItemCode());
                    item.put("qty", rfqItem.getQty());
                    item.put("rate", rate);
                    item.put("uom", rfqItem.getUom());
                    
                    // Add description if available
                    if (rfqItem.getDescription() != null && !rfqItem.getDescription().isEmpty()) {
                        item.put("description", rfqItem.getDescription());
                    } else {
                        item.put("description", rfqItem.getItemCode());
                    }
                    
                    itemsArray.put(item);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid rate format for item " + rfqItem.getItemCode());
                }
            }
        }
        
        requestBody.put("items", itemsArray);
        return requestBody;
    }
}