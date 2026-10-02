package org.apollo.mobile.auth.gateway;

import java.util.Collections;
import java.util.List;

/**
 * Resposta de POST /api/v1/mobile/auth/login.
 * status = AUTHENTICATED (token, tokenType e role preenchidos) ou
 * OTP_REQUIRED (challengeId e methods preenchidos; falta confirmar o código).
 */
public final class LoginResponse {

    private static final String STATUS_OTP_REQUIRED = "OTP_REQUIRED";

    private String status;
    private String token;
    private String tokenType;
    private String role;
    private String challengeId;
    private List<OtpMethod> methods;

    public LoginResponse() {
    }

    LoginResponse(String token, String tokenType, String role) {
        this.token = token;
        this.tokenType = tokenType;
        this.role = role;
    }

    public boolean isOtpRequired() {
        return STATUS_OTP_REQUIRED.equalsIgnoreCase(status);
    }

    public String getStatus() {
        return status;
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

    public String getChallengeId() {
        return challengeId;
    }

    public List<OtpMethod> getMethods() {
        return methods == null ? Collections.emptyList() : methods;
    }
}
