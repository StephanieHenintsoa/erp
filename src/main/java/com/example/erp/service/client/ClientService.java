package com.example.erp.service.client;

import com.example.erp.entity.auth.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private static final String ERPNEXT_CLIENT_RESOURCE_URL = "http://erpnext.localhost:8000/api/resource/Client?fields=[\"*\"]";

    @Autowired
    private RestTemplate restTemplate;

    public List<Client> getClients(String apiKey, String apiSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            Map<String, Object> response = restTemplate.exchange(ERPNEXT_CLIENT_RESOURCE_URL, HttpMethod.GET, request, Map.class).getBody();
            if (response != null && response.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) response.get("message");
                if (message.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) message.get("data");
                    if (data.containsKey("clients")) {
                        List<Map<String, Object>> clients = (List<Map<String, Object>>) data.get("clients");
                        return clients.stream().map(client -> new Client(
                                (String) client.get("name"),
                                (String) client.get("Nom"),
                                client.get("Date") != null ? client.get("Date").toString() : null
                        )).collect(Collectors.toList());
                    }
                }
            }
            throw new RuntimeException("No clients found in response");
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch clients from ERPNext: " + e.getMessage());
        }
    }

    public void createClient(Client client, String apiKey, String apiSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        Map<String, Object> requestBody = Map.of(
                "data", Map.of(
                        "Nom", client.getNom(),
                        "Date", client.getDate() != null ? client.getDate() : ""
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForObject(ERPNEXT_CLIENT_RESOURCE_URL, request, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create client in ERPNext: " + e.getMessage());
        }
    }

    public void updateClient(String name, Client client, String apiKey, String apiSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        Map<String, Object> requestBody = Map.of(
                "data", Map.of(
                        "Nom", client.getNom(),
                        "Date", client.getDate() != null ? client.getDate() : ""
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(ERPNEXT_CLIENT_RESOURCE_URL + "/" + name, HttpMethod.PUT, request, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update client in ERPNext: " + e.getMessage());
        }
    }

    public void deleteClient(String name, String apiKey, String apiSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(ERPNEXT_CLIENT_RESOURCE_URL + "/" + name, HttpMethod.DELETE, request, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete client in ERPNext: " + e.getMessage());
        }
    }
}