package org.apollo.mobile.auth.gateway;

import android.util.Log;

import org.apollo.mobile.auth.api.AuthApiService;
import org.apollo.mobile.auth.error.AuthError;

import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Response;

public final class RetrofitAuthenticationGateway implements AuthenticationGateway {

    private static final String TAG = "ApolloAuth";

    private final AuthApiService authApiService;

    public RetrofitAuthenticationGateway(AuthApiService authApiService) {
        this.authApiService = authApiService;
    }

    @Override
    public void login(String email, String password, Callback<LoginResponse> callback) {
        enqueue(() -> authApiService.login(new LoginRequest(email, password)),
                callback, false, RetrofitAuthenticationGateway::mapLoginError);
    }

    @Override
    public void startRecovery(String email, Callback<RecoveryResponse> callback) {
        enqueue(() -> authApiService.startRecovery(new RecoveryRequest(email)),
                callback, false, RetrofitAuthenticationGateway::mapGenericError);
    }

    @Override
    public void sendOtp(String challengeId, String method, Callback<OtpSentResponse> callback) {
        enqueue(() -> authApiService.sendOtp(new OtpSendRequest(challengeId, method)),
                callback, false, RetrofitAuthenticationGateway::mapSendError);
    }

    @Override
    public void verifyOtp(String challengeId, String code, Callback<OtpVerifyResponse> callback) {
        enqueue(() -> authApiService.verifyOtp(new OtpVerifyRequest(challengeId, code)),
                callback, false, RetrofitAuthenticationGateway::mapVerifyError);
    }

    @Override
    public void resetPassword(String resetToken, String newPassword, Callback<Void> callback) {
        enqueue(() -> authApiService.resetPassword(new PasswordResetRequest(resetToken, newPassword)),
                callback, true, RetrofitAuthenticationGateway::mapResetError);
    }

    private interface CallFactory<T> {
        Call<T> create();
    }

    private interface ErrorMapper {
        AuthError map(int statusCode);
    }

    private <T> void enqueue(CallFactory<T> factory, Callback<T> callback, boolean emptyBodyAllowed,
                             ErrorMapper errorMapper) {
        try {
            factory.create().enqueue(new retrofit2.Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null || emptyBodyAllowed) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onFailure(new AuthError(AuthError.Type.INVALID_RESPONSE));
                        }
                        return;
                    }
                    callback.onFailure(errorMapper.map(response.code()));
                }

                @Override
                public void onFailure(Call<T> call, Throwable throwable) {
                    // Sem isso, "sem rede", "resposta fora do formato" e "sem permissão" parecem iguais na tela.
                    Log.w(TAG, "Falha na chamada " + call.request().url().encodedPath(), throwable);
                    AuthError.Type type = throwable instanceof SocketTimeoutException
                            ? AuthError.Type.TIMEOUT
                            : AuthError.Type.NETWORK;
                    callback.onFailure(new AuthError(type));
                }
            });
        } catch (RuntimeException exception) {
            callback.onFailure(new AuthError(AuthError.Type.NETWORK));
        }
    }

    private static AuthError mapLoginError(int statusCode) {
        if (statusCode == 401) {
            return new AuthError(AuthError.Type.INVALID_CREDENTIALS);
        }
        if (statusCode == 423) {
            return new AuthError(AuthError.Type.LOCKED);
        }
        return mapGenericError(statusCode);
    }

    private static AuthError mapSendError(int statusCode) {
        if (statusCode == 429) {
            return new AuthError(AuthError.Type.OTP_RESEND_TOO_SOON);
        }
        if (statusCode == 404 || statusCode == 410) {
            // Desafio inexistente ou vencido: o fluxo precisa recomeçar.
            return new AuthError(AuthError.Type.OTP_EXPIRED);
        }
        return mapGenericError(statusCode);
    }

    private static AuthError mapVerifyError(int statusCode) {
        if (statusCode == 400) {
            return new AuthError(AuthError.Type.OTP_INVALID);
        }
        if (statusCode == 410 || statusCode == 404) {
            return new AuthError(AuthError.Type.OTP_EXPIRED);
        }
        if (statusCode == 429) {
            return new AuthError(AuthError.Type.OTP_TOO_MANY_ATTEMPTS);
        }
        return mapGenericError(statusCode);
    }

    private static AuthError mapResetError(int statusCode) {
        if (statusCode == 400 || statusCode == 404 || statusCode == 410) {
            return new AuthError(AuthError.Type.RESET_REJECTED);
        }
        return mapGenericError(statusCode);
    }

    private static AuthError mapGenericError(int statusCode) {
        if (statusCode >= 500) {
            return new AuthError(AuthError.Type.SERVER);
        }
        return new AuthError(AuthError.Type.INVALID_RESPONSE);
    }
}
