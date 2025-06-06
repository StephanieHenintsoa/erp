package com.example.erp.entity.salary;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SalaryComponent {

    @JsonProperty("name")
    private String name;

    @JsonProperty("salary_component_abbr")
    private String salaryComponentAbbr;

    @JsonProperty("amount")
    private Double amount;

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSalaryComponentAbbr() {
        return salaryComponentAbbr;
    }

    public void setSalaryComponentAbbr(String salaryComponentAbbr) {
        this.salaryComponentAbbr = salaryComponentAbbr;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}