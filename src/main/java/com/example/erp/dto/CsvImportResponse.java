package com.example.erp.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.erp.entity.CsvImportError;

public class CsvImportResponse {

    private boolean success;
    private List<CsvImportError> errors;
    private String summaryStatistics;

    // Constructors
    public CsvImportResponse() {
        this.errors = new ArrayList<>();
    }

    public CsvImportResponse(boolean success, List<CsvImportError> errors, String summaryStatistics) {
        this.success = success;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.summaryStatistics = summaryStatistics;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<CsvImportError> getErrors() {
        return errors;
    }

    public void setErrors(List<CsvImportError> errors) {
        this.errors = errors;
    }

    public String getSummaryStatistics() {
        return summaryStatistics;
    }

    public void setSummaryStatistics(String summaryStatistics) {
        this.summaryStatistics = summaryStatistics;
    }

    // Helper method to add an error
    public void addError(CsvImportError error) {
        this.errors.add(error);
    }
}
