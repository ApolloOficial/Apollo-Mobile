package org.apollo.mobile.push;

final class PushTokenRequest {
    private final String token;
    private final String platform;
    private final String deviceName;

    PushTokenRequest(String token, String platform, String deviceName) {
        this.token = token;
        this.platform = platform;
        this.deviceName = deviceName;
    }
}
