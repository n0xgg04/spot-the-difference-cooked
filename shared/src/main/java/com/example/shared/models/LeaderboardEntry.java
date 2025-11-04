package com.example.shared.models;

public class LeaderboardEntry {
    public String username;
    public int totalPoints;
    public int totalWins;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String username, int totalPoints, int totalWins) {
        this.username = username;
        this.totalPoints = totalPoints;
        this.totalWins = totalWins;
    }
}
