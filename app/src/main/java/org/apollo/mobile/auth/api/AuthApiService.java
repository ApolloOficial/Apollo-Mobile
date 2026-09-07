package org.apollo.mobile.auth.api;

import org.apollo.mobile.auth.gateway.LoginRequest;
import org.apollo.mobile.auth.gateway.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
