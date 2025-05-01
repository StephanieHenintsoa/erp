package com.example.erp.service.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginService {

    private static final String ERPNEXT_LOGIN_URL = "http://erpnext.localhost:8000/api/method/login";

    @Autowired
    private RestTemplate restTemplate;

    public String login(String username, String password, HttpSession session) {
        // Prepare request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("usr", username);
        requestBody.put("pwd", password);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HTTP entity
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        // Make API call
        try {
            String response = restTemplate.postForObject(ERPNEXT_LOGIN_URL, request, String.class);
            // Supposons que la réponse contient un identifiant ou un token
            // Vous devrez peut-être parser la réponse JSON pour extraire des informations spécifiques
            session.setAttribute("loggedInUser", username); // Stocker l'utilisateur dans la session
            session.setAttribute("isAuthenticated", true); // Indicateur d'authentification
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to login to ERPNext: " + e.getMessage());
        }
    }
    
    // Méthode utilitaire pour vérifier l'état de la session
    public static boolean isAuthenticated(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        return isAuthenticated != null && isAuthenticated;
    }
}