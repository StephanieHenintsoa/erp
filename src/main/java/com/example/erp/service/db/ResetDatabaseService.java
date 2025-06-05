package com.example.erp.service.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ResetDatabaseService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${erpnext.api.url}")
    private String erpNextApiUrl;

    @Value("${erpnext.api.key}")
    private String apiKey;

    @Value("${erpnext.api.secret}")
    private String apiSecret;

    public void resetDatabase() {
        String endpoint = erpNextApiUrl + "/api/method/hrms.utils.database_reset_util.reset_database";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
    }
}
