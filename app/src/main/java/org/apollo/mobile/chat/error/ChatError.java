package org.apollo.mobile.chat.error;

public final class ChatError {
    public enum Type {
        NETWORK,
        TIMEOUT,
        RATE_LIMITED,
        SESSION_EXPIRED,
        FORBIDDEN,
        INVALID_REQUEST,
        UNAVAILABLE,
        SERVER,
        INVALID_RESPONSE
    }

    private final Type type;

    public ChatError(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
