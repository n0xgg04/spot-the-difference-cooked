package com.ltm.game.client.controllers;

import com.ltm.game.shared.Message;
import com.ltm.game.shared.Protocol;
import com.ltm.game.client.services.NetworkClient;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.util.Map;
import java.util.function.BiConsumer;

public class LoginController {
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    private NetworkClient networkClient;
    private BiConsumer<String, Integer> onLoginSuccess;

    public void setNetworkClient(NetworkClient client) {
        this.networkClient = client;
    }

    public void setOnLoginSuccess(BiConsumer<String, Integer> callback) {
        this.onLoginSuccess = callback;
    }

    @FXML
    private void initialize() {
        if (passwordField != null) {
            passwordField.setOnAction(e -> handleLogin());
        }
        
        addFocusAnimation(usernameField);
        addFocusAnimation(passwordField);
    }
    
    private void addFocusAnimation(javafx.scene.Node node) {
        node.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
            if (isNowFocused) {
                st.setToX(1.02);
                st.setToY(1.02);
            } else {
                st.setToX(1.0);
                st.setToY(1.0);
            }
            st.play();
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        
        networkClient.send(new Message(Protocol.AUTH_LOGIN, Map.of(
            "username", username,
            "password", password
        )));
    }

    public void handleAuthResult(Map<?, ?> payload) {
        boolean success = Boolean.parseBoolean(String.valueOf(payload.get("success")));
        if (success) {
            Map<?, ?> user = (Map<?, ?>) payload.get("user");
            String username = String.valueOf(user.get("username"));
            Object tp = user.get("totalPoints");
            int totalPoints = 0;
            if (tp != null) {
                if (tp instanceof Number) {
                    totalPoints = ((Number) tp).intValue();
                } else {
                    try {
                        totalPoints = Integer.parseInt(String.valueOf(tp).split("\\.")[0]);
                    } catch (Exception e) {
                        totalPoints = 0;
                    }
                }
            }
            
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(username, totalPoints);
            }
        } else {
            showError(String.valueOf(payload.get("message")));
        }
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}

