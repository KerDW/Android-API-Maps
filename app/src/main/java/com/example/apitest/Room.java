package com.example.apitest;

public class Room {

    private String name;
    private int capacity;
    private int user_count;

    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public Room() {
    }

    public int getUser_count() {
        return user_count;
    }

    public void setUser_count(int user_count) {
        this.user_count = user_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", capacity=" + capacity +
                ", user_count=" + user_count +
                '}';
    }
}
