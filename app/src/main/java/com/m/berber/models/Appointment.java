package com.m.berber.models;

import java.io.Serializable;

public class Appointment implements Serializable {
    public static final String STATUS_PENDING = "BEKLEMEDE";
    public static final String STATUS_APPROVED = "ONAYLANDI";
    public static final String STATUS_REJECTED = "REDDEDILDI";
    public static final String STATUS_COMPLETED = "TAMAMLANDI";
    public static final String STATUS_CANCELLED = "IPTAL";

    private String id;
    private String barberId;
    private String barberName;
    private String shopName;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String serviceName;
    private double price;
    private String date; // YYYY-MM-DD
    private String time; // HH:mm
    private String status; // BEKLEMEDE, ONAYLANDI, REDDEDILDI, TAMAMLANDI, IPTAL
    private long createdAt;

    public Appointment() {
        // Default constructor
    }

    public Appointment(String id, String barberId, String barberName, String shopName,
                       String customerId, String customerName, String customerPhone,
                       String serviceName, double price, String date, String time, String status, long createdAt) {
        this.id = id;
        this.barberId = barberId;
        this.barberName = barberName;
        this.shopName = shopName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.serviceName = serviceName;
        this.price = price;
        this.date = date;
        this.time = time;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getBarberName() {
        return barberName;
    }

    public void setBarberName(String barberName) {
        this.barberName = barberName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}