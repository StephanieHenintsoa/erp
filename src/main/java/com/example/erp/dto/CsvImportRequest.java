package com.example.erp.dto;

import org.springframework.web.multipart.MultipartFile;

public class CsvImportRequest {

    private MultipartFile employeeCsv;
    private MultipartFile salaryStructureCsv;
    private MultipartFile salarySlipCsv;

    // Constructors
    public CsvImportRequest() {
    }

    public CsvImportRequest(MultipartFile employeeCsv, MultipartFile salaryStructureCsv, MultipartFile salarySlipCsv) {
        this.employeeCsv = employeeCsv;
        this.salaryStructureCsv = salaryStructureCsv;
        this.salarySlipCsv = salarySlipCsv;
    }

    // Getters and Setters
    public MultipartFile getEmployeeCsv() {
        return employeeCsv;
    }

    public void setEmployeeCsv(MultipartFile employeeCsv) {
        this.employeeCsv = employeeCsv;
    }

    public MultipartFile getSalaryStructureCsv() {
        return salaryStructureCsv;
    }

    public void setSalaryStructureCsv(MultipartFile salaryStructureCsv) {
        this.salaryStructureCsv = salaryStructureCsv;
    }

    public MultipartFile getSalarySlipCsv() {
        return salarySlipCsv;
    }

    public void setSalarySlipCsv(MultipartFile salarySlipCsv) {
        this.salarySlipCsv = salarySlipCsv;
    }
}
