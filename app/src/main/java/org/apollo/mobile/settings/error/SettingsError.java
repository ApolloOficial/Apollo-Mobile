package org.apollo.mobile.settings.error;

public final class SettingsError {
    public enum Type {
        NETWORK,
        TIMEOUT,
        SESSION_EXPIRED,
        FORBIDDEN,
        NOT_FOUND,
        INVALID_REQUEST,
        UNAVAILABLE,
        SERVER,
        INVALID_RESPONSE
    }

    private final Type type;

    public SettingsError(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
