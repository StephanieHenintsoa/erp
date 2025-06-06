package com.example.erp.dto.salary;

import java.util.List;

import com.example.erp.entity.salary.SalaryComponent;

public class SalaryComponentResponse {

    private List<SalaryComponent> data;

    public List<SalaryComponent> getData() {
        return data;
    }

    public void setData(List<SalaryComponent> data) {
        this.data = data;
    }
}