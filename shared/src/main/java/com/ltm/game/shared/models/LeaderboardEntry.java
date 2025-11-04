package com.ltm.game.shared.models;

public class LeaderboardEntry {
    private String username;
    private int totalPoints;
    private int totalWins;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String username, int totalPoints, int totalWins) {
        this.username = username;
        this.totalPoints = totalPoints;
        this.totalWins = totalWins;
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

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }
}

