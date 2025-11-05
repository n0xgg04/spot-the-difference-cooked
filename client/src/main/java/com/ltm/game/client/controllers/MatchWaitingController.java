package com.ltm.game.client.controllers;

import com.ltm.game.client.services.NetworkClient;
import com.ltm.game.client.services.AudioService;
import com.ltm.game.shared.Message;
import com.ltm.game.shared.Protocol;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.Map;

public class MatchWaitingController {

    @FXML
    private Circle spinnerOuter;

    @FXML
    private Circle spinnerInner;

    @FXML
    private Label countdownLabel;

    @FXML
    private Label opponentLabel;

    @FXML
    private Label statusLabel;

    private NetworkClient networkClient;
    private AudioService audioService;
    private String opponentName;
    private Stage dialogStage;
    private Stage ownerStage;
    private Timeline countdownTimeline;
    private RotateTransition spinnerOuterRotation;
    private RotateTransition spinnerInnerRotation;
    private int remainingSeconds = 11; // Changed to 11 to match sound (10→0 = 11 ticks)
    private boolean isReady = false; // Flag to track if both players are ready

    public void initialize() {
        // Start spinner animations immediately
        startSpinnerAnimations();
        
        // Do NOT start countdown or sound yet - wait for MATCH_READY
        countdownLabel.setText("...");
        statusLabel.setText("Đang chờ người chơi khác chấp nhận...");
    }

    private void startSpinnerAnimations() {
        // Outer circle - rotate clockwise
        spinnerOuterRotation = new RotateTransition(javafx.util.Duration.seconds(4), spinnerOuter);
        spinnerOuterRotation.setByAngle(360);
        spinnerOuterRotation.setCycleCount(Animation.INDEFINITE);
        spinnerOuterRotation.setInterpolator(Interpolator.LINEAR);
        spinnerOuterRotation.play();

        // Inner circle - rotate counter-clockwise
        spinnerInnerRotation = new RotateTransition(javafx.util.Duration.seconds(3), spinnerInner);
        spinnerInnerRotation.setByAngle(-360);
        spinnerInnerRotation.setCycleCount(Animation.INDEFINITE);
        spinnerInnerRotation.setInterpolator(Interpolator.LINEAR);
        spinnerInnerRotation.play();
    }

    private void startCountdown() {
        remainingSeconds = 11; // Start from 11 to count: 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0
        countdownLabel.setText("10"); // Display "10" initially

        // Add pulse animation to countdown
        addPulseAnimation();

        countdownTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            remainingSeconds--;
            
            // Display the countdown (10, 9, 8, ..., 1, 0)
            if (remainingSeconds > 0) {
                countdownLabel.setText(String.valueOf(remainingSeconds - 1));
            } else {
                countdownLabel.setText("0");
            }

            // Add extra pulse on last 3 seconds (3, 2, 1)
            if (remainingSeconds <= 4 && remainingSeconds > 1) {
                addUrgentPulse();
            }

            if (remainingSeconds <= 0) {
                // Countdown finished - close dialog and enter game
                countdownTimeline.stop();
                System.out.println("[MatchWaiting] Countdown finished! Entering game...");
                closeDialog();
            }
        }));
        countdownTimeline.setCycleCount(11); // 11 cycles: 10→9→8→7→6→5→4→3→2→1→0
        countdownTimeline.play();
    }

    private void addPulseAnimation() {
        ScaleTransition pulse = new ScaleTransition(javafx.util.Duration.seconds(1), countdownLabel);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.15);
        pulse.setToY(1.15);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private void addUrgentPulse() {
        // Quick pulse for urgency
        ScaleTransition urgentPulse = new ScaleTransition(javafx.util.Duration.millis(200), countdownLabel);
        urgentPulse.setFromX(1.0);
        urgentPulse.setFromY(1.0);
        urgentPulse.setToX(1.3);
        urgentPulse.setToY(1.3);
        urgentPulse.setCycleCount(2);
        urgentPulse.setAutoReverse(true);
        urgentPulse.play();
    }

    public void setNetworkClient(NetworkClient client) {
        this.networkClient = client;
    }

    public void setAudioService(AudioService audioService) {
        this.audioService = audioService;
        
        // Do NOT play countdown sound yet - wait for MATCH_READY (when both players accept)
    }

    public void setOpponentName(String name) {
        this.opponentName = name;
        if (opponentLabel != null) {
            opponentLabel.setText(name);
        }
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOwnerStage(Stage ownerStage) {
        this.ownerStage = ownerStage;
        
        // Center dialog when first shown
        Platform.runLater(() -> centerDialog());
        
        // Position tracking - follow game window
        if (ownerStage != null) {
            Runnable centerDialog = this::centerDialog;
            
            ownerStage.xProperty().addListener((obs, old, newVal) -> centerDialog.run());
            ownerStage.yProperty().addListener((obs, old, newVal) -> centerDialog.run());
            ownerStage.widthProperty().addListener((obs, old, newVal) -> centerDialog.run());
            ownerStage.heightProperty().addListener((obs, old, newVal) -> centerDialog.run());
        }
    }

    private void centerDialog() {
        if (dialogStage != null && ownerStage != null && !ownerStage.isIconified()) {
            double x = ownerStage.getX() + (ownerStage.getWidth() - dialogStage.getWidth()) / 2;
            double y = ownerStage.getY() + (ownerStage.getHeight() - dialogStage.getHeight()) / 2;
            dialogStage.setX(x);
            dialogStage.setY(y);
        }
    }

    public void onOpponentAccepted() {
        Platform.runLater(() -> {
            statusLabel.setText("Cả hai đã sẵn sàng! Bắt đầu trận đấu...");
        });
    }

    public void onMatchDeclined(String reason) {
        Platform.runLater(() -> {
            cleanup();
            closeDialog();
        });
    }

    public void onMatchReady() {
        Platform.runLater(() -> {
            if (!isReady) {
                isReady = true;
                statusLabel.setText("Cả hai đã sẵn sàng! Bắt đầu sau...");
                
                // NOW start countdown and sound
                System.out.println("[MatchWaiting] Both players ready! Starting countdown...");
                startCountdown();
                
                // Play countdown sound
                if (audioService != null) {
                    System.out.println("[MatchWaiting] Playing countdown sound...");
                    audioService.playCountdownSound();
                }
            }
        });
    }

    public void closeDialog() {
        cleanup();
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void cleanup() {
        // Stop countdown timer
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        // Stop spinner animations
        if (spinnerOuterRotation != null) {
            spinnerOuterRotation.stop();
        }
        if (spinnerInnerRotation != null) {
            spinnerInnerRotation.stop();
        }
        // Stop countdown sound
        if (audioService != null) {
            audioService.stopCountdownSound();
        }
    }
}
