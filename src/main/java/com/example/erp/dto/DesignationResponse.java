package com.example.erp.dto;

import java.util.List;

import com.example.erp.entity.Designation;

public class DesignationResponse {

    private List<Designation> data;

    public List<Designation> getData() {
        return data;
    }

    public void setData(List<Designation> data) {
        this.data = data;
    }
    
}
