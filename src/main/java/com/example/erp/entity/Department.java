package com.example.erp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Department {
    
    @JsonProperty("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}