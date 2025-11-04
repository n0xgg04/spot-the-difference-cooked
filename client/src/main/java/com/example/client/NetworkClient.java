package com.example.client;

import com.example.shared.Message;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.Consumer;

public class NetworkClient {
    private static final Gson GSON = new Gson();
    private final Consumer<Message> onMessage;
    private PrintWriter out;
    private Socket socket;
    private BufferedReader reader;
    private final Map<String, Consumer<Message>> handlers = new ConcurrentHashMap<>();

    public NetworkClient(Consumer<Message> onMessage) { this.onMessage = onMessage; }

    public void connect() {
        try {
            Properties props = new Properties();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("client-config.properties")) {
                if (in != null) props.load(in);
            }
            
            String host = System.getenv("SERVER_HOST");
            if (host == null) {
                host = props.getProperty("server.host", "127.0.0.1");
            }
            
            String portStr = System.getenv("SERVER_PORT");
            int port;
            if (portStr != null) {
                port = Integer.parseInt(portStr);
            } else {
                port = Integer.parseInt(props.getProperty("server.port", "5050"));
            }
            
            socket = new Socket(host, port);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            Thread t = new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Message msg = Message.fromJson(line);
                    Consumer<Message> handler = handlers.get(msg.type);
                    if (handler != null) {
                        handler.accept(msg);
                    } else {
                        onMessage.accept(msg);
                    }
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> System.err.println("Disconnected: "+e.getMessage()));
                }
            }, "net-reader");
            t.setDaemon(true); t.start();
        } catch (Exception e) {
            throw new RuntimeException("Cannot connect to server: "+e.getMessage(), e);
        }
    }

    public void send(Message msg) {
        out.println(msg.toJson());
    }

    public void addHandler(String type, Consumer<Message> handler) {
        handlers.put(type, handler);
    }

    public void disconnect() {
        try { if (reader != null) reader.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }
}
