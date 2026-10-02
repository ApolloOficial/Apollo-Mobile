package org.apollo.mobile.auth.api;

import org.apollo.mobile.auth.gateway.LoginRequest;
import org.apollo.mobile.auth.gateway.LoginResponse;
import org.apollo.mobile.auth.gateway.OtpSendRequest;
import org.apollo.mobile.auth.gateway.OtpSentResponse;
import org.apollo.mobile.auth.gateway.OtpVerifyRequest;
import org.apollo.mobile.auth.gateway.OtpVerifyResponse;
import org.apollo.mobile.auth.gateway.PasswordResetRequest;
import org.apollo.mobile.auth.gateway.RecoveryRequest;
import org.apollo.mobile.auth.gateway.RecoveryResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    // Compatível com a API publicada hoje (main). Quando o módulo mobile do back entrar,
    // trocar para "api/v1/mobile/auth/login" (traz 2FA e bloqueio de conta).
    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/v1/mobile/auth/recovery")
    Call<RecoveryResponse> startRecovery(@Body RecoveryRequest request);

    @POST("api/v1/mobile/auth/otp/send")
    Call<OtpSentResponse> sendOtp(@Body OtpSendRequest request);

    @POST("api/v1/mobile/auth/otp/verify")
    Call<OtpVerifyResponse> verifyOtp(@Body OtpVerifyRequest request);

    @POST("api/v1/mobile/auth/password/reset")
    Call<Void> resetPassword(@Body PasswordResetRequest request);
}
