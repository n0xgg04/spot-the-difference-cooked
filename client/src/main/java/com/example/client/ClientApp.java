package com.example.client;

import com.example.shared.Message;
import com.example.shared.Protocol;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class ClientApp extends Application {
    private NetworkClient net;
    private Stage stage;
    private String username;
    private String myPoints = "0";
    private String myStatus = "Rảnh";
    // Background music
    private AudioClip backgroundMusic;
    // Lobby UI
    private TableView<LobbyUserRow> lobbyTable;
    private ObservableList<LobbyUserRow> lobbyData = FXCollections.observableArrayList();
    private FilteredList<LobbyUserRow> filteredLobby = new FilteredList<>(lobbyData, r -> true);
    private TextField searchField;
    private CheckBox autoRefresh;
    private Label headerUserInfo;
    private String currentRoomId;
    private GameView gameView;
    // Cache when lobby list arrives before UI ready
    private java.util.List<Map<String,Object>> pendingLobby;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.net = new NetworkClient(this::onMessage);
        net.connect();
        stage.setTitle("Spot The Difference");
        
        // Set kích thước full màn hình
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        
        showLogin();
        stage.show();
    }

    private void showLogin() {
        // Title
        Label title = new Label("🎮 SPOT THE DIFFERENCE");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label subtitle = new Label("TÌM ĐIỂM KHÁC BIỆT");
        subtitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.5, 2, 2);");
        
        // Input fields
        TextField userField = new TextField();
        userField.setPromptText("Tên đăng nhập");
        userField.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5px; -fx-border-color: #3498db; -fx-border-radius: 5px; -fx-border-width: 2px;");
        userField.setMaxWidth(300);
        
        PasswordField passField = new PasswordField();
        passField.setPromptText("Mật khẩu");
        passField.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5px; -fx-border-color: #3498db; -fx-border-radius: 5px; -fx-border-width: 2px;");
        passField.setMaxWidth(300);
        
        // Login button
        Button loginBtn = new Button("ĐĂNG NHẬP");
        loginBtn.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-padding: 12px 40px; " +
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5px; " +
            "-fx-cursor: hand;"
        );
        loginBtn.setMaxWidth(300);
        
        // Hover effect
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-padding: 12px 40px; " +
            "-fx-background-color: #2980b9; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5px; " +
            "-fx-cursor: hand;"
        ));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-padding: 12px 40px; " +
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5px; " +
            "-fx-cursor: hand;"
        ));
        
        // Status label
        Label status = new Label();
        status.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");
        
        // Login action
        loginBtn.setOnAction(e -> {
            net.send(new Message(Protocol.AUTH_LOGIN, Map.of(
                    "username", userField.getText(),
                    "password", passField.getText()
            )));
        });
        
        // Allow Enter key to login
        passField.setOnAction(e -> loginBtn.fire());
        
        // Layout
        VBox box = new VBox(20);
        box.getChildren().addAll(title, subtitle, userField, passField, loginBtn, status);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: linear-gradient(to bottom, #ecf0f1, #bdc3c7);");
        
        stage.setScene(new Scene(box));
    }

    private void showLobby() {
        // Stop game music if playing
        if (gameView != null) {
            gameView.stopMusic();
        }
        
        // Play background music
        try {
            if (backgroundMusic != null) {
                backgroundMusic.stop();
            }
            String musicPath = getClass().getResource("/sounds/y_ke_que.mp3").toExternalForm();
            backgroundMusic = new AudioClip(musicPath);
            backgroundMusic.setCycleCount(AudioClip.INDEFINITE); // Loop forever
            backgroundMusic.setVolume(0.3); // 30% volume
            backgroundMusic.play();
            System.out.println("Playing background music: " + musicPath);
        } catch (Exception e) {
            System.out.println("Could not load background music: " + e.getMessage());
        }
        
        // Header with current user + summary + logout
        headerUserInfo = new Label();
        updateHeaderUserInfo();
        headerUserInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 3, 0.5, 0, 0);");
        
        Button logoutBtn = new Button("🚪 Thoát");
        logoutBtn.setStyle(
            "-fx-font-size: 14px; -fx-padding: 8px 20px; " +
            "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
            "-fx-background-radius: 5px; -fx-cursor: hand; -fx-font-weight: bold;"
        );
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(
            "-fx-font-size: 14px; -fx-padding: 8px 20px; " +
            "-fx-background-color: #c0392b; -fx-text-fill: white; " +
            "-fx-background-radius: 5px; -fx-cursor: hand; -fx-font-weight: bold;"
        ));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(
            "-fx-font-size: 14px; -fx-padding: 8px 20px; " +
            "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
            "-fx-background-radius: 5px; -fx-cursor: hand; -fx-font-weight: bold;"
        ));
        
        HBox header = new HBox(10, headerUserInfo, logoutBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20));
        header.setStyle(
            "-fx-background-color: rgba(41, 128, 185, 0.85); " +
            "-fx-background-radius: 15px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0.6, 0, 3);"
        );

        // TableView of online players
        lobbyTable = new TableView<>();
        lobbyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        lobbyTable.setPlaceholder(new Label("Không có người chơi khác đang trực tuyến."));
        lobbyTable.setItems(lobbyData);
        lobbyTable.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95); " +
            "-fx-background-radius: 10px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 2); " +
            "-fx-font-size: 13px;"
        );
        
        // Style table header
        lobbyTable.setOnMouseEntered(e -> {
            lobbyTable.lookup(".column-header-background").setStyle(
                "-fx-background-color: #3498db; " +
                "-fx-background-radius: 10px 10px 0 0;"
            );
        });

        TableColumn<LobbyUserRow, String> colUser = new TableColumn<>("👤 Tên");
        colUser.setCellValueFactory(c -> c.getValue().usernameProperty());
        colUser.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        TableColumn<LobbyUserRow, String> colPoints = new TableColumn<>("⭐ Tổng điểm");
        colPoints.setCellValueFactory(c -> c.getValue().totalPointsProperty());
        colPoints.setStyle("-fx-alignment: CENTER; -fx-text-fill: #f39c12; -fx-font-weight: bold;");

        TableColumn<LobbyUserRow, String> colStatus = new TableColumn<>("📊 Trạng thái");
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colStatus.setStyle("-fx-alignment: CENTER;");

        TableColumn<LobbyUserRow, Void> colAction = new TableColumn<>("🎯 Hành động");
        colAction.setCellFactory(makeActionCellFactory());
        colAction.setStyle("-fx-alignment: CENTER;");

        lobbyTable.getColumns().addAll(colUser, colPoints, colStatus, colAction);

        // Search + auto refresh
        searchField = new TextField();
        searchField.setPromptText("🔍 Tìm người chơi...");
        searchField.setPrefHeight(40);
        searchField.setStyle(
            "-fx-font-size: 14px; -fx-padding: 10px 15px; " +
            "-fx-background-radius: 20px; " +
            "-fx-background-color: rgba(255, 255, 255, 0.9); " +
            "-fx-border-color: #3498db; -fx-border-radius: 20px; -fx-border-width: 2px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0.3, 0, 1);"
        );
        searchField.textProperty().addListener((obs, old, q) -> {
            String query = q == null ? "" : q.trim().toLowerCase();
            filteredLobby.setPredicate(row ->
                    query.isEmpty() || row.getUsername().toLowerCase().contains(query));
        });
        
        autoRefresh = new CheckBox("🔄 Refresh tự động");
        autoRefresh.setStyle(
            "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-effect: dropshadow(gaussian, black, 2, 0.5, 0, 0);"
        );
        
        HBox searchBar = new HBox(15, searchField, autoRefresh);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(15));
        searchBar.setStyle(
            "-fx-background-color: rgba(52, 152, 219, 0.3); " +
            "-fx-background-radius: 10px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.4, 0, 2);"
        );

        // Right control panel with styled buttons
        Button randomInviteBtn = createStyledButton("🎲 Mời người lạ ngẫu nhiên", "#9b59b6", "#8e44ad");
        Button leaderboardBtn = createStyledButton("🏆 Xem bảng xếp hạng", "#f39c12", "#e67e22");
        Button toggleStatusBtn = createStyledButton("🔄 Đổi trạng thái", "#16a085", "#138d75");
        
        Label menuLabel = new Label("🎮 MENU");
        menuLabel.setStyle(
            "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; " +
            "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.8), 5, 0.8, 0, 0); " +
            "-fx-padding: 10px;"
        );
        
        VBox controlPanel = new VBox(15,
                menuLabel,
                randomInviteBtn,
                leaderboardBtn,
                toggleStatusBtn
        );
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setPadding(new Insets(20));
        controlPanel.setStyle(
            "-fx-background-color: rgba(44, 62, 80, 0.85); " +
            "-fx-background-radius: 15px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0.6, 0, 3);"
        );

        // Actions
        randomInviteBtn.setOnAction(e -> {
            var candidates = filteredLobby.filtered(r -> "idle".equalsIgnoreCase(r.getStatus()));
            if (candidates.isEmpty()) { new Alert(Alert.AlertType.INFORMATION, "Không có ai rảnh.").show(); return; }
            LobbyUserRow pick = candidates.get(new Random().nextInt(candidates.size()));
            net.send(new Message(Protocol.INVITE_SEND, Map.of("toUser", pick.getUsername())));
        });

        leaderboardBtn.setOnAction(e -> {
            showLeaderboard();
        });

        toggleStatusBtn.setOnAction(e -> {
            // Client-side toggle for UI; server vẫn quản lý bận/khi đang chơi
            myStatus = "Rảnh".equals(myStatus) ? "Bận" : "Rảnh";
            updateHeaderUserInfo();
        });

        logoutBtn.setOnAction(e -> {
            // Reset trạng thái về online (Rảnh) trước khi logout
            myStatus = "Rảnh";
            updateHeaderUserInfo();
            
            try { if (net != null) net.disconnect(); } catch (Exception ignored) {}
            // Quay về màn hình đăng nhập, tạo kết nối mới
            this.net = new NetworkClient(this::onMessage);
            net.connect();
            showLogin();
        });

        BorderPane root = new BorderPane();
        VBox left = new VBox(15, searchBar, lobbyTable);
        left.setPadding(new Insets(10));
        root.setTop(header);
        root.setCenter(left);
        root.setRight(controlPanel);
        root.setPadding(new Insets(20));
        
        // Try to load background image
        try {
            // Try PNG first (since we have avatar_home.png)
            var stream = getClass().getResourceAsStream("/images/avatar_home.png");
            if (stream == null) {
                // Fallback to JPG
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
                root.setBackground(new Background(bg));
                System.out.println("✓ Background image loaded successfully!");
            } else {
                System.err.println("✗ Could not find background image - using gradient fallback");
                root.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c3e50, #3498db);");
            }
        } catch (Exception e) {
            // Fallback to gradient if image not found
            System.err.println("✗ Error loading background image: " + e.getMessage());
            e.printStackTrace();
            root.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c3e50, #3498db);");
        }
        
        stage.setScene(new Scene(root));
        // If we received lobby data earlier, populate now
        if (pendingLobby != null) {
            populateLobbyFromList(pendingLobby);
            pendingLobby = null;
        }
    }
    
    // Helper method to create styled buttons
    private Button createStyledButton(String text, String normalColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setMinWidth(200);
        String normalStyle = String.format(
            "-fx-font-size: 14px; -fx-padding: 10px 20px; " +
            "-fx-background-color: %s; -fx-text-fill: white; " +
            "-fx-background-radius: 5px; -fx-cursor: hand; -fx-font-weight: bold;",
            normalColor
        );
        String hoverStyle = String.format(
            "-fx-font-size: 14px; -fx-padding: 10px 20px; " +
            "-fx-background-color: %s; -fx-text-fill: white; " +
            "-fx-background-radius: 5px; -fx-cursor: hand; -fx-font-weight: bold;",
            hoverColor
        );
        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        return btn;
    }

    private void showGame() {
        // Stop background music when entering game
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            System.out.println("Stopped background music");
        }
        
        gameView = new GameView((x,y) -> {
            System.out.println("onClick callback triggered: x=" + x + ", y=" + y + ", currentRoomId=" + currentRoomId);
            if (currentRoomId != null) {
                System.out.println("Sending GAME_CLICK to server...");
                net.send(new Message(Protocol.GAME_CLICK, Map.of("roomId", currentRoomId, "x", x, "y", y)));
            } else {
                System.err.println("currentRoomId is null - cannot send click!");
            }
        }, username);
        stage.setScene(new Scene(gameView.getRoot()));
    }

    private void onMessage(Message msg) {
        Platform.runLater(() -> {
            switch (msg.type) {
                case Protocol.AUTH_RESULT -> {
                    Map<?,?> p = (Map<?,?>) msg.payload;
                    boolean ok = Boolean.parseBoolean(String.valueOf(p.get("success")));
                    if (ok) {
                        Map<?,?> user = (Map<?,?>) p.get("user");
                        username = String.valueOf(user.get("username"));
                        Object tp = user.get("totalPoints");
                        if (tp != null) myPoints = String.valueOf(tp);
                        showLobby();
                    } else {
                        new Alert(Alert.AlertType.ERROR, String.valueOf(p.get("message"))).showAndWait();
                    }
                }
                case Protocol.LOBBY_LIST -> {
                    var list = (java.util.List<Map<String,Object>>) msg.payload;
                    if (lobbyTable == null) {
                        pendingLobby = list; // cache to apply after UI is ready
                    } else {
                        populateLobbyFromList(list);
                    }
                }
                case Protocol.INVITE_RECEIVED -> {
                    String from = String.valueOf(((Map<?,?>)msg.payload).get("fromUser"));
                    
                    // Create custom styled dialog
                    Stage inviteDialog = new Stage();
                    inviteDialog.initModality(Modality.APPLICATION_MODAL);
                    inviteDialog.setTitle("Lời mời thi đấu");
                    inviteDialog.setResizable(false);
                    
                    // Content VBox with background
                    VBox dialogContent = new VBox(20);
                    dialogContent.setAlignment(Pos.CENTER);
                    dialogContent.setPadding(new Insets(40, 50, 40, 50));
                    
                    // Load and apply background image using CSS
                    try {
                        var bgStream = getClass().getResourceAsStream("/images/anh_moi_choi.jpg");
                        if (bgStream != null) {
                            // Use CSS to set background image
                            String imageUrl = getClass().getResource("/images/anh_moi_choi.jpg").toExternalForm();
                            dialogContent.setStyle(
                                "-fx-background-image: url('" + imageUrl + "');" +
                                "-fx-background-size: cover;" +
                                "-fx-background-position: center;" +
                                "-fx-background-repeat: no-repeat;" +
                                "-fx-background-radius: 15px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0.8, 0, 5);"
                            );
                            System.out.println("Loaded invite background image successfully via CSS: " + imageUrl);
                        } else {
                            System.out.println("Background image stream is null, using gradient");
                            dialogContent.setStyle(
                                "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                                "-fx-background-radius: 15px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0.8, 0, 5);"
                            );
                        }
                    } catch (Exception e) {
                        System.out.println("Error loading background image: " + e.getMessage());
                        e.printStackTrace();
                        dialogContent.setStyle(
                            "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                            "-fx-background-radius: 15px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0.8, 0, 5);"
                        );
                    }
                    
                    // Header icon and title
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
                    
                    // Message
                    Label messageLabel = new Label(from + " mời bạn thi đấu!");
                    messageLabel.setStyle(
                        "-fx-font-size: 18px;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-style: italic;" +
                        "-fx-padding: 15px;" +
                        "-fx-background-color: rgba(255,255,255,0.2);" +
                        "-fx-background-radius: 10px;"
                    );
                    
                    // Buttons
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
                    acceptBtn.setOnMouseEntered(e -> acceptBtn.setStyle(
                        acceptBtn.getStyle() + "-fx-background-color: #2ecc71; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"
                    ));
                    acceptBtn.setOnMouseExited(e -> acceptBtn.setStyle(
                        acceptBtn.getStyle().replace("-fx-background-color: #2ecc71;", "-fx-background-color: #27ae60;")
                            .replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")
                    ));
                    acceptBtn.setOnAction(e -> {
                        net.send(new Message(Protocol.INVITE_RESPONSE, Map.of("fromUser", from, "accepted", true)));
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
                    declineBtn.setOnMouseEntered(e -> declineBtn.setStyle(
                        declineBtn.getStyle() + "-fx-background-color: #c0392b; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"
                    ));
                    declineBtn.setOnMouseExited(e -> declineBtn.setStyle(
                        declineBtn.getStyle().replace("-fx-background-color: #c0392b;", "-fx-background-color: #e74c3c;")
                            .replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")
                    ));
                    declineBtn.setOnAction(e -> {
                        net.send(new Message(Protocol.INVITE_RESPONSE, Map.of("fromUser", from, "accepted", false)));
                        inviteDialog.close();
                    });
                    
                    buttonBox.getChildren().addAll(acceptBtn, declineBtn);
                    dialogContent.getChildren().addAll(iconLabel, headerLabel, messageLabel, buttonBox);
                    
                    Scene dialogScene = new Scene(dialogContent);
                    dialogScene.setFill(Color.TRANSPARENT);
                    inviteDialog.setScene(dialogScene);
                    inviteDialog.show();
                }
                case Protocol.INVITE_RESPONSE -> {
                    boolean accepted = Boolean.parseBoolean(String.valueOf(((Map<?,?>)msg.payload).get("accepted")));
                    if (!accepted) {
                        // Create custom styled notification
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
                        okBtn.setOnMouseEntered(e -> okBtn.setStyle(
                            okBtn.getStyle() + "-fx-background-color: rgba(255,255,255,0.5);"
                        ));
                        okBtn.setOnMouseExited(e -> okBtn.setStyle(
                            okBtn.getStyle().replace("-fx-background-color: rgba(255,255,255,0.5);", "-fx-background-color: rgba(255,255,255,0.3);")
                        ));
                        okBtn.setOnAction(e -> notifyDialog.close());
                        
                        notifyContent.getChildren().addAll(iconLabel, messageLabel, okBtn);
                        
                        Scene notifyScene = new Scene(notifyContent);
                        notifyScene.setFill(Color.TRANSPARENT);
                        notifyDialog.setScene(notifyScene);
                        notifyDialog.show();
                    }
                }
                case Protocol.GAME_START -> {
                    Map<?,?> p = (Map<?,?>) msg.payload;
                    currentRoomId = String.valueOf(p.get("roomId"));
                    showGame();
                    // Decode images if provided
                    try {
                        String b64L = (String) p.get("imgLeft");
                        String b64R = (String) p.get("imgRight");
                        Integer w = p.get("imageWidth") != null ? ((Number) p.get("imageWidth")).intValue() : 0;
                        Integer h = p.get("imageHeight") != null ? ((Number) p.get("imageHeight")).intValue() : 0;
                        System.out.println("GAME_START: imgLeft=" + (b64L != null ? b64L.length() : "null") + " chars, imgRight=" + (b64R != null ? b64R.length() : "null") + " chars, w=" + w + ", h=" + h);
                        byte[] leftBytes = (b64L != null) ? java.util.Base64.getDecoder().decode(b64L) : null;
                        byte[] rightBytes = (b64R != null) ? java.util.Base64.getDecoder().decode(b64R) : null;
                        System.out.println("Decoded: leftBytes=" + (leftBytes != null ? leftBytes.length : "null") + " bytes, rightBytes=" + (rightBytes != null ? rightBytes.length : "null") + " bytes");
                        if (gameView != null) {
                            gameView.setImages(leftBytes, rightBytes, w, h);
                            System.out.println("Called gameView.setImages()");
                        } else {
                            System.err.println("gameView is null!");
                        }
                    } catch (Exception e) {
                        System.err.println("Error decoding images: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                case Protocol.GAME_UPDATE -> {
                    if (gameView != null) {
                        gameView.updateFromPayload((Map<?,?>) msg.payload);
                    }
                }
                case Protocol.GAME_END -> {
                    if (gameView != null) {
                        gameView.updateFromPayload((Map<?,?>) msg.payload);
                    }
                    // Show beautiful result page instead of alert
                    showGameResult((Map<?,?>) msg.payload);
                }
                case Protocol.ERROR -> {
                    String errorMsg = msg.payload != null ? String.valueOf(((Map<?,?>)msg.payload).get("message")) : "Unknown error";
                    System.out.println("Error from server: " + errorMsg);
                    // Don't play wrong sound here - only play on actual miss (handled in GameView)
                }
            }
        });
    }

    private void showGameResult(Map<?,?> payload) {
        // Play celebration sound
        try {
            String soundPath = getClass().getResource("/sounds/ving_quang.mp3").toExternalForm();
            AudioClip celebrationSound = new AudioClip(soundPath);
            celebrationSound.setVolume(0.2); // 20% volume
            celebrationSound.play();
            System.out.println("Playing celebration sound: " + soundPath);
        } catch (Exception e) {
            System.err.println("Error playing celebration sound: " + e.getMessage());
        }
        
        // Parse result data
        String reason = String.valueOf(payload.get("reason"));
        Map<?,?> scores = (Map<?,?>) payload.get("scores");
        String result = String.valueOf(payload.get("result"));
        
        // Determine winner/loser
        int myScore = 0;
        int opponentScore = 0;
        String opponentName = "";
        
        for (var entry : scores.entrySet()) {
            String player = String.valueOf(entry.getKey());
            int score = ((Number) entry.getValue()).intValue();
            if (player.equals(username)) {
                myScore = score;
            } else {
                opponentName = player;
                opponentScore = score;
            }
        }
        
        boolean isWinner = myScore > opponentScore;
        boolean isDraw = myScore == opponentScore;
        
        // Main container
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #1a1a2e, #16213e);"
        );
        
        // Result icon and title
        Label resultIcon = new Label();
        Label resultTitle = new Label();
        
        if (isDraw) {
            resultIcon.setText("🤝");
            resultIcon.setStyle("-fx-font-size: 120px;");
            resultTitle.setText("HÒA!");
            resultTitle.setStyle(
                "-fx-font-size: 48px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #f39c12;" +
                "-fx-effect: dropshadow(gaussian, rgba(243,156,18,0.8), 15, 0.8, 0, 0);"
            );
        } else if (isWinner) {
            resultIcon.setText("🏆");
            resultIcon.setStyle("-fx-font-size: 120px;");
            resultTitle.setText("CHIẾN THẮNG!");
            resultTitle.setStyle(
                "-fx-font-size: 48px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2ecc71;" +
                "-fx-effect: dropshadow(gaussian, rgba(46,204,113,0.8), 15, 0.8, 0, 0);"
            );
        } else {
            resultIcon.setText("😢");
            resultIcon.setStyle("-fx-font-size: 120px;");
            resultTitle.setText("THUA RỒI!");
            resultTitle.setStyle(
                "-fx-font-size: 48px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #e74c3c;" +
                "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.8), 15, 0.8, 0, 0);"
            );
        }
        
        VBox resultHeader = new VBox(15, resultIcon, resultTitle);
        resultHeader.setAlignment(Pos.CENTER);
        
        // Score board
        HBox scoreBoard = new HBox(40);
        scoreBoard.setAlignment(Pos.CENTER);
        scoreBoard.setPadding(new Insets(30));
        scoreBoard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: rgba(255,255,255,0.3);" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 20px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0.7, 0, 5);"
        );
        
        // Your score
        VBox yourScoreBox = new VBox(10);
        yourScoreBox.setAlignment(Pos.CENTER);
        
        Label yourLabel = new Label("BẠN");
        yourLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: rgba(255,255,255,0.8);"
        );
        
        Label yourNameLabel = new Label(username);
        yourNameLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        
        Label yourScoreLabel = new Label(String.valueOf(myScore));
        yourScoreLabel.setStyle(
            "-fx-font-size: 64px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + (isWinner ? "#2ecc71" : (isDraw ? "#f39c12" : "#e74c3c")) + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.5), 10, 0.7, 0, 0);"
        );
        
        Label yourPointsLabel = new Label("điểm");
        yourPointsLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-text-fill: rgba(255,255,255,0.6);"
        );
        
        yourScoreBox.getChildren().addAll(yourLabel, yourNameLabel, yourScoreLabel, yourPointsLabel);
        
        // VS label
        Label vsLabel = new Label("VS");
        vsLabel.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: rgba(255,255,255,0.5);" +
            "-fx-padding: 20px 0;"
        );
        
        // Opponent score
        VBox opponentScoreBox = new VBox(10);
        opponentScoreBox.setAlignment(Pos.CENTER);
        
        Label oppLabel = new Label("ĐỐI THỦ");
        oppLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: rgba(255,255,255,0.8);"
        );
        
        Label oppNameLabel = new Label(opponentName);
        oppNameLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        
        Label oppScoreLabel = new Label(String.valueOf(opponentScore));
        oppScoreLabel.setStyle(
            "-fx-font-size: 64px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + (!isWinner ? "#2ecc71" : (isDraw ? "#f39c12" : "#e74c3c")) + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.5), 10, 0.7, 0, 0);"
        );
        
        Label oppPointsLabel = new Label("điểm");
        oppPointsLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-text-fill: rgba(255,255,255,0.6);"
        );
        
        opponentScoreBox.getChildren().addAll(oppLabel, oppNameLabel, oppScoreLabel, oppPointsLabel);
        
        scoreBoard.getChildren().addAll(yourScoreBox, vsLabel, opponentScoreBox);
        
        // Reason label
        String reasonText = "";
        if (reason.equals("all-found")) {
            reasonText = "🎯 Tất cả điểm khác biệt đã được tìm thấy!";
        } else if (reason.equals("quit")) {
            reasonText = "👋 Đối thủ đã rời khỏi trận đấu";
        } else {
            reasonText = "✅ Trận đấu kết thúc";
        }
        
        Label reasonLabel = new Label(reasonText);
        reasonLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-text-fill: rgba(255,255,255,0.8);" +
            "-fx-font-style: italic;" +
            "-fx-padding: 10px 30px;" +
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-background-radius: 15px;"
        );
        
        // Buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button backToLobbyBtn = createStyledButton("🏠 Về Trang Chủ", "#3498db", "#2980b9");
        backToLobbyBtn.setOnAction(e -> showLobby());
        
        Button viewLeaderboardBtn = createStyledButton("🏆 Xem Bảng Xếp Hạng", "#f39c12", "#e67e22");
        viewLeaderboardBtn.setOnAction(e -> showLeaderboard());
        
        buttonBox.getChildren().addAll(backToLobbyBtn, viewLeaderboardBtn);
        
        mainContainer.getChildren().addAll(resultHeader, scoreBoard, reasonLabel, buttonBox);
        
        Scene resultScene = new Scene(mainContainer);
        stage.setScene(resultScene);
    }

    private void showLeaderboard() {
        // Request leaderboard data from server
        net.send(new Message(Protocol.LEADERBOARD, null));
        
        // Main container with dark blue gradient background
        VBox mainContainer = new VBox(0);
        mainContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0a1929, #1a2f4a);" +
            "-fx-padding: 0;"
        );
        
        // Header section - more compact
        VBox leaderboardHeader = new VBox(12);
        leaderboardHeader.setAlignment(Pos.CENTER);
        leaderboardHeader.setPadding(new Insets(20, 15, 18, 15));
        leaderboardHeader.setStyle(
            "-fx-background-color: linear-gradient(to bottom, rgba(30,144,255,0.3), rgba(0,0,0,0.1));" +
            "-fx-border-color: rgba(255,255,255,0.1);" +
            "-fx-border-width: 0 0 2 0;"
        );
        
        Label titleLabel = new Label("BẢNG XẾP HẠNG");
        titleLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: #1e5a9e;" +
            "-fx-padding: 8px 35px;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 8, 0.6, 0, 3);"
        );
        
        // Back button - smaller
        Button backBtn = new Button("⬅ Quay lại");
        backBtn.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-padding: 6px 18px;" +
            "-fx-background-color: #7f8c8d;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;"
        );
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
            backBtn.getStyle().replace("#7f8c8d", "#95a5a6")
        ));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(
            backBtn.getStyle().replace("#95a5a6", "#7f8c8d")
        ));
        backBtn.setOnAction(e -> showLobby());
        
        leaderboardHeader.getChildren().addAll(titleLabel, backBtn);
        
        // Leaderboard entries container - more compact and centered
        VBox entriesContainer = new VBox(8);
        entriesContainer.setPadding(new Insets(18, 20, 18, 20));
        entriesContainer.setAlignment(Pos.TOP_CENTER);
        entriesContainer.setMaxWidth(700);
        
        // StackPane to center the entries container
        StackPane centerWrapper = new StackPane(entriesContainer);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        
        // Scrollpane for entries
        ScrollPane scrollPane = new ScrollPane(centerWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background: transparent;"
        );
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        mainContainer.getChildren().addAll(leaderboardHeader, scrollPane);
        
        // Handle leaderboard data
        net.addHandler(Protocol.LEADERBOARD, msg -> {
            Platform.runLater(() -> {
                entriesContainer.getChildren().clear();
                var entries = (List<Map<String,Object>>) msg.payload;
                
                for (int i = 0; i < entries.size(); i++) {
                    var entry = entries.get(i);
                    int rank = i + 1;
                    String playerName = String.valueOf(entry.get("username"));
                    int totalPoints = ((Number) entry.get("totalPoints")).intValue();
                    int totalWins = ((Number) entry.get("totalWins")).intValue();
                    
                    // Create entry row - more compact
                    HBox entryRow = new HBox(12);
                    entryRow.setAlignment(Pos.CENTER_LEFT);
                    entryRow.setPadding(new Insets(10, 15, 10, 15));
                    entryRow.setMaxWidth(650);
                    
                    // Background color based on rank
                    String bgColor;
                    if (rank == 1) {
                        bgColor = "linear-gradient(to right, rgba(255,215,0,0.45), rgba(255,165,0,0.35))"; // Gold
                    } else if (rank == 2) {
                        bgColor = "linear-gradient(to right, rgba(192,192,192,0.45), rgba(169,169,169,0.35))"; // Silver
                    } else if (rank == 3) {
                        bgColor = "linear-gradient(to right, rgba(205,127,50,0.45), rgba(184,115,51,0.35))"; // Bronze
                    } else {
                        bgColor = "rgba(30,90,158,0.3)";
                    }
                    
                    entryRow.setStyle(
                        "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: rgba(100,180,255,0.4);" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 5, 0.6, 0, 2);"
                    );
                    
                    // Medal or rank number - smaller
                    Label rankLabel = new Label();
                    if (rank == 1) {
                        rankLabel.setText("🥇");
                        rankLabel.setStyle("-fx-font-size: 28px;");
                    } else if (rank == 2) {
                        rankLabel.setText("🥈");
                        rankLabel.setStyle("-fx-font-size: 28px;");
                    } else if (rank == 3) {
                        rankLabel.setText("🥉");
                        rankLabel.setStyle("-fx-font-size: 28px;");
                    } else {
                        rankLabel.setText(String.valueOf(rank));
                        rankLabel.setStyle(
                            "-fx-font-size: 20px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;" +
                            "-fx-min-width: 32px;" +
                            "-fx-alignment: center;"
                        );
                    }
                    rankLabel.setMinWidth(38);
                    rankLabel.setAlignment(Pos.CENTER);
                    
                    // Avatar placeholder - smaller
                    Label avatarLabel = new Label("👤");
                    avatarLabel.setStyle(
                        "-fx-font-size: 24px;" +
                        "-fx-background-color: rgba(255,255,255,0.2);" +
                        "-fx-background-radius: 22px;" +
                        "-fx-min-width: 44px;" +
                        "-fx-min-height: 44px;" +
                        "-fx-alignment: center;"
                    );
                    
                    // Player name - smaller
                    Label nameLabel = new Label(playerName);
                    nameLabel.setStyle(
                        "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 2, 0.6, 0, 1);"
                    );
                    nameLabel.setMinWidth(140);
                    
                    // Star rating (based on points) - smaller stars
                    HBox starsBox = new HBox(3);
                    starsBox.setAlignment(Pos.CENTER_LEFT);
                    int fullStars = Math.min(5, totalPoints / 500); // 1 star per 500 points
                    for (int s = 0; s < 5; s++) {
                        Label star = new Label(s < fullStars ? "⭐" : "☆");
                        star.setStyle(
                            "-fx-font-size: 16px;" +
                            "-fx-text-fill: " + (s < fullStars ? "#FFD700" : "rgba(255,255,255,0.3)") + ";"
                        );
                        starsBox.getChildren().add(star);
                    }
                    
                    // Spacer
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    // Points display with label - more compact
                    VBox pointsBox = new VBox(2);
                    pointsBox.setAlignment(Pos.CENTER);
                    
                    Label pointsLabel = new Label(String.valueOf(totalPoints));
                    pointsLabel.setStyle(
                        "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #FFD700;" +
                        "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.6), 4, 0.7, 0, 2);"
                    );
                    
                    Label pointsTextLabel = new Label("điểm");
                    pointsTextLabel.setStyle(
                        "-fx-font-size: 10px;" +
                        "-fx-text-fill: rgba(255,255,255,0.75);"
                    );
                    
                    pointsBox.getChildren().addAll(pointsLabel, pointsTextLabel);
                    
                    // Wins display - more compact
                    VBox winsBox = new VBox(2);
                    winsBox.setAlignment(Pos.CENTER);
                    
                    Label winsLabel = new Label(String.valueOf(totalWins));
                    winsLabel.setStyle(
                        "-fx-font-size: 19px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #4CAF50;" +
                        "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.5), 3, 0.6, 0, 1);"
                    );
                    
                    Label winsTextLabel = new Label("thắng");
                    winsTextLabel.setStyle(
                        "-fx-font-size: 10px;" +
                        "-fx-text-fill: rgba(255,255,255,0.75);"
                    );
                    
                    winsBox.getChildren().addAll(winsLabel, winsTextLabel);
                    
                    entryRow.getChildren().addAll(
                        rankLabel, 
                        avatarLabel, 
                        nameLabel, 
                        starsBox, 
                        spacer, 
                        pointsBox,
                        winsBox
                    );
                    
                    entriesContainer.getChildren().add(entryRow);
                }
            });
        });
        
        Scene leaderboardScene = new Scene(mainContainer);
        stage.setScene(leaderboardScene);
    }

    // Helpers (inside ClientApp)
    private void updateHeaderUserInfo() {
        if (headerUserInfo != null) {
            headerUserInfo.setText("Xin chào, "+username+"  •  Tổng điểm: "+myPoints+"  •  "+myStatus);
        }
    }

    private void populateLobbyFromList(java.util.List<Map<String,Object>> list) {
        lobbyData.clear();
        for (var u : list) {
            String name = String.valueOf(u.get("username"));
            String pts = String.valueOf(u.get("totalPoints"));
            String st = String.valueOf(u.get("status"));
            if (name != null && name.equals(username)) continue; // hide self
            lobbyData.add(new LobbyUserRow(name, pts, st));
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
                            "Người chơi: "+row.getUsername()+"\nĐiểm: "+row.getTotalPoints()+"\nTrạng thái: "+row.getStatus())
                            .show();
                });
                inviteBtn.setOnAction(e -> {
                    LobbyUserRow row = getTableView().getItems().get(getIndex());
                    if (!row.getUsername().equals(username)) {
                        net.send(new Message(Protocol.INVITE_SEND, Map.of("toUser", row.getUsername())));
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

// Simple row model for the lobby table
class LobbyUserRow {
    private final javafx.beans.property.SimpleStringProperty username = new javafx.beans.property.SimpleStringProperty("");
    private final javafx.beans.property.SimpleStringProperty totalPoints = new javafx.beans.property.SimpleStringProperty("");
    private final javafx.beans.property.SimpleStringProperty status = new javafx.beans.property.SimpleStringProperty("");

    public LobbyUserRow(String username, String totalPoints, String status) {
        this.username.set(username);
        this.totalPoints.set(totalPoints);
        this.status.set(status);
    }
    public String getUsername() { return username.get(); }
    public javafx.beans.property.StringProperty usernameProperty() { return username; }
    public String getTotalPoints() { return totalPoints.get(); }
    public javafx.beans.property.StringProperty totalPointsProperty() { return totalPoints; }
    public String getStatus() { return status.get(); }
    public javafx.beans.property.StringProperty statusProperty() { return status; }
}

 
