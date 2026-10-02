package org.apollo.mobile.auth.error;

public final class AuthError {
    public enum Type {
        VALIDATION,
        INVALID_CREDENTIALS,
        LOCKED,
        NETWORK,
        TIMEOUT,
        SERVER,
        INVALID_RESPONSE,
        EXPIRED_TOKEN,
        STORAGE,
        OTP_INVALID,
        OTP_EXPIRED,
        OTP_TOO_MANY_ATTEMPTS,
        OTP_RESEND_TOO_SOON,
        RESET_REJECTED
    }

    private final Type type;

    public AuthError(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
