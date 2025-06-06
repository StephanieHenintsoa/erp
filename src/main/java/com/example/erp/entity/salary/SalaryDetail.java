package com.example.erp.entity.salary;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SalaryDetail {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("salary_component")
    private String salaryComponent;
    
    @JsonProperty("abbr")
    private String abbr;
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public String getSalaryComponent() {
        return salaryComponent;
    }
    
    public void setSalaryComponent(String salaryComponent) {
        this.salaryComponent = salaryComponent;
    }
    
    public String getAbbr() {
        return abbr;
    }
    
    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }
}