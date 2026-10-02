package org.apollo.mobile.auth.gateway;

/**
 * Resposta de POST /otp/verify. No login vem token/tokenType/role;
 * na recuperação de senha vem resetToken.
 */
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
