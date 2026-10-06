package org.apollo.mobile.home.api;

import org.apollo.mobile.home.gateway.HomeResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HomeApiService {

    @GET("api/v1/mobile/home")
    Call<HomeResponse> getHome();
}