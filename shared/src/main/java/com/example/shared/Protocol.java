package com.example.shared;

public final class Protocol {
    private Protocol() {}

    // Message types
    public static final String AUTH_LOGIN = "auth/login"; // payload: {username, password}
    public static final String AUTH_RESULT = "auth/result"; // payload: {success, message, user}

    public static final String ERROR = "error"; // payload: {message}

    public static final String LOBBY_LIST = "lobby/list"; // payload: [UserStatus]
    public static final String INVITE_SEND = "invite/send"; // payload: {toUser}
    public static final String INVITE_RECEIVED = "invite/received"; // payload: {fromUser}
    public static final String INVITE_RESPONSE = "invite/response"; // payload: {fromUser, accepted}

    public static final String GAME_START = "game/start"; // payload: {roomId, imageSetId, imageWidth, imageHeight, imgLeft(b64), imgRight(b64), differences:[{x,y,radius}], currentTurn}
    public static final String GAME_CLICK = "game/click"; // payload: {roomId, x, y}
    public static final String GAME_UPDATE = "game/update"; // payload: {scores, found, lastFinder, nextTurn, remainingTurnMs}
    public static final String GAME_END = "game/end"; // payload: {reason, scores, result}
    public static final String GAME_QUIT = "game/quit"; // payload: {roomId}

    public static final String TIMER_TICK = "timer/tick"; // payload: {remainingTurnMs}

    public static final String LEADERBOARD = "leaderboard/data"; // payload: [LeaderboardEntry]
}
