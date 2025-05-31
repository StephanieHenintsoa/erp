package com.example.erp.dto;

import java.util.List;

import com.example.erp.entity.Department;

public class DepartmentResponse {

    private List<Department> data;

    public List<Department> getData() {
        return data;
    }

    public void setData(List<Department> data) {
        this.data = data;
    }
    
}
