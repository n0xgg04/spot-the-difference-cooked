package com.example.shared;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Message {
    private static final Gson GSON = new Gson();

    @SerializedName("type")
    public String type;

    @SerializedName("payload")
    public Object payload;

    public Message() {}

    public Message(String type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static Message fromJson(String json) {
        return GSON.fromJson(json, Message.class);
    }
}
