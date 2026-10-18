package org.apollo.mobile.settings.gateway;

public final class ChangePasswordBody {
    private final String currentPassword;
    private final String newPassword;

    public ChangePasswordBody(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
