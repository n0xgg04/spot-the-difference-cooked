package com.example.shared.models;

public class User {
    public int id;
    public String username;
    public int totalPoints;
    public int totalWins;
    public int totalLosses;
    public int totalDraws;

    public User() {}

    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }
}
