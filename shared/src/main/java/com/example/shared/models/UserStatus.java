package com.example.shared.models;

public class UserStatus {
    public String username;
    public int totalPoints;
    public String status; // "busy" | "idle"

    public UserStatus() {}

    public UserStatus(String username, int totalPoints, String status) {
        this.username = username;
        this.totalPoints = totalPoints;
        this.status = status;
    }
}
