Those are 3 CSV samples files, intended for import into an ERPNext with FRappe HR module application built on the Frappe Framework. 

The main theme focusing the 3 csv files is : Human Resource (employee and their salaries) 

CSV Data Samples:
    csv-1: 

    Ref,Nom,Prenom,genre,Date embauche,date naissance,company
    1,Rakoto,Alain,Masculin,03/04/2024,01/01/1980,My Company
    2,Rasoa,Jeanne,Feminin,08/06/2024,01/01/1990,My Company

    csv-2: 

    salary structure,name,Abbr,type,valeur,Remarque
    gasy1,Salaire Base,SB,earning,100%,
    gasy1,Indemnité,IND,earning,30%,salaire base
    gasy1,Taxe sociale,TS,deduction,20%,salaire base + indemnité

    csv-3: 

    Mois,Ref Employe,Salaire Base,Salaire
    01/04/2025,1,1500000,gasy1
    01/04/2025,2,900000,gasy1
    01/03/2025,1,1600000,gasy1
    01/03/2025,2,900000,gasy1

## Data Description

**CSV-1 (Employee Master Data)**
- Contains basic employee information with fields: Reference ID, Last Name, First Name, Gender, Hire Date, Birth Date, and Company
- Maps to ERPNext **Employee** DocType
- Represents the core employee registry with personal and employment details

**CSV-2 (Salary Structure Components)**
- Defines salary structure components with fields: Structure Name, Component Name, Abbreviation, Type (earning/deduction), Value (percentage), and Remarks
- Maps to ERPNext **Salary Structure** and **Salary Component** DocTypes
- Represents the template for calculating employee compensation including base salary, allowances, and deductions

**CSV-3 (Salary Records)**
- Contains monthly salary data with fields: Month, Employee Reference, Base Salary Amount, and Salary Structure Reference
- Maps to ERPNext **Salary Slip** DocType
- Represents actual salary payments/calculations for specific periods

## Relationships

**Primary Relationships:**
- CSV-1 ↔ CSV-3: `Ref` (CSV-1) links to `Ref Employe` (CSV-3) - Employee to Salary Records
- CSV-2 ↔ CSV-3: `salary structure` (CSV-2) links to `Salaire` (CSV-3) - Structure Template to Applied Salary

**Dependency Chain:**
1. Employees must exist before salary records
2. Salary structures must be defined before salary slip generation
3. Salary slips reference both employee and structure data

## Potential Import Errors

**Mandatory Field Issues:**
- Missing Employee ID or naming series in CSV-1
- Incomplete salary component setup in CSV-2
- Missing posting date format in CSV-3

**Data Type Mismatches:**
- Date formats (DD/MM/YYYY) may not match ERPNext expected format
- Percentage values (100%, 30%) need conversion to decimal format
- Currency amounts lack proper formatting

**Reference Integrity:**
- Employee references in CSV-3 must exist in Employee master
- Salary structure "gasy1" must be properly created before salary slip import
- Company "My Company" must exist in ERPNext

**Duplicate/Validation Issues:**
- Potential duplicate salary entries for same employee-month combination
- Gender values need mapping to ERPNext standard values
- Missing employee naming conventions

## Recommendations

**Pre-Import Setup:**
1. Create Company master data first
2. Set up Salary Components (Base, Allowances, Deductions) in ERPNext
3. Configure Salary Structure with proper percentage calculations
4. Establish Employee naming series and mandatory field defaults

**Import Sequence:**
1. **Employees** (CSV-1) → Employee DocType
2. **Salary Structure** (CSV-2) → Salary Structure/Component DocTypes  
3. **Salary Slips** (CSV-3) → Salary Slip DocType

**Data Preparation:**
- Convert date formats to YYYY-MM-DD
- Transform percentage values to decimals (100% → 1.0)
- Standardize gender values (Masculin/Feminin → Male/Female)
- Add required fields like Employee naming series, department assignments

Those CSV files provided to me are samples, but the headers will match those in the real data. However, the actual data may include additional rows, and the specific values (e.g., company names, salary components, salary structures, or employee naming conventions) are unknown and may differ from the samples. Below is the context and requirements for this task:

### Context
- **Sample CSV Files**: I have been provided with three sample CSV files to understand the CSV headers and structure.
- **Real Data**: On the presentation day, I will receive the final versions of the three CSV files containing the real data. I will not be able to modify the code or the application after receiving these files.
- **Import Process**: The Spring Boot application will:
  1. Import the data from the three CSV files into the ERPNext system.
  2. Validate the imported data to ensure accuracy (e.g., verify calculations and confirm successful import).
  3. Handle any errors that occur during the import process.
- **Dynamic Data Creation**: If any mandatory values are missing in the CSV files (e.g., required fields for companies, salary components, or salary structures), the import functionality must dynamically create these values during the import process. Pre-import setup is not feasible due to the variability in the real data.


Here are the implementation instructions and take into considerations all of the things that we've been talking before. Don't add anything else apart the things mentionned here, unless it's really rquired but i've forget to mention it here, tell me specifically about this if it happens: 


1. **Import Logic**:
   - Implement a "tout-ou-rien" (all-or-nothing) principle: all rows across the three CSV files must be valid for any data to be inserted into the ERPNext database.
   - Validate all data before saving to the database, ensuring no errors exist.
   - Display a table below the form detailing any errors, with columns: `File | Line Number | Error Description`.
  ### Dynamic Data Handling Strategy

  **1. Implement Hierarchical Import Logic**
  1. Pre-validate all CSV structures and relationships
  2. Create missing master data dynamically (Company, Departments, etc.)
  3. Import Employees with auto-generated naming series
  4. Create Salary Components and Structures on-the-fly
  5. Import Salary Slips with validation

  **2. Dynamic Master Data Creation**
  - **Company Creation**: If company doesn't exist, create with minimal required fields
  - **Employee Naming**: Implement auto-generation (EMP-YYYY-##### format)
  - **Salary Components**: Auto-create components from CSV-2 with proper account mappings
  - **Department Assignment**: Create default department if missing

  **3. Robust Validation Framework**
  - Schema Validation: Check CSV headers and basic structure
  - Data Type Validation: Convert and validate dates, numbers, percentages
  - Business Logic Validation: Verify salary calculations match structure
  - Reference Integrity: Ensure all foreign key relationships exist
  - ERPNext API Validation: Confirm successful creation in ERPNext

  ### Critical Validation Checks

  **1. Salary Calculation Verification**
  - Recalculate salary components based on structure percentages
  - Compare calculated values with imported base salary amounts
  - Validate earning vs deduction classifications
  - Ensure monthly totals match expected calculations

  **2. Data Consistency Checks**
  - Verify employee-salary record relationships
  - Check for duplicate entries across months
  - Validate date ranges and chronological order
  - Confirm currency formatting and precision (EUR)

  ### High-Risk Scenarios

  **1. Data Format Variations**
  Risk: Date formats, number formats, encoding issues
  Mitigation:
  - Implement multiple date format parsers (DD/MM/YYYY, MM/DD/YYYY, etc.)
  - Handle various CSV encodings (UTF-8, ISO-8859-1, etc.)

  **2. Missing Critical Data**
  Risk: Empty mandatory fields, incomplete salary structures
  Mitigation:
  - Create comprehensive default value strategies
  - Build fallback data generation logic

2. **Spring Boot Implementation**:
   - Adhere to the package structure: `java/com/example/erp/{config,controller,entity,service}`.
   - Keep controllers thin, handling only HTTP requests, validation, and redirection.
   - Place business logic (e.g., CSV parsing, validation, ERPNext API calls) in service classes.
   - Provide code for:
     - A controller to handle form submission and file uploads.
     - A service class to parse CSVs, validate data, and interact with ERPNext APIs.
     - Any necessary entities for temporary data storage or error tracking.
     - A configuration class if needed (e.g., for multipart file handling).

3. **ERPNext Implementation**:
   - Specify whether new DocTypes are required based on the CSV data (e.g., for custom entities or temporary error logging).
   - Identify if existing ERPNext DocTypes (e.g., Item, Customer) or files need updates.
   - Provide Frappe Framework code (e.g., Python scripts, custom API endpoints) if server-side logic is needed, referencing official documentation:
     - ERPNext: `https://docs.frappe.io/erpnext/user/manual/en/introduction`
     - Frappe HR: `https://docs.frappe.io/hr/introduction`
     - Frappe Framework: `https://docs.frappe.io/framework/user/en/introduction`

4. **Error Handling**:
   - Validate CSV data against ERPNext DocType schemas (e.g., mandatory fields, data types).
   - Log errors for each file, including file name, line number, and detailed error description.
   - Return errors in a structured format for display in the UI table.

5. **Code Deliverables**:
   - For Spring Boot, include controller, service, and entity classes as separate artifacts if needed.
   - For ERPNext, include Python scripts, DocType JSON (if applicable), or modifications to existing files.
   - Ensure Spring Boot controllers follow best practices (thin, with business logic in services).
   - For the frontend, provide a simple HTML/JS form for file uploads and error table display, using modern JavaScript and Tailwind CSS.

6. **Assumptions and Clarifications**:
   - If CSV data structure is unknown, request sample data or assume typical ERPNext entities (e.g., Item, Customer, Sales Order).
   - Use ERPNext’s REST API for data import unless custom server-side logic is required.
   - Reference official ERPNext/Frappe documentation for import processes or API usage.
   - If web searches or X posts are needed for specific issues (e.g., common CSV import errors), summarize findings briefly.

**Instructions**:
- Keep responses concise, focusing on code and minimal explanations unless requested.
- Use UUIDs for new artifacts; reuse artifact IDs if updating previous ones.
- Ensure ERPNext code aligns with Frappe Framework best practices and documentation.
- For Spring Boot, follow the specified package structure and controller best practices.
- Avoid generating charts or images unless explicitly requested.


# CSV Import Functionality Requirements

## Spring Boot
- **Controller**: Handle form submission, file uploads; validate requests; redirect to service.
- **Service**: Parse CSVs, validate data, call ERPNext APIs, collect errors.
- **Entity**: Model for error tracking (e.g., `CsvError` with file, line, description).
- **Config**: Configure multipart file handling if needed.
- **Thymeleaf**: A thymeleaf view with the single form within 3 inputs fields

## ERPNext
- **DocTypes**: Specify if new DocTypes are needed (e.g., for custom data or error logging).
- **Files**: Identify updates to existing files (e.g., custom scripts, API endpoints).
- **API**: Use ERPNext REST API for data import unless server-side logic is required.

## Frontend
- HTML/JS form with three file inputs and error table (columns: File, Line Number, Error Description).

## Validation
- Ensure all CSV rows are valid before database insertion ("tout-ou-rien").
- Validate against ERPNext DocType schemas (e.g., mandatory fields, data types).
- Display errors in a table with file name, line number, and description.


## Precautions and Best Practices

### Dynamic Data Handling Strategy

**1. Implement Hierarchical Import Logic**
1. Pre-validate all CSV structures and relationships
2. Create missing master data dynamically (Company, Departments, etc.)
3. Import Employees with auto-generated naming series
4. Create Salary Components and Structures on-the-fly
5. Import Salary Slips with validation

**2. Dynamic Master Data Creation**
- **Company Creation**: If company doesn't exist, create with minimal required fields
- **Employee Naming**: Implement auto-generation (EMP-YYYY-##### format)
- **Salary Components**: Auto-create components from CSV-2 with proper account mappings
- **Department Assignment**: Create default department if missing

**3. Robust Validation Framework**
- Schema Validation: Check CSV headers and basic structure
- Data Type Validation: Convert and validate dates, numbers, percentages
- Business Logic Validation: Verify salary calculations match structure
- Reference Integrity: Ensure all foreign key relationships exist
- ERPNext API Validation: Confirm successful creation in ERPNext

### Error Handling and Reporting

**1. Comprehensive Error Logging**
- Row-level error tracking with specific failure reasons
- Rollback mechanism for failed imports
- Detailed error reports with actionable recommendations
- Success/failure statistics per DocType

### Critical Validation Checks

**1. Salary Calculation Verification**
- Recalculate salary components based on structure percentages
- Compare calculated values with imported base salary amounts
- Validate earning vs deduction classifications
- Ensure monthly totals match expected calculations

**2. Data Consistency Checks**
- Verify employee-salary record relationships
- Check for duplicate entries across months
- Validate date ranges and chronological order
- Confirm currency formatting and precision

## Potential Risks and Mitigation

### High-Risk Scenarios

**1. Data Format Variations**
Risk: Date formats, number formats, encoding issues
Mitigation:
- Implement multiple date format parsers (DD/MM/YYYY, MM/DD/YYYY, etc.)
- Use locale-aware number parsing with fallback mechanisms
- Handle various CSV encodings (UTF-8, ISO-8859-1, etc.)

**2. Missing Critical Data**
Risk: Empty mandatory fields, incomplete salary structures
Mitigation:
- Create comprehensive default value strategies
- Build fallback data generation logic

### Technical Implementation Recommendations

**1. Flexible CSV Processing**
- Use header-based field mapping (not positional)
- Implement case-insensitive header matching
- Handle extra columns gracefully
- Support various delimiter types (, ; | \t)

**2. Transaction Management**
- Use database transactions for related records
- Cancel import when an error occured


### Critical Success Factors

1. **Flexibility Over Assumptions**: Design for variability rather than specific data patterns
2. **Validation First Approach**: Validate everything before committing any data


/* ----------------------------------------- */
/* ----------------------------------------- */

in your implementations you need to use those existing class in the Spring Boot App : 

--
--
--

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

--
--
--

package com.example.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
public class FileUploadConfig {

    @Bean(name = "multipartResolver")
    public StandardServletMultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        return resolver;
    }
}

--
--
--

package com.example.erp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CsvImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private int lineNumber;
    private String errorDescription;
    private LocalDateTime timestamp;

    // Constructors
    public CsvImportError() {
    }

    public CsvImportError(String fileName, int lineNumber, String errorDescription, LocalDateTime timestamp) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.errorDescription = errorDescription;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}



--
--
--

package com.example.erp.service.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.erp.entity.CsvImportError;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ErpNextApiService {

    @Value("${erpnext.api.url}")
    private String erpNextUrl;

    @Value("${erpnext.api.username}")
    private String username;

    @Value("${erpnext.api.password}")
    private String password;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;
    
    // Store session cookies for subsequent requests
    private String sessionCookies;
    private long lastLoginTime = 0;
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    @Autowired
    public ErpNextApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Login and get session cookies
    private boolean loginAndGetSession() {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("usr", username);
            requestBody.put("pwd", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                erpNextUrl + "/api/method/login",
                HttpMethod.POST,
                request,
                String.class
            );

            // Extract cookies from response
            List<String> cookies = response.getHeaders().get("Set-Cookie");
            if (cookies != null && !cookies.isEmpty()) {
                sessionCookies = String.join("; ", cookies);
                lastLoginTime = System.currentTimeMillis();
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    // Check if session is valid and login if necessary
    private boolean ensureAuthenticated() {
        long currentTime = System.currentTimeMillis();
        
        // If no session or session expired, login again
        if (sessionCookies == null || (currentTime - lastLoginTime) > SESSION_TIMEOUT) {
            return loginAndGetSession();
        }
        return true;
    }

    // Create authenticated headers with session cookies
    private HttpHeaders createAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (sessionCookies != null) {
            headers.set("Cookie", sessionCookies);
        }
        
        return headers;
    }

    // Create a record in ERPNext
    public boolean createRecord(String docType, Map<String, Object> data, List<CsvImportError> errors) {
        try {
            if (!ensureAuthenticated()) {
                errors.add(new CsvImportError(docType + ".csv", 0, "Failed to authenticate with ERPNext", LocalDateTime.now()));
                return false;
            }

            HttpHeaders headers = createAuthenticatedHeaders();
            String json = objectMapper.writeValueAsString(data);
            HttpEntity<String> request = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                erpNextUrl + "/api/resource/" + docType,
                HttpMethod.POST,
                request,
                String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // If we get 403, try to re-login once
            if (e.getStatusCode().value() == 403) {
                sessionCookies = null; // Force re-login
                if (ensureAuthenticated()) {
                    return createRecord(docType, data, errors); // Retry once
                }
            }
            
            String errorMessage = e.getResponseBodyAsString();
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(errorMessage, Map.class);
                String serverMessage = errorResponse.getOrDefault("_error_message", e.getMessage()).toString();
                errors.add(new CsvImportError(docType + ".csv", 0, "API error creating record: " + serverMessage, LocalDateTime.now()));
            } catch (Exception ex) {
                errors.add(new CsvImportError(docType + ".csv", 0, "API error creating record: " + e.getStatusCode() + " - " + e.getMessage(), LocalDateTime.now()));
            }
            return false;
        } catch (Exception e) {
            errors.add(new CsvImportError(docType + ".csv", 0, "API error creating record: " + e.getMessage(), LocalDateTime.now()));
            return false;
        }
    }

    // Retrieve a record from ERPNext
    public Map<String, Object> getRecord(String docType, String name, List<CsvImportError> errors) {
        try {
            if (!ensureAuthenticated()) {
                errors.add(new CsvImportError(docType + ".csv", 0, "Failed to authenticate with ERPNext", LocalDateTime.now()));
                return null;
            }

            HttpHeaders headers = createAuthenticatedHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                erpNextUrl + "/api/resource/" + docType + "/" + name,
                HttpMethod.GET,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            errors.add(new CsvImportError(docType + ".csv", 0, "Failed to retrieve record: " + name, LocalDateTime.now()));
            return null;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                errors.add(new CsvImportError(docType + ".csv", 0, "Record not found: " + name, LocalDateTime.now()));
                return null;
            }
            if (e.getStatusCode().value() == 403) {
                sessionCookies = null; // Force re-login
                if (ensureAuthenticated()) {
                    return getRecord(docType, name, errors); // Retry once
                }
            }
            String errorMessage = e.getResponseBodyAsString();
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(errorMessage, Map.class);
                String serverMessage = errorResponse.getOrDefault("_error_message", e.getMessage()).toString();
                errors.add(new CsvImportError(docType + ".csv", 0, "API error retrieving record: " + serverMessage, LocalDateTime.now()));
            } catch (Exception ex) {
                errors.add(new CsvImportError(docType + ".csv", 0, "API error retrieving record: " + e.getStatusCode() + " - " + e.getMessage(), LocalDateTime.now()));
            }
            return null;
        } catch (Exception e) {
            errors.add(new CsvImportError(docType + ".csv", 0, "API error retrieving record: " + e.getMessage(), LocalDateTime.now()));
            return null;
        }
    }

    // Update a record in ERPNext
    public boolean updateRecord(String docType, String name, Map<String, Object> data, List<CsvImportError> errors) {
        try {
            if (!ensureAuthenticated()) {
                errors.add(new CsvImportError(docType + ".csv", 0, "Failed to authenticate with ERPNext", LocalDateTime.now()));
                return false;
            }

            HttpHeaders headers = createAuthenticatedHeaders();
            String json = objectMapper.writeValueAsString(data);
            HttpEntity<String> request = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                erpNextUrl + "/api/resource/" + docType + "/" + name,
                HttpMethod.PUT,
                request,
                String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            if (e.getStatusCode().value() == 403) {
                sessionCookies = null; // Force re-login
                if (ensureAuthenticated()) {
                    return updateRecord(docType, name, data, errors); // Retry once
                }
            }
            String errorMessage = e.getResponseBodyAsString();
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(errorMessage, Map.class);
                String serverMessage = errorResponse.getOrDefault("_error_message", e.getMessage()).toString();
                errors.add(new CsvImportError(docType + ".csv", 0, "API error updating record: " + serverMessage, LocalDateTime.now()));
            } catch (Exception ex) {
                errors.add(new CsvImportError(docType + ".csv", 0, "API error updating record: " + e.getStatusCode() + " - " + e.getMessage(), LocalDateTime.now()));
            }
            return false;
        } catch (Exception e) {
            errors.add(new CsvImportError(docType + ".csv", 0, "API error updating record: " + e.getMessage(), LocalDateTime.now()));
            return false;
        }
    }

    // Check if a record exists with better error handling
    public boolean checkRecordExists(String docType, String name, List<CsvImportError> errors) {
        try {
            if (!ensureAuthenticated()) {
                errors.add(new CsvImportError(docType + ".csv", 0, "Failed to authenticate with ERPNext", LocalDateTime.now()));
                return false;
            }

            HttpHeaders headers = createAuthenticatedHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                erpNextUrl + "/api/resource/" + docType + "/" + name,
                HttpMethod.GET,
                request,
                String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                // Record doesn't exist, which is not an error
                return false;
            }
            
            // If we get 403, try to re-login once
            if (e.getStatusCode().value() == 403) {
                sessionCookies = null; // Force re-login
                if (ensureAuthenticated()) {
                    return checkRecordExists(docType, name, errors); // Retry once
                }
            }
            
            String errorMsg = "API error checking record existence: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
            errors.add(new CsvImportError(docType + ".csv", 0, errorMsg, LocalDateTime.now()));
            return false;
        } catch (Exception e) {
            errors.add(new CsvImportError(docType + ".csv", 0, "API error checking record existence: " + e.getMessage(), LocalDateTime.now()));
            return false;
        }
    }

    // Method to manually refresh session (useful for long-running operations)
    public boolean refreshSession() {
        sessionCookies = null;
        return ensureAuthenticated();
    }
}


---
---
---

thymeleaf view : 


<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Import CSV</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <style>
        .main-container {
            display: flex;
        }
        .content-wrapper {
            flex-grow: 1;
            padding: 20px;
        }
        .form-section {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 5px;
        }
        .table-container {
            margin-top: 20px;
        }
        .card {
            margin-top: 20px;
        }
        .card-title {
            font-size: 1.5rem;
        }
        .btn-import {
            background-color: #007bff;
            color: white;
        }
        .btn-import:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>
<div class="main-container">
    <!-- Sidebar -->
    <div th:replace="fragments/sidebar :: sidebar"></div>

    <!-- Content -->
    <div class="content-wrapper">
        <h1>Importation de Fichiers CSV</h1>

        <!-- Import Form Card -->
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">Sélectionner les fichiers à importer</h5>
                <form th:action="@{/csv-import}" th:object="${csvImportRequest}" method="post" enctype="multipart/form-data">
                    <div class="mb-3">
                        <label for="employeeCsv" class="form-label">Fichier - 1 (.csv)</label>
                        <input type="file" id="employeeCsv" name="employeeCsv" class="form-control" accept=".csv" required>
                    </div>
                    <div class="mb-3">
                        <label for="salaryStructureCsv" class="form-label">Fichier - 2 (.csv)</label>
                        <input type="file" id="salaryStructureCsv" name="salaryStructureCsv" class="form-control" accept=".csv" required>
                    </div>
                    <div class="mb-3">
                        <label for="salarySlipCsv" class="form-label">Fichier -(.csv)</label>
                        <input type="file" id="salarySlipCsv" name="salarySlipCsv" class="form-control" accept=".csv" required>
                    </div>
                    <button type="submit" class="btn btn-import">Importer</button>
                </form>
            </div>
        </div>

        <!-- Response Section -->
        <div th:if="${response != null}" class="mt-4">
            <p th:class="${response.success} ? 'text-success' : 'text-danger'" th:text="${response.summaryStatistics}"></p>

            <div th:if="${!response.errors.isEmpty()}" class="card mt-3">
                <div class="card-body">
                    <h5 class="card-title">Erreurs d'importation</h5>
                    <div class="table-responsive">
                        <table class="table table-striped">
                            <thead>
                                <tr>
                                    <th>Fichier</th>
                                    <th>Ligne</th>
                                    <th>Description de l'erreur</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr th:each="error : ${response.errors}">
                                    <td th:text="${error.fileName}"></td>
                                    <td th:text="${error.lineNumber}"></td>
                                    <td th:text="${error.errorDescription}"></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Footer -->
<div th:replace="fragments/footer :: footer"></div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
