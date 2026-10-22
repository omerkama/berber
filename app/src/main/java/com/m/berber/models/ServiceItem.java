package com.m.berber.models;

import java.io.Serializable;

public class ServiceItem implements Serializable {
    private String id;
    private String name;
    private double price;
    private int durationMinutes;

    public ServiceItem() {
        // Default constructor for Firebase
    }

    public ServiceItem(String id, String name, double price, int durationMinutes) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.durationMinutes = durationMinutes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}