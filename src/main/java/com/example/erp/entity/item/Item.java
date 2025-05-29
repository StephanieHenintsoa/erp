package com.example.erp.entity.item;

import java.time.LocalDate;

public class Item {
    private String name;
    private int qty;
    private LocalDate scheduleDate;

    // Constructors
    public Item() {}

    public Item(String name) {
        this.name = name;
    }

    public Item(String name, int qty, LocalDate scheduleDate) {
        this.name = name;
        this.qty = qty;
        this.scheduleDate = scheduleDate;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }
}