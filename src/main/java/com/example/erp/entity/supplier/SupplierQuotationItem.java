package com.example.erp.entity.supplier;

public class SupplierQuotationItem {
    private String itemName;
    private Double qty;
    private Double rate;
    private Double amount;

    public SupplierQuotationItem(String itemName, Double qty, Double rate, Double amount) {
        this.itemName = itemName;
        this.qty = qty;
        this.rate = rate;
        this.amount = amount;
    }

    // Getters and Setters
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}