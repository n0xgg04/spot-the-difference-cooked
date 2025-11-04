package com.example.client;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.io.ByteArrayInputStream;

public class GameView {
    private final BorderPane root = new BorderPane();
    private final Canvas canvas = new Canvas(900, 450);
    private final Label scoreLabel = new Label("Điểm số");
    private final Label timerLabel = new Label("⏱ 15s");
    private final Label turnLabel = new Label("Lượt của bạn");
    private AudioClip gameMusic;
    private AudioClip correctSound;
    private AudioClip wrongSound;
    private String nextTurn = "";
    private int scoreA = 0, scoreB = 0;
    private String playerA = "";
    private String playerB = "";
    private String myUsername = "";
    private int remainingSeconds = 15;
    private Timeline countdownTimer;
    private final List<Map<String,Object>> found = new ArrayList<>();
    private Double lastClickX = null; // Để hiển thị dấu X tạm thời
    private Double lastClickY = null;
    private boolean justClicked = false; // Track if player just clicked
    // Images and geometry
    private Image leftImg;
    private Image rightImg;
    private int imgW = 300;
    private int imgH = 300;
    private final double boxX1 = 50, boxY = 80, boxX2 = 500, boxSize = 350;

    public GameView(BiConsumer<Double, Double> onClick, String myUsername) {
        this.myUsername = myUsername;
        
        // Play game background music
        try {
            String musicPath = getClass().getResource("/sounds/nhac_ingame.mp3").toExternalForm();
            gameMusic = new AudioClip(musicPath);
            gameMusic.setCycleCount(AudioClip.INDEFINITE); // Loop forever
            gameMusic.setVolume(0.25); // 25% volume
            gameMusic.play();
            System.out.println("Playing game music: " + musicPath);
        } catch (Exception e) {
            System.out.println("Could not load game music: " + e.getMessage());
        }
        
        // Load correct answer sound effect
        try {
            String correctPath = getClass().getResource("/sounds/ye_đoan_dung_roi.mp3").toExternalForm();
            correctSound = new AudioClip(correctPath);
            correctSound.setVolume(0.6); // 60% volume
            System.out.println("Loaded correct sound: " + correctPath);
        } catch (Exception e) {
            System.out.println("Could not load correct sound: " + e.getMessage());
        }
        
        // Load wrong answer sound effect
        try {
            String wrongPath = getClass().getResource("/sounds/phai_chiu.mp3").toExternalForm();
            wrongSound = new AudioClip(wrongPath);
            wrongSound.setVolume(0.5); // 50% volume
            System.out.println("Loaded wrong sound: " + wrongPath);
        } catch (Exception e) {
            System.out.println("Could not load wrong sound: " + e.getMessage());
        }
        
        // Gradient background - darker and more modern
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #0f2027, #203a43, #2c5364);"
        );
        
        // Top panel - Game info header with glass effect
        VBox topPanel = new VBox(8);
        topPanel.setPadding(new Insets(20, 25, 15, 25));
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.12);" +
            "-fx-background-radius: 15px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.2);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 15px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0.6, 0, 3);"
        );
        
        Label titleLabel = new Label("🎯 TÌM ĐIỂM KHÁC BIỆT");
        titleLabel.setStyle(
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: linear-gradient(to right, #a8edea, #fed6e3);" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 4, 0.6, 0, 2);"
        );
        
        scoreLabel.setStyle(
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #ffeaa7;" +
            "-fx-effect: dropshadow(gaussian, rgba(255,234,167,0.5), 3, 0.6, 0, 1);"
        );
        
        topPanel.getChildren().addAll(titleLabel, scoreLabel);
        
        // Center - Canvas with modern padding
        VBox centerContainer = new VBox();
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.setPadding(new Insets(15));
        centerContainer.getChildren().add(canvas);
        
        // Bottom panel - Modern timer and turn indicator
        HBox bottomPanel = new HBox(40);
        bottomPanel.setPadding(new Insets(15, 25, 20, 25));
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.12);" +
            "-fx-background-radius: 15px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.2);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 15px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0.6, 0, 3);"
        );
        
        turnLabel.setStyle(
            "-fx-font-size: 19px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 3, 0.6, 0, 2);"
        );
        
        timerLabel.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #fff;" +
            "-fx-padding: 8px 20px;" +
            "-fx-background-color: rgba(255, 107, 107, 0.25);" +
            "-fx-background-radius: 25px;" +
            "-fx-border-color: rgba(255, 107, 107, 0.5);" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 25px;" +
            "-fx-effect: dropshadow(gaussian, rgba(255,107,107,0.6), 5, 0.7, 0, 2);"
        );
        
        bottomPanel.getChildren().addAll(turnLabel, timerLabel);
        
        root.setTop(topPanel);
        root.setCenter(centerContainer);
        root.setBottom(bottomPanel);
        root.setPadding(new Insets(10));
        
        // Setup countdown timer (runs every 1 second)
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (remainingSeconds > 0) {
                remainingSeconds--;
                updateTimerDisplay();
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        
        drawBase();
        canvas.setOnMouseClicked(e -> {
            double x = e.getX();
            double y = e.getY();
            System.out.println("Canvas clicked at: (" + x + ", " + y + ")");
            // Map click to image-space if within either box
            Double mappedX = null, mappedY = null;
            if (x >= boxX1 && x <= boxX1 + boxSize && y >= boxY && y <= boxY + boxSize) {
                double lx = x - boxX1; double ly = y - boxY;
                mappedX = lx * (imgW / boxSize);
                mappedY = ly * (imgH / boxSize);
                System.out.println("Left box clicked - mapped to image coords: (" + mappedX + ", " + mappedY + ")");
            } else if (x >= boxX2 && x <= boxX2 + boxSize && y >= boxY && y <= boxY + boxSize) {
                double rx = x - boxX2; double ry = y - boxY;
                mappedX = rx * (imgW / boxSize);
                mappedY = ry * (imgH / boxSize);
                System.out.println("Right box clicked - mapped to image coords: (" + mappedX + ", " + mappedY + ")");
            }
            if (mappedX != null) {
                System.out.println("Sending onClick callback with coords: (" + mappedX + ", " + mappedY + ")");
                lastClickX = mappedX;
                lastClickY = mappedY;
                justClicked = true; // Mark that we just clicked
                render(); // Vẽ lại để hiển thị dấu X
                onClick.accept(mappedX, mappedY);
            } else {
                System.out.println("Click outside image boxes - ignoring");
            }
        });
    }

    private void drawBase() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        
        // Clean white background for easy viewing
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, 900, 450);
        
        // Draw subtle shadow frames
        g.setFill(Color.web("#e0e0e0"));
        g.fillRect(boxX1 - 5, boxY - 5, boxSize + 10, boxSize + 10);
        g.fillRect(boxX2 - 5, boxY - 5, boxSize + 10, boxSize + 10);
        
        // Draw images or placeholders
        if (leftImg != null) {
            g.drawImage(leftImg, boxX1, boxY, boxSize, boxSize);
        } else { 
            g.setFill(Color.web("#f5f5f5")); 
            g.fillRect(boxX1, boxY, boxSize, boxSize);
        }
        if (rightImg != null) {
            g.drawImage(rightImg, boxX2, boxY, boxSize, boxSize);
        } else { 
            g.setFill(Color.web("#f5f5f5")); 
            g.fillRect(boxX2, boxY, boxSize, boxSize);
        }
        
        // Draw clean borders
        g.setStroke(Color.web("#333333"));
        g.setLineWidth(3);
        g.strokeRect(boxX1, boxY, boxSize, boxSize);
        g.strokeRect(boxX2, boxY, boxSize, boxSize);
        g.setLineWidth(1);
        
        // Simple labels above images
        g.setFill(Color.web("#333333"));
        g.setFont(Font.font("System", FontWeight.BOLD, 18));
        g.fillText("ẢNH GỐC", boxX1 + boxSize/2 - 35, boxY - 12);
        g.fillText("ẢNH SAI KHÁC", boxX2 + boxSize/2 - 55, boxY - 12);
    }

    public void updateFromPayload(Map<?,?> p) {
        String oldNextTurn = nextTurn;
        int oldScoreA = scoreA, oldScoreB = scoreB;
        
        Object scoresObj = p.get("scores");
        if (scoresObj instanceof Map<?,?> s) {
            // Extract player names and scores
            var entries = new ArrayList<>(s.entrySet());
            if (entries.size() >= 2) {
                var e1 = (Map.Entry<?,?>) entries.get(0);
                var e2 = (Map.Entry<?,?>) entries.get(1);
                playerA = String.valueOf(e1.getKey());
                playerB = String.valueOf(e2.getKey());
                scoreA = ((Number)e1.getValue()).intValue();
                scoreB = ((Number)e2.getValue()).intValue();
            }
        }
        
        // Check if turn changed
        String newNextTurn = p.get("nextTurn") != null ? String.valueOf(p.get("nextTurn")) : nextTurn;
        boolean turnChanged = !oldNextTurn.isEmpty() && !newNextTurn.equals(oldNextTurn);
        boolean wasMyTurn = oldNextTurn.equals(myUsername);
        
        // Check if my score increased
        int myOldScore = 0, myNewScore = 0;
        if (playerA.equals(myUsername)) {
            myOldScore = oldScoreA;
            myNewScore = scoreA;
        } else if (playerB.equals(myUsername)) {
            myOldScore = oldScoreB;
            myNewScore = scoreB;
        }
        boolean myScoreIncreased = myNewScore > myOldScore;
        
        nextTurn = newNextTurn;
        
        // Update timer from remainingTurnMs and restart countdown
        if (p.get("remainingTurnMs") != null) {
            long ms = ((Number)p.get("remainingTurnMs")).longValue();
            remainingSeconds = (int) Math.max(0, ms / 1000);
            
            // Restart countdown timer
            countdownTimer.stop();
            if (remainingSeconds > 0) {
                countdownTimer.playFromStart();
            }
            updateTimerDisplay();
        }
        
        if (p.get("found") instanceof List<?> f) {
            int oldFoundCount = found.size();
            found.clear();
            lastClickX = null; // Xóa dấu X tạm thời khi có update từ server
            lastClickY = null;
            
            Map<String,Object> latestFind = null;
            for (Object o: f) {
                if (o instanceof Map<?,?> m) {
                    Map<String,Object> findMap = (Map<String,Object>) m;
                    found.add(findMap);
                    latestFind = findMap;
                }
            }
            
            // Play sound if new difference found by current player
            if (found.size() > oldFoundCount && latestFind != null) {
                String finder = latestFind.get("finder") != null ? String.valueOf(latestFind.get("finder")) : "";
                if (finder.equals(myUsername) && correctSound != null) {
                    correctSound.play();
                    System.out.println("Playing correct sound - player found a difference!");
                }
                justClicked = false; // Reset flag after successful find
            } else if (justClicked && found.size() == oldFoundCount) {
                // We clicked but no new difference was found = WRONG click
                if (wrongSound != null) {
                    wrongSound.play();
                    System.out.println("Playing wrong sound - clicked but missed!");
                }
                justClicked = false; // Reset flag after miss
            } else if (turnChanged) {
                // Turn changed (timeout or other reason) - reset click flag
                justClicked = false;
            }
        }
        render();
    }
    
    private void updateTimerDisplay() {
        boolean isMyTurn = nextTurn.equals(myUsername);
        
        if (isMyTurn) {
            turnLabel.setText("⚡ LƯỢT CỦA BẠN ⚡");
            turnLabel.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #00ff88;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,255,136,0.8), 6, 0.8, 0, 0);"
            );
            
            // Change timer color based on remaining time
            String timerColor = remainingSeconds <= 5 ? "#ff3838" : "#ffeaa7";
            String glowColor = remainingSeconds <= 5 ? "rgba(255,56,56,0.7)" : "rgba(255,234,167,0.5)";
            
            timerLabel.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + timerColor + ";" +
                "-fx-padding: 10px 22px;" +
                "-fx-background-color: rgba(255, 255, 255, 0.15);" +
                "-fx-background-radius: 30px;" +
                "-fx-border-color: " + timerColor + ";" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 30px;" +
                "-fx-effect: dropshadow(gaussian, " + glowColor + ", 8, 0.8, 0, 2);"
            );
        } else {
            turnLabel.setText("⏳ Đợi đối thủ...");
            turnLabel.setStyle(
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #95a5a6;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 3, 0.6, 0, 2);"
            );
            timerLabel.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #bdc3c7;" +
                "-fx-padding: 8px 20px;" +
                "-fx-background-color: rgba(255, 255, 255, 0.08);" +
                "-fx-background-radius: 25px;" +
                "-fx-border-color: rgba(189, 195, 199, 0.3);" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 25px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0.5, 0, 1);"
            );
        }
        
        timerLabel.setText(String.format("⏱ %ds", remainingSeconds));
    }

    private void render() {
        drawBase();
        GraphicsContext g = canvas.getGraphicsContext2D();
        
        // Draw found circles on both images with premium color and glow effects
        for (Map<String,Object> d : found) {
            double x = ((Number)d.get("x")).doubleValue();
            double y = ((Number)d.get("y")).doubleValue();
            double r = ((Number)d.get("radius")).doubleValue();
            double sx = x * (boxSize / imgW);
            double sy = y * (boxSize / imgH);
            double rr = r * (boxSize / imgW);
            
            // Determine color based on who found this difference
            String finder = d.get("finder") != null ? String.valueOf(d.get("finder")) : "";
            Color circleColor;
            if (finder.equals(myUsername)) {
                circleColor = Color.web("#00ff88"); // Neon green for my points
            } else if (finder.equals(playerA) || finder.equals(playerB)) {
                circleColor = Color.web("#ff3838"); // Neon red for opponent's points
            } else {
                circleColor = Color.ORANGE; // Unknown/legacy
            }
            
            // Draw outer glow (largest)
            g.setStroke(circleColor.deriveColor(0, 1, 1, 0.15));
            g.setLineWidth(14);
            g.strokeOval(boxX1 + sx - rr - 5, boxY + sy - rr - 5, rr*2 + 10, rr*2 + 10);
            g.strokeOval(boxX2 + sx - rr - 5, boxY + sy - rr - 5, rr*2 + 10, rr*2 + 10);
            
            // Draw middle glow
            g.setStroke(circleColor.deriveColor(0, 1, 1, 0.4));
            g.setLineWidth(8);
            g.strokeOval(boxX1 + sx - rr - 2, boxY + sy - rr - 2, rr*2 + 4, rr*2 + 4);
            g.strokeOval(boxX2 + sx - rr - 2, boxY + sy - rr - 2, rr*2 + 4, rr*2 + 4);
            
            // Draw main circle (brightest)
            g.setStroke(circleColor);
            g.setLineWidth(5);
            g.strokeOval(boxX1 + sx - rr, boxY + sy - rr, rr*2, rr*2);
            g.strokeOval(boxX2 + sx - rr, boxY + sy - rr, rr*2, rr*2);
            g.setLineWidth(1);
        }
        
        // Vẽ dấu X tạm thời tại vị trí click gần nhất
        if (lastClickX != null && lastClickY != null) {
            double sx = lastClickX * (boxSize / imgW);
            double sy = lastClickY * (boxSize / imgH);
            g.setStroke(Color.YELLOW);
            g.setLineWidth(3);
            double crossSize = 12;
            // Vẽ X trên cả 2 ảnh
            g.strokeLine(boxX1 + sx - crossSize, boxY + sy - crossSize, boxX1 + sx + crossSize, boxY + sy + crossSize);
            g.strokeLine(boxX1 + sx + crossSize, boxY + sy - crossSize, boxX1 + sx - crossSize, boxY + sy + crossSize);
            g.strokeLine(boxX2 + sx - crossSize, boxY + sy - crossSize, boxX2 + sx + crossSize, boxY + sy + crossSize);
            g.strokeLine(boxX2 + sx + crossSize, boxY + sy - crossSize, boxX2 + sx - crossSize, boxY + sy + crossSize);
            g.setLineWidth(1);
        }
        
        // Display scores with player names - highlight current player
        String myScore = "", opponentScore = "", opponent = "";
        if (playerA.equals(myUsername)) {
            myScore = playerA + ": " + scoreA;
            opponentScore = playerB + ": " + scoreB;
            opponent = playerB;
        } else if (playerB.equals(myUsername)) {
            myScore = playerB + ": " + scoreB;
            opponentScore = playerA + ": " + scoreA;
            opponent = playerA;
        } else {
            myScore = "Bạn: ?";
            opponentScore = "Đối thủ: ?";
        }
        
        String scoreText = String.format("👤 %s  |  🎯 %s", myScore, opponentScore);
        scoreLabel.setText(scoreText);
    }

    public BorderPane getRoot() { return root; }

    public void setImages(byte[] leftBytes, byte[] rightBytes, int width, int height) {
        System.out.println("GameView.setImages called: leftBytes=" + (leftBytes != null ? leftBytes.length : "null") + ", rightBytes=" + (rightBytes != null ? rightBytes.length : "null") + ", w=" + width + ", h=" + height);
        this.imgW = width > 0 ? width : this.imgW;
        this.imgH = height > 0 ? height : this.imgH;
        try {
            this.leftImg = leftBytes != null ? new Image(new ByteArrayInputStream(leftBytes)) : null;
            this.rightImg = rightBytes != null ? new Image(new ByteArrayInputStream(rightBytes)) : null;
            System.out.println("Images created: leftImg=" + (leftImg != null) + ", rightImg=" + (rightImg != null));
            if (leftImg != null) System.out.println("Left image size: " + leftImg.getWidth() + "x" + leftImg.getHeight());
            if (rightImg != null) System.out.println("Right image size: " + rightImg.getWidth() + "x" + rightImg.getHeight());
        } catch (Exception e) {
            System.err.println("Error creating Image from bytes: " + e.getMessage());
            e.printStackTrace();
        }
        render();
    }
    
    public void stopMusic() {
        if (gameMusic != null) {
            gameMusic.stop();
            System.out.println("Stopped game music");
        }
    }
    
    public void playWrongSound() {
        if (wrongSound != null) {
            wrongSound.play();
            System.out.println("Playing wrong sound - incorrect guess!");
        }
    }
}
