# Updated CSV Import Integration Implementation Guideline

## Phase 1: Spring Boot Application Development

### Step 1: Project Setup and Dependencies
- **Add required dependencies** to `pom.xml` or `build.gradle`:
  - Spring Web (for REST endpoints)
  - Spring Boot Starter Validation (for data validation)
  - OpenCSV or Apache Commons CSV (for CSV parsing)
  - Spring Boot Starter Web (for HTTP client functionality)
  - Jackson (for JSON serialization/deserialization)

### Step 2: Configuration Files
- **Create `application.yml` or `application.properties`** with:
  - ERPNext base URL configuration
  - API authentication credentials (username/password or API key/secret)
  - File upload size limits (multipart.max-file-size, multipart.max-request-size)

- **Create `FileUploadConfig.java`** configuration class in `/config` package:
  - Bean definition for MultipartConfigElement
  - Configure maximum file sizes
  - Set temporary file storage location

### Step 3: Entity Classes
- **Create `CsvError.java`** entity in `/entity` package:
  - Fields: fileName (String), lineNumber (Integer), errorDescription (String)
  - Constructor, getters, setters
  - This entity is for temporary error tracking, not database persistence

- **Create data model entities** for CSV validation:
  - `MaterialRequestData.java` (for CSV-1 validation)
  - `SupplierData.java` (for CSV-2 validation)
  - `RequestQuotationData.java` (for CSV-3 validation)

### Step 4: Service Layer Implementation
- **Create `CsvImportService.java`** in `/service` package:
  - Method `parseAndValidateCSVs(MultipartFile[] files)` returning `List<CsvError>`
  - Method `validateMaterialRequests(List<MaterialRequestData> data, String fileName)` 
  - Method `validateSuppliers(List<SupplierData> data, String fileName)`
  - Method `validateRequestQuotations(List<RequestQuotationData> data, String fileName)`
  - Method `importToERPNext(parsed data)` for API calls
  - All-or-nothing validation logic implementation

- **Create `ERPNextApiService.java`** in `/service` package:
  - Method `authenticateWithERPNext()` for login
  - Method `createSupplier(SupplierData data)` 
  - Method `createMaterialRequest(MaterialRequestData data)`
  - Method `createRequestQuotation(RequestQuotationData data)`
  - REST client implementation using RestTemplate
  - Error handling for API responses

### Step 5: Controller Layer
- **Create `CsvImportController.java`** in `/controller` package:
  - GET endpoint `/csv-import` to display the form
  - POST endpoint `/csv-import` to handle form submission
  - Accept three MultipartFile parameters
  - Call service for validation and processing
  - Return validation results to the view
  - Keep controller thin - only handle HTTP requests and validation

### Step 6: Exception Handling
- **Create `CsvValidationException.java`** in `/entity` package:
  - Custom exception for CSV validation errors
  - Include list of CsvError objects

- **Add error handling** in controller:
  - Catch and handle file upload errors
  - Return structured error responses
  - Handle ERPNext API communication errors

## Phase 2: Frontend Implementation

### Step 7: HTML Form Creation
- **Create `csv-import.html`** template:
  - Form with three file input fields (for CSV-1, CSV-2, CSV-3)
  - Submit button to trigger import
  - Error display table with columns: File | Line Number | Error Description
  - Use Tailwind CSS for styling
  - Avoid `<form onSubmit>` due to sandbox restrictions

### Step 8: JavaScript Implementation
- **Create client-side JavaScript**:
  - Handle form submission via fetch API
  - File validation (ensure CSV format, file size)
  - Display loading states during processing
  - Populate error table with validation results
  - Clear previous errors on new submission

## Phase 3: ERPNext Integration

### Step 9: ERPNext API Assessment
- **Determine if existing ERPNext DocTypes are sufficient**:
  - Material Request DocType for CSV-1 data
  - Supplier DocType for CSV-2 data  
  - Request for Quotation DocType for CSV-3 data

- **Identify if custom DocTypes are needed**:
  - Assess if standard ERPNext DocTypes cover all CSV fields
  - Create custom DocTypes only if existing ones are insufficient

### Step 10: ERPNext API Endpoint Usage
- **Use standard ERPNext REST API endpoints**:
  - `/api/resource/Supplier` for supplier creation
  - `/api/resource/Material Request` for material request creation
  - `/api/resource/Request for Quotation` for RFQ creation
  - Authentication via `/api/method/login`

- **No custom server-side ERPNext code required** unless:
  - Standard API endpoints don't support required functionality
  - Complex business logic needs server-side processing
  - Custom validation rules beyond standard DocType validation

### Step 11: ERPNext Configuration (if needed)
- **Configure API access permissions**:
  - Ensure API user has create permissions for required DocTypes
  - Set up proper role-based access control

- **Verify DocType field mappings**:
  - Map CSV fields to ERPNext DocType fields
  - Handle any field name differences in the Spring Boot service

## Phase 4: Validation and Error Handling

### Step 12: CSV Data Validation Implementation
- **Implement "tout-ou-rien" principle**:
  - Validate all three CSV files completely before any database operations
  - Collect all errors across all files
  - Only proceed with ERPNext import if zero validation errors

### Step 13: ERPNext Schema Validation
- **Validate against ERPNext DocType requirements**:
  - Check mandatory fields for each DocType
  - Validate data types (dates, numbers, text)
  - Verify foreign key references exist
  - Check field length limitations

### Step 14: Error Collection and Display
- **Structure error information**:
  - File name (CSV-1, CSV-2, or CSV-3)
  - Line number (starting from 2, accounting for headers)
  - Detailed error description
  - Return errors in format suitable for HTML table display

## Phase 5: Integration Testing

### Step 15: End-to-End Testing
- **Test complete workflow**:
  - File upload functionality
  - CSV parsing and validation
  - Error display in HTML table
  - Successful import to ERPNext
  - Rollback behavior on validation failures

### Step 16: Error Scenario Testing
- **Test various error conditions**:
  - Invalid CSV format
  - Missing mandatory fields
  - Invalid data types
  - Foreign key reference errors
  - ERPNext API communication failures

This streamlined implementation focuses specifically on the core CSV import functionality with three file inputs, all-or-nothing validation, and error display table, without additional features like notifications, background processing, or custom ERPNext apps.