# CSV Import Implementation Guideline

## Phase 1: Spring Boot Application Setup

### 1.1 Project Structure and File Creation

**Create the following files in order:**

1. **`CsvImportError.java`** (Entity)
   - **Purpose**: Model class to represent validation errors found in CSV files
   - **Location**: `src/main/java/com/example/erp/entity/`
   - **Contains**: Fields for fileName, lineNumber, errorDescription, timestamp

2. **`CsvImportRequest.java`** (DTO)
   - **Purpose**: Data transfer object to encapsulate the three CSV files from frontend
   - **Location**: `src/main/java/com/example/erp/entity/`
   - **Contains**: Three MultipartFile fields for employee, salary structure, and salary slip CSVs

3. **`CsvImportResponse.java`** (DTO)
   - **Purpose**: Response object containing validation results and error details
   - **Location**: `src/main/java/com/example/erp/entity/`
   - **Contains**: Success flag, list of errors, summary statistics

4. **`FileUploadConfig.java`** (Configuration)
   - **Purpose**: Configure multipart file handling settings
   - **Location**: `src/main/java/com/example/erp/config/`
   - **Contains**: Max file size, max request size, temp directory settings

5. **`ErpNextApiService.java`** (Service)
   - **Purpose**: Handle all ERPNext REST API communications
   - **Location**: `src/main/java/com/example/erp/service/`
   - **Contains**: Authentication, CRUD operations for Employee, Salary Structure, Salary Slip DocTypes

6. **`CsvValidationService.java`** (Service)
   - **Purpose**: Parse and validate CSV files according to ERPNext schema requirements
   - **Location**: `src/main/java/com/example/erp/service/`
   - **Contains**: CSV parsing, data type validation, business rule validation

7. **`CsvImportService.java`** (Service)
   - **Purpose**: Orchestrate the entire import process with "all-or-nothing" logic
   - **Location**: `src/main/java/com/example/erp/service/`
   - **Contains**: Main import workflow, error aggregation, rollback logic

8. **`CsvImportController.java`** (Controller)
   - **Purpose**: Handle HTTP requests for CSV import functionality
   - **Location**: `src/main/java/com/example/erp/controller/`
   - **Contains**: File upload endpoint, validation trigger, response formatting

### 1.2 Configuration Requirements

**Add to `application.properties`:**
- ERPNext server URL, authentication credentials
- File upload size limits and temporary storage paths
- CSV parsing configuration (delimiter, encoding options)

**Add Maven dependencies:**
- OpenCSV for CSV parsing with multiple format support
- Apache HttpClient for ERPNext API calls
- Jackson for JSON serialization/deserialization

### 1.3 Endpoint and Workflow Implementation

**Primary Endpoint: `POST /api/csv-import`**
- Accept three CSV files via multipart form data
- Trigger validation service for all three files simultaneously
- Return structured response with all validation errors
- Only proceed to ERPNext import if zero validation errors exist

**Validation Workflow:**
1. Parse all three CSV files and validate headers match expected structure
2. Validate data types, date formats, and required fields
3. Check referential integrity between the three files
4. Validate against ERPNext DocType mandatory fields and constraints
5. Perform salary calculation verification
6. Aggregate all errors across all files before returning response

### 1.4 Data Formatting for ERPNext

**Transform CSV data into ERPNext-compatible JSON structures:**
- Employee data → Employee DocType JSON format
- Salary structure data → Salary Structure and Salary Component DocType formats
- Salary slip data → Salary Slip DocType JSON format
- Include auto-generated naming series and mandatory fields
- Handle currency formatting and percentage-to-decimal conversions

## Phase 2: ERPNext Application Setup

### 2.1 Python Files Creation

**No new custom DocTypes required** - use existing ERPNext HR DocTypes:
- Employee (existing)
- Salary Structure (existing)
- Salary Component (existing)
- Salary Slip (existing)

**Create custom API endpoint:**

1. **`csv_import_api.py`**
   - **Purpose**: Custom API endpoint to handle bulk import from Spring Boot
   - **Location**: `{your_app}/api/csv_import_api.py`
   - **Contains**: Bulk creation logic, transaction management, error handling

2. **`csv_import_utils.py`**
   - **Purpose**: Utility functions for data validation and transformation
   - **Location**: `{your_app}/utils/csv_import_utils.py`
   - **Contains**: Data validation helpers, auto-generation logic for missing master data

### 2.2 File Placement Structure

**Create a custom ERPNext app for this functionality:**
```
{your_custom_app}/
├── api/
│   └── csv_import_api.py
├── utils/
│   └── csv_import_utils.py
└── hooks.py (modify existing)
```

### 2.3 Existing File Modifications

**Modify `hooks.py`:**
- Add custom API route mapping for the bulk import endpoint
- Configure any required background job scheduling

**No JavaScript files needed** - using ERPNext REST API approach rather than UI-based import

### 2.4 ERPNext Hooks and Events

**Transaction Management:**
- Implement database transaction wrapping around entire import process
- Create rollback mechanism if any record fails validation or creation

## Phase 3: Integration Implementation Sequence

### 3.1 Development Sequence

1. **Setup ERPNext custom app and API endpoints first**
2. **Test ERPNext API endpoints manually using Postman or similar**
3. **Implement Spring Boot services starting with ERPNext API communication**
4. **Build CSV validation service with comprehensive error checking**
5. **Create main import orchestration service with "all-or-nothing" logic**
6. **Implement Spring Boot controller and request/response handling**
7. **Create frontend form with three file inputs and error display table**

This implementation guideline provides a complete roadmap for building the CSV import functionality while adhering to the "all-or-nothing" principle and comprehensive error handling requirements.