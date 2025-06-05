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

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.erp.entity.CsvImportError;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@Service
public class CsvValidationService {

    private static final String[] EMPLOYEE_HEADERS = {"Ref", "Nom", "Prenom", "genre", "Date embauche", "date naissance", "company"};
    private static final String[] SALARY_STRUCTURE_HEADERS = {"salary structure", "name", "Abbr", "type", "valeur", "Remarque"};
    private static final String[] SALARY_SLIP_HEADERS = {"Mois", "Ref Employe", "Salaire Base", "Salaire"};
    public static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    // validate a CSV file and return errors
    public List<CsvImportError> validateCsv(MultipartFile file, String fileType) {
        List<CsvImportError> errors = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            // validate headers
            String[] headers = csvReader.readNext();
            if (!validateHeaders(headers, fileType, errors, file.getOriginalFilename())) {
                return errors;
            }

            // validate data rows
            String[] row;
            int lineNumber = 2; // header is line 1
            while ((row = csvReader.readNext()) != null) {
                if (row.length == 0 || row[0].trim().isEmpty()) continue; // skip empty rows
                validateRow(row, fileType, lineNumber, file.getOriginalFilename(), errors);
                lineNumber++;
            }
        } catch (IOException | CsvValidationException e) {
            errors.add(new CsvImportError(file.getOriginalFilename(), 0, "Error reading CSV: " + e.getMessage(), LocalDateTime.now()));
        }
        return errors;
    }

    // validate CSV headers
    private boolean validateHeaders(String[] headers, String fileType, List<CsvImportError> errors, String fileName) {
        String[] expectedHeaders = switch (fileType) {
            case "employee" -> EMPLOYEE_HEADERS;
            case "salary_structure" -> SALARY_STRUCTURE_HEADERS;
            case "salary_slip" -> SALARY_SLIP_HEADERS;
            default -> throw new IllegalArgumentException("Unknown file type: " + fileType);
        };

        if (headers == null || headers.length != expectedHeaders.length) {
            errors.add(new CsvImportError(fileName, 1, "Invalid header structure", LocalDateTime.now()));
            return false;
        }

        for (int i = 0; i < expectedHeaders.length; i++) {
            if (!headers[i].trim().equalsIgnoreCase(expectedHeaders[i])) {
                errors.add(new CsvImportError(fileName, 1, "Expected header '" + expectedHeaders[i] + "' but found '" + headers[i] + "'", LocalDateTime.now()));
                return false;
            }
        }
        return true;
    }

    // validate a single row based on file type
    private void validateRow(String[] row, String fileType, int lineNumber, String fileName, List<CsvImportError> errors) {
        switch (fileType) {
            case "employee" -> validateEmployeeRow(row, lineNumber, fileName, errors);
            case "salary_structure" -> validateSalaryStructureRow(row, lineNumber, fileName, errors);
            case "salary_slip" -> validateSalarySlipRow(row, lineNumber, fileName, errors);
        }
    }

    // validate employee row
    private void validateEmployeeRow(String[] row, int lineNumber, String fileName, List<CsvImportError> errors) {
        if (row.length != EMPLOYEE_HEADERS.length) {
            errors.add(new CsvImportError(fileName, lineNumber, "Invalid number of columns", LocalDateTime.now()));
            return;
        }

        // validate Ref
        if (row[0].trim().isEmpty()) {
            errors.add(new CsvImportError(fileName, lineNumber, "Employee Ref is required", LocalDateTime.now()));
        }

        // validate Nom, Prenom
        if (row[1].trim().isEmpty() || row[2].trim().isEmpty()) {
            errors.add(new CsvImportError(fileName, lineNumber, "Nom and Prenom are required", LocalDateTime.now()));
        }

        // validate gender
        String gender = row[3].trim().toLowerCase();
        if (!gender.equals("masculin") && !gender.equals("feminin")) {
            errors.add(new CsvImportError(fileName, lineNumber, "Invalid gender, expected 'Masculin' or 'Feminin'", LocalDateTime.now()));
        }

        // validate dates
        validateDate(row[4], "Date embauche", lineNumber, fileName, errors);
        validateDate(row[5], "date naissance", lineNumber, fileName, errors);

        // validate company
        if (row[6].trim().isEmpty()) {
            errors.add(new CsvImportError(fileName, lineNumber, "Company is required", LocalDateTime.now()));
        }
    }

    // validate salary structure row
    private void validateSalaryStructureRow(String[] row, int lineNumber, String fileName, List<CsvImportError> errors) {
        if (row.length != SALARY_STRUCTURE_HEADERS.length) {
            errors.add(new CsvImportError(fileName, lineNumber, "Invalid number of columns", LocalDateTime.now()));
            return;
        }

        // validate salary structure
        if (row[0].trim().isEmpty()) {
            errors.add(new CsvImportError(fileName, lineNumber, "Salary structure name is required", LocalDateTime.now()));
        }

        // validate name, abbr
        if (row[1].trim().isEmpty() || row[2].trim().isEmpty()) {
            errors.add(new CsvImportError(fileName, lineNumber, "Component name and abbreviation are required", LocalDateTime.now()));
        }

        // validate type
        String type = row[3].trim().toLowerCase();
        if (!type.equals("earning") && !type.equals("deduction")) {
            errors.add(new CsvImportError(fileName, lineNumber, "Invalid type, expected 'earning' or 'deduction'", LocalDateTime.now()));
        }

        // validate valeur
        String value = row[4].trim();
        if (!value.matches("\\d+%")) {
            errors.add(new CsvImportError(fileName, lineNumber, "Valeur must be a percentage (e.g., '100%')", LocalDateTime.now()));
        }
    }

    // validate salary slip row
    private void validateSalarySlipRow(String[] row, int lineNumber, String fileName, List<CsvImportError> errors) {
        if (row.length != SALARY_SLIP_HEADERS.length) {
            errors.add(new CsvImportError(fileName, lineNumber, "Invalid number of columns", LocalDateTime.now()));
            return;
        }

        // validate Mois
        validateDate(row[0], "Mois", lineNumber, fileName, errors);

        // validate Ref Employe
        if (row[1].trim().isEmpty()) {
            errors.add(new CsvImportError(fileName, lineNumber, "Employee Ref is required", LocalDateTime.now()));
        }

        // validate Salaire Base
        try {
            double salary = Double.parseDouble(row[2].trim());
            if (salary <= 0) {
                errors.add(new CsvImportError(fileName, lineNumber, "Base salary must be positive", LocalDateTime.now()));
            }
        } catch (NumberFormatException e) {
            errors.add(new CsvImportError(fileName, lineNumber, "Invalid base salary format", LocalDateTime.now()));
        }

        // validate Salaire (structure reference)
        if (row[3].trim().isEmpty()) {
            errors.add(new CsvImportError(fileName, lineNumber, "Salary structure reference is required", LocalDateTime.now()));
        }
    }

    // validate date format
    private void validateDate(String dateStr, String fieldName, int lineNumber, String fileName, List<CsvImportError> errors) {
        if (dateStr.trim().isEmpty()) {
            errors.add(new CsvImportError(fileName, lineNumber, fieldName + " is required", LocalDateTime.now()));
            return;
        }

        boolean valid = false;
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate.parse(dateStr, formatter);
                valid = true;
                break;
            } catch (DateTimeParseException ignored) {
            }
        }
        if (!valid) {
            errors.add(new CsvImportError(fileName, lineNumber, "Invalid " + fieldName + " format, expected DD/MM/YYYY or similar", LocalDateTime.now()));
        }
    }

    // parse CSV for further processing (used in import)
    public List<Map<String, String>> parseCsv(MultipartFile file, String fileType) throws IOException, CsvValidationException {
        List<Map<String, String>> records = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String[] headers = csvReader.readNext();
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                if (row.length == 0 || row[0].trim().isEmpty()) continue;
                Map<String, String> record = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    record.put(headers[i], row[i].trim());
                }
                records.add(record);
            }
        }
        return records;
    }
}