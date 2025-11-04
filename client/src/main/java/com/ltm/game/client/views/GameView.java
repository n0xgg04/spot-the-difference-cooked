package com.ltm.game.client.views;

import com.ltm.game.client.services.AudioService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class GameView {
    private final BorderPane root = new BorderPane();
    private final Canvas canvas = new Canvas(900, 450);
    private final Label scoreLabel = new Label("Điểm số");
    private final Label timerLabel = new Label("⏱ 15s");
    private final Label turnLabel = new Label("Lượt của bạn");
    
    private AudioService audioService;
    private String nextTurn = "";
    private int scoreA = 0, scoreB = 0;
    private String playerA = "";
    private String playerB = "";
    private String myUsername = "";
    private int remainingSeconds = 15;
    private Timeline countdownTimer;
    private final List<Map<String,Object>> found = new ArrayList<>();
    private Double lastClickX = null;
    private Double lastClickY = null;
    private boolean justClicked = false;
    
    private Image leftImg;
    private Image rightImg;
    private int imgW = 300;
    private int imgH = 300;
    private final double boxX1 = 50, boxY = 80, boxX2 = 500, boxSize = 350;

    public GameView(BiConsumer<Double, Double> onClick, String myUsername, AudioService audioService) {
        this.myUsername = myUsername;
        this.audioService = audioService;
        
        if (audioService != null) {
            audioService.playGameMusic();
            audioService.loadGameSounds();
        }
        
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #0f2027, #203a43, #2c5364);"
        );
        
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
        
        VBox centerContainer = new VBox();
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.setPadding(new Insets(15));
        centerContainer.getChildren().add(canvas);
        
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
                justClicked = true;
                render();
                onClick.accept(mappedX, mappedY);
            } else {
                System.out.println("Click outside image boxes - ignoring");
            }
        });
    }

    private void drawBase() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, 900, 450);
        
        g.setFill(Color.web("#e0e0e0"));
        g.fillRect(boxX1 - 5, boxY - 5, boxSize + 10, boxSize + 10);
        g.fillRect(boxX2 - 5, boxY - 5, boxSize + 10, boxSize + 10);
        
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
        
        g.setStroke(Color.web("#333333"));
        g.setLineWidth(3);
        g.strokeRect(boxX1, boxY, boxSize, boxSize);
        g.strokeRect(boxX2, boxY, boxSize, boxSize);
        g.setLineWidth(1);
        
        g.setFill(Color.web("#333333"));
        g.setFont(Font.font("System", FontWeight.BOLD, 18));
        g.fillText("ẢNH GỐC", boxX1 + boxSize/2 - 35, boxY - 12);
        g.fillText("ẢNH SAI KHÁC", boxX2 + boxSize/2 - 55, boxY - 12);
    }

    public void updateFromPayload(Map<?,?> p) {
        String oldNextTurn = nextTurn;
        
        Object scoresObj = p.get("scores");
        if (scoresObj instanceof Map<?,?> s) {
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
        
        String newNextTurn = p.get("nextTurn") != null ? String.valueOf(p.get("nextTurn")) : nextTurn;
        boolean turnChanged = !oldNextTurn.isEmpty() && !newNextTurn.equals(oldNextTurn);
        
        nextTurn = newNextTurn;
        
        if (p.get("remainingTurnMs") != null) {
            long ms = ((Number)p.get("remainingTurnMs")).longValue();
            remainingSeconds = (int) Math.max(0, ms / 1000);
            
            countdownTimer.stop();
            if (remainingSeconds > 0) {
                countdownTimer.playFromStart();
            }
            updateTimerDisplay();
        }
        
        if (p.get("found") instanceof List<?> f) {
            int oldFoundCount = found.size();
            found.clear();
            lastClickX = null;
            lastClickY = null;
            
            Map<String,Object> latestFind = null;
            for (Object o: f) {
                if (o instanceof Map<?,?> m) {
                    @SuppressWarnings("unchecked")
                    Map<String,Object> findMap = (Map<String,Object>) m;
                    found.add(findMap);
                    latestFind = findMap;
                }
            }
            
            if (found.size() > oldFoundCount && latestFind != null) {
                String finder = latestFind.get("finder") != null ? String.valueOf(latestFind.get("finder")) : "";
                if (finder.equals(myUsername) && audioService != null) {
                    audioService.playCorrectSound();
                    System.out.println("Playing correct sound - player found a difference!");
                }
                justClicked = false;
            } else if (justClicked && found.size() == oldFoundCount) {
                if (audioService != null) {
                    audioService.playWrongSound();
                    System.out.println("Playing wrong sound - clicked but missed!");
                }
                justClicked = false;
            } else if (turnChanged) {
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
        
        for (Map<String,Object> d : found) {
            double x = ((Number)d.get("x")).doubleValue();
            double y = ((Number)d.get("y")).doubleValue();
            double r = ((Number)d.get("radius")).doubleValue();
            double sx = x * (boxSize / imgW);
            double sy = y * (boxSize / imgH);
            double rr = r * (boxSize / imgW);
            
            String finder = d.get("finder") != null ? String.valueOf(d.get("finder")) : "";
            Color circleColor;
            if (finder.equals(myUsername)) {
                circleColor = Color.web("#00ff88");
            } else if (finder.equals(playerA) || finder.equals(playerB)) {
                circleColor = Color.web("#ff3838");
            } else {
                circleColor = Color.ORANGE;
            }
            
            g.setStroke(circleColor.deriveColor(0, 1, 1, 0.15));
            g.setLineWidth(14);
            g.strokeOval(boxX1 + sx - rr - 5, boxY + sy - rr - 5, rr*2 + 10, rr*2 + 10);
            g.strokeOval(boxX2 + sx - rr - 5, boxY + sy - rr - 5, rr*2 + 10, rr*2 + 10);
            
            g.setStroke(circleColor.deriveColor(0, 1, 1, 0.4));
            g.setLineWidth(8);
            g.strokeOval(boxX1 + sx - rr - 2, boxY + sy - rr - 2, rr*2 + 4, rr*2 + 4);
            g.strokeOval(boxX2 + sx - rr - 2, boxY + sy - rr - 2, rr*2 + 4, rr*2 + 4);
            
            g.setStroke(circleColor);
            g.setLineWidth(5);
            g.strokeOval(boxX1 + sx - rr, boxY + sy - rr, rr*2, rr*2);
            g.strokeOval(boxX2 + sx - rr, boxY + sy - rr, rr*2, rr*2);
            g.setLineWidth(1);
        }
        
        if (lastClickX != null && lastClickY != null) {
            double sx = lastClickX * (boxSize / imgW);
            double sy = lastClickY * (boxSize / imgH);
            g.setStroke(Color.YELLOW);
            g.setLineWidth(3);
            double crossSize = 12;
            g.strokeLine(boxX1 + sx - crossSize, boxY + sy - crossSize, boxX1 + sx + crossSize, boxY + sy + crossSize);
            g.strokeLine(boxX1 + sx + crossSize, boxY + sy - crossSize, boxX1 + sx - crossSize, boxY + sy + crossSize);
            g.strokeLine(boxX2 + sx - crossSize, boxY + sy - crossSize, boxX2 + sx + crossSize, boxY + sy + crossSize);
            g.strokeLine(boxX2 + sx + crossSize, boxY + sy - crossSize, boxX2 + sx - crossSize, boxY + sy + crossSize);
            g.setLineWidth(1);
        }
        
        String myScore = "", opponentScore = "";
        if (playerA.equals(myUsername)) {
            myScore = playerA + ": " + scoreA;
            opponentScore = playerB + ": " + scoreB;
        } else if (playerB.equals(myUsername)) {
            myScore = playerB + ": " + scoreB;
            opponentScore = playerA + ": " + scoreA;
        } else {
            myScore = "Bạn: ?";
            opponentScore = "Đối thủ: ?";
        }
        
        String scoreText = String.format("👤 %s  |  🎯 %s", myScore, opponentScore);
        scoreLabel.setText(scoreText);
    }

    public BorderPane getRoot() { 
        return root; 
    }

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
}

