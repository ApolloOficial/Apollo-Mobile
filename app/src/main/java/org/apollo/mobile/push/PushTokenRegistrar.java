package org.apollo.mobile.push;

import android.content.Context;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessaging;

import org.apollo.mobile.api.ApiClient;
import org.apollo.mobile.session.EncryptedSessionStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class PushTokenRegistrar {
    private static final String PLATFORM = "ANDROID";
    private static final long REVOKE_TIMEOUT_SECONDS = 5;

    private PushTokenRegistrar() {
    }

    public static void sync(Context context) {
        Context app = context.getApplicationContext();
        if (!hasSession(app)) {
            return;
        }
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    return;
                }
                String token = task.getResult();
                if (token == null || token.isEmpty()) {
                    return;
                }
                register(app, token);
            });
        } catch (RuntimeException ignored) {
        }
    }

    static void register(Context context, String token) {
        Context app = context.getApplicationContext();
        if (!hasSession(app)) {
            return;
        }
        PushApiService api;
        try {
            api = ApiClient.createService(app, PushApiService.class);
        } catch (RuntimeException exception) {
            return;
        }
        PushTokenStore store = new PushTokenStore(app);
        api.register(new PushTokenRequest(token, PLATFORM, deviceName())).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    store.saveToken(token);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
            }
        });
    }

    public static void revoke(Context context, Runnable done) {
        Context app = context.getApplicationContext();
        PushTokenStore store = new PushTokenStore(app);
        String token = store.token();
        if (token == null || !hasSession(app)) {
            done.run();
            return;
        }
        PushApiService api;
        try {
            api = ApiClient.createService(app, PushApiService.class, REVOKE_TIMEOUT_SECONDS);
        } catch (RuntimeException exception) {
            done.run();
            return;
        }
        api.revoke(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                finish(store, done);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                finish(store, done);
            }
        });
    }

    private static void finish(PushTokenStore store, Runnable done) {
        store.clearToken();
        try {
            FirebaseMessaging.getInstance().deleteToken();
        } catch (RuntimeException ignored) {
        }
        done.run();
    }

    private static boolean hasSession(Context context) {
        return new EncryptedSessionStorage(context).read() != null;
    }

    private static String deviceName() {
        String name = (Build.MANUFACTURER + " " + Build.MODEL)
                .replaceAll("[^A-Za-z0-9 ._-]", "")
                .trim();
        if (name.isEmpty()) {
            name = "Android";
        }
        return name.length() > 60 ? name.substring(0, 60) : name;
    }
}
