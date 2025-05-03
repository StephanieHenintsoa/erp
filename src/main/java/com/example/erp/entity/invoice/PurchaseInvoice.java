package com.example.erp.entity.invoice;

import java.time.LocalDate;

public class PurchaseInvoice {
    private String name;
    private String supplier;
    private LocalDate postingDate;
    private LocalDate dueDate;
    private double grandTotal;
    private double outstandingAmount;
    private String status;

    // Constructors
    public PurchaseInvoice() {}

    public PurchaseInvoice(String name, String supplier, String postingDate, String dueDate, double grandTotal, double outstandingAmount, String status) {
        this.name = name;
        this.supplier = supplier;
        this.postingDate = LocalDate.parse(postingDate);
        this.dueDate = dueDate != null ? LocalDate.parse(dueDate) : null;
        this.grandTotal = grandTotal;
        this.outstandingAmount = outstandingAmount;
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

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(String postingDate) {
        this.postingDate = LocalDate.parse(postingDate);
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate != null ? LocalDate.parse(dueDate) : null;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public double getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(double outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}