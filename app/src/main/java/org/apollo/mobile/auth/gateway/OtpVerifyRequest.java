package org.apollo.mobile.auth.gateway;

public final class OtpVerifyRequest {

    private final String challengeId;
    private final String code;

    public OtpVerifyRequest(String challengeId, String code) {
        this.challengeId = challengeId;
        this.code = code;
    }
}
