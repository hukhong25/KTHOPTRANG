package com.daovancong.techstore.model;

import com.daovancong.techstore.TechOrderService.CustomerTier;

public class Customer {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private CustomerTier tier;
    private int loyaltyPoints;

    public Customer() {}

    public Customer(String id, String name, String phone, String email, String address, CustomerTier tier, int loyaltyPoints) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tier = tier;
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public CustomerTier getTier() { return tier; }
    public void setTier(CustomerTier tier) { this.tier = tier; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public void addPoints(int points) {
        this.loyaltyPoints += points;
    }
}
