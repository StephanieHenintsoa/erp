package com.example.erp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Employee {

    @JsonProperty("name")
    private String name;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("company")
    private String company;

    @JsonProperty("date_of_joining")
    private String dateOfJoining;

    @JsonProperty("status")
    private String status;

    @JsonProperty("department")
    private String department;

    @JsonProperty("designation")
    private String designation;

    @JsonProperty("gender")
    private String gender;
    
    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    // getters and setters
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(String dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}