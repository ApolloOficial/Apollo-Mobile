package org.apollo.mobile.tracking.gateway;

import org.apollo.mobile.tracking.api.TrackingApi;
import org.apollo.mobile.tracking.storage.TrackingStore;
import org.apollo.mobile.tracking.security.TrackingIdentity;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.apollo.mobile.session.EncryptedSessionStorage;
import org.apollo.mobile.session.UserSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TrackingDeliveryTest {
    private Context context;
    private EncryptedSessionStorage sessions;
    private MockWebServer server;

    @Before
    public void before() throws IOException {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        context.deleteDatabase("apollo_tracking.db");
        sessions = new EncryptedSessionStorage(context);
        sessions.clear();
        server = new MockWebServer();
        server.start();
    }

    @After
    public void after() throws IOException {
        sessions.clear();
        context.deleteDatabase("apollo_tracking.db");
        server.shutdown();
    }

    private UserSession session(UUID user) {
        String claims = "{\"userId\":\"" + user + "\",\"companyId\":10}";
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(claims.getBytes(StandardCharsets.UTF_8));
        return new UserSession("test." + payload + ".signature", "Bearer", "",
                System.currentTimeMillis() + 3600000);
    }

    private TrackingRepository repository(TrackingStore store) {
        TrackingApi api = new Retrofit.Builder().baseUrl(server.url("/"))
                .client(new OkHttpClient.Builder().retryOnConnectionFailure(false)
                        .callTimeout(3, TimeUnit.SECONDS).build())
                .addConverterFactory(GsonConverterFactory.create()).build().create(TrackingApi.class);
        return new TrackingRepository(store, sessions, api);
    }

    @Test
    public void retriesAfterConnectionLossWithSameIdAndRemovesAfterAck() throws Exception {
        UserSession session = session(UUID.randomUUID());
        sessions.save(session);
        String owner = TrackingIdentity.owner(session);
        TrackingEvent event = new TrackingEvent("screen_viewed", UUID.randomUUID().toString(),
                "HomeActivity", Map.of());
        try (TrackingStore store = new TrackingStore(context)) {
            store.insert(owner, event);
            TrackingRepository repository = repository(store);
            server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));
            try { repository.syncOneBatch(); } catch (IOException expected) { }
            assertEquals(event.eventId, store.pending(owner).get(0).eventId);
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                    .setBody("{\"accepted_event_ids\":[\"" + event.eventId + "\"]}"));
            assertEquals(TrackingRepository.Outcome.DONE, repository.syncOneBatch());
            assertTrue(store.pending(owner).isEmpty());
        }
    }

    @Test
    public void accountBCannotSendAccountAQueue() throws Exception {
        UserSession a = session(UUID.randomUUID());
        UserSession b = session(UUID.randomUUID());
        sessions.save(b);
        try (TrackingStore store = new TrackingStore(context)) {
            store.insert(TrackingIdentity.owner(a), new TrackingEvent("screen_viewed",
                    UUID.randomUUID().toString(), "HomeActivity", Map.of()));
            assertEquals(TrackingRepository.Outcome.DONE, repository(store).syncOneBatch());
            assertEquals(0, server.getRequestCount());
            assertEquals(1, store.pending(TrackingIdentity.owner(a)).size());
        }
    }

    @Test
    public void unauthorizedKeepsEventsAndDoesNotEraseBusinessSession() throws Exception {
        UserSession session = session(UUID.randomUUID());
        sessions.save(session);
        String owner = TrackingIdentity.owner(session);
        try (TrackingStore store = new TrackingStore(context)) {
            store.insert(owner, new TrackingEvent("screen_viewed", UUID.randomUUID().toString(),
                    "HomeActivity", Map.of()));
            server.enqueue(new MockResponse().setResponseCode(401));
            assertEquals(TrackingRepository.Outcome.AUTH_REQUIRED, repository(store).syncOneBatch());
            assertEquals(1, store.pending(owner).size());
            assertNotNull(sessions.read());
        }
    }
}
