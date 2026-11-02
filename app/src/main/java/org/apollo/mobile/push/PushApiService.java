package org.apollo.mobile.push;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Query;

interface PushApiService {

    @POST("api/v1/devices/push-tokens")
    Call<Void> register(@Body PushTokenRequest body);

    @DELETE("api/v1/devices/push-tokens")
    Call<Void> revoke(@Query("token") String token);
}
