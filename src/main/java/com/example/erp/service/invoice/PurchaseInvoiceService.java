package com.example.erp.service.invoice;


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

import com.example.erp.entity.invoice.PurchaseInvoice;

@Service
public class PurchaseInvoiceService {

    private static final String ERPNEXT_PURCHASE_INVOICE_API_URL = "http://erpnext.localhost:8000/api/method/erpnext.purchase_order.purchase_invoice_api_controller.get_purchase_invoices";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.key}")
    private String apiKey;

    @Value("${erpnext.api.secret}")
    private String apiSecret;

    private List<PurchaseInvoice> cachedPurchaseInvoices = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public List<PurchaseInvoice> getPurchaseInvoices() {
        if (!cachedPurchaseInvoices.isEmpty()) {
            return cachedPurchaseInvoices;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                ERPNEXT_PURCHASE_INVOICE_API_URL,
                HttpMethod.GET,
                request,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) responseBody.get("message");
                if (message.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) message.get("data");
                    if (data.containsKey("purchase_invoices")) {
                        List<Map<String, Object>> purchaseInvoices = (List<Map<String, Object>>) data.get("purchase_invoices");
                        List<PurchaseInvoice> result = purchaseInvoices.stream().map(invoice -> new PurchaseInvoice(
                            (String) invoice.get("name"),
                            (String) invoice.get("supplier"),
                            (String) invoice.get("posting_date"),
                            (String) invoice.get("due_date"),
                            ((Number) invoice.get("grand_total")).doubleValue(),
                            ((Number) invoice.get("outstanding_amount")).doubleValue(),
                            (String) invoice.get("status")
                        )).collect(Collectors.toList());

                        this.cachedPurchaseInvoices = result;
                        return result;
                    }
                }
            }
            throw new RuntimeException("No purchase invoices found in response");
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch purchase invoices: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to fetch purchase invoices from ERPNext: " + e.getMessage());
        }
    }
}