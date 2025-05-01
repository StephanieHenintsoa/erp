package com.example.erp.entity.supplier;

import java.util.List;

public class SupplierQuotation {
    private String name;
    private String supplier;
    private String transactionDate;
    private Double grandTotal;
    private String status;
    private List<SupplierQuotationItem> items;

    public SupplierQuotation(String name, String supplier, String transactionDate, Double grandTotal, String status, List<SupplierQuotationItem> items) {
        this.name = name;
        this.supplier = supplier;
        this.transactionDate = transactionDate;
        this.grandTotal = grandTotal;
        this.status = status;
        this.items = items;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<SupplierQuotationItem> getItems() {
        return items;
    }

    public void setItems(List<SupplierQuotationItem> items) {
        this.items = items;
    }
}