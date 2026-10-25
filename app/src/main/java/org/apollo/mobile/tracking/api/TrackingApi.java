package org.apollo.mobile.tracking.api;

import org.apollo.mobile.tracking.gateway.TrackingEvent;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface TrackingApi {
    @POST("api/tracking/events/batch")
    Call<BatchResponse> send(@Header("Authorization") String authorization, @Body Batch batch);

    final class Batch {
        public final List<TrackingEvent> events;
        public Batch(List<TrackingEvent> events) { this.events = events; }
    }

    final class BatchResponse {
        @SerializedName("accepted_event_ids") public List<String> acceptedEventIds;
    }
}