package org.apollo.mobile.settings.gateway;

public final class ProfileInfo {
    private String fullName;
    private String email;
    private String companyName;
    private String unitName;
    private String role;
    private String roleLabel;
    private boolean hasPhoto;
    private String photoUrl;

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getRole() {
        return role;
    }

    public String getRoleLabel() {
        return roleLabel;
    }

    public boolean hasPhoto() {
        return hasPhoto;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
