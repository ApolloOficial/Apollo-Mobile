package org.apollo.mobile.notifications;

import android.content.Context;

import org.apollo.mobile.api.ApiClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class NotificationRepository {

    public interface Result<T> {
        void onSuccess(T value);
        void onFailure();
    }

    private final NotificationApiService api;

    public NotificationRepository(Context context) {
        this.api = ApiClient.createService(context.getApplicationContext(), NotificationApiService.class);
    }

    public void list(boolean unreadOnly, Result<List<NotificationItem>> result) {
        api.list(unreadOnly, 0, 50).enqueue(new Callback<List<NotificationItem>>() {
            @Override
            public void onResponse(Call<List<NotificationItem>> call, Response<List<NotificationItem>> response) {
                if (!response.isSuccessful()) {
                    result.onFailure();
                    return;
                }
                List<NotificationItem> body = response.body();
                result.onSuccess(body == null ? Collections.emptyList() : body);
            }

            @Override
            public void onFailure(Call<List<NotificationItem>> call, Throwable t) {
                result.onFailure();
            }
        });
    }

    public void unreadCount(Result<Long> result) {
        api.unreadCount().enqueue(new Callback<Map<String, Long>>() {
            @Override
            public void onResponse(Call<Map<String, Long>> call, Response<Map<String, Long>> response) {
                Map<String, Long> body = response.body();
                if (!response.isSuccessful() || body == null) {
                    result.onFailure();
                    return;
                }
                Long unread = body.get("unread");
                result.onSuccess(unread == null ? 0L : unread);
            }

            @Override
            public void onFailure(Call<Map<String, Long>> call, Throwable t) {
                result.onFailure();
            }
        });
    }

    public void markRead(String id) {
        if (id == null) return;
        api.markRead(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) { }

            @Override
            public void onFailure(Call<Void> call, Throwable t) { }
        });
    }

    public void markAllRead(Runnable done) {
        api.markAllRead().enqueue(new Callback<Map<String, Integer>>() {
            @Override
            public void onResponse(Call<Map<String, Integer>> call, Response<Map<String, Integer>> response) {
                done.run();
            }

            @Override
            public void onFailure(Call<Map<String, Integer>> call, Throwable t) {
                done.run();
            }
        });
    }
}
