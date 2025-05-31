package com.example.erp.dto;

import java.util.List;

import com.example.erp.entity.Employee;

public class EmployeeResponse {
    
    private List<Employee> data;

    public List<Employee> getData() {
        return data;
    }

    public void setData(List<Employee> data) {
        this.data = data;
    }
}
