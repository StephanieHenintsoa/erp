package com.example.erp.entity.salary;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SalaryDetail {
    @JsonProperty("salary_component")
    private String salaryComponent;
    
    @JsonProperty("amount")
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