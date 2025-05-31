package com.example.erp.controller;

import com.example.erp.entity.CsvError;
import com.example.erp.service.CsvImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/csv")
@CrossOrigin(origins = "*")
public class CsvImportController {

    @Autowired
    private CsvImportService csvImportService;

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCsvFiles(
            @RequestParam("materialRequestFile") MultipartFile materialRequestFile,
            @RequestParam("supplierFile") MultipartFile supplierFile,
            @RequestParam("quotationFile") MultipartFile quotationFile) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file uploads
            if (materialRequestFile.isEmpty() || supplierFile.isEmpty() || quotationFile.isEmpty()) {
                response.put("success", false);
                response.put("message", "All three CSV files are required");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file types
            if (!isValidCsvFile(materialRequestFile) || 
                !isValidCsvFile(supplierFile) || 
                !isValidCsvFile(quotationFile)) {
                response.put("success", false);
                response.put("message", "Only CSV files are allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // Process import with all-or-nothing validation
            List<CsvError> errors = csvImportService.processImport(
                materialRequestFile, supplierFile, quotationFile);
            
            if (errors.isEmpty()) {
                response.put("success", true);
                response.put("message", "All files imported successfully");
                response.put("recordsProcessed", csvImportService.getLastImportCount());
            } else {
                response.put("success", false);
                response.put("message", "Import failed due to validation errors");
                response.put("errors", errors);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Import failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCsvFiles(
            @RequestParam("materialRequestFile") MultipartFile materialRequestFile,
            @RequestParam("supplierFile") MultipartFile supplierFile,
            @RequestParam("quotationFile") MultipartFile quotationFile) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<CsvError> errors = csvImportService.validateOnly(
                materialRequestFile, supplierFile, quotationFile);
            
            response.put("valid", errors.isEmpty());
            response.put("errors", errors);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Validation failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private boolean isValidCsvFile(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        return (contentType != null && contentType.equals("text/csv")) ||
               (filename != null && filename.toLowerCase().endsWith(".csv"));
    }
}



package com.example.erp.service;

import com.example.erp.entity.CsvError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class CsvImportService {

    @Value("${erpnext.base.url}")
    private String erpNextBaseUrl;

    @Value("${erpnext.api.key}")
    private String apiKey;

    @Value("${erpnext.api.secret}")
    private String apiSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private int lastImportCount = 0;

    // Material Request structure
    private static class MaterialRequest {
        String date, itemName, itemGroup, requiredBy, quantity, purpose, targetWarehouse, ref;
    }

    // Supplier structure
    private static class Supplier {
        String supplierName, country, type;
    }

    // Quotation Request structure
    private static class QuotationRequest {
        String refRequestQuotation, supplier;
    }

    public List<CsvError> processImport(MultipartFile materialRequestFile, 
                                       MultipartFile supplierFile, 
                                       MultipartFile quotationFile) {
        
        List<CsvError> errors = validateOnly(materialRequestFile, supplierFile, quotationFile);
        
        if (!errors.isEmpty()) {
            return errors; // Return validation errors without importing
        }

        // If validation passes, proceed with import
        try {
            // Parse all files
            List<Supplier> suppliers = parseSupplierCsv(supplierFile);
            List<MaterialRequest> materialRequests = parseMaterialRequestCsv(materialRequestFile);
            List<QuotationRequest> quotationRequests = parseQuotationRequestCsv(quotationFile);

            // Import to ERPNext
            importSuppliersToErpNext(suppliers);
            importMaterialRequestsToErpNext(materialRequests);
            importQuotationRequestsToErpNext(quotationRequests);

            lastImportCount = suppliers.size() + materialRequests.size() + quotationRequests.size();
            
        } catch (Exception e) {
            errors.add(new CsvError("System", 0, "Import failed: " + e.getMessage()));
        }

        return errors;
    }

    public List<CsvError> validateOnly(MultipartFile materialRequestFile, 
                                      MultipartFile supplierFile, 
                                      MultipartFile quotationFile) {
        
        List<CsvError> errors = new ArrayList<>();
        
        try {
            // Validate each file structure and data
            errors.addAll(validateMaterialRequestCsv(materialRequestFile));
            errors.addAll(validateSupplierCsv(supplierFile));
            errors.addAll(validateQuotationRequestCsv(quotationFile));
            
            // Cross-file validation
            if (errors.isEmpty()) {
                errors.addAll(validateCrossFileReferences(materialRequestFile, supplierFile, quotationFile));
            }
            
        } catch (Exception e) {
            errors.add(new CsvError("System", 0, "Validation failed: " + e.getMessage()));
        }
        
        return errors;
    }

    private List<CsvError> validateMaterialRequestCsv(MultipartFile file) {
        List<CsvError> errors = new ArrayList<>();
        String fileName = "Material Request CSV";
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            String[] expectedHeaders = {"date", "item_name", "item_groupe", "required_by", "quantity", "purpose", "target_warehouse", "ref"};
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] values = line.split(",");
                
                if (lineNumber == 1) {
                    // Validate headers
                    if (values.length != expectedHeaders.length) {
                        errors.add(new CsvError(fileName, lineNumber, "Expected " + expectedHeaders.length + " columns, found " + values.length));
                        break;
                    }
                    continue;
                }
                
                if (values.length != expectedHeaders.length) {
                    errors.add(new CsvError(fileName, lineNumber, "Invalid number of columns"));
                    continue;
                }
                
                // Validate data types and mandatory fields
                if (values[0].trim().isEmpty()) {
                    errors.add(new CsvError(fileName, lineNumber, "Date is mandatory"));
                }
                if (values[1].trim().isEmpty()) {
                    errors.add(new CsvError(fileName, lineNumber, "Item name is mandatory"));
                }
                if (values[4].trim().isEmpty() || !isValidNumber(values[4].trim())) {
                    errors.add(new CsvError(fileName, lineNumber, "Quantity must be a valid number"));
                }
                if (values[7].trim().isEmpty()) {
                    errors.add(new CsvError(fileName, lineNumber, "Reference is mandatory"));
                }
                
                // Validate date format
                if (!isValidDate(values[0].trim())) {
                    errors.add(new CsvError(fileName, lineNumber, "Invalid date format (expected DD/MM/YYYY)"));
                }
                if (!isValidDate(values[3].trim())) {
                    errors.add(new CsvError(fileName, lineNumber, "Invalid required_by date format (expected DD/MM/YYYY)"));
                }
            }
            
        } catch (Exception e) {
            errors.add(new CsvError(fileName, 0, "Error reading file: " + e.getMessage()));
        }
        
        return errors;
    }

    private List<CsvError> validateSupplierCsv(MultipartFile file) {
        List<CsvError> errors = new ArrayList<>();
        String fileName = "Supplier CSV";
        Set<String> supplierNames = new HashSet<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            String[] expectedHeaders = {"supplier_name", "country", "type"};
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] values = line.split(",");
                
                if (lineNumber == 1) {
                    if (values.length != expectedHeaders.length) {
                        errors.add(new CsvError(fileName, lineNumber, "Expected " + expectedHeaders.length + " columns, found " + values.length));
                        break;
                    }
                    continue;
                }
                
                if (values.length != expectedHeaders.length) {
                    errors.add(new CsvError(fileName, lineNumber, "Invalid number of columns"));
                    continue;
                }
                
                // Validate mandatory fields
                if (values[0].trim().isEmpty()) {
                    errors.add(new CsvError(fileName, lineNumber, "Supplier name is mandatory"));
                } else {
                    // Check for duplicates
                    if (supplierNames.contains(values[0].trim())) {
                        errors.add(new CsvError(fileName, lineNumber, "Duplicate supplier name: " + values[0].trim()));
                    }
                    supplierNames.add(values[0].trim());
                }
                
                if (values[1].trim().isEmpty()) {
                    errors.add(new CsvError(fileName, lineNumber, "Country is mandatory"));
                }
                
                if (values[2].trim().isEmpty()) {
                    errors.add(new CsvError(fileName, lineNumber, "Type is mandatory"));
                }
            }
            
        } catch (Exception e) {
            errors.add(new CsvError(fileName, 0, "Error reading file: " + e.getMessage()));
        }
        
        return errors;
    }

    private List<CsvError> validateQuotationRequestCsv(MultipartFile file) {
        List<CsvError> errors = new ArrayList<>();
        String fileName = "Quotation Request CSV";
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            String[] expectedHeaders = {"ref_request_quotation", "supplier"};
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] values = line.split(",");
                
                if (lineNumber == 1) {
                    if (values.length != expectedHeaders.length) {
                        errors.add(new CsvError(fileName, lineNumber, "Expected " + expectedHeaders.length + " columns, found " + values.length));
                        break;
                    }
                    continue;
                }
                
                if (values.length != expectedHeaders.length) {
                    errors.add(new CsvError(fileName, lineNumber, "Invalid number of columns"));
                    continue;
                }
                
                // Validate mandatory fields
                if (values[0].trim().isEmpty()) {
                    errors.add(new CsvError(fileName, lineNumber, "Reference request quotation is mandatory"));
                }
                if (values[1].trim().isEmpty()) {
                    errors.add(new CsvError(fileName, lineNumber, "Supplier is mandatory"));
                }
            }
            
        } catch (Exception e) {
            errors.add(new CsvError(fileName, 0, "Error reading file: " + e.getMessage()));
        }
        
        return errors;
    }

    private List<CsvError> validateCrossFileReferences(MultipartFile materialRequestFile, 
                                                      MultipartFile supplierFile, 
                                                      MultipartFile quotationFile) {
        List<CsvError> errors = new ArrayList<>();
        
        try {
            // Parse data for cross-validation
            List<MaterialRequest> materialRequests = parseMaterialRequestCsv(materialRequestFile);
            List<Supplier> suppliers = parseSupplierCsv(supplierFile);
            List<QuotationRequest> quotationRequests = parseQuotationRequestCsv(quotationFile);
            
            // Create lookup sets
            Set<String> materialRequestRefs = new HashSet<>();
            for (MaterialRequest mr : materialRequests) {
                materialRequestRefs.add(mr.ref);
            }
            
            Set<String> supplierNames = new HashSet<>();
            for (Supplier s : suppliers) {
                supplierNames.add(s.supplierName);
            }
            
            // Validate quotation requests reference valid material requests and suppliers
            int lineNumber = 1; // Skip header
            for (QuotationRequest qr : quotationRequests) {
                lineNumber++;
                
                if (!materialRequestRefs.contains(qr.refRequestQuotation)) {
                    errors.add(new CsvError("Quotation Request CSV", lineNumber, 
                        "Reference '" + qr.refRequestQuotation + "' not found in Material Request CSV"));
                }
                
                if (!supplierNames.contains(qr.supplier)) {
                    errors.add(new CsvError("Quotation Request CSV", lineNumber, 
                        "Supplier '" + qr.supplier + "' not found in Supplier CSV"));
                }
            }
            
        } catch (Exception e) {
            errors.add(new CsvError("Cross-validation", 0, "Error during cross-file validation: " + e.getMessage()));
        }
        
        return errors;
    }

    // Helper methods for parsing
    private List<MaterialRequest> parseMaterialRequestCsv(MultipartFile file) throws Exception {
        List<MaterialRequest> requests = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1) continue; // Skip header
                
                String[] values = line.split(",");
                MaterialRequest request = new MaterialRequest();
                request.date = values[0].trim();
                request.itemName = values[1].trim();
                request.itemGroup = values[2].trim();
                request.requiredBy = values[3].trim();
                request.quantity = values[4].trim();
                request.purpose = values[5].trim();
                request.targetWarehouse = values[6].trim();
                request.ref = values[7].trim();
                
                requests.add(request);
            }
        }
        return requests;
    }

    private List<Supplier> parseSupplierCsv(MultipartFile file) throws Exception {
        List<Supplier> suppliers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1) continue; // Skip header
                
                String[] values = line.split(",");
                Supplier supplier = new Supplier();
                supplier.supplierName = values[0].trim();
                supplier.country = values[1].trim();
                supplier.type = values[2].trim();
                
                suppliers.add(supplier);
            }
        }
        return suppliers;
    }

    private List<QuotationRequest> parseQuotationRequestCsv(MultipartFile file) throws Exception {
        List<QuotationRequest> requests = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1) continue; // Skip header
                
                String[] values = line.split(",");
                QuotationRequest request = new QuotationRequest();
                request.refRequestQuotation = values[0].trim();
                request.supplier = values[1].trim();
                
                requests.add(request);
            }
        }
        return requests;
    }

    // ERPNext import methods
    private void importSuppliersToErpNext(List<Supplier> suppliers) throws Exception {
        for (Supplier supplier : suppliers) {
            Map<String, Object> supplierData = new HashMap<>();
            supplierData.put("supplier_name", supplier.supplierName);
            supplierData.put("country", supplier.country);
            supplierData.put("supplier_type", supplier.type);
            
            callErpNextApi("POST", "/api/resource/Supplier", supplierData);
        }
    }

    private void importMaterialRequestsToErpNext(List<MaterialRequest> requests) throws Exception {
        for (MaterialRequest request : requests) {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("transaction_date", convertDate(request.date));
            requestData.put("schedule_date", convertDate(request.requiredBy));
            requestData.put("material_request_type", request.purpose);
            
            // Add items array
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("item_code", request.itemName);
            item.put("qty", Double.parseDouble(request.quantity));
            item.put("warehouse", request.targetWarehouse);
            items.add(item);
            
            requestData.put("items", items);
            
            callErpNextApi("POST", "/api/resource/Material Request", requestData);
        }
    }

    private void importQuotationRequestsToErpNext(List<QuotationRequest> requests) throws Exception {
        // Group by reference to create RFQ documents
        Map<String, List<String>> rfqGroups = new HashMap<>();
        for (QuotationRequest request : requests) {
            rfqGroups.computeIfAbsent(request.refRequestQuotation, k -> new ArrayList<>())
                    .add(request.supplier);
        }
        
        for (Map.Entry<String, List<String>> entry : rfqGroups.entrySet()) {
            Map<String, Object> rfqData = new HashMap<>();
            rfqData.put("transaction_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            
            // Add suppliers
            List<Map<String, Object>> suppliers = new ArrayList<>();
            for (String supplierName : entry.getValue()) {
                Map<String, Object> supplier = new HashMap<>();
                supplier.put("supplier", supplierName);
                suppliers.add(supplier);
            }
            rfqData.put("suppliers", suppliers);
            
            callErpNextApi("POST", "/api/resource/Request for Quotation", rfqData);
        }
    }

    private void callErpNextApi(String method, String endpoint, Object data) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + apiKey + ":" + apiSecret);
        
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(data), headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            erpNextBaseUrl + endpoint,
            HttpMethod.valueOf(method),
            entity,
            String.class
        );
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("ERPNext API call failed: " + response.getBody());
        }
    }

    // Utility methods
    private boolean isValidDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private String convertDate(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            return outputFormat.format(inputFormat.parse(date));
        } catch (ParseException e) {
            return date; // Return as-is if conversion fails
        }
    }

    private boolean isValidNumber(String number) {
        try {
            Double.parseDouble(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public int getLastImportCount() {
        return lastImportCount;
    }
}


package com.example.erp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CsvError {
    
    @JsonProperty("file")
    private String fileName;
    
    @JsonProperty("lineNumber")
    private int lineNumber;
    
    @JsonProperty("errorDescription")
    private String errorDescription;
    
    public CsvError() {
    }
    
    public CsvError(String fileName, int lineNumber, String errorDescription) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.errorDescription = errorDescription;
    }
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "CsvError{" +
                "fileName='" + fileName + '\'' +
                ", lineNumber=" + lineNumber +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }
}


package com.example.erp.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Set max file size (10MB per file)
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        
        // Set max request size (30MB total for all files)
        factory.setMaxRequestSize(DataSize.ofMegabytes(30));
        
        // Set file size threshold (1KB)
        factory.setFileSizeThreshold(DataSize.ofKilobytes(1));
        
        return factory.createMultipartConfig();
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}


# File: /path/to/erpnext/apps/erpnext/erpnext/custom/csv_import_api.py
# Custom API endpoints for CSV import validation and processing

import frappe
from frappe import _
from frappe.utils import validate_email_address, getdate, nowdate
import json

@frappe.whitelist()
def validate_material_request_data(data):
    """
    Validate material request data before import
    """
    errors = []
    
    if not isinstance(data, list):
        data = [data]
    
    for idx, item in enumerate(data, 1):
        # Check mandatory fields
        if not item.get('item_code'):
            errors.append({
                'line': idx,
                'field': 'item_code',
                'message': _('Item Code is mandatory')
            })
        
        if not item.get('qty'):
            errors.append({
                'line': idx,
                'field': 'qty',
                'message': _('Quantity is mandatory')
            })
        
        # Validate item exists
        if item.get('item_code') and not frappe.db.exists('Item', item.get('item_code')):
            errors.append({
                'line': idx,
                'field': 'item_code',
                'message': _('Item {0} does not exist').format(item.get('item_code'))
            })
        
        # Validate warehouse exists
        if item.get('warehouse') and not frappe.db.exists('Warehouse', item.get('warehouse')):
            errors.append({
                'line': idx,
                'field': 'warehouse',
                'message': _('Warehouse {0} does not exist').format(item.get('warehouse'))
            })
        
        # Validate quantity is positive
        try:
            qty = float(item.get('qty', 0))
            if qty <= 0:
                errors.append({
                    'line': idx,
                    'field': 'qty',
                    'message': _('Quantity must be greater than 0')
                })
        except (ValueError, TypeError):
            errors.append({
                'line': idx,
                'field': 'qty',
                'message': _('Quantity must be a valid number')
            })
        
        # Validate dates
        if item.get('schedule_date'):
            try:
                schedule_date = getdate(item.get('schedule_date'))
                if schedule_date < getdate(nowdate()):
                    errors.append({
                        'line': idx,
                        'field': 'schedule_date',
                        'message': _('Schedule date cannot be in the past')
                    })
            except:
                errors.append({
                    'line': idx,
                    'field': 'schedule_date',
                    'message': _('Invalid date format')
                })
    
    return {'valid': len(errors) == 0, 'errors': errors}

@frappe.whitelist()
def validate_supplier_data(data):
    """
    Validate supplier data before import
    """
    errors = []
    
    if not isinstance(data, list):
        data = [data]
    
    existing_suppliers = set(frappe.db.sql("SELECT name FROM `tabSupplier`", as_list=True))
    
    for idx, supplier in enumerate(data, 1):
        # Check mandatory fields
        if not supplier.get('supplier_name'):
            errors.append({
                'line': idx,
                'field': 'supplier_name',
                'message': _('Supplier Name is mandatory')
            })
        
        # Check for duplicates
        if supplier.get('supplier_name') in existing_suppliers:
            errors.append({
                'line': idx,
                'field': 'supplier_name',
                'message': _('Supplier {0} already exists').format(supplier.get('supplier_name'))
            })
        
        # Validate country exists
        if supplier.get('country') and not frappe.db.exists('Country', supplier.get('country')):
            errors.append({
                'line': idx,
                'field': 'country',
                'message': _('Country {0} does not exist').format(supplier.get('country'))
            })
        
        # Validate supplier type
        valid_supplier_types = ['Company', 'Individual']
        if supplier.get('supplier_type') and supplier.get('supplier_type') not in valid_supplier_types:
            errors.append({
                'line': idx,
                'field': 'supplier_type',
                'message': _('Supplier Type must be one of: {0}').format(', '.join(valid_supplier_types))
            })
    
    return {'valid': len(errors) == 0, 'errors': errors}

@frappe.whitelist()
def validate_rfq_data(data):
    """
    Validate Request for Quotation data before import
    """
    errors = []
    
    if not isinstance(data, list):
        data = [data]
    
    for idx, rfq_item in enumerate(data, 1):
        # Check mandatory fields
        if not rfq_item.get('supplier'):
            errors.append({
                'line': idx,
                'field': 'supplier',
                'message': _('Supplier is mandatory')
            })
        
        # Validate supplier exists
        if rfq_item.get('supplier') and not frappe.db.exists('Supplier', rfq_item.get('supplier')):
            errors.append({
                'line': idx,
                'field': 'supplier',
                'message': _('Supplier {0} does not exist').format(rfq_item.get('supplier'))
            })
    
    return {'valid': len(errors) == 0, 'errors': errors}

@frappe.whitelist()
def bulk_import_csv_data(material_requests=None, suppliers=None, rfq_data=None):
    """
    Bulk import CSV data with transaction support
    """
    try:
        # Start transaction
        frappe.db.begin()
        
        results = {
            'suppliers_created': 0,
            'material_requests_created': 0,
            'rfqs_created': 0,
            'errors': []
        }
        
        # Import suppliers first
        if suppliers:
            for supplier_data in suppliers:
                try:
                    supplier = frappe.new_doc('Supplier')
                    supplier.supplier_name = supplier_data.get('supplier_name')
                    supplier.country = supplier_data.get('country')
                    supplier.supplier_type = supplier_data.get('supplier_type', 'Company')
                    supplier.insert()
                    results['suppliers_created'] += 1
                except Exception as e:
                    results['errors'].append(_('Error creating supplier {0}: {1}').format(
                        supplier_data.get('supplier_name'), str(e)))
        
        # Import material requests
        if material_requests:
            for mr_data in material_requests:
                try:
                    mr = frappe.new_doc('Material Request')
                    mr.transaction_date = mr_data.get('transaction_date', nowdate())
                    mr.schedule_date = mr_data.get('schedule_date')
                    mr.material_request_type = mr_data.get('material_request_type', 'Purchase')
                    
                    # Add items
                    for item_data in mr_data.get('items', []):
                        mr.append('items', {
                            'item_code': item_data.get('item_code'),
                            'qty': item_data.get('qty'),
                            'warehouse': item_data.get('warehouse'),
                            'schedule_date': mr_data.get('schedule_date')
                        })
                    
                    mr.insert()
                    results['material_requests_created'] += 1
                except Exception as e:
                    results['errors'].append(_('Error creating material request: {0}').format(str(e)))
        
        # Import RFQ data
        if rfq_data:
            # Group RFQ data by reference
            rfq_groups = {}
            for rfq_item in rfq_data:
                ref = rfq_item.get('reference', 'default')
                if ref not in rfq_groups:
                    rfq_groups[ref] = []
                rfq_groups[ref].append(rfq_item)
            
            for ref, rfq_items in rfq_groups.items():
                try:
                    rfq = frappe.new_doc('Request for Quotation')
                    rfq.transaction_date = nowdate()
                    
                    # Add suppliers
                    for rfq_item in rfq_items:
                        rfq.append('suppliers', {
                            'supplier': rfq_item.get('supplier')
                        })
                    
                    rfq.insert()
                    results['rfqs_created'] += 1
                except Exception as e:
                    results['errors'].append(_('Error creating RFQ for reference {0}: {1}').format(ref, str(e)))
        
        # Commit transaction if no errors
        if not results['errors']:
            frappe.db.commit()
            return {'success': True, 'results': results}
        else:
            frappe.db.rollback()
            return {'success': False, 'results': results}
            
    except Exception as e:
        frappe.db.rollback()
        return {'success': False, 'error': str(e)}

@frappe.whitelist()
def validate_item_exists(item_codes):
    """
    Check if items exist in the system
    """
    if isinstance(item_codes, str):
        item_codes = [item_codes]
    
    existing_items = frappe.db.sql("""
        SELECT name FROM `tabItem` 
        WHERE name IN ({0})
    """.format(','.join(['%s'] * len(item_codes))), item_codes, as_dict=True)
    
    existing_item_codes = [item.name for item in existing_items]
    missing_items = [code for code in item_codes if code not in existing_item_codes]
    
    return {
        'existing': existing_item_codes,
        'missing': missing_items
    }

@frappe.whitelist()
def validate_warehouse_exists(warehouse_names):
    """
    Check if warehouses exist in the system
    """
    if isinstance(warehouse_names, str):
        warehouse_names = [warehouse_names]
    
    existing_warehouses = frappe.db.sql("""
        SELECT name FROM `tabWarehouse` 
        WHERE name IN ({0})
    """.format(','.join(['%s'] * len(warehouse_names))), warehouse_names, as_dict=True)
    
    existing_warehouse_names = [wh.name for wh in existing_warehouses]
    missing_warehouses = [name for name in warehouse_names if name not in existing_warehouse_names]
    
    return {
        'existing': existing_warehouse_names,
        'missing': missing_warehouses
    }


<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CSV Import - ERPNext Integration</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <style>
        body {
            background: linear-gradient(to bottom right, #e6f0fa, #ffffff, #f3e8ff);
            min-height: 100vh;
        }
        .gradient-text {
            background: linear-gradient(to right, #2563eb, #7c3aed);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .gradient-bar {
            background: linear-gradient(to right, #3b82f6, #8b5cf6);
            height: 4px;
            width: 100px;
            margin: 1rem auto;
            border-radius: 9999px;
        }
        .btn-gradient-yellow {
            background: linear-gradient(to right, #f59e0b, #f97316);
            border: none;
            color: white;
        }
        .btn-gradient-yellow:hover {
            background: linear-gradient(to right, #d97706, #ea580c);
            color: white;
        }
        .btn-gradient-blue {
            background: linear-gradient(to right, #2563eb, #7c3aed);
            border: none;
            color: white;
        }
        .btn-gradient-blue:hover {
            background: linear-gradient(to right, #1d4ed8, #6d28d9);
            color: white;
        }
        .input-file-custom::file-selector-button {
            background-color: #eff6ff;
            color: #2563eb;
            border: none;
            padding: 0.75rem 1rem;
            border-radius: 0.5rem;
            margin-right: 1rem;
            font-weight: 500;
        }
        .input-file-custom:hover::file-selector-button {
            background-color: #dbeafe;
        }
        .input-file-green::file-selector-button {
            background-color: #f0fdf4;
            color: #15803d;
        }
        .input-file-green:hover::file-selector-button {
            background-color: #dcfce7;
        }
        .input-file-purple::file-selector-button {
            background-color: #faf5ff;
            color: #7c3aed;
        }
        .input-file-purple:hover::file-selector-button {
            background-color: #f3e8ff;
        }
        .spinner {
            animation: spin 2s linear infinite;
        }
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="container px-4 py-5" style="max-width: 1200px;">
        <!-- Header -->
        <div class="text-center mb-5">
            <h1 class="display-4 fw-bold gradient-text mb-3">CSV Import to ERPNext</h1>
            <p class="text-muted fs-5">Import Material Requests, Suppliers, and Quotation Data</p>
            <div class="gradient-bar"></div>
        </div>

        <!-- Import Form -->
        <div class="card shadow-sm border-0 p-4 mb-5">
            <h2 class="h4 fw-semibold text-dark mb-4 d-flex align-items-center">
                <svg class="me-2" width="24" height="24" fill="none" stroke="#2563eb" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
                </svg>
                Upload CSV Files
            </h2>
            
            <div class="row row-cols-1 row-cols-md-3 g-4 mb-4">
                <!-- Material Request File -->
                <div class="col">
                    <label class="form-label fw-medium text-dark">
                        Material Request CSV
                        <span class="text-danger">*</span>
                    </label>
                    <input type="file" id="materialRequestFile" accept=".csv" required
                           class="form-control input-file-custom">
                    <div class="form-text">Expected: date, item_name, item_groupe, required_by, quantity, purpose, target_warehouse, ref</div>
                </div>

                <!-- Supplier File -->
                <div class="col">
                    <label class="form-label fw-medium text-dark">
                        Supplier CSV
                        <span class="text-danger">*</span>
                    </label>
                    <input type="file" id="supplierFile" accept=".csv" required
                           class="form-control input-file-green">
                    <div class="form-text">Expected: supplier_name, country, type</div>
                </div>

                <!-- Quotation Request File -->
                <div class="col">
                    <label class="form-label fw-medium text-dark">
                        Quotation Request CSV
                        <span class="text-danger">*</span>
                    </label>
                    <input type="file" id="quotationFile" accept=".csv" required
                           class="form-control input-file-purple">
                    <div class="form-text">Expected: ref_request_quotation, supplier</div>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="d-flex flex-column flex-sm-row gap-3 justify-content-center">
                <button id="validateBtn" 
                        class="btn btn-gradient-yellow fw-medium px-4 py-2 d-flex align-items-center justify-content-center gap-2">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                    <span>Validate Only</span>
                </button>
                
                <button id="importBtn" 
                        class="btn btn-gradient-blue fw-medium px-4 py-2 d-flex align-items-center justify-content-center gap-2">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 12l2 2 4-4"/>
                    </svg>
                    <span>Import Data</span>
                </button>
            </div>
        </div>

        <!-- Loading Indicator -->
        <div id="loadingIndicator" class="d-none">
            <div class="card shadow-sm border-0 p-4 mb-5">
                <div class="d-flex align-items-center justify-content-center gap-3">
                    <div class="spinner">
                        <svg class="w-8 h-8 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                        </svg>
                    </div>
                    <div>
                        <p class="h5 fw-medium text-dark">Processing...</p>
                        <p class="text-muted">Validating and importing your CSV files</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Success Message -->
        <div id="successMessage" class="d-none alert alert-success border-success-subtle p-4 mb-5">
            <div class="d-flex align-items-center gap-3">
                <div class="flex-shrink-0">
                    <svg class="w-6 h-6 text-success" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
                <div>
                    <h3 class="h5 fw-medium text-success">Import Successful!</h3>
                    <p id="successDetails" class="text-success-emphasis mt-1"></p>
                </div>
            </div>
        </div>

        <!-- Error Display Table -->
        <div id="errorSection" class="d-none">
            <div class="card shadow-sm border-danger-subtle">
                <div class="card-header bg-danger-subtle border-bottom border-danger-subtle">
                    <h3 class="h5 fw-semibold text-danger d-flex align-items-center">
                        <svg class="me-2" width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"/>
                        </svg>
                        Validation Errors Found
                    </h3>
                    <p class="text-danger-emphasis fs-6 mt-1">Please fix the following errors before importing:</p>
                </div>
                
                <div class="table-responsive">
                    <table class="table table-hover">
                        <thead class="table-light">
                            <tr>
                                <th scope="col" class="px-4 py-3 text-xs font-medium text-muted text-uppercase">File</th>
                                <th scope="col" class="px-4 py-3 text-xs font-medium text-muted text-uppercase">Line Number</th>
                                <th scope="col" class="px-4 py-3 text-xs font-medium text-muted text-uppercase">Error Description</th>
                            </tr>
                        </thead>
                        <tbody id="errorTableBody" class="divide-y">
                            <!-- Error rows will be inserted here -->
                        </tbody>
                    </table>
                </div>
                
                <div class="card-footer bg-light border-top border-danger-subtle">
                    <p class="text-muted fs-6">
                        <span id="errorCount" class="fw-medium"></span> error(s) found. 
                        All errors must be resolved before data can be imported.
                    </p>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
    <script>
        const API_BASE_URL = 'http://localhost:8080/api/csv';
        
        // DOM elements
        const validateBtn = document.getElementById('validateBtn');
        const importBtn = document.getElementById('importBtn');
        const loadingIndicator = document.getElementById('loadingIndicator');
        const successMessage = document.getElementById('successMessage');
        const errorSection = document.getElementById('errorSection');
        const errorTableBody = document.getElementById('errorTableBody');
        const errorCount = document.getElementById('errorCount');
        const successDetails = document.getElementById('successDetails');

        // File inputs
        const materialRequestFile = document.getElementById('materialRequestFile');
        const supplierFile = document.getElementById('supplierFile');
        const quotationFile = document.getElementById('quotationFile');

        // Event listeners
        validateBtn.addEventListener('click', handleValidation);
        importBtn.addEventListener('click', handleImport);

        function areFilesSelected() {
            return materialRequestFile.files.length > 0 && 
                   supplierFile.files.length > 0 && 
                   quotationFile.files.length > 0;
        }

        function showLoading() {
            loadingIndicator.classList.remove('d-none');
            successMessage.classList.add('d-none');
            errorSection.classList.add('d-none');
            validateBtn.disabled = true;
            importBtn.disabled = true;
        }

        function hideLoading() {
            loadingIndicator.classList.add('d-none');
            validateBtn.disabled = false;
            importBtn.disabled = false;
        }

        function displayErrors(errors) {
            if (errors && errors.length > 0) {
                errorTableBody.innerHTML = '';
                
                errors.forEach(error => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td class="px-4 py-3">
                            <div class="d-flex align-items-center">
                                <div class="rounded-circle bg-danger me-3" style="width: 12px; height: 12px;"></div>
                                <span class="text-sm font-medium">${error.file || 'Unknown'}</span>
                            </div>
                        </td>
                        <td class="px-4 py-3">
                            <span class="badge bg-danger-subtle text-danger">
                                Line ${error.lineNumber || 'N/A'}
                            </span>
                        </td>
                        <td class="px-4 py-3">
                            <div class="text-sm">${error.errorDescription || 'Unknown error'}</div>
                        </td>
                    `;
                    errorTableBody.appendChild(row);
                });
                
                errorCount.textContent = errors.length;
                errorSection.classList.remove('d-none');
            } else {
                errorSection.classList.add('d-none');
            }
        }

        function displaySuccess(message, details = '') {
            successMessage.classList.remove('d-none');
            successDetails.textContent = details;
        }

        async function handleValidation() {
            if (!areFilesSelected()) {
                alert('Please select all three CSV files before validating.');
                return;
            }

            showLoading();

            try {
                const formData = new FormData();
                formData.append('materialRequestFile', materialRequestFile.files[0]);
                formData.append('supplierFile', supplierFile.files[0]);
                formData.append('quotationFile', quotationFile.files[0]);

                const response = await fetch(`${API_BASE_URL}/validate`, {
                    method: 'POST',
                    body: formData
                });

                const result = await response.json();

                if (result.valid) {
                    displaySuccess('Validation passed! All files are ready for import.');
                    displayErrors([]);
                } else {
                    displayErrors(result.errors || []);
                }

            } catch (error) {
                console.error('Validation error:', error);
                displayErrors([{
                    file: 'System',
                    lineNumber: 0,
                    errorDescription: 'Network error occurred during validation: ' + error.message
                }]);
            } finally {
                hideLoading();
            }
        }

        async function handleImport() {
            if (!areFilesSelected()) {
                alert('Please select all three CSV files before importing.');
                return;
            }

            if (!confirm('Are you sure you want to import these CSV files? This action cannot be undone.')) {
                return;
            }

            showLoading();

            try {
                const formData = new FormData();
                formData.append('materialRequestFile', materialRequestFile.files[0]);
                formData.append('supplierFile', supplierFile.files[0]);
                formData.append('quotationFile', quotationFile.files[0]);

                const response = await fetch(`${API_BASE_URL}/import`, {
                    method: 'POST',
                    body: formData
                });

                const result = await response.json();

                if (result.success) {
                    displaySuccess(
                        result.message, 
                        `Successfully processed ${result.recordsProcessed || 0} records.`
                    );
                    displayErrors([]);
                    
                    // Clear file inputs
                    materialRequestFile.value = '';
                    supplierFile.value = '';
                    quotationFile.value = '';
                } else {
                    displayErrors(result.errors || []);
                }

            } catch (error) {
                console.error('Import error:', error);
                displayErrors([{
                    file: 'System',
                    lineNumber: 0,
                    errorDescription: 'Network error occurred during import: ' + error.message
                }]);
            } finally {
                hideLoading();
            }
        }

        // File input change handlers for visual feedback
        [materialRequestFile, supplierFile, quotationFile].forEach(input => {
            input.addEventListener('change', function() {
                if (this.files.length > 0) {
                    this.classList.add('border-success-subtle', 'bg-success-subtle');
                    this.classList.remove('border-light-subtle');
                } else {
                    this.classList.remove('border-success-subtle', 'bg-success-subtle');
                    this.classList.add('border-light-subtle');
                }
            });
        });
    </script>
</body>
</html>




# Spring Boot Application Configuration
server.port=8080

# ERPNext Configuration
erpnext.base.url=https://your-erpnext-instance.com
erpnext.api.key=your_api_key_here
erpnext.api.secret=your_api_secret_here

# Multipart File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=30MB
spring.servlet.multipart.file-size-threshold=1KB

# Database Configuration (if using local database for error logging)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true

# Logging Configuration
logging.level.com.example.erp=DEBUG
logging.level.org.springframework.web=INFO

# CORS Configuration
cors.allowed-origins=http://localhost:3000,http://localhost:8080
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*


# File: /path/to/erpnext/apps/erpnext/erpnext/hooks.py
# Add these entries to your existing hooks.py file

# Custom API endpoints for CSV import
api_endpoints = [
    "erpnext.custom.csv_import_api.validate_material_request_data",
    "erpnext.custom.csv_import_api.validate_supplier_data", 
    "erpnext.custom.csv_import_api.validate_rfq_data",
    "erpnext.custom.csv_import_api.bulk_import_csv_data",
    "erpnext.custom.csv_import_api.validate_item_exists",
    "erpnext.custom.csv_import_api.validate_warehouse_exists"
]

# Custom DocType validations for CSV import
doc_events = {
    "Material Request": {
        "before_insert": "erpnext.custom.csv_import_validations.validate_material_request_before_insert",
        "on_submit": "erpnext.custom.csv_import_validations.on_material_request_submit"
    },
    "Supplier": {
        "before_insert": "erpnext.custom.csv_import_validations.validate_supplier_before_insert"
    },
    "Request for Quotation": {
        "before_insert": "erpnext.custom.csv_import_validations.validate_rfq_before_insert"
    }
}

# Custom permissions for CSV import
# Add this to allow API access for CSV import operations
override_whitelisted_methods = {
    "frappe.desk.form.save.savedocs": "erpnext.custom.csv_import_api.custom_save_docs"
}


# File: /path/to/erpnext/apps/erpnext/erpnext/custom/csv_import_validations.py
# Custom validation functions for CSV import

import frappe
from frappe import _
from frappe.utils import validate_email_address, getdate, nowdate, flt

def validate_material_request_before_insert(doc, method):
    """
    Custom validation for Material Request during CSV import
    """
    #