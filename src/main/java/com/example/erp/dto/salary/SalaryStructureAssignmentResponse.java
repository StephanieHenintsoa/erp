package com.example.erp.dto.salary;

import java.util.List;

import com.example.erp.entity.salary.SalaryStructureAssignment;

public class SalaryStructureAssignmentResponse {

    private List<SalaryStructureAssignment> data;

    public List<SalaryStructureAssignment> getData() {
        return data;
    }

    public void setData(List<SalaryStructureAssignment> data) {
        this.data = data;
    }
}