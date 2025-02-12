package com.mongodb.modernisationfactory;

public class User {
    private String name;
    private final int id;

    public User(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }
}
