package org.apollo.mobile.panels.error;

public final class PanelsError {

    public enum Type {
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        INVALID_REQUEST,
        NETWORK,
        TIMEOUT,
        SERVER,
        INVALID_RESPONSE
    }

    private final Type type;

    public PanelsError(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public static PanelsError fromStatus(int statusCode) {
        if (statusCode == 401) {
            return new PanelsError(Type.UNAUTHORIZED);
        }
        if (statusCode == 403) {
            return new PanelsError(Type.FORBIDDEN);
        }
        if (statusCode == 404) {
            return new PanelsError(Type.NOT_FOUND);
        }
        if (statusCode == 400 || statusCode == 409 || statusCode == 422) {
            return new PanelsError(Type.INVALID_REQUEST);
        }
        if (statusCode >= 500) {
            return new PanelsError(Type.SERVER);
        }
        return new PanelsError(Type.INVALID_RESPONSE);
    }
}
