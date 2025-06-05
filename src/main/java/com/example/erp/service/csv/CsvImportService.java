package com.example.erp.service.csv;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.erp.dto.CsvImportRequest;
import com.example.erp.dto.CsvImportResponse;
import com.example.erp.entity.CsvImportError;
import com.example.erp.service.api.ErpNextApiService;
import com.example.erp.service.utils.UtilityService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@Service
public class CsvImportService {

    private final CsvValidationService csvValidationService;
    private final ErpNextApiService erpNextApiService;

    @Autowired
    public CsvImportService(CsvValidationService csvValidationService, ErpNextApiService erpNextApiService) {
        this.csvValidationService = csvValidationService;
        this.erpNextApiService = erpNextApiService;
    }

    public CsvImportResponse importCsvFiles(CsvImportRequest request) {
        List<CsvImportError> errors = new ArrayList<>();
        StringBuilder summary = new StringBuilder();

        // Validate all CSV files
        errors.addAll(csvValidationService.validateCsv(request.getEmployeeCsv(), "employee"));
        errors.addAll(csvValidationService.validateCsv(request.getSalaryStructureCsv(), "salary_structure"));
        errors.addAll(csvValidationService.validateCsv(request.getSalarySlipCsv(), "salary_slip"));

        // Check referential integrity and create companies
        try {
            validateReferentialIntegrity(request, errors);
        } catch (IOException | CsvValidationException e) {
            errors.add(new CsvImportError("general", 0, "Error validating referential integrity: " + e.getMessage(), LocalDateTime.now()));
        }

        // If any errors, return early
        if (!errors.isEmpty()) {
            summary.append("Validation failed. Found ").append(errors.size()).append(" errors.");
            return new CsvImportResponse(false, errors, summary.toString());
        }

        // Proceed with import if no errors
        try {
            boolean success = performImport(request, errors);
            if (success) {
                summary.append("Import successful. Processed ")
                        .append(getRecordCount(request.getEmployeeCsv())).append(" employees, ")
                        .append(getRecordCount(request.getSalaryStructureCsv())).append(" salary structures, ")
                        .append(getRecordCount(request.getSalarySlipCsv())).append(" salary slips.");
                return new CsvImportResponse(true, errors, summary.toString());
            } else {
                summary.append("Import failed due to ERPNext API errors.");
                return new CsvImportResponse(false, errors, summary.toString());
            }
        } catch (IOException | CsvValidationException e) {
            errors.add(new CsvImportError("general", 0, "Import error: " + e.getMessage(), LocalDateTime.now()));
            summary.append("Import failed: ").append(e.getMessage());
            return new CsvImportResponse(false, errors, summary.toString());
        }
    }

    // Validate referential integrity and create companies with holiday lists
    private void validateReferentialIntegrity(CsvImportRequest request, List<CsvImportError> errors) throws IOException, CsvValidationException {
        List<Map<String, String>> employees = csvValidationService.parseCsv(request.getEmployeeCsv(), "employee");
        List<Map<String, String>> salaryStructures = csvValidationService.parseCsv(request.getSalaryStructureCsv(), "salary_structure");
        List<Map<String, String>> salarySlips = csvValidationService.parseCsv(request.getSalarySlipCsv(), "salary_slip");

        // Collect employee refs and company names
        List<String> employeeRefs = new ArrayList<>();
        List<String> companyNames = new ArrayList<>();
        for (int i = 0; i < employees.size(); i++) {
            Map<String, String> employee = employees.get(i);
            employeeRefs.add(employee.get("Ref"));
            String company = employee.get("company");
            if (!companyNames.contains(company)) {
                companyNames.add(company);
            }
        }

        // Collect salary structure names
        List<String> structureNames = new ArrayList<>();
        for (Map<String, String> structure : salaryStructures) {
            String name = structure.get("salary structure");
            if (!structureNames.contains(name)) {
                structureNames.add(name);
            }
        }

        // Validate salary slips
        for (int i = 0; i < salarySlips.size(); i++) {
            Map<String, String> slip = salarySlips.get(i);
            String employeeRef = slip.get("Ref Employe");
            String structureName = slip.get("Salaire");

            if (!employeeRefs.contains(employeeRef)) {
                errors.add(new CsvImportError("salary_slip.csv", i + 2, "Employee Ref " + employeeRef + " not found in employee CSV", LocalDateTime.now()));
            }

            if (!structureNames.contains(structureName)) {
                errors.add(new CsvImportError("salary_slip.csv", i + 2, "Salary structure " + structureName + " not found in salary structure CSV", LocalDateTime.now()));
            }
        }

        // Create companies with holiday lists
        for (String company : companyNames) {
            createCompanyWithHolidayList(company, errors);
        }
    }

    private void createCompanyWithHolidayList(String companyName, List<CsvImportError> errors) {
        try {
            // Check if company exists
            if (!erpNextApiService.checkRecordExists("Company", companyName, errors)) {
                // Create holiday list first
                String holidayListName = companyName + " Holiday List " + LocalDate.now().getYear();
                
                Map<String, Object> holidayListData = new HashMap<>();
                holidayListData.put("doctype", "Holiday List");
                holidayListData.put("holiday_list_name", holidayListName);
                holidayListData.put("from_date", LocalDate.now().getYear() + "-01-01");
                holidayListData.put("to_date", LocalDate.now().getYear() + "-12-31");
                holidayListData.put("weekly_off", "Sunday");
                
                List<Map<String, Object>> holidays = new ArrayList<>();
                Map<String, Object> newYear = new HashMap<>();
                newYear.put("description", "New Year's Day");
                newYear.put("holiday_date", LocalDate.now().getYear() + "-01-01");
                holidays.add(newYear);
                
                Map<String, Object> laborDay = new HashMap<>();
                laborDay.put("description", "Labor Day");
                laborDay.put("holiday_date", LocalDate.now().getYear() + "-05-01");
                holidays.add(laborDay);
                
                Map<String, Object> bastilleDay = new HashMap<>();
                bastilleDay.put("description", "Bastille Day");
                bastilleDay.put("holiday_date", LocalDate.now().getYear() + "-07-14");
                holidays.add(bastilleDay);
                
                Map<String, Object> christmas = new HashMap<>();
                christmas.put("description", "Christmas");
                christmas.put("holiday_date", LocalDate.now().getYear() + "-12-25");
                holidays.add(christmas);
                
                holidayListData.put("holidays", holidays);
                
                // Create holiday list and verify
                erpNextApiService.createRecord("Holiday List", holidayListData, errors);
                if (!erpNextApiService.checkRecordExists("Holiday List", holidayListName, errors)) {
                    errors.add(new CsvImportError("general", 0, "Failed to verify holiday list creation: " + holidayListName, LocalDateTime.now()));
                    return;
                }
                
                // Create company with holiday list
                Map<String, Object> companyData = new HashMap<>();
                companyData.put("doctype", "Company");
                companyData.put("company_name", companyName);
                companyData.put("abbr", companyName.replaceAll("[^a-zA-Z0-9]", "").substring(0, Math.min(5, companyName.length())).toUpperCase());
                companyData.put("default_currency", "EUR");
                companyData.put("country", "France");
                companyData.put("create_chart_of_accounts_based_on", "Standard Template");
                companyData.put("chart_of_accounts", "Standard");
                companyData.put("enable_perpetual_inventory", 0);
                companyData.put("default_holiday_list", holidayListName);
                
                erpNextApiService.createRecord("Company", companyData, errors);
            } else {
                // Company exists, ensure it has a holiday list
                updateCompanyHolidayListIfNeeded(companyName, errors);
            }
        } catch (Exception e) {
            errors.add(new CsvImportError("general", 0, "Failed to create company with holiday list: " + e.getMessage(), LocalDateTime.now()));
        }
    }

    private void updateCompanyHolidayListIfNeeded(String companyName, List<CsvImportError> errors) {
        try {
            // Get company details
            Map<String, Object> companyDetails = erpNextApiService.getRecord("Company", companyName, errors);
            
            if (companyDetails != null && (companyDetails.get("default_holiday_list") == null || 
                companyDetails.get("default_holiday_list").toString().isEmpty())) {
                
                String holidayListName = companyName + " Holiday List " + LocalDate.now().getYear();
                
                // Create holiday list if it doesn't exist
                if (!erpNextApiService.checkRecordExists("Holiday List", holidayListName, errors)) {
                    Map<String, Object> holidayListData = new HashMap<>();
                    holidayListData.put("doctype", "Holiday List");
                    holidayListData.put("holiday_list_name", holidayListName);
                    holidayListData.put("from_date", LocalDate.now().getYear() + "-01-01");
                    holidayListData.put("to_date", LocalDate.now().getYear() + "-12-31");
                    holidayListData.put("weekly_off", "Sunday");
                    
                    List<Map<String, Object>> holidays = new ArrayList<>();
                    Map<String, Object> newYear = new HashMap<>();
                    newYear.put("description", "New Year's Day");
                    newYear.put("holiday_date", LocalDate.now().getYear() + "-01-01");
                    holidays.add(newYear);
                    
                    Map<String, Object> laborDay = new HashMap<>();
                    laborDay.put("description", "Labor Day");
                    laborDay.put("holiday_date", LocalDate.now().getYear() + "-05-01");
                    holidays.add(laborDay);
                    
                    Map<String, Object> bastilleDay = new HashMap<>();
                    bastilleDay.put("description", "Bastille Day");
                    bastilleDay.put("holiday_date", LocalDate.now().getYear() + "-07-14");
                    holidays.add(bastilleDay);
                    
                    Map<String, Object> christmas = new HashMap<>();
                    christmas.put("description", "Christmas");
                    christmas.put("holiday_date", LocalDate.now().getYear() + "-12-25");
                    holidays.add(christmas);
                    
                    holidayListData.put("holidays", holidays);
                    erpNextApiService.createRecord("Holiday List", holidayListData, errors);
                }
                
                // Update company with holiday list
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("default_holiday_list", holidayListName);
                erpNextApiService.updateRecord("Company", companyName, updateData, errors);
            }
        } catch (Exception e) {
            errors.add(new CsvImportError("general", 0, "Failed to update company holiday list: " + e.getMessage(), LocalDateTime.now()));
        }
    }

    private boolean performImport(CsvImportRequest request, List<CsvImportError> errors) throws IOException, CsvValidationException {
        // Parse CSVs
        List<Map<String, String>> employees = csvValidationService.parseCsv(request.getEmployeeCsv(), "employee");
        List<Map<String, String>> salaryStructures = csvValidationService.parseCsv(request.getSalaryStructureCsv(), "salary_structure");
        List<Map<String, String>> salarySlips = csvValidationService.parseCsv(request.getSalarySlipCsv(), "salary_slip");

        // Transform data for ERPNext
        List<Map<String, Object>> employeeData = new ArrayList<>();
        for (Map<String, String> emp : employees) {
            Map<String, Object> employee = new HashMap<>();
            employee.put("employee_id", emp.get("Ref")); // Use employee_id instead of name
            employee.put("first_name", emp.get("Prenom"));
            employee.put("last_name", emp.get("Nom"));
            employee.put("gender", emp.get("genre").equalsIgnoreCase("masculin") ? "Male" : "Female");
            employee.put("date_of_joining", convertDate(emp.get("Date embauche")));
            employee.put("date_of_birth", convertDate(emp.get("date naissance")));
            employee.put("company", emp.get("company"));
            employee.put("naming_series", "EMP-");
            employeeData.add(employee);
        }

        Map<String, List<Map<String, Object>>> structureComponents = new HashMap<>();
        List<Map<String, Object>> structureData = new ArrayList<>();
        for (Map<String, String> struct : salaryStructures) {
            String structureName = struct.get("salary structure");
            structureComponents.computeIfAbsent(structureName, k -> new ArrayList<>());

            Map<String, Object> component = new HashMap<>();
            component.put("salary_component", struct.get("name"));
            component.put("abbr", struct.get("Abbr"));
            component.put("type", UtilityService.capitalize(struct.get("type")));
            component.put("amount", convertPercentage(struct.get("valeur")));
            component.put("description", struct.get("Remarque"));
            component.put("company", employees.get(0).get("company")); // Associate with company
            structureComponents.get(structureName).add(component);
        }

        for (String structureName : structureComponents.keySet()) {
            Map<String, Object> struct = new HashMap<>();
            struct.put("name", structureName);
            struct.put("components", structureComponents.get(structureName));
            structureData.add(struct);
        }

        List<Map<String, Object>> slipData = new ArrayList<>();
        for (Map<String, String> slip : salarySlips) {
            Map<String, Object> slipMap = new HashMap<>();
            slipMap.put("posting_date", convertDate(slip.get("Mois")));
            slipMap.put("employee", slip.get("Ref Employe"));
            slipMap.put("salary_structure", slip.get("Salaire"));
            slipMap.put("gross_pay", Double.parseDouble(slip.get("Salaire Base")));
            slipData.add(slipMap);
        }

        // Call bulk import API
        Map<String, Object> response = erpNextApiService.bulkImport(employeeData, structureData, slipData, errors);
        System.out.println("Bulk import response: " + response);

        ObjectMapper mapper = new ObjectMapper();
        if (!response.isEmpty() && "success".equalsIgnoreCase((String) response.get("status"))) {
            return true;
        } else {
            // Handle errors from ERPNext
            if (response.containsKey("success") && !(Boolean) response.get("success")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> apiErrors = (List<Map<String, Object>>) response.get("errors");
                if (apiErrors != null) {
                    for (Map<String, Object> err : apiErrors) {
                        errors.add(new CsvImportError(
                            (String) err.getOrDefault("file", "general"),
                            err.get("line_number") != null ? ((Number) err.get("line_number")).intValue() : 0,
                            (String) err.getOrDefault("error_description", "Unknown error"),
                            LocalDateTime.now()
                        ));
                    }
                }
            } else if (response.containsKey("_server_messages")) {
                try {
                    String serverMessagesJson = (String) response.get("_server_messages");
                    List<String> serverMessages = mapper.readValue(serverMessagesJson, new TypeReference<List<String>>() {});
                    
                    for (String msg : serverMessages) {
                        try {
                            Map<String, String> messageMap = mapper.readValue(msg, new TypeReference<Map<String, String>>() {});
                            errors.add(new CsvImportError(
                                "general",
                                0,
                                "ERPNext error: " + messageMap.getOrDefault("message", "Unknown error"),
                                LocalDateTime.now()
                            ));
                        } catch (Exception msgParseEx) {
                            errors.add(new CsvImportError(
                                "general",
                                0,
                                "ERPNext error (raw): " + msg,
                                LocalDateTime.now()
                            ));
                        }
                    }
                } catch (Exception e) {
                    String rawMessages = response.get("_server_messages").toString();
                    errors.add(new CsvImportError(
                        "general",
                        0,
                        "ERPNext error (unparsed): " + rawMessages,
                        LocalDateTime.now()
                    ));
                }
            } else if (response.containsKey("_error_message")) {
                errors.add(new CsvImportError("general", 0, "ERPNext error: " + response.get("_error_message"), LocalDateTime.now()));
            } else if (response.containsKey("message")) {
                errors.add(new CsvImportError("general", 0, "ERPNext error: " + response.get("message"), LocalDateTime.now()));
            } else {
                errors.add(new CsvImportError("general", 0, "Bulk import failed with unknown error", LocalDateTime.now()));
            }
            return false;
        }
    }

    // Convert date to ERPNext format (YYYY-MM-DD)
    private String convertDate(String dateStr) {
        for (DateTimeFormatter formatter : CsvValidationService.DATE_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(dateStr, formatter);
                return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) {
            }
        }
        return dateStr; // Fallback, will be caught in validation
    }

    // Convert percentage to decimal
    private double convertPercentage(String percentage) {
        return Double.parseDouble(percentage.replace("%", "")) / 100.0;
    }

    // Count non-empty records in a CSV
    private int getRecordCount(MultipartFile file) throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            csvReader.readNext(); // Skip headers
            int count = 0;
            while (csvReader.readNext() != null) {
                count++;
            }
            return count;
        }
    }
}