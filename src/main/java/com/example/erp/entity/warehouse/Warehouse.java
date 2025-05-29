package com.example.erp.entity.warehouse;

public class Warehouse {
    private String name;

    // Constructors
    public Warehouse() {}

    public Warehouse(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}