package com.example.erp.service.rfq;

import com.example.erp.entity.supplier.Supplier;
import com.example.erp.entity.warehouse.Warehouse;
import com.example.erp.entity.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RfqService {

    private static final Logger logger = LoggerFactory.getLogger(RfqService.class);

    @Value("${erpnext.api.url}")
    private String erpNextUrl;

    @Value("${erpnext.api.key}")
    private String apiKey;

    @Value("${erpnext.api.secret}")
    private String apiSecret;

    private final RestTemplate restTemplate;

    @Autowired
    public RfqService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        logger.info("RfqService initialized with ERPNext URL: {}", erpNextUrl);
    }

    public List<Item> getAllItems() {
        String endpoint = "/api/method/erpnext.item.item_controller.get_all_items";
        String fullUrl = erpNextUrl + endpoint;
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            logger.info("Making request to ERPNext: {}", fullUrl);
            logger.debug("Request headers: {}", headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            logger.info("Received response with status: {}", response.getStatusCode());
            
            if (responseBody != null && responseBody.containsKey("data")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) responseBody.get("data");
                List<Item> result = items.stream()
                        .map(item -> new Item((String) item.get("name")))
                        .collect(Collectors.toList());
                logger.info("Successfully fetched {} items", result.size());
                return result;
            } else {
                logger.warn("No data found in response from ERPNext. Response: {}", responseBody);
                return new ArrayList<>();
            }
        } catch (HttpClientErrorException e) {
            logger.error("HTTP Error while fetching items. Status: {}, Body: {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HTTP Error fetching items: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            logger.error("REST client error while fetching items: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch items from ERPNext: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching items: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch items from ERPNext: " + e.getMessage(), e);
        }
    }

    public List<Warehouse> getAllWarehouseNames() {
        String endpoint = "/api/method/erpnext.warehouse.warehouse_controller.get_warehouse_names";
        String fullUrl = erpNextUrl + endpoint;
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            logger.info("Making request to ERPNext: {}", fullUrl);
            
            ResponseEntity<Map> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            logger.info("Received response with status: {}", response.getStatusCode());
            
            if (responseBody != null && responseBody.containsKey("data")) {
                List<Map<String, Object>> warehouses = (List<Map<String, Object>>) responseBody.get("data");
                List<Warehouse> result = warehouses.stream()
                        .map(warehouse -> new Warehouse((String) warehouse.get("name")))
                        .collect(Collectors.toList());
                logger.info("Successfully fetched {} warehouses", result.size());
                return result;
            } else {
                logger.warn("No data found in response from ERPNext. Response: {}", responseBody);
                return new ArrayList<>();
            }
        } catch (HttpClientErrorException e) {
            logger.error("HTTP Error while fetching warehouses. Status: {}, Body: {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HTTP Error fetching warehouses: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            logger.error("REST client error while fetching warehouses: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch warehouses from ERPNext: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching warehouses: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch warehouses from ERPNext: " + e.getMessage(), e);
        }
    }

    public List<Supplier> getAllSuppliers() {
        String endpoint = "/api/method/erpnext.supplier.supplier_controller.get_supplier_names";
        String fullUrl = erpNextUrl + endpoint;
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            logger.info("Making request to ERPNext: {}", fullUrl);
            
            ResponseEntity<Map> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            logger.info("Received response with status: {}", response.getStatusCode());
            
            if (responseBody != null && responseBody.containsKey("data")) {
                List<Map<String, Object>> suppliers = (List<Map<String, Object>>) responseBody.get("data");
                List<Supplier> result = suppliers.stream()
                        .map(supplier -> new Supplier(
                                (String) supplier.get("name"),
                                null, // supplierName not fetched
                                null, // supplierGroup not fetched
                                null  // country not fetched
                        ))
                        .collect(Collectors.toList());
                logger.info("Successfully fetched {} suppliers", result.size());
                return result;
            } else {
                logger.warn("No data found in response from ERPNext. Response: {}", responseBody);
                return new ArrayList<>();
            }
        } catch (HttpClientErrorException e) {
            logger.error("HTTP Error while fetching suppliers. Status: {}, Body: {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HTTP Error fetching suppliers: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            logger.error("REST client error while fetching suppliers: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch suppliers from ERPNext: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching suppliers: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch suppliers from ERPNext: " + e.getMessage(), e);
        }
    }

    public Map<String, List<?>> getAllEntities() {
        Map<String, List<?>> entities = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        // Fetch items
        try {
            logger.info("Fetching items...");
            List<Item> items = getAllItems();
            entities.put("items", items);
            logger.info("Successfully fetched {} items", items.size());
            System.out.println("Items fetched: " + items.size());
        } catch (Exception e) {
            logger.error("Error fetching items: {}", e.getMessage(), e);
            entities.put("items", new ArrayList<>());
            errors.add("Failed to fetch items: " + e.getMessage());
        }
        
        // Fetch warehouses
        try {
            logger.info("Fetching warehouses...");
            List<Warehouse> warehouses = getAllWarehouseNames();
            entities.put("warehouses", warehouses);
            logger.info("Successfully fetched {} warehouses", warehouses.size());
            System.out.println("Warehouses fetched: " + warehouses.size());
        } catch (Exception e) {
            logger.error("Error fetching warehouses: {}", e.getMessage(), e);
            entities.put("warehouses", new ArrayList<>());
            errors.add("Failed to fetch warehouses: " + e.getMessage());
        }
        
        // Fetch suppliers
        try {
            logger.info("Fetching suppliers...");
            List<Supplier> suppliers = getAllSuppliers();
            entities.put("suppliers", suppliers);
            logger.info("Successfully fetched {} suppliers", suppliers.size());
            System.out.println("Suppliers fetched: " + suppliers.size());
        } catch (Exception e) {
            logger.error("Error fetching suppliers: {}", e.getMessage(), e);
            entities.put("suppliers", new ArrayList<>());
            errors.add("Failed to fetch suppliers: " + e.getMessage());
        }
        
        // Log summary
        if (errors.isEmpty()) {
            logger.info("Successfully fetched all entities - Items: {}, Warehouses: {}, Suppliers: {}", 
                        entities.get("items").size(), 
                        entities.get("warehouses").size(), 
                        entities.get("suppliers").size());
        } else {
            logger.warn("Completed with {} errors. Errors: {}", errors.size(), errors);
             throw new RuntimeException("Some entities failed to fetch: " + String.join("; ", errors));
        }
        
        return entities;
    }
    
    public String updateItemUom(String itemCode) {
        String endpoint = "/api/method/erpnext.item.item_controller.handle_item_uom_update";
        String fullUrl = erpNextUrl + endpoint;
        HttpHeaders headers = createHeaders();
        
        // Prepare request body with item_code only
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", itemCode);
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            logger.info("Making PUT request to ERPNext for item UOM update: {}", fullUrl);
            logger.debug("Request body: {}", requestBody);
            
            ResponseEntity<Map> response = restTemplate.exchange(fullUrl, HttpMethod.PUT, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            logger.info("Received response with status: {}", response.getStatusCode());
            
            if (responseBody != null && responseBody.containsKey("message")) {
                Object messageObj = responseBody.get("message");
                if (messageObj instanceof Map) {
                    Map<String, String> messageMap = (Map<String, String>) messageObj;
                    String result = messageMap.get("message");
                    if (result != null) {
                        logger.info("Successfully updated UOM for item: {}", itemCode);
                        return result;
                    } else {
                        String error = messageMap.get("error");
                        logger.warn("Error in response from ERPNext: {}", error);
                        throw new RuntimeException("ERPNext error: " + error);
                    }
                } else {
                    logger.warn("Unexpected message format in response: {}", messageObj);
                    throw new RuntimeException("Unexpected response format from ERPNext");
                }
            } else {
                logger.warn("No message found in response from ERPNext. Response: {}", responseBody);
                throw new RuntimeException("No message returned from ERPNext for item UOM update");
            }
        } catch (HttpClientErrorException e) {
            logger.error("HTTP Error while updating item UOM. Status: {}, Body: {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HTTP Error updating item UOM: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            logger.error("REST client error while updating item UOM: {}", e.getMessage());
            throw new RuntimeException("Failed to update item UOM in ERPNext: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while updating item UOM: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update item UOM in ERPNext: " + e.getMessage(), e);
        }
    }

    


    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);
        logger.debug("Created headers with Authorization: token {}:***", apiKey);
        return headers;
    }
    public Map<String, Object> saveRfq(
        String suppliers,
        String transactionDate,
        String scheduleDate,
        String status,
        String messageForSupplier,
        String items,
        Double quantity,
        String itemScheduleDate,
        String warehouses) {

    String endpoint = "/api/method/erpnext.rfq.rfq.save";
    String fullUrl = erpNextUrl + endpoint;

    try {
        logger.info("Creating RFQ with supplier: {}, transaction_date: {}, schedule_date: {}", 
                suppliers, transactionDate, scheduleDate);
        
        // Création des headers
        HttpHeaders headers = createHeaders();
        
        // Préparation des paramètres de la requête
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("suppliers", suppliers);
        requestParams.put("transaction_date", transactionDate);
        requestParams.put("schedule_date", scheduleDate);
        requestParams.put("status", status != null ? status : "Draft");
        
        if (messageForSupplier != null && !messageForSupplier.trim().isEmpty()) {
            requestParams.put("message_for_supplier", messageForSupplier);
        }
        
        if (items != null && !items.trim().isEmpty()) {
            requestParams.put("items", items);
        }
        
        if (quantity != null) {
            requestParams.put("quantity", quantity);
        }
        
        if (itemScheduleDate != null && !itemScheduleDate.trim().isEmpty()) {
            requestParams.put("item_schedule_date", itemScheduleDate);
        }
        
        if (warehouses != null && !warehouses.trim().isEmpty()) {
            requestParams.put("warehouses", warehouses);
        }
        
        logger.debug("RFQ request parameters: {}", requestParams);
        
        // Création de l'entité HTTP
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestParams, headers);
        
        // Envoi de la requête POST
        ResponseEntity<Map> response = restTemplate.exchange(
            fullUrl, 
            HttpMethod.POST, 
            entity, 
            Map.class
        );
        
        Map<String, Object> responseBody = response.getBody();
        logger.info("RFQ creation response status: {}", response.getStatusCode());
        logger.debug("RFQ creation response body: {}", responseBody);
        
        if (responseBody != null) {
            String responseStatus = (String) responseBody.get("status");
            
            if ("success".equals(responseStatus)) {
                logger.info("RFQ created successfully with name: {}", responseBody.get("name"));
                return Map.of(
                    "success", true,
                    "message", "RFQ created successfully",
                    "rfqName", responseBody.get("name") != null ? responseBody.get("name") : "",
                    "data", responseBody.get("data") != null ? responseBody.get("data") : new HashMap<>()
                );
            } else {
                // Gestion du champ message qui peut être un LinkedHashMap
                Object errorMessageObj = responseBody.get("message");
                String errorMessage;
                if (errorMessageObj instanceof Map) {
                    // Si message est un LinkedHashMap, extraire une représentation pertinente
                    errorMessage = responseBody.get("message").toString();
                } else {
                    errorMessage = errorMessageObj != null ? errorMessageObj.toString() : "Unknown error occurred";
                }
                logger.error("RFQ creation failed: {}", errorMessage);
                return Map.of(
                    "success", false,
                    "message", errorMessage
                );
            }
        } else {
            logger.error("Empty response body received from ERPNext");
            return Map.of(
                "success", false,
                "message", "Empty response received from ERPNext"
            );
        }
        
    } catch (HttpClientErrorException e) {
        logger.error("HTTP Error while creating RFQ. Status: {}, Body: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
        return Map.of(
            "success", false,
            "message", "HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString()
        );
    } catch (RestClientException e) {
        logger.error("REST client error while creating RFQ: {}", e.getMessage());
        return Map.of(
            "success", false,
            "message", "Network error: " + e.getMessage()
        );
    } catch (Exception e) {
        logger.error("Unexpected error while creating RFQ: {}", e.getMessage(), e);
        return Map.of(
            "success", false,
            "message", "Unexpected error: " + e.getMessage()
        );
    }
}
}