package com.ltm.game.server;

import com.ltm.game.shared.Message;
import com.ltm.game.shared.Protocol;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.*;

public class QueueService {
    private final GameService gameService;
    private final LobbyService lobbyService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public QueueService(GameService gameService, LobbyService lobbyService) {
        this.gameService = gameService;
        this.lobbyService = lobbyService;
        startMatchmaking();
    }

    public void joinQueue(String username) {
        try (Connection c = Database.getConnection()) {
            // Remove any existing entry
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM matchmaking_queue WHERE username = ?")) {
                ps.setString(1, username);
                ps.executeUpdate();
            }

            // Add to queue
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO matchmaking_queue (username, status) VALUES (?, 'waiting')")) {
                ps.setString(1, username);
                ps.executeUpdate();
            }

            System.out.println("[QUEUE] " + username + " joined queue");
            tryMatchmaking();
        } catch (Exception e) {
            System.err.println("[QUEUE] Error joining: " + e.getMessage());
        }
    }

    public void leaveQueue(String username) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM matchmaking_queue WHERE username = ?")) {
            ps.setString(1, username);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[QUEUE] " + username + " left queue");
            }
        } catch (Exception e) {
            System.err.println("[QUEUE] Error leaving: " + e.getMessage());
        }
    }

    public Map<String, Object> getQueueStatus(String username) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT join_time, TIMESTAMPDIFF(SECOND, join_time, NOW()) as wait_seconds " +
                     "FROM matchmaking_queue WHERE username = ? AND status = 'waiting'")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int waitSeconds = rs.getInt("wait_seconds");
                    return Map.of(
                            "inQueue", true,
                            "waitSeconds", waitSeconds
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("[QUEUE] Error getting status: " + e.getMessage());
        }
        return Map.of("inQueue", false);
    }

    private void startMatchmaking() {
        scheduler.scheduleAtFixedRate(this::tryMatchmaking, 1, 1, TimeUnit.SECONDS);
    }

    private void tryMatchmaking() {
        try (Connection c = Database.getConnection()) {
            // Get 2 waiting players
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT username FROM matchmaking_queue WHERE status = 'waiting' ORDER BY join_time LIMIT 2")) {
                try (ResultSet rs = ps.executeQuery()) {
                    String player1 = null;
                    String player2 = null;

                    if (rs.next()) {
                        player1 = rs.getString("username");
                    }
                    if (rs.next()) {
                        player2 = rs.getString("username");
                    }

                    if (player1 != null && player2 != null) {
                        matchPlayers(c, player1, player2);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[QUEUE] Matchmaking error: " + e.getMessage());
        }
    }

    private void matchPlayers(Connection c, String player1, String player2) throws Exception {
        // Mark as matched
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE matchmaking_queue SET status = 'matched' WHERE username IN (?, ?)")) {
            ps.setString(1, player1);
            ps.setString(2, player2);
            ps.executeUpdate();
        }

        System.out.println("[QUEUE] Matched: " + player1 + " vs " + player2);

        // Notify both players
        ClientSession s1 = lobbyService.getOnline(player1);
        ClientSession s2 = lobbyService.getOnline(player2);
        
        if (s1 != null) {
            s1.send(new Message(Protocol.QUEUE_MATCHED, Map.of("opponent", player2)).toJson());
        }
        if (s2 != null) {
            s2.send(new Message(Protocol.QUEUE_MATCHED, Map.of("opponent", player1)).toJson());
        }

        // Start game after small delay
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                gameService.startGame(player1, player2);
            } catch (Exception e) {
                System.err.println("[QUEUE] Error starting game: " + e.getMessage());
            }
        }).start();

        // Remove from queue
        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM matchmaking_queue WHERE username IN (?, ?)")) {
            ps.setString(1, player1);
            ps.setString(2, player2);
            ps.executeUpdate();
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}

