package org.apollo.mobile.tracking.sync;

import org.apollo.mobile.tracking.gateway.TrackingEvent;
import org.apollo.mobile.tracking.storage.TrackingStore;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.ListenableWorker;
import androidx.work.testing.TestWorkerBuilder;
import org.apollo.mobile.session.EncryptedSessionStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TrackingWorkerTest {
    @Test
    public void noLoginDoesNotDeleteOrRetryForever() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        context.deleteDatabase("apollo_tracking.db");
        new EncryptedSessionStorage(context).clear();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (TrackingStore store = new TrackingStore(context)) {
            store.insert("old-account", new TrackingEvent("screen_viewed",
                    UUID.randomUUID().toString(), "HomeActivity", Map.of()));
            TrackingSyncWorker worker = TestWorkerBuilder.from(context,
                    TrackingSyncWorker.class, executor).build();
            assertEquals(ListenableWorker.Result.success(), worker.doWork());
            assertEquals(1, store.pending("old-account").size());
        } finally {
            executor.shutdownNow();
            context.deleteDatabase("apollo_tracking.db");
        }
    }
}
