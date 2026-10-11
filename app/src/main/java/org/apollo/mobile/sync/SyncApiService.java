package org.apollo.mobile.sync;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Url;

public interface SyncApiService {
    @POST
    Call<ResponseBody> post(@Url String url, @Body RequestBody body);

    @PUT
    Call<ResponseBody> put(@Url String url, @Body RequestBody body);

    @PATCH
    Call<ResponseBody> patch(@Url String url, @Body RequestBody body);
}
