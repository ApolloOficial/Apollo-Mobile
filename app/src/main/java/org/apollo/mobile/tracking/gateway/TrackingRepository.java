package org.apollo.mobile.tracking.gateway;

import org.apollo.mobile.tracking.api.TrackingApi;
import org.apollo.mobile.tracking.storage.TrackingStore;
import org.apollo.mobile.tracking.security.TrackingIdentity;

import android.content.Context;
import org.apollo.mobile.BuildConfig;
import org.apollo.mobile.session.EncryptedSessionStorage;
import org.apollo.mobile.session.UserSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class TrackingRepository {
    public enum Outcome { DONE, MORE, RETRY, AUTH_REQUIRED }
    private final TrackingStore store;
    private final EncryptedSessionStorage sessions;
    private final TrackingApi api;

    public TrackingRepository(Context context, TrackingStore store) {
        this(store, new EncryptedSessionStorage(context), createApi());
    }

    TrackingRepository(TrackingStore store, EncryptedSessionStorage sessions, TrackingApi api) {
        this.store = store;
        this.sessions = sessions;
        this.api = api;
    }

    private static TrackingApi createApi() {
        String base = BuildConfig.API_BASE_URL;
        if (!BuildConfig.DEBUG && !base.startsWith("https://")) {
            throw new IllegalStateException("Tracking requires HTTPS");
        }
        OkHttpClient http = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS).build();
        return new Retrofit.Builder().baseUrl(base.endsWith("/") ? base : base + "/")
                .client(http).addConverterFactory(GsonConverterFactory.create())
                .build().create(TrackingApi.class);
    }

    public Outcome syncOneBatch() throws IOException {
        UserSession session = sessions.read();
        String owner = TrackingIdentity.owner(session);
        if (owner == null) {
            android.util.Log.i("ApolloTracking", "Sync paused: AUTH_REQUIRED");
            return Outcome.AUTH_REQUIRED;
        }
        List<TrackingEvent> batch = store.pending(owner);
        if (batch.isEmpty()) return Outcome.DONE;
        Response<TrackingApi.BatchResponse> response = api.send(
                session.getTokenType() + " " + session.getAccessToken(), new TrackingApi.Batch(batch)).execute();
        if (response.errorBody() != null) {
            String code = "HTTP_" + response.code();
            try (okhttp3.ResponseBody body = response.errorBody()) {
                String json = body.string();
                com.google.gson.JsonObject object = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                for (String key : new String[]{"message", "detail"}) {
                    if (object.has(key) && object.get(key).isJsonPrimitive()) {
                        String value = object.get(key).getAsString();
                        if (value.matches("[A-Z][A-Z0-9_]{2,79}")) code = value;
                    }
                }
            } catch (RuntimeException ignored) { }
            android.util.Log.w("ApolloTracking", "Sync rejected: status=" + response.code() + " code=" + code);
        }
        if (response.code() == 401 || response.code() == 403) return Outcome.AUTH_REQUIRED;
        if (response.code() == 400 || response.code() == 409
                || response.code() == 413 || response.code() == 422) {
            store.quarantine(owner, batch);
            return Outcome.MORE;
        }
        if (!response.isSuccessful() || response.body() == null
                || response.body().acceptedEventIds == null) return Outcome.RETRY;
        Set<String> sent = new HashSet<>();
        for (TrackingEvent event : batch) sent.add(event.eventId);
        List<String> confirmed = new ArrayList<>();
        for (String id : response.body().acceptedEventIds) if (sent.contains(id)) confirmed.add(id);
        if (confirmed.isEmpty()) return Outcome.RETRY;
        store.acknowledge(owner, confirmed);
        android.util.Log.i("ApolloTracking", "Sync acknowledged count=" + confirmed.size());
        return store.pending(owner).isEmpty() ? Outcome.DONE : Outcome.MORE;
    }
}
