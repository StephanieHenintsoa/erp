package com.example.erp.entity.supplier;

public class Supplier {
    private String name;
    private String supplierName;
    private String supplierGroup;
    private String country;

    public Supplier(String name, String supplierName, String supplierGroup, String country) {
        this.name = name;
        this.supplierName = supplierName;
        this.supplierGroup = supplierGroup;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierGroup() {
        return supplierGroup;
    }

    public void setSupplierGroup(String supplierGroup) {
        this.supplierGroup = supplierGroup;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}