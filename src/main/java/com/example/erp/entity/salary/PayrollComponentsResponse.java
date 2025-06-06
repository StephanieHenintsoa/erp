package com.example.erp.entity.salary;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayrollComponentsResponse {
    private String month;
    
    @JsonProperty("month_name")
    private String monthName;
    
    private List<SalaryComponent> earnings;
    private List<SalaryComponent> deductions;
    
    @JsonProperty("total_gross")
    private Double totalGross;
    
    @JsonProperty("total_deduction")
    private Double totalDeduction;
    
    @JsonProperty("total_net")
    private Double totalNet;
    
    private String currency;
    
    @JsonProperty("employee_count")
    private Integer employeeCount;
    
    private String year; // Added to store year for display

    // Getters and Setters
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public List<SalaryComponent> getEarnings() {
        return earnings;
    }

    public void setEarnings(List<SalaryComponent> earnings) {
        this.earnings = earnings;
    }

    public List<SalaryComponent> getDeductions() {
        return deductions;
    }

    public void setDeductions(List<SalaryComponent> deductions) {
        this.deductions = deductions;
    }

    public Double getTotalGross() {
        return totalGross;
    }

    public void setTotalGross(Double totalGross) {
        this.totalGross = totalGross;
    }

    public Double getTotalDeduction() {
        return totalDeduction;
    }

    public void setTotalDeduction(Double totalDeduction) {
        this.totalDeduction = totalDeduction;
    }

    public Double getTotalNet() {
        return totalNet;
    }

    public void setTotalNet(Double totalNet) {
        this.totalNet = totalNet;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public static class SalaryComponent {
        @JsonProperty("salary_component")
        private String salaryComponent;
        
        private Double amount;

        // Getters and Setters
        public String getSalaryComponent() {
            return salaryComponent;
        }

        public void setSalaryComponent(String salaryComponent) {
            this.salaryComponent = salaryComponent;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }
    }
}