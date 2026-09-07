package org.apollo.mobile.auth.gateway;

import org.apollo.mobile.auth.error.AuthError;
import org.apollo.mobile.auth.api.AuthApiService;

import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Response;

public final class RetrofitAuthenticationGateway implements AuthenticationGateway {

    private final AuthApiService authApiService;

    public RetrofitAuthenticationGateway(AuthApiService authApiService) {
        this.authApiService = authApiService;
    }

    @Override
    public void login(String email, String password, Callback callback) {
        try {
            authApiService.login(new LoginRequest(email, password)).enqueue(new retrofit2.Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onSuccess(response.body());
                        return;
                    }

                    callback.onFailure(mapHttpError(response.code()));
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable throwable) {
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

    private AuthError mapHttpError(int statusCode) {
        if (statusCode == 401) {
            return new AuthError(AuthError.Type.INVALID_CREDENTIALS);
        }
        if (statusCode >= 500) {
            return new AuthError(AuthError.Type.SERVER);
        }
        return new AuthError(AuthError.Type.INVALID_RESPONSE);
    }
}
