package org.apollo.mobile.auth.gateway;

public final class LoginResponse {

    private String token;
    private String tokenType;
    private String role;

    public LoginResponse() {
    }

    LoginResponse(String token, String tokenType, String role) {
        this.token = token;
        this.tokenType = tokenType;
        this.role = role;
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
}
