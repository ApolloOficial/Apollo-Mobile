package org.apollo.mobile.auth.gateway;

import org.apollo.mobile.auth.error.AuthError;

public interface AuthenticationGateway {
    void login(String email, String password, Callback<LoginResponse> callback);

    void startRecovery(String email, Callback<RecoveryResponse> callback);

    void sendOtp(String challengeId, String method, Callback<OtpSentResponse> callback);

    void verifyOtp(String challengeId, String code, Callback<OtpVerifyResponse> callback);

    void resetPassword(String resetToken, String newPassword, Callback<Void> callback);

    interface Callback<T> {
        void onSuccess(T response);

        void onFailure(AuthError error);
    }
}
