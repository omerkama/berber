package com.m.berber.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BarberShop implements Serializable {
    private String barberId;
    private String barberName;
    private String shopName;
    private String address;
    private String phone;
    private String workingHours;
    private List<ServiceItem> services = new ArrayList<>();

    public BarberShop() {
        // Default constructor
    }

    public BarberShop(String barberId, String barberName, String shopName, String address, String phone, String workingHours) {
        this.barberId = barberId;
        this.barberName = barberName;
        this.shopName = shopName;
        this.address = address;
        this.phone = phone;
        this.workingHours = workingHours;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public List<ServiceItem> getServices() {
        return services;
    }

    public void setServices(List<ServiceItem> services) {
        this.services = services;
    }
}