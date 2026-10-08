package org.apollo.mobile.chat.gateway;

public final class ChatMessageRequest {
    private final String sessionId;
    private final String message;

    public ChatMessageRequest(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMessage() {
        return message;
    }
}
