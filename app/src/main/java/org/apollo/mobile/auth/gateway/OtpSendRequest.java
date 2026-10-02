package org.apollo.mobile.auth.gateway;

public final class OtpSendRequest {
    private final String challengeId;
    private final String method;

    public OtpSendRequest(String challengeId, String method) {
        this.challengeId = challengeId;
        this.method = method;
    }
}
