package org.apollo.mobile.auth.gateway;

public final class OtpVerifyResponse {
    private String token;
    private String tokenType;
    private String role;
    private String resetToken;

    public OtpVerifyResponse() {
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getRole() {
        return role;
    }

    public String getResetToken() {
        return resetToken;
    }
}
