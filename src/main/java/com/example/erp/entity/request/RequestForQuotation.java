package com.example.erp.entity.request;

import com.example.erp.entity.request.RequestForQuotationItem;
import java.util.List;

public class RequestForQuotation {
    private String name;
    private String transactionDate;
    private String scheduleDate;
    private String status;
    private String company;
    private List<RequestForQuotationItem> items;

    public RequestForQuotation(String name, String transactionDate, String scheduleDate, String status, String company) {
        this.name = name;
        this.transactionDate = transactionDate;
        this.scheduleDate = scheduleDate;
        this.status = status;
        this.company = company;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public List<RequestForQuotationItem> getItems() {
        return items;
    }

    public void setItems(List<RequestForQuotationItem> items) {
        this.items = items;
    }
}