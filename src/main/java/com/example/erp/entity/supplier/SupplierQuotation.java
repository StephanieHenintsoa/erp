package com.example.erp.entity.supplier;

public class SupplierQuotation {
    private String name;
    private String supplier;
    private String transactionDate;
    private Double grandTotal;
    private String status;

    public SupplierQuotation(String name, String supplier, String transactionDate, Double grandTotal, String status) {
        this.name = name;
        this.supplier = supplier;
        this.transactionDate = transactionDate;
        this.grandTotal = grandTotal;
        this.status = status;
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
}