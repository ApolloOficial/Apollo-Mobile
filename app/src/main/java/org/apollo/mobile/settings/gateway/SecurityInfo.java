package org.apollo.mobile.settings.gateway;

public final class SecurityInfo {
    private String maskedPhone;
    private boolean phoneChangePending;
    private String passwordChangedAt;

    public String getMaskedPhone() {
        return maskedPhone;
    }

    public boolean isPhoneChangePending() {
        return phoneChangePending;
    }

    public String getPasswordChangedAt() {
        return passwordChangedAt;
    }
}
