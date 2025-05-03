package com.example.erp.entity.purchase;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrder {

    private String name;
    private String supplier;
    private String transactionDate;
    private String scheduleDate;
    private double grandTotal;
    private String status;
    private boolean isReceived; // New field for Reçu status
    private boolean isPaid;    // New field for Payé status
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public PurchaseOrder(String name, String supplier, String transactionDate, String scheduleDate,
                         double grandTotal, String status, List<PurchaseOrderItem> items) {
        this.name = name;
        this.supplier = supplier;
        this.transactionDate = transactionDate;
        this.scheduleDate = scheduleDate;
        this.grandTotal = grandTotal;
        this.status = status;
        this.items = items;
        // Default values for new fields
        this.isReceived = false;
        this.isPaid = false;
    }

    // Getters and setters
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

    public String getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getIsReceived() {
        return isReceived;
    }

    public void setIsReceived(boolean isReceived) {
        this.isReceived = isReceived;
    }

    public boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    public List<PurchaseOrderItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItem> items) {
        this.items = items;
    }
}