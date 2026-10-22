package com.m.berber.models;

import java.io.Serializable;

public class User implements Serializable {
    public static final String ROLE_CUSTOMER = "MUSTERI";
    public static final String ROLE_BARBER = "BERBER";

    private String uid;
    private String name;
    private String email;
    private String phone;
    private String role; // MUSTERI or BERBER
    private String shopName;
    private String shopAddress;
    private String workingHours;

    public User() {
        // Default constructor required for Firebase Firestore
    }

    public User(String uid, String name, String email, String phone, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public boolean isBarber() {
        return ROLE_BARBER.equalsIgnoreCase(this.role);
    }
}