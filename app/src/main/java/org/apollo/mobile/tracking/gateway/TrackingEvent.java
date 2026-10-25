package org.apollo.mobile.tracking.gateway;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TrackingEvent {
    @SerializedName("event_id") public final String eventId;
    @SerializedName("event_name") public final String eventName;
    @SerializedName("session_id") public final String sessionId;
    public final String screen;
    public final String timestamp;
    public final String platform = "android";
    public final Map<String, String> properties;

    public TrackingEvent(String name, String session, String screen, Map<String, String> properties) {
        this.eventId = UUID.randomUUID().toString();
        this.eventName = name;
        this.sessionId = UUID.fromString(session).toString();
        if (screen == null || !screen.matches("[A-Za-z][A-Za-z0-9_]{0,79}")) {
            throw new IllegalArgumentException("Invalid screen");
        }
        this.screen = screen;
        this.timestamp = Instant.ofEpochMilli(System.currentTimeMillis()).toString();
        this.properties = new HashMap<>(properties);
    }
}