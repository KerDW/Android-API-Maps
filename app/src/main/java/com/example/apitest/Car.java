package com.example.apitest;

public class Car {

    private int id;

    private String model;

    private String registration_plate;

    private int door_number;

    public int getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getRegistrationPlate() {
        return registration_plate;
    }

    public int getDoor_number() {
        return door_number;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", model='" + model + '\'' +
                ", plate='" + registration_plate + '\'' +
                ", door_number=" + door_number +
                '}';
    }
}
