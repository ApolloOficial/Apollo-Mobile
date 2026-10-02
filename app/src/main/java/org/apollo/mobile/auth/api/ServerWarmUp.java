package org.apollo.mobile.auth.api;

import org.apollo.mobile.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Acorda a API assim que o app abre. Em hospedagem gratuita (Render) o servidor dorme e a primeira
 * chamada pode levar mais de 30 s; disparando um ping na abertura, ele já está de pé quando o usuário
 * chega ao login. O resultado é ignorado de propósito.
 */
public final class ServerWarmUp {

    private static final long TIMEOUT_SECONDS = 90;

    private ServerWarmUp() {
    }

    public static void start() {
        String baseUrl = BuildConfig.API_BASE_URL;
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .callTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder().url(baseUrl + "v3/api-docs").get().build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    // Ignorado: é só para acordar o servidor.
                }

                @Override
                public void onResponse(Call call, Response response) {
                    response.close();
                }
            });
        } catch (RuntimeException ignored) {
            // URL inválida etc.: o login mostrará o erro de verdade.
        }
    }
}
