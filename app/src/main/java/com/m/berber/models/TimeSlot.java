package com.m.berber.models;

import java.io.Serializable;

public class TimeSlot implements Serializable {
    private String id;
    private String barberId;
    private String date; // YYYY-MM-DD
    private String time; // HH:mm
    private boolean available;

    public TimeSlot() {
        // Default constructor
    }

    public TimeSlot(String id, String barberId, String date, String time, boolean available) {
        this.id = id;
        this.barberId = barberId;
        this.date = date;
        this.time = time;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBarberId() {
        return barberId;
    }

    public void setBarberId(String barberId) {
        this.barberId = barberId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}