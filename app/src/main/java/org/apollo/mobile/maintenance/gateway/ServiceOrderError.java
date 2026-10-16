package org.apollo.mobile.maintenance.gateway;

public final class ServiceOrderError {
    public enum Type {
        UNAUTHORIZED,
        NOT_FOUND,
        INVALID_RESPONSE,
        NETWORK,
        SERVER
    }

    private final Type type;

    public ServiceOrderError(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
