package com.ltm.game.client.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LobbyUserRow {
    private final SimpleStringProperty username = new SimpleStringProperty("");
    private final SimpleStringProperty totalPoints = new SimpleStringProperty("");
    private final SimpleStringProperty status = new SimpleStringProperty("");

    public LobbyUserRow(String username, String totalPoints, String status) {
        this.username.set(username);
        this.totalPoints.set(totalPoints);
        this.status.set(status);
    }

    public String getUsername() { 
        return username.get(); 
    }
    
    public StringProperty usernameProperty() { 
        return username; 
    }

    public String getTotalPoints() { 
        return totalPoints.get(); 
    }
    
    public StringProperty totalPointsProperty() { 
        return totalPoints; 
    }

    public String getStatus() { 
        return status.get(); 
    }
    
    public StringProperty statusProperty() { 
        return status; 
    }
}

