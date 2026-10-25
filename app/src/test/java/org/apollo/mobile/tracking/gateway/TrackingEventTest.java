package org.apollo.mobile.tracking.gateway;

import org.apollo.mobile.tracking.TrackingEvents;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import java.util.Map;
import java.util.UUID;
import static org.junit.Assert.*;

public class TrackingEventTest {
    @Test
    public void serializesStableIdAndUtcTimestampWithoutIdentityOrSecret() {
        TrackingEvent event = new TrackingEvent(TrackingEvents.WORK_ORDER_VIEWED,
                UUID.randomUUID().toString(), "OsDetailsActivity", Map.of("work_order_id", "3"));
        Gson gson = new Gson();
        String json = gson.toJson(event);
        TrackingEvent restored = gson.fromJson(json, TrackingEvent.class);
        assertEquals(event.eventId, restored.eventId);
        assertEquals(event.timestamp, restored.timestamp);
        assertTrue(event.timestamp.endsWith("Z"));
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        assertEquals(event.eventId, object.get("event_id").getAsString());
        assertFalse(object.has("user_id"));
        assertFalse(object.has("token"));
        assertFalse(object.has("received_at"));
    }

    @Test
    public void distinctActionsHaveDifferentIds() {
        String session = UUID.randomUUID().toString();
        TrackingEvent a = new TrackingEvent("screen_viewed", session, "HomeActivity", Map.of());
        TrackingEvent b = new TrackingEvent("screen_viewed", session, "HomeActivity", Map.of());
        assertNotEquals(a.eventId, b.eventId);
    }
}
