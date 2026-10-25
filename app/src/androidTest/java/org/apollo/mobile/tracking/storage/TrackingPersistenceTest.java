package org.apollo.mobile.tracking.storage;

import org.apollo.mobile.tracking.gateway.TrackingEvent;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TrackingPersistenceTest {
    private Context context;

    @Before
    public void before() {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        context.deleteDatabase("apollo_tracking.db");
    }

    @After
    public void after() { context.deleteDatabase("apollo_tracking.db"); }

    @Test
    public void persistsAcrossReopenAndAcknowledgesOnlySpecifiedOwner() {
        TrackingEvent a = new TrackingEvent("screen_viewed", UUID.randomUUID().toString(),
                "HomeActivity", Map.of());
        TrackingEvent b = new TrackingEvent("screen_viewed", UUID.randomUUID().toString(),
                "HomeActivity", Map.of());
        try (TrackingStore store = new TrackingStore(context)) {
            store.insert("account-a", a);
            store.insert("account-a", a);
            store.insert("account-b", b);
        }
        try (TrackingStore store = new TrackingStore(context)) {
            assertEquals(1, store.pending("account-a").size());
            assertEquals(a.eventId, store.pending("account-a").get(0).eventId);
            store.acknowledge("account-b", List.of(a.eventId));
            assertEquals(1, store.pending("account-a").size());
            store.quarantine("account-a", store.pending("account-a"));
            assertTrue(store.pending("account-a").isEmpty());
            assertEquals(1, store.pending("account-b").size());
            store.acknowledge("account-b", List.of(b.eventId));
            assertTrue(store.pending("account-b").isEmpty());
        }
    }
}
