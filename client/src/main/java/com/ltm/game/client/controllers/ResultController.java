package com.ltm.game.client.controllers;

import com.ltm.game.client.services.AudioService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Map;
import java.util.function.Consumer;

public class ResultController {
    @FXML
    private Label resultIcon;
    
    @FXML
    private Label resultTitle;
    
    @FXML
    private Label yourNameLabel;
    
    @FXML
    private Label yourScoreLabel;
    
    @FXML
    private Label oppNameLabel;
    
    @FXML
    private Label oppScoreLabel;
    
    @FXML
    private Label reasonLabel;

    private Consumer<Void> onBackToLobby;
    private Consumer<Void> onShowLeaderboard;
    private AudioService audioService;

    public void setAudioService(AudioService service) {
        this.audioService = service;
    }

    public void setOnBackToLobby(Consumer<Void> callback) {
        this.onBackToLobby = callback;
    }

    public void setOnShowLeaderboard(Consumer<Void> callback) {
        this.onShowLeaderboard = callback;
    }

    public void setGameResult(Map<?, ?> payload, String myUsername) {
        if (audioService != null) {
            audioService.playCelebrationSound();
        }

        String reason = String.valueOf(payload.get("reason"));
        Map<?, ?> scores = (Map<?, ?>) payload.get("scores");
        
        int myScore = 0;
        int opponentScore = 0;
        String opponentName = "";
        
        for (var entry : scores.entrySet()) {
            String player = String.valueOf(entry.getKey());
            int score = ((Number) entry.getValue()).intValue();
            if (player.equals(myUsername)) {
                myScore = score;
            } else {
                opponentName = player;
                opponentScore = score;
            }
        }
        
        boolean isWinner = myScore > opponentScore;
        boolean isDraw = myScore == opponentScore;
        
        if (isDraw) {
            resultIcon.setText("🤝");
            resultTitle.setText("HÒA!");
            resultTitle.setStyle(resultTitle.getStyle() + "-fx-text-fill: #f39c12;");
        } else if (isWinner) {
            resultIcon.setText("🏆");
            resultTitle.setText("CHIẾN THẮNG!");
            resultTitle.setStyle(resultTitle.getStyle() + "-fx-text-fill: #2ecc71;");
        } else {
            resultIcon.setText("😢");
            resultTitle.setText("THUA RỒI!");
            resultTitle.setStyle(resultTitle.getStyle() + "-fx-text-fill: #e74c3c;");
        }
        
        yourNameLabel.setText(myUsername);
        yourScoreLabel.setText(String.valueOf(myScore));
        
        String scoreColor = isWinner ? "#2ecc71" : (isDraw ? "#f39c12" : "#e74c3c");
        yourScoreLabel.setStyle(yourScoreLabel.getStyle() + "-fx-text-fill: " + scoreColor + ";");
        
        oppNameLabel.setText(opponentName);
        oppScoreLabel.setText(String.valueOf(opponentScore));
        
        String oppScoreColor = !isWinner ? "#2ecc71" : (isDraw ? "#f39c12" : "#e74c3c");
        oppScoreLabel.setStyle(oppScoreLabel.getStyle() + "-fx-text-fill: " + oppScoreColor + ";");
        
        String reasonText = "";
        if (reason.equals("all-found")) {
            reasonText = "🎯 Tất cả điểm khác biệt đã được tìm thấy!";
        } else if (reason.equals("quit")) {
            reasonText = "👋 Đối thủ đã rời khỏi trận đấu";
        } else {
            reasonText = "✅ Trận đấu kết thúc";
        }
        
        reasonLabel.setText(reasonText);
    }

    @FXML
    private void handleBackToLobby() {
        if (onBackToLobby != null) {
            onBackToLobby.accept(null);
        }
    }

    @FXML
    private void handleViewLeaderboard() {
        if (onShowLeaderboard != null) {
            onShowLeaderboard.accept(null);
        }
    }
}

