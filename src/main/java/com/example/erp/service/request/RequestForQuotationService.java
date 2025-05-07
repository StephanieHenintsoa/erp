package com.example.erp.service.request;

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

import com.example.erp.entity.request.RequestForQuotation;
import com.example.erp.entity.request.RequestForQuotationItem;

@Service
public class RequestForQuotationService {

    private static final String ERPNEXT_RFQ_API_URL =
        "http://erpnext.localhost:8000/api/method/erpnext.request.request_for_quotation_api_controller.get_request_for_quotations?supplier_name=";
    private static final String ERPNEXT_RFQ_BY_NAME_API_URL =
        "http://erpnext.localhost:8000/api/method/erpnext.request.request_for_quotation_api_controller.get_request_for_quotation_by_name?rfq_name=";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.key}")
    private String apiKey;

    @Value("${erpnext.api.secret}")
    private String apiSecret;

    /**
     * Get request for quotations for a supplier.
     * This method is an alias for getRequestForQuotations to maintain semantic clarity in controller code.
     *
     * @param supplierName The name of the supplier
     * @return List of RequestForQuotation objects
     */
    public List<RequestForQuotation> getRequestForQuotationsForSupplier(String supplierName) {
        return getRequestForQuotations(supplierName);
    }

    @SuppressWarnings("unchecked")
    public List<RequestForQuotation> getRequestForQuotations(String supplierName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                ERPNEXT_RFQ_API_URL + supplierName,
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
                    if (data.containsKey("request_for_quotations")) {
                        List<Map<String, Object>> rfqs = (List<Map<String, Object>>) data.get("request_for_quotations");

                        List<RequestForQuotation> result = rfqs.stream().map(rfq -> {
                            RequestForQuotation rfqEntity = new RequestForQuotation(
                                (String) rfq.get("name"),
                                (String) rfq.get("transaction_date"),
                                (String) rfq.get("schedule_date"),
                                (String) rfq.get("status"),
                                (String) rfq.get("company")
                            );

                            // Extraction des items
                            List<Map<String, Object>> items = (List<Map<String, Object>>) rfq.get("items");
                            if (items != null) {
                                List<RequestForQuotationItem> rfqItems = items.stream().map(item -> {
                                    RequestForQuotationItem rfqItem = new RequestForQuotationItem();
                                    rfqItem.setItemCode((String) item.get("item_code"));
                                    rfqItem.setItemName((String) item.get("item_name"));
                                    rfqItem.setDescription((String) item.get("description"));
                                    rfqItem.setQty(item.get("qty") != null ? ((Number) item.get("qty")).doubleValue() : 0.0);
                                    rfqItem.setUom((String) item.get("uom"));
                                    return rfqItem;
                                }).collect(Collectors.toList());
                                rfqEntity.setItems(rfqItems);
                            }

                            return rfqEntity;
                        }).collect(Collectors.toList());
                        return result;
                    }
                }
            }

            throw new RuntimeException("No request for quotations found in response");

        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch request for quotations: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to fetch request for quotations from ERPNext: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public RequestForQuotation getRequestForQuotationByName(String rfqName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                ERPNEXT_RFQ_BY_NAME_API_URL + rfqName,
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
                    if (data.containsKey("request_for_quotation")) {
                        Map<String, Object> rfq = (Map<String, Object>) data.get("request_for_quotation");

                        RequestForQuotation rfqEntity = new RequestForQuotation(
                            (String) rfq.get("name"),
                            (String) rfq.get("transaction_date"),
                            (String) rfq.get("schedule_date"),
                            (String) rfq.get("status"),
                            (String) rfq.get("company")
                        );

                        // Extraction des items
                        List<Map<String, Object>> items = (List<Map<String, Object>>) rfq.get("items");
                        if (items != null) {
                            List<RequestForQuotationItem> rfqItems = items.stream().map(item -> {
                                RequestForQuotationItem rfqItem = new RequestForQuotationItem();
                                rfqItem.setItemCode((String) item.get("item_code"));
                                rfqItem.setItemName((String) item.get("item_name"));
                                rfqItem.setDescription((String) item.get("description"));
                                rfqItem.setQty(item.get("qty") != null ? ((Number) item.get("qty")).doubleValue() : 0.0);
                                rfqItem.setUom((String) item.get("uom"));
                                return rfqItem;
                            }).collect(Collectors.toList());
                            rfqEntity.setItems(rfqItems);
                        }

                        return rfqEntity;
                    }
                }
            }

            throw new RuntimeException("No request for quotation found in response");

        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch request for quotation: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to fetch request for quotation from ERPNext: " + e.getMessage());
        }
    }
}