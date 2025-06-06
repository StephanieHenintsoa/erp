package com.example.erp.dto.salary;


import java.util.List;

import com.example.erp.entity.salary.SalarySlip;

public class SalarySlipResponse {

    private List<SalarySlip> data;

    public List<SalarySlip> getData() {
        return data;
    }

    public void setData(List<SalarySlip> data) {
        this.data = data;
    }
}