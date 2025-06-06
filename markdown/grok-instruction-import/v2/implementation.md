# CSV Import Implementation Guideline

## Overview
This guideline provides a step-by-step implementation plan for building a robust CSV import system that transfers HR data from Spring Boot to ERPNext using the "tout-ou-rien" (all-or-nothing) principle.

## Phase 1: Spring Boot Application Implementation

### 1.1 Project Structure Setup

**Files to Create:**
- `config/FileUploadConfig.java` - Configure multipart file handling
- `entity/CsvError.java` - Model for error tracking
- `dto/ImportResultDto.java` - Data transfer object for import results
- `service/CsvParserService.java` - CSV parsing and validation logic
- `service/ErpNextApiService.java` - ERPNext API communication
- `service/ImportService.java` - Main import orchestration
- `controller/ImportController.java` - HTTP request handling
- `import/import-data.html` - Thymeleaf template for upload form

### 1.2 Configuration Layer

**FileUploadConfig.java Purpose:**
- Configure maximum file sizes for CSV uploads
- Set multipart resolver properties
- Configure temporary file storage locations
- Set encoding handling for international characters

**Specific Configurations Required:**
- Maximum file size: 50MB per file
- Maximum request size: 150MB total
- Temporary file threshold: 1MB
- Default encoding: UTF-8
- Enable multipart file support

### 1.3 Entity and DTO Layer

**CsvError.java Purpose:**
- Store validation errors with file context
- Track line numbers and error descriptions
- Provide structured error reporting

**ImportResultDto.java Purpose:**
- Encapsulate import operation results
- Contain success/failure counts
- Hold error collections for UI display

### 1.4 Service Layer Implementation

**CsvParserService.java Purpose:**
- Parse CSV files with multiple encoding support
- Validate CSV structure and headers
- Convert CSV data to Java objects
- Handle date format variations (DD/MM/YYYY, MM/DD/YYYY)
- Validate data types and business rules

**ErpNextApiService.java Purpose:**
- Manage ERPNext API authentication
- Handle HTTP communication with ERPNext
- Implement retry logic for failed requests
- Manage API rate limiting
- Convert Java objects to ERPNext-compatible JSON

**ImportService.java Purpose:**
- Orchestrate the entire import process
- Implement "tout-ou-rien" validation logic
- Coordinate between parsing and API services
- Handle dynamic master data creation
- Manage transaction rollback scenarios

### 1.5 Controller Layer

**ImportController.java Purpose:**
- Handle HTTP POST requests for file uploads
- Validate incoming multipart files
- Delegate to service layer for processing
- Return structured responses with error details
- Manage user session and progress tracking

**Endpoints to Implement:**
- `POST /import/csv` - Main import endpoint
- `GET /import` - Display upload form

### 1.6 Data Processing Workflow

**Pre-validation Phase:**
- Verify all three CSV files are present
- Check file formats and encodings
- Validate CSV headers match expected structure
- Perform basic data type checks

**Validation Phase:**
- Parse all CSV data into memory
- Validate cross-file relationships
- Check ERPNext DocType requirements
- Verify salary calculation formulas
- Validate date ranges and formats

**Dynamic Creation Phase:**
- Identify missing master data (companies, departments)
- Generate employee naming series
- Create salary components and structures
- Prepare all data for ERPNext insertion

**Import Execution Phase:**
- Send data to ERPNext in correct order
- Monitor API responses for errors
- Implement rollback if any step fails

### 1.7 Frontend Implementation

**import-data.html Purpose:**
- Provide three file input fields for CSV uploads
- Display upload progress indicators
- Show error table with file/line/description columns
- Use Bootstrap for styling
- Implement client-side file validation

**Required Features:**
- File type validation (CSV only)
- File size validation
- Error table filled with errors
    - headers : 
        + File
        + Error Line
        + Error message
- Success/failure status indicators

## Phase 2: ERPNext Application Implementation

### 2.2 API Endpoint Implementation

**Files to Create:**
- `hrms/api/hr_import.py` - Custom API endpoints for HR data import
- `hrms/hooks.py` - Register custom API endpoints

**hr_import.py Purpose:**
- Handle bulk employee creation with validation
- Manage salary structure creation
- Process salary slip imports
- Implement transaction management
- Provide detailed error responses

**API Endpoints to Implement:**
- `/api/method/hrms.api.hr_import.validate_import_data`
- `/api/method/hrms.api.hr_import.import_employees`
- `/api/method/hrms.api.hr_import.import_salary_structures`
- `/api/method/hrms.api.hr_import.import_salary_slips`

### 2.3 Dynamic Master Data Creation

**Files to Create:**
- `hrms/utils/master_data_creator.py` - Utility for creating missing master data

**Purpose:**
- Auto-create companies with minimal required fields
- Generate employee naming series
- Create default departments and designations
- Set up salary components with proper accounts

### 2.4 Validation Framework

**Files to Create:**
- `hrms/validators/hr_data_validator.py` - Comprehensive validation logic

**Purpose:**
- Validate employee data against ERPNext requirements
- Check salary calculation accuracy
- Verify date formats and ranges
- Ensure data consistency across entities

### 2.5 Error Handling and Logging

**Files to Create:**
- `hrms/utils/error_handler.py` - Centralized error handling
- `hrms/utils/import_logger.py` - Detailed import logging

**Purpose:**
- Standardize error responses
- Log all import operations
- Provide debugging information

This implementation guideline ensures a robust, production-ready CSV import system that handles dynamic data creation while maintaining data integrity through comprehensive validation and error handling.