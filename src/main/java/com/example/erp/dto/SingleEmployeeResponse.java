package com.example.erp.dto;

import com.example.erp.entity.Employee;

public class SingleEmployeeResponse {
    private Employee data;

    public Employee getData() {
        return data;
    }

    public void setData(Employee data) {
        this.data = data;
    }
}