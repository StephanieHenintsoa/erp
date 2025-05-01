package com.example.erp.entity.client;

public class Client {
    private String name;
    private String nom;
    private String date;

    // Constructors
    public Client() {}

    public Client(String name, String nom, String date) {
        this.name = name;
        this.nom = nom;
        this.date = date;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}