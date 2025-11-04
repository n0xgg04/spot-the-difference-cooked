package com.ltm.game.client.controllers;

import com.ltm.game.shared.Message;
import com.ltm.game.shared.Protocol;
import com.ltm.game.client.models.LobbyUserRow;
import com.ltm.game.client.services.NetworkClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class LobbyController {
    @FXML
    private Label headerUserInfo;
    
    @FXML
    private TableView<LobbyUserRow> lobbyTable;
    
    @FXML
    private TableColumn<LobbyUserRow, String> colUser;
    
    @FXML
    private TableColumn<LobbyUserRow, String> colPoints;
    
    @FXML
    private TableColumn<LobbyUserRow, String> colStatus;
    
    @FXML
    private TableColumn<LobbyUserRow, Void> colAction;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private CheckBox autoRefresh;
    
    @FXML
    private BorderPane rootPane;

    private ObservableList<LobbyUserRow> lobbyData = FXCollections.observableArrayList();
    private FilteredList<LobbyUserRow> filteredLobby;
    
    private NetworkClient networkClient;
    private String username;
    private String myPoints = "0";
    private String myStatus = "Rảnh";
    
    private Consumer<Void> onLogout;
    private Consumer<Void> onShowLeaderboard;

    public void setNetworkClient(NetworkClient client) {
        this.networkClient = client;
    }

    public void setUsername(String username) {
        this.username = username;
        updateHeaderUserInfo();
    }

    public void setTotalPoints(int points) {
        this.myPoints = String.valueOf(points);
        updateHeaderUserInfo();
    }

    public void setOnLogout(Consumer<Void> callback) {
        this.onLogout = callback;
    }

    public void setOnShowLeaderboard(Consumer<Void> callback) {
        this.onShowLeaderboard = callback;
    }

    @FXML
    private void initialize() {
        filteredLobby = new FilteredList<>(lobbyData, r -> true);
        lobbyTable.setItems(filteredLobby);
        
        colUser.setCellValueFactory(c -> c.getValue().usernameProperty());
        colPoints.setCellValueFactory(c -> c.getValue().totalPointsProperty());
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colAction.setCellFactory(makeActionCellFactory());
        
        searchField.textProperty().addListener((obs, old, q) -> {
            String query = q == null ? "" : q.trim().toLowerCase();
            filteredLobby.setPredicate(row ->
                query.isEmpty() || row.getUsername().toLowerCase().contains(query));
        });
        
        try {
            var stream = getClass().getResourceAsStream("/images/avatar_home.png");
            if (stream == null) {
                stream = getClass().getResourceAsStream("/images/avatar_home.jpg");
            }
            
            if (stream != null) {
                Image bgImage = new Image(stream);
                BackgroundImage bg = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, false, true)
                );
                rootPane.setBackground(new Background(bg));
                System.out.println("✓ Background image loaded successfully!");
            } else {
                System.err.println("✗ Could not find background image - using gradient fallback");
                rootPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c3e50, #3498db);");
            }
        } catch (Exception e) {
            System.err.println("✗ Error loading background image: " + e.getMessage());
            rootPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c3e50, #3498db);");
        }
    }

    @FXML
    private void handleLogout() {
        myStatus = "Rảnh";
        updateHeaderUserInfo();
        if (onLogout != null) {
            onLogout.accept(null);
        }
    }

    @FXML
    private void handleRandomInvite() {
        var candidates = filteredLobby.filtered(r -> "idle".equalsIgnoreCase(r.getStatus()));
        if (candidates.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Không có ai rảnh.").show();
            return;
        }
        LobbyUserRow pick = candidates.get(new Random().nextInt(candidates.size()));
        networkClient.send(new Message(Protocol.INVITE_SEND, Map.of("toUser", pick.getUsername())));
    }

    @FXML
    private void handleShowLeaderboard() {
        if (onShowLeaderboard != null) {
            onShowLeaderboard.accept(null);
        }
    }

    @FXML
    private void handleToggleStatus() {
        myStatus = "Rảnh".equals(myStatus) ? "Bận" : "Rảnh";
        updateHeaderUserInfo();
    }

    public void updateLobbyList(List<Map<String, Object>> list) {
        lobbyData.clear();
        for (var u : list) {
            String name = String.valueOf(u.get("username"));
            String pts = String.valueOf(u.get("totalPoints"));
            String st = String.valueOf(u.get("status"));
            if (name != null && name.equals(username)) continue;
            lobbyData.add(new LobbyUserRow(name, pts, st));
        }
    }

    public void showInviteDialog(String fromUser) {
        Stage inviteDialog = new Stage();
        inviteDialog.initModality(Modality.APPLICATION_MODAL);
        inviteDialog.setTitle("Lời mời thi đấu");
        inviteDialog.setResizable(false);
        
        VBox dialogContent = new VBox(20);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(40, 50, 40, 50));
        
        try {
            var bgStream = getClass().getResourceAsStream("/images/anh_moi_choi.jpg");
            if (bgStream != null) {
                String imageUrl = getClass().getResource("/images/anh_moi_choi.jpg").toExternalForm();
                dialogContent.setStyle(
                    "-fx-background-image: url('" + imageUrl + "');" +
                    "-fx-background-size: cover;" +
                    "-fx-background-position: center;" +
                    "-fx-background-repeat: no-repeat;" +
                    "-fx-background-radius: 15px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0.8, 0, 5);"
                );
            } else {
                dialogContent.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                    "-fx-background-radius: 15px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0.8, 0, 5);"
                );
            }
        } catch (Exception e) {
            dialogContent.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                "-fx-background-radius: 15px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0.8, 0, 5);"
            );
        }
        
        Label iconLabel = new Label("⚔️");
        iconLabel.setStyle(
            "-fx-font-size: 48px;" +
            "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.5), 10, 0.5, 0, 0);"
        );
        
        Label headerLabel = new Label("Lời mời thi đấu");
        headerLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0.5, 0, 2);"
        );
        
        Label messageLabel = new Label(fromUser + " mời bạn thi đấu!");
        messageLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-text-fill: white;" +
            "-fx-font-style: italic;" +
            "-fx-padding: 15px;" +
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-background-radius: 10px;"
        );
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button acceptBtn = new Button("✓ Chấp nhận");
        acceptBtn.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: #27ae60;" +
            "-fx-background-radius: 25px;" +
            "-fx-padding: 12px 30px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.5, 0, 2);"
        );
        acceptBtn.setOnAction(e -> {
            networkClient.send(new Message(Protocol.INVITE_RESPONSE, Map.of("fromUser", fromUser, "accepted", true)));
            inviteDialog.close();
        });
        
        Button declineBtn = new Button("✗ Từ chối");
        declineBtn.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: #e74c3c;" +
            "-fx-background-radius: 25px;" +
            "-fx-padding: 12px 30px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.5, 0, 2);"
        );
        declineBtn.setOnAction(e -> {
            networkClient.send(new Message(Protocol.INVITE_RESPONSE, Map.of("fromUser", fromUser, "accepted", false)));
            inviteDialog.close();
        });
        
        buttonBox.getChildren().addAll(acceptBtn, declineBtn);
        dialogContent.getChildren().addAll(iconLabel, headerLabel, messageLabel, buttonBox);
        
        Scene dialogScene = new Scene(dialogContent);
        dialogScene.setFill(Color.TRANSPARENT);
        inviteDialog.setScene(dialogScene);
        inviteDialog.show();
    }

    public void showInviteRejected() {
        Stage notifyDialog = new Stage();
        notifyDialog.initModality(Modality.APPLICATION_MODAL);
        notifyDialog.setTitle("Thông báo");
        notifyDialog.setResizable(false);
        
        VBox notifyContent = new VBox(20);
        notifyContent.setAlignment(Pos.CENTER);
        notifyContent.setPadding(new Insets(30, 40, 30, 40));
        notifyContent.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #f093fb, #f5576c);" +
            "-fx-background-radius: 15px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0.8, 0, 5);"
        );
        
        Label iconLabel = new Label("😔");
        iconLabel.setStyle("-fx-font-size: 40px;");
        
        Label messageLabel = new Label("Lời mời bị từ chối");
        messageLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0.5, 0, 2);"
        );
        
        Button okBtn = new Button("OK");
        okBtn.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: rgba(255,255,255,0.3);" +
            "-fx-background-radius: 20px;" +
            "-fx-padding: 10px 25px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.5, 0, 2);"
        );
        okBtn.setOnAction(e -> notifyDialog.close());
        
        notifyContent.getChildren().addAll(iconLabel, messageLabel, okBtn);
        
        Scene notifyScene = new Scene(notifyContent);
        notifyScene.setFill(Color.TRANSPARENT);
        notifyDialog.setScene(notifyScene);
        notifyDialog.show();
    }

    private void updateHeaderUserInfo() {
        if (headerUserInfo != null) {
            headerUserInfo.setText("Xin chào, " + username + "  •  Tổng điểm: " + myPoints + "  •  " + myStatus);
        }
    }

    private Callback<TableColumn<LobbyUserRow, Void>, TableCell<LobbyUserRow, Void>> makeActionCellFactory() {
        return col -> new TableCell<>() {
            private final Button viewBtn = new Button("Xem");
            private final Button inviteBtn = new Button("Mời");
            private final HBox box = new HBox(6, viewBtn, inviteBtn);
            
            {
                viewBtn.setOnAction(e -> {
                    LobbyUserRow row = getTableView().getItems().get(getIndex());
                    new Alert(Alert.AlertType.INFORMATION,
                        "Người chơi: " + row.getUsername() + "\nĐiểm: " + row.getTotalPoints() + "\nTrạng thái: " + row.getStatus())
                        .show();
                });
                inviteBtn.setOnAction(e -> {
                    LobbyUserRow row = getTableView().getItems().get(getIndex());
                    if (!row.getUsername().equals(username)) {
                        networkClient.send(new Message(Protocol.INVITE_SEND, Map.of("toUser", row.getUsername())));
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
    }
}

