package org.apollo.mobile.auth.gateway;

public final class PasswordResetRequest {
    private final String resetToken;
    private final String newPassword;

    public PasswordResetRequest(String resetToken, String newPassword) {
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }
}
