package com.example.erp.dto.salary;

import com.example.erp.entity.salary.SalarySlip;

public class SingleSalarySlipResponse {
    private SalarySlip data;

    public SalarySlip getData() {
        return data;
    }

    public void setData(SalarySlip data) {
        this.data = data;
    }
}