package org.apollo.mobile.notifications;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApiService {

    @GET("api/v1/notifications")
    Call<List<NotificationItem>> list(@Query("unread") boolean unread,
                                      @Query("page") int page,
                                      @Query("size") int size);

    @GET("api/v1/notifications/unread-count")
    Call<Map<String, Long>> unreadCount();

    @PATCH("api/v1/notifications/{id}/read")
    Call<Void> markRead(@Path("id") String id);

    @PATCH("api/v1/notifications/read-all")
    Call<Map<String, Integer>> markAllRead();
}
