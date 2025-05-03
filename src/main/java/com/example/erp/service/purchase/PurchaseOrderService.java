package com.example.erp.service.purchase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.example.erp.entity.purchase.PurchaseOrder;
import com.example.erp.entity.purchase.PurchaseOrderItem;

@Service
public class PurchaseOrderService {

    private static final String ERPNEXT_PURCHASE_ORDER_API_URL = "http://erpnext.localhost:8000/api/method/erpnext.purchase_order.purchase_order_api_controller.get_purchase_orders?supplier_name=";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.key}")
    private String apiKey;

    @Value("${erpnext.api.secret}")
    private String apiSecret;

    private List<PurchaseOrder> cachedPurchaseOrders = new ArrayList<>();
    private String lastFetchedSupplier = null;
    private String lastFetchedStatus = null;

    @SuppressWarnings("unchecked")
    public List<PurchaseOrder> getPurchaseOrders(String supplierName, String status) {
        if (supplierName.equals(lastFetchedSupplier) && status.equals(lastFetchedStatus) && !cachedPurchaseOrders.isEmpty()) {
            return cachedPurchaseOrders;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = ERPNEXT_PURCHASE_ORDER_API_URL + supplierName;
        if (status != null && !status.equals("all")) {
            url += "&status=" + status;
        }

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            System.out.println("Response: " + responseBody);

            if (responseBody != null && responseBody.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) responseBody.get("message");
                if (message.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) message.get("data");
                    if (data.containsKey("purchase_orders")) {
                        List<Map<String, Object>> purchaseOrders = (List<Map<String, Object>>) data.get("purchase_orders");
                        List<PurchaseOrder> result = purchaseOrders.stream().map(order -> {
                            List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
                            List<PurchaseOrderItem> orderItems = items != null ? items.stream().map(item -> new PurchaseOrderItem(
                                (String) item.getOrDefault("item_name", null),
                                ((Number) item.get("qty")).doubleValue(),
                                ((Number) item.get("rate")).doubleValue(),
                                ((Number) item.get("amount")).doubleValue()
                            )).collect(Collectors.toList()) : new ArrayList<>();
                            PurchaseOrder po = new PurchaseOrder(
                                (String) order.get("name"),
                                (String) order.get("supplier"),
                                (String) order.get("transaction_date"),
                                (String) order.get("schedule_date"),
                                ((Number) order.get("grand_total")).doubleValue(),
                                (String) order.get("status"),
                                orderItems
                            );
                            // Set Reçu status (per_received == 100)
                            po.setIsReceived(((Number) order.getOrDefault("per_received", 0.0)).doubleValue() >= 100.0);
                            // Set Payé status
                            po.setIsPaid((Boolean) order.getOrDefault("is_paid", false));
                            return po;
                        }).collect(Collectors.toList());

                        this.cachedPurchaseOrders = result;
                        this.lastFetchedSupplier = supplierName;
                        this.lastFetchedStatus = status;

                        return result;
                    }
                }
            }
            throw new RuntimeException("No purchase orders found in response");
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch purchase orders: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to fetch purchase orders from ERPNext: " + e.getMessage());
        }
    }
}