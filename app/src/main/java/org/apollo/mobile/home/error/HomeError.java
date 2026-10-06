package org.apollo.mobile.home.error;

public final class HomeError {

    public enum Type {
        UNAUTHORIZED,
        NETWORK,
        TIMEOUT,
        SERVER,
        INVALID_RESPONSE
    }

    private final Type type;

    public HomeError(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}