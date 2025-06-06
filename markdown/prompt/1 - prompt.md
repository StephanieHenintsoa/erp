
i'm creating a Spring Boot App in top of ERP-Next and Frappe-HR App ... and i'm implementing a import functionality from Spring Boot App into the ERP-Next/Frappe HR app ... when correcting the code, dont re-write the whole code, just give me the part to be udpated or just give me the new code part and tell me where to paste it . 

here is the CSV sample for more context : 

CSV Data Samples: 
csv-1: 
Ref,Nom,Prenom,genre,Date embauche,date naissance,company 
1,Rakoto,Alain,Masculin,03/04/2024,01/01/1980,My Company 
2,Rasoa,Jeanne,Feminin,08/06/2024,01/01/1990,My Company 

csv-2: salary structure,name,Abbr,type,valeur,Remarque 
gasy1,Salaire Base,SB,earning,100%, 
gasy1,Indemnité,IND,earning,30%,salaire base 
gasy1,Taxe sociale,TS,deduction,20%,salaire base + indemnité 

csv-3: Mois,Ref Employe,Salaire Base,Salaire 
01/04/2025,1,1500000,gasy1 
01/04/2025,2,900000,gasy1 
01/03/2025,1,1600000,gasy1 
01/03/2025,2,900000,gasy1 


what may cause this error and how to solve it , considering the context :

20:32:45 web.1         | Created employee: EMP-00037 with ID: 1 Bulk Import Debug
20:32:47 web.1         | Created employee: EMP-00038 with ID: 2 Bulk Import Debug
20:32:47 web.1         | Created salary component: Salaire Base Bulk Import Debug
20:32:47 web.1         | Created salary component: Indemnité Bulk Import Debug
20:32:47 web.1         | Created salary component: Taxe sociale Bulk Import Debug
20:32:47 web.1         | Created salary structure: gasy1 Bulk Import Debug
20:32:47 web.1         | Created and submitted salary structure assignment for employee: EMP-00037, structure: gasy1, from_date: 2024-04-03 Bulk Import Debug
20:32:48 web.1         | Created and submitted salary structure assignment for employee: EMP-00038, structure: gasy1, from_date: 2024-06-08 Bulk Import Debug
20:32:48 web.1         | Import failed with error: Please assign a Salary Structure for Employee Alain Rakoto applicable from or before 01-04-2025 first
20:32:48 web.1         | Traceback: Traceback (most recent call last):
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/hrms/hrms/api/csv_import_api.py", line 282, in bulk_import
20:32:48 web.1         |     salary_slip_doc.insert(ignore_permissions=True)
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/frappe/frappe/model/document.py", line 414, in insert
20:32:48 web.1         |     self.run_before_save_methods()
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/frappe/frappe/model/document.py", line 1277, in run_before_save_methods
20:32:48 web.1         |     self.run_method("validate")
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/frappe/frappe/model/document.py", line 1128, in run_method
20:32:48 web.1         |     out = Document.hook(fn)(self, *args, **kwargs)
20:32:48 web.1         |           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/frappe/frappe/model/document.py", line 1517, in composer
20:32:48 web.1         |     return composed(self, method, *args, **kwargs)
20:32:48 web.1         |            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/frappe/frappe/model/document.py", line 1495, in runner
20:32:48 web.1         |     add_to_return_value(self, fn(self, *args, **kwargs))
20:32:48 web.1         |                               ^^^^^^^^^^^^^^^^^^^^^^^^^
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/frappe/frappe/model/document.py", line 1125, in fn
20:32:48 web.1         |     return method_object(*args, **kwargs)
20:32:48 web.1         |            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/hrms/hrms/payroll/doctype/salary_slip/salary_slip.py", line 154, in validate
20:32:48 web.1         |     self.set_salary_structure_assignment()
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/hrms/hrms/payroll/doctype/salary_slip/salary_slip.py", line 808, in set_salary_structure_assignment
20:32:48 web.1         |     frappe.throw(
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/frappe/frappe/utils/messages.py", line 145, in throw
20:32:48 web.1         |     msgprint(
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/frappe/frappe/utils/messages.py", line 106, in msgprint
20:32:48 web.1         |     _raise_exception()
20:32:48 web.1         |   File "/home/ny-haritina/Documents/Studies/ITU/S6/_Evaluation/29-05-2025_ERP_NEXT/erp_next/apps/frappe/frappe/utils/messages.py", line 57, in _raise_exception
20:32:48 web.1         |     raise exc
20:32:48 web.1         | frappe.exceptions.ValidationError: Please assign a Salary Structure for Employee Alain Rakoto applicable from or before 01-04-2025 first
20:32:48 web.1         |  Bulk Import Error



-> ERPNext error: Accounts not set for Salary Component Salaire Base
-> ERPNext error: Accounts not set for Salary Component Indemnité
-> ERPNext error: Accounts not set for Salary Component Taxe sociale
-> ERPNext error: No active or default Salary Structure found for employee EMP-00037 for the given dates
-> ERPNext error: Please assign a Salary Structure for Employee <strong>Alain Rakoto</strong> applicable from or before <strong>01-04-2025</strong> first


--- 

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



--- 



import frappe
from frappe.utils import getdate, flt
from hrms.utils.csv_import_utils import validate_employee_data, validate_salary_structure_data, validate_salary_slip_data, create_company_if_not_exists, create_salary_component

@frappe.whitelist()
def bulk_import(employees, salary_structures, salary_slips):
    """
    Import employees, salary structures, and salary slips from CSV data.
    Args:
        employees (list): List of employee data dictionaries.
        salary_structures (list): List of salary structure data dictionaries.
        salary_slips (list): List of salary slip data dictionaries.
    Returns:
        dict: Status and errors, if any.
    """
    errors = []
    employee_map = {}  # Maps Ref (e.g., "1") to ERPNext name (e.g., "EMP-1")
    structure_map = {}

    # Validate and create companies
    company_names = set(emp.get("company") for emp in employees if emp.get("company"))
    for company in company_names:
        if not frappe.db.exists("Company", company):
            create_company_if_not_exists(company, errors)

    # Validate employee data
    for i, emp in enumerate(employees, start=2):
        emp_errors = validate_employee_data(emp, i)
        errors.extend(emp_errors)
        if not emp_errors:
            # Map employee_id to potential ERPNext name
            employee_id = emp.get("employee_id")
            employee_map[employee_id] = None  # Will be updated after creation

    # Validate salary structure data
    for i, struct in enumerate(salary_structures, start=2):
        struct_errors = validate_salary_structure_data(struct, i)
        errors.extend(struct_errors)
        if not struct_errors:
            structure_map[struct.get("name")] = struct.get("name")

    # Validate salary slip data
    for i, slip in enumerate(salary_slips, start=2):
        slip_errors = validate_salary_slip_data(slip, employee_map, structure_map, i)
        errors.extend(slip_errors)

    if errors:
        return {"status": "error", "errors": errors}

    try:
        # Create employees
        for emp in employees:
            employee_id = emp.get("employee_id")
            # Check if employee already exists by employee_id
            existing_emp = frappe.db.exists("Employee", {"employee": employee_id})
            if not existing_emp:
                employee_doc = frappe.get_doc({
                    "doctype": "Employee",
                    "employee": employee_id,
                    "employee_name": f"{emp.get('first_name')} {emp.get('last_name', '')}".strip(),
                    "first_name": emp.get("first_name"),
                    "last_name": emp.get("last_name", ""),
                    "gender": emp.get("gender"),
                    "date_of_joining": getdate(emp.get("date_of_joining")),
                    "date_of_birth": getdate(emp.get("date_of_birth")),
                    "company": emp.get("company"),
                    "naming_series": "EMP-"
                })
                employee_doc.insert(ignore_permissions=True)
                employee_map[employee_id] = employee_doc.name
                print(f"Created employee: {employee_doc.name} with ID: {employee_id}", "Bulk Import Debug")
            else:
                # Employee exists, update the mapping
                employee_map[employee_id] = existing_emp
                print(f"Employee exists: {existing_emp} with ID: {employee_id}", "Bulk Import Debug")

        # Create salary structures and components
        for struct in salary_structures:
            struct_name = struct.get("name")
            if not frappe.db.exists("Salary Structure", struct_name):
                # Get company from employees
                company_name = employees[0].get("company") if employees else "My Company"
                
                # Create salary components first
                for comp in struct.get("components", []):
                    create_salary_component(comp, errors)
                
                # Separate earnings and deductions
                earnings = []
                deductions = []
                
                # Process your custom components
                for comp in struct.get("components", []):
                    if not comp.get("amount"):
                        continue
                        
                    amount_str = str(comp.get("amount")).strip().replace("%", "")
                    try:
                        percentage = flt(amount_str) / 100 if "%" in str(comp.get("amount")) else flt(amount_str)
                    except:
                        percentage = 0
                        
                    # Use average gross_pay for this salary structure
                    relevant_slips = [slip for slip in salary_slips if slip.get("salary_structure") == struct_name]
                    avg_gross_pay = 0
                    if relevant_slips:
                        total_gross = sum(flt(slip.get("gross_pay", 0)) for slip in relevant_slips)
                        avg_gross_pay = total_gross / len(relevant_slips)
                    
                    amount = percentage * avg_gross_pay if "%" in str(comp.get("amount")) else flt(comp.get("amount", 0))
                    
                    comp_entry = {
                        "salary_component": comp.get("salary_component"),
                        "amount": amount,
                        "abbr": comp.get("abbr", ""),
                        "description": comp.get("description", "")
                    }
                    
                    if comp.get("type", "").lower() == "earning":
                        earnings.append(comp_entry)
                    else:
                        deductions.append(comp_entry)

                # Create the salary structure document
                salary_structure_doc = frappe.get_doc({
                    "doctype": "Salary Structure",
                    "name": struct_name,
                    "is_active": "Yes",
                    "payroll_frequency": "Monthly",
                    "company": company_name,
                    "salary_slip_based_on_timesheet": 0,
                    "earnings": earnings,
                    "deductions": deductions
                })
                
                # Insert without triggering tax component validation
                salary_structure_doc.flags.ignore_validate = True
                salary_structure_doc.insert(ignore_permissions=True)
                
                # Now manually handle the tax component addition to avoid the warning
                try:
                    # Get all tax components from Salary Component master
                    tax_components = frappe.get_all("Salary Component", 
                        filters={"type": "Deduction", "is_tax_applicable": 1},
                        fields=["name", "salary_component_abbr"])
                    
                    # Add tax components with 0 amount to the salary structure
                    for tax_comp in tax_components:
                        # Check if this component is not already in deductions
                        existing = any(d.get("salary_component") == tax_comp.name for d in salary_structure_doc.deductions)
                        if not existing:
                            salary_structure_doc.append("deductions", {
                                "salary_component": tax_comp.name,
                                "amount": 0,
                                "abbr": tax_comp.salary_component_abbr or tax_comp.name[:3].upper()
                            })
                    
                    # Save the updated structure
                    salary_structure_doc.save(ignore_permissions=True)
                    
                except Exception as e:
                    # If tax component handling fails, log but don't fail the import
                    frappe.log_error(f"Tax component handling failed for {struct_name}: {str(e)}", "Bulk Import Tax Warning")
                
                structure_map[struct_name] = struct_name
                print(f"Created salary structure: {struct_name}", "Bulk Import Debug")

        # Create salary structure assignments
        for emp in employees:
            employee_id = emp.get("employee_id")
            if employee_id not in employee_map or not employee_map[employee_id]:
                continue
                
            erp_name = employee_map[employee_id]
            emp_slips = [slip for slip in salary_slips if slip.get("employee") == employee_id]
            
            if emp_slips:
                salary_structure = emp_slips[0].get("salary_structure")
                if not salary_structure:
                    continue
                    
                # Get all posting dates for this employee to find the earliest
                posting_dates = []
                for slip in emp_slips:
                    try:
                        posting_dates.append(getdate(slip.get("posting_date")))
                    except:
                        pass
                        
                if posting_dates:
                    earliest_slip_date = min(posting_dates)
                    joining_date = getdate(emp.get("date_of_joining"))
                    
                    # CRITICAL FIX: Always use joining date as from_date to ensure coverage
                    # This ensures the assignment is active from joining date onwards
                    from_date = joining_date
                    
                    # If the earliest slip is before joining date, adjust from_date accordingly
                    if earliest_slip_date < joining_date:
                        from_date = earliest_slip_date
                    
                    # Check if assignment already exists for this combination
                    existing_assignment = frappe.db.exists("Salary Structure Assignment", {
                        "employee": erp_name,
                        "salary_structure": salary_structure,
                        "from_date": ["<=", earliest_slip_date],
                        "docstatus": 1  # Only submitted assignments
                    })
                    
                    if not existing_assignment:
                        base_amount = flt(emp_slips[0].get("gross_pay", 0))
                        assignment_doc = frappe.get_doc({
                            "doctype": "Salary Structure Assignment",
                            "employee": erp_name,
                            "salary_structure": salary_structure,
                            "company": emp.get("company"),
                            "from_date": from_date,
                            "base": base_amount,
                            "currency": "EUR",
                            "payroll_frequency": "Monthly"
                        })
                        assignment_doc.insert(ignore_permissions=True)
                        assignment_doc.submit()  # Submit the assignment to make it active
                        
                        # CRITICAL: Commit and reload to ensure ERPNext recognizes the assignment
                        frappe.db.commit()
                        frappe.db.sql("SELECT 1")  # Force a database sync
                        
                        print(f"Created and submitted salary structure assignment for employee: {erp_name}, structure: {salary_structure}, from_date: {from_date}", "Bulk Import Debug")

        # Additional commit and wait to ensure all assignments are properly registered
        frappe.db.commit()
        

        # Create salary slips
        for slip in salary_slips:
            emp_ref = slip.get("employee")
            if emp_ref not in employee_map:
                errors.append({
                    "file": "salary_slip.csv",
                    "line_number": 0,
                    "error_description": f"Skipping salary slip due to invalid employee ref: {emp_ref}"
                })
                continue

            erp_name = employee_map[emp_ref]
            posting_date = getdate(slip.get("posting_date"))
            
            # Verify salary structure assignment exists and is effective
            assignment_exists = frappe.db.sql("""
                SELECT name FROM `tabSalary Structure Assignment` 
                WHERE employee = %s 
                AND salary_structure = %s 
                AND from_date <= %s 
                AND docstatus = 1
                ORDER BY from_date DESC
                LIMIT 1
            """, (erp_name, slip.get("salary_structure"), posting_date))

            if not assignment_exists:
                errors.append({
                    "file": "salary_slip.csv",
                    "line_number": 0,
                    "error_description": f"No active salary structure assignment found for employee {erp_name} for date {posting_date}"
                })
                continue
            
            if not frappe.db.exists("Salary Slip", {
                "employee": erp_name,
                "posting_date": posting_date
            }):
                salary_slip_doc = frappe.get_doc({
                    "doctype": "Salary Slip",
                    "employee": erp_name,
                    "salary_structure": slip.get("salary_structure"),
                    "posting_date": posting_date,
                    "gross_pay": flt(slip.get("gross_pay")),
                    "company": employees[0].get("company"),
                    "payroll_frequency": "Monthly",
                    "currency": "EUR"
                })
                salary_slip_doc.insert(ignore_permissions=True)
                print(f"Created salary slip for employee: {erp_name}, date: {slip.get('posting_date')}", "Bulk Import Debug")

        frappe.db.commit()
        return {"status": "success", "message": "Import completed successfully"}

    except Exception as e:
        frappe.db.rollback()
        import traceback
        error_traceback = traceback.format_exc()
        print(f"Import failed with error: {str(e)}\nTraceback: {error_traceback}", "Bulk Import Error")
        errors.append({
            "file": "general",
            "line_number": 0,
            "error_description": f"Import failed: {str(e)}"
        })
        return {"status": "error", "errors": errors}




--- 



import frappe
from frappe.utils import getdate, flt
from frappe import _

def validate_employee_data(emp, line_number):
    """
    Validate employee data against ERPNext Employee DocType.
    Args:
        emp (dict): Employee data from CSV.
        line_number (int): Line number in CSV for error reporting.
    Returns:
        list: List of errors, if any.
    """
    errors = []
    required_fields = ["employee_id", "first_name", "gender", "date_of_joining", "date_of_birth", "company"]

    for field in required_fields:
        if not emp.get(field):
            errors.append({
                "file": "employee.csv",
                "line_number": line_number,
                "error_description": f"Missing required field: {field}"
            })

    if emp.get("date_of_joining"):
        try:
            getdate(emp.get("date_of_joining"))
        except Exception:
            errors.append({
                "file": "employee.csv",
                "line_number": line_number,
                "error_description": "Invalid date_of_joining format"
            })

    if emp.get("date_of_birth"):
        try:
            getdate(emp.get("date_of_birth"))
        except Exception:
            errors.append({
                "file": "employee.csv",
                "line_number": line_number,
                "error_description": "Invalid date_of_birth format"
            })

    if emp.get("gender") and emp.get("gender") not in ["Male", "Female", "Other"]:
        errors.append({
            "file": "employee.csv",
            "line_number": line_number,
            "error_description": f"Invalid gender: {emp.get('gender')}"
        })

    return errors

def validate_salary_structure_data(struct, line_number):
    """
    Validate salary structure data against ERPNext Salary Structure DocType.
    Args:
        struct (dict): Salary structure data from CSV.
        line_number (int): Line number in CSV for error reporting.
    Returns:
        list: List of errors, if any.
    """
    errors = []
    required_fields = ["name", "components"]

    for field in required_fields:
        if not struct.get(field):
            errors.append({
                "file": "salary_structure.csv",
                "line_number": line_number,
                "error_description": f"Missing required field: {field}"
            })

    for comp in struct.get("components", []):
        if not comp.get("salary_component") or not comp.get("type") or not comp.get("amount"):
            errors.append({
                "file": "salary_structure.csv",
                "line_number": line_number,
                "error_description": "Missing component fields: salary_component, type, or amount"
            })

        if comp.get("type") not in ["Earning", "Deduction"]:
            errors.append({
                "file": "salary_structure.csv",
                "line_number": line_number,
                "error_description": f"Invalid component type: {comp.get('type')}"
            })

        if comp.get("amount"):
            try:
                # Handle percentage values (e.g., "100%", "30%")
                amount_str = str(comp.get("amount")).strip("%")
                flt(amount_str)
            except Exception:
                errors.append({
                    "file": "salary_structure.csv",
                    "line_number": line_number,
                    "error_description": f"Invalid amount format: {comp.get('amount')}"
                })

    return errors

def validate_salary_slip_data(slip, employee_map, structure_map, line_number):
    """
    Validate salary slip data against ERPNext Salary Slip DocType.
    Args:
        slip (dict): Salary slip data from CSV.
        employee_map (dict): Mapping of employee refs to ERPNext names.
        structure_map (dict): Mapping of structure names to ERPNext names.
        line_number (int): Line number in CSV for error reporting.
    Returns:
        list: List of errors, if any.
    """
    errors = []
    required_fields = ["posting_date", "employee", "salary_structure", "gross_pay"]
    
    for field in required_fields:
        if not slip.get(field):
            errors.append({
                "file": "salary_slip.csv",
                "line_number": line_number,
                "error_description": f"Missing required field: {field}"
            })

    if slip.get("employee") and slip.get("employee") not in employee_map:
        errors.append({
            "file": "salary_slip.csv",
            "line_number": line_number,
            "error_description": f"Employee ref {slip.get('employee')} not found in employee CSV"
        })

    if slip.get("salary_structure") and slip.get("salary_structure") not in structure_map:
        errors.append({
            "file": "salary_slip.csv",
            "line_number": line_number,
            "error_description": f"Salary structure {slip.get('salary_structure')} not found"
        })

    if slip.get("posting_date"):
        try:
            getdate(slip.get("posting_date"))
        except Exception:
            errors.append({
                "file": "salary_slip.csv",
                "line_number": line_number,
                "error_description": "Invalid posting_date format"
            })

    if slip.get("gross_pay"):
        try:
            flt(slip.get("gross_pay"))
            if flt(slip.get("gross_pay")) <= 0:
                errors.append({
                    "file": "salary_slip.csv",
                    "line_number": line_number,
                    "error_description": "Gross pay must be positive"
                })
        except Exception:
            errors.append({
                "file": "salary_slip.csv",
                "line_number": line_number,
                "error_description": "Invalid gross pay format"
            })

    return errors

def create_company_if_not_exists(company_name, errors):
    """
    Create a company if it doesn't exist.
    Args:
        company_name (str): Name of the company.
        errors (list): List to append errors.
    """
    try:
        if not frappe.db.exists("Company", company_name):
            company_doc = frappe.get_doc({
                "doctype": "Company",
                "company_name": company_name,
                "default_currency": "EUR"
            })
            company_doc.insert(ignore_permissions=True)
            frappe.log_error(f"Created company: {company_name}", "Bulk Import Debug")
    except Exception as e:
        errors.append({
            "file": "general",
            "line_number": 0,
            "error_description": f"Failed to create company {company_name}: {str(e)}"
        })

def create_salary_component(comp, errors):
    """
    Create a salary component if it doesn't exist.
    Args:
        comp (dict): Component data.
        errors (list): List to append errors.
    """
    try:
        component_name = comp.get("salary_component")
        if not frappe.db.exists("Salary Component", component_name):
            comp_doc = frappe.get_doc({
                "doctype": "Salary Component",
                "salary_component": component_name,
                "type": comp.get("type"),
                "description": comp.get("description", ""),
                "salary_component_abbr": comp.get("abbr", component_name[:3].upper())
            })
            comp_doc.insert(ignore_permissions=True)
            print(f"Created salary component: {component_name}", "Bulk Import Debug")
    except Exception as e:
        errors.append({
            "file": "salary_structure.csv",
            "line_number": 0,
            "error_description": f"Failed to create salary component {comp.get('salary_component')}: {str(e)}"
        })

def ensure_holiday_list_exists(company, errors):
    """
    Ensure a holiday list exists for the company for the current year.
    Args:
        company (str): Company name.
        errors (list): List to append errors.
    Returns:
        str: Name of the holiday list.
    """
    try:
        year = frappe.utils.now_datetime().year
        holiday_list_name = f"{company} Holiday List {year}"
        if not frappe.db.exists("Holiday List", holiday_list_name):
            holiday_list_doc = frappe.get_doc({
                "doctype": "Holiday List",
                "holiday_list_name": holiday_list_name,
                "from_date": f"{year}-01-01",
                "to_date": f"{year}-12-31",
                "holidays": []
            })
            holiday_list_doc.insert(ignore_permissions=True)
            frappe.log_error(f"Created holiday list: {holiday_list_name}", "Bulk Import Debug")
        return holiday_list_name
    except Exception as e:
        errors.append({
            "file": "general",
            "line_number": 0,
            "error_description": f"Failed to create holiday list for {company}: {str(e)}"
        })
        return None