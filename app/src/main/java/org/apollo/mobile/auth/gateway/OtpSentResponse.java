package org.apollo.mobile.auth.gateway;

/** Resposta de POST /otp/send: para onde foi e quanto tempo falta para poder reenviar. */
public final class OtpSentResponse {

    private String destination;
    private int resendInSeconds;
    private int expiresInSeconds;

    public OtpSentResponse() {
    }

    public String getDestination() {
        return destination;
    }

    public int getResendInSeconds() {
        return resendInSeconds;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
