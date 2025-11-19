package com.ltm.game.client.controllers;

import com.ltm.game.client.services.NetworkClient;
import com.ltm.game.shared.Message;
import com.ltm.game.shared.Protocol;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.Map;

public class MatchFoundController {

    @FXML
    private Circle spinnerOuter;

    @FXML
    private Circle spinnerInner;

    @FXML
    private Button acceptButton;

    @FXML
    private Button declineButton;

    @FXML
    private Label countdownLabel;

    @FXML
    private Label waitingLabel;

    private NetworkClient networkClient;
    private String opponentName;
    private String username;
    private Stage dialogStage;
    private Stage ownerStage;
    private boolean accepted = false;
    private boolean declined = false;
    private Timeline countdownTimeline;
    private RotateTransition spinnerOuterRotation;
    private RotateTransition spinnerInnerRotation;
    private int remainingSeconds = 10;
    private Runnable onAcceptCallback;

    public void initialize() {
        startCountdown();
        
        startSpinnerAnimations();
    }

    private void startSpinnerAnimations() {
        spinnerOuterRotation = new RotateTransition(javafx.util.Duration.seconds(3), spinnerOuter);
        spinnerOuterRotation.setByAngle(360);
        spinnerOuterRotation.setCycleCount(Animation.INDEFINITE);
        spinnerOuterRotation.setInterpolator(Interpolator.LINEAR);
        spinnerOuterRotation.play();

        spinnerInnerRotation = new RotateTransition(javafx.util.Duration.seconds(2), spinnerInner);
        spinnerInnerRotation.setByAngle(-360);
        spinnerInnerRotation.setCycleCount(Animation.INDEFINITE);
        spinnerInnerRotation.setInterpolator(Interpolator.LINEAR);
        spinnerInnerRotation.play();
    }

    private void startCountdown() {
        remainingSeconds = 10;
        countdownLabel.setText(String.valueOf(remainingSeconds));

        countdownTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            remainingSeconds--;
            countdownLabel.setText(String.valueOf(remainingSeconds));

            if (remainingSeconds <= 0) {
                if (!accepted && !declined) {
                    handleDecline();
                }
            }
        }));
        countdownTimeline.setCycleCount(10);
        countdownTimeline.play();
    }

    public void setNetworkClient(NetworkClient client) {
        this.networkClient = client;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setOpponentName(String name) {
        this.opponentName = name;
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;

        stage.setOnCloseRequest(e -> {
            if (!accepted && !declined) {
                sendDeclineResponse();
            }
            cleanup();
        });
    }

    public void setOwnerStage(Stage ownerStage) {
        this.ownerStage = ownerStage;
        
        Platform.runLater(() -> centerDialog());
        
        if (ownerStage != null) {
            Runnable centerDialog = this::centerDialog;
            
            ownerStage.xProperty().addListener((obs, old, newVal) -> centerDialog.run());
            ownerStage.yProperty().addListener((obs, old, newVal) -> centerDialog.run());
            ownerStage.widthProperty().addListener((obs, old, newVal) -> centerDialog.run());
            ownerStage.heightProperty().addListener((obs, old, newVal) -> centerDialog.run());
            
            ownerStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (dialogStage != null && !dialogStage.isIconified()) {
                    if (isNowFocused) {
                        dialogStage.toFront();
                    } else {
                        dialogStage.toBack();
                    }
                }
            });
        }
    }

    public void setOnAcceptCallback(Runnable callback) {
        this.onAcceptCallback = callback;
    }

    private void centerDialog() {
        if (dialogStage != null && ownerStage != null && !ownerStage.isIconified()) {
            double x = ownerStage.getX() + (ownerStage.getWidth() - dialogStage.getWidth()) / 2;
            double y = ownerStage.getY() + (ownerStage.getHeight() - dialogStage.getHeight()) / 2;
            dialogStage.setX(x);
            dialogStage.setY(y);
        }
    }

    @FXML
    private void handleAccept() {
        if (accepted || declined) {
            return; // Prevent double-click
        }

        accepted = true;
        acceptButton.setDisable(true);
        declineButton.setDisable(true);

        networkClient.send(new Message(Protocol.MATCH_ACCEPT, Map.of()));

        cleanup();
        closeDialog();
        
        if (onAcceptCallback != null) {
            onAcceptCallback.run();
        }
    }

    @FXML
    private void handleDecline() {
        if (accepted || declined) {
            return;
        }

        declined = true;
        sendDeclineResponse();
        closeDialog();
    }

    private void sendDeclineResponse() {
        if (networkClient != null) {
            networkClient.send(new Message(Protocol.MATCH_DECLINE, Map.of()));
        }
    }

    public void closeDialog() {
        cleanup();
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void cleanup() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        if (spinnerOuterRotation != null) {
            spinnerOuterRotation.stop();
        }
        if (spinnerInnerRotation != null) {
            spinnerInnerRotation.stop();
        }
    }

    public void onOpponentDeclined(String reason, String decliner) {
        Platform.runLater(() -> {
            cleanup();
            closeDialog();
        });
    }

    public void onWaitingForOpponent() {
        Platform.runLater(() -> {
            waitingLabel.setText("Đang chuẩn bị trận đấu...");
        });
    }

    public void onMatchStarting() {
        Platform.runLater(() -> {
            cleanup();
            closeDialog();
        });
    }
}

