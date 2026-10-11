package org.apollo.mobile.settings.gateway;

public final class SessionInfo {
    private String id;
    private String deviceName;
    private boolean current;
    private String firstLoginAt;
    private String lastLoginAt;
    private String lastSeenAt;

    public String getId() {
        return id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isCurrent() {
        return current;
    }

    public String getFirstLoginAt() {
        return firstLoginAt;
    }

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public String getLastSeenAt() {
        return lastSeenAt;
    }
}
