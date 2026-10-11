package org.apollo.mobile.settings.gateway;

public final class SettingsSummary {
    private String userId;
    private String fullName;
    private String email;
    private String role;
    private String roleLabel;
    private String unitName;
    private boolean hasPhoto;
    private String photoUrl;
    private String language;

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getRoleLabel() {
        return roleLabel;
    }

    public String getUnitName() {
        return unitName;
    }

    public boolean hasPhoto() {
        return hasPhoto;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getLanguage() {
        return language;
    }
}
