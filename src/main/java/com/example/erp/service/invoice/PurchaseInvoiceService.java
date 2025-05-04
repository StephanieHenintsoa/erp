package com.example.erp.service.invoice;

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
    private static final String ERPNEXT_MARK_INVOICE_PAID_API_URL = "http://erpnext.localhost:8000/api/method/erpnext.purchase_order.purchase_invoice_api_controller.mark_invoice_paid";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.key}")
    private String apiKey;

    @Value("${erpnext.api.secret}")
    private String apiSecret;

    @SuppressWarnings("unchecked")
    public List<PurchaseInvoice> getPurchaseInvoices() {
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

                        return result;
                    }
                }
            }
            throw new RuntimeException("No purchase invoices found in response");
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch purchase invoices: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch purchase invoices from ERPNext: " + e.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    public void markInvoicePaid(String invoiceName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        String url = ERPNEXT_MARK_INVOICE_PAID_API_URL + "?invoice_name=" + invoiceName;
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            // Fixed response parsing logic
            if (responseBody != null) {
                if (responseBody.containsKey("message")) {
                    Map<String, Object> messageBody = (Map<String, Object>) responseBody.get("message");
                    
                    // Now check the status in the message body
                    if (messageBody.containsKey("status") && "success".equals(messageBody.get("status"))) {
                        return;
                    } else if (messageBody.containsKey("status") && "error".equals(messageBody.get("status"))) {
                        throw new RuntimeException("Failed to mark invoice as paid: " + messageBody.get("message"));
                    }
                } 
                // Direct check for success status in the response
                else if (responseBody.containsKey("status") && "success".equals(responseBody.get("status"))) {
                    return;
                }
            }
            
            throw new RuntimeException("Failed to mark invoice as paid: " + responseBody);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            e.printStackTrace();
            throw new RuntimeException("Failed to mark invoice as paid: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to mark invoice as paid: " + e.getMessage());
        }
    }
}