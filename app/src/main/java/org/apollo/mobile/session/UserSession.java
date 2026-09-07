package org.apollo.mobile.session;

import org.apollo.mobile.auth.policy.UserRole;

public final class UserSession {

    private final String accessToken;
    private final String tokenType;
    private final String email;
    private final long expiresAtEpochMillis;
    private final UserRole role;

    public UserSession(String accessToken, String tokenType, String email, long expiresAtEpochMillis) {
        this(accessToken, tokenType, email, expiresAtEpochMillis, UserRole.UNKNOWN);
    }

    public UserSession(
            String accessToken,
            String tokenType,
            String email,
            long expiresAtEpochMillis,
            UserRole role
    ) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.email = email;
        this.expiresAtEpochMillis = expiresAtEpochMillis;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getEmail() {
        return email;
    }

    public long getExpiresAtEpochMillis() {
        return expiresAtEpochMillis;
    }

    public UserRole getRole() {
        return role == null ? UserRole.UNKNOWN : role;
    }

    public boolean isExpired(long nowEpochMillis) {
        return expiresAtEpochMillis <= nowEpochMillis;
    }
}
