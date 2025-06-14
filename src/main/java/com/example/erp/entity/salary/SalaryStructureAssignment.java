package com.example.erp.entity.salary;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SalaryStructureAssignment {

    @JsonProperty("base")
    private Double base;

    @JsonProperty("variable")
    private Double variable;

    @JsonProperty("salary_structure")
    private String salaryStructure; 

    // getters and setters
    public Double getBase() {
        return base;
    }

    public void setBase(Double base) {
        this.base = base;
    }

    public Double getVariable() {
        return variable;
    }

    public void setVariable(Double variable) {
        this.variable = variable;
    }
    public String getSalaryStructure() {
        return salaryStructure;
    }
    public void setSalaryStructure(String salaryStructure) {
        this.salaryStructure = salaryStructure;
    }


   
}