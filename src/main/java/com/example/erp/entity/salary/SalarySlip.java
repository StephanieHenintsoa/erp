package com.example.erp.entity.salary;

import com.example.erp.entity.salary.SalaryDetail;

import java.beans.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.itextpdf.layout.element.List;

public class SalarySlip {

    @JsonProperty("name")
    private String name;

    @JsonProperty("posting_date")
    private String postingDate;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("total_deduction")
    private Double totalDeduction;

    @JsonProperty("total_earnings")
    private Double totalEarnings;

    @JsonProperty("net_pay")
    private Double netPay;

    @JsonProperty("gross_pay")
    private Double grossPay;

    @JsonProperty("employee")
    private String employee;

    // Getters and Setters pour les nouveaux champs
    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(String postingDate) {
        this.postingDate = postingDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalDeduction() {
        return totalDeduction;
    }

    public void setTotalDeduction(Double totalDeduction) {
        this.totalDeduction = totalDeduction;
    }

    public Double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(Double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public Double getNetPay() {
        return netPay;
    }

    public void setNetPay(Double netPay) {
        this.netPay = netPay;
    }

    public Double getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(Double grossPay) {
        this.grossPay = grossPay;
    }
   
}