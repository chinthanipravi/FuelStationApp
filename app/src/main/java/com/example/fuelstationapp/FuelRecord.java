package com.example.fuelstationapp;

public class FuelRecord {
    private int id;
    private String fuelType;
    private double liters;
    private double pricePerLiter;
    private double totalAmount;
    private String createdAt;

    public FuelRecord(int id, String fuelType, double liters, double pricePerLiter, double totalAmount, String createdAt) {
        this.id = id;
        this.fuelType = fuelType;
        this.liters = liters;
        this.pricePerLiter = pricePerLiter;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getFuelType() { return fuelType; }
    public double getLiters() { return liters; }
    public double getPricePerLiter() { return pricePerLiter; }
    public double getTotalAmount() { return totalAmount; }
    public String getCreatedAt() { return createdAt; }
}