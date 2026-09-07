package org.apollo.mobile.auth.api;

import android.content.Context;

import org.apollo.mobile.BuildConfig;
import org.apollo.mobile.auth.securty.AuthInterceptor;
import org.apollo.mobile.session.EncryptedSessionStorage;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class AuthApiClient {

    private static final long NETWORK_TIMEOUT_SECONDS = 20;

    private AuthApiClient() {
    }

    public static AuthApiService createAuthService(Context context) {
        String baseUrl = BuildConfig.API_BASE_URL;
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("API_BASE_URL precisa estar configurada");
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        if (!BuildConfig.DEBUG && !baseUrl.startsWith("https://")) {
            throw new IllegalStateException("Build requer HTTPS API_BASE_URL");
        }

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(new EncryptedSessionStorage(context)))
                .addInterceptor(loggingInterceptor)
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApiService.class);
    }
}
