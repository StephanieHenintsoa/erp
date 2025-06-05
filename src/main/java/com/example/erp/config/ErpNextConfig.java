package com.example.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ErpNextConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // ADMIN CREDENTIALS
    public static final String ADMIN_USERNAME = "Administrator";
    public static final String ADMIN_PASSWORD = "admin";

    // API KEY
    public static final String API_KEY = "ae80c13a3676603";
    public static final String API_SECRET = "6ff142270f3a856";

    // ENDPOINTS 
    public static final String ERP_NEXT_API_EMPLOYEE_URL = "http://erpnext.localhost:8000/api/resource/Employee";  
    public static final String ERP_NEXT_API_DEPARTMENT_URL = "http://erpnext.localhost:8000/api/resource/Department";  
    public static final String ERP_NEXT_API_DESIGNATION_URL = "http://erpnext.localhost:8000/api/resource/Designation";  
}