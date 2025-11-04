package com.ltm.game.shared.models;

public class UserStatus {
    private String username;
    private int totalPoints;
    private String status;

    public UserStatus() {}

    public UserStatus(String username, int totalPoints, String status) {
        this.username = username;
        this.totalPoints = totalPoints;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

