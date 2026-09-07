package org.apollo.mobile.auth.error;

public final class AuthError {

    public enum Type {
        VALIDATION,
        INVALID_CREDENTIALS,
        NETWORK,
        TIMEOUT,
        SERVER,
        INVALID_RESPONSE,
        EXPIRED_TOKEN,
        STORAGE
    }

    private final Type type;

    public AuthError(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
