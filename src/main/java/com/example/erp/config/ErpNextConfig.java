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
    public static final String API_KEY = "c2626e78b79c88b";
    public static final String API_SECRET = "1a90aa5401c94b4";

    // ENDPOINTS  
    public static final String ERP_NEXT_API_BASE_URL = "http://erpnext.localhost:8000/api/method/";
    public static final String ERP_NEXT_API_EMPLOYEE_URL = "http://erpnext.localhost:8000/api/resource/Employee";  
    public static final String ERP_NEXT_API_DEPARTMENT_URL = "http://erpnext.localhost:8000/api/resource/Department";  
    public static final String ERP_NEXT_API_DESIGNATION_URL = "http://erpnext.localhost:8000/api/resource/Designation";
    public static final String ERP_NEXT_API_SALARY_COMPONENT_URL = "http://erpnext.localhost:8000/api/resource/Salary Component";
    public static final String ERP_NEXT_API_SALARY_SLIP_URL = "http://erpnext.localhost:8000/api/resource/Salary Slip";
    public static final String ERP_NEXT_API_SALARY_STRUCTURE_ASSIGNMENT_URL = "http://erpnext.localhost:8000/api/resource/Salary Structure Assignment";
}