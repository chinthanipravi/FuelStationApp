package com.example.fuelstationapp;

public class FuelStock {
    private int id;
    private String fuelName;
    private double capacityLiters;

    public FuelStock(int id, String fuelName, double capacityLiters) {
        this.id = id;
        this.fuelName = fuelName;
        this.capacityLiters = capacityLiters;
    }

    public int getId() { return id; }
    public String getFuelName() { return fuelName; }
    public double getCapacityLiters() { return capacityLiters; }
    public boolean isLowStock() { return capacityLiters < 1000.0; }
}