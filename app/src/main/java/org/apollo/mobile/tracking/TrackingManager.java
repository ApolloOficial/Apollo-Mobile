package org.apollo.mobile.tracking;

import org.apollo.mobile.tracking.gateway.TrackingEvent;
import org.apollo.mobile.tracking.storage.TrackingStore;
import org.apollo.mobile.tracking.security.TrackingIdentity;
import org.apollo.mobile.tracking.session.TrackingSessionManager;
import org.apollo.mobile.tracking.sync.TrackingSyncWorker;
import org.apollo.mobile.tracking.lifecycle.TrackingLifecycle;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import org.apollo.mobile.session.EncryptedSessionStorage;
import org.apollo.mobile.session.UserSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.Request;

public final class TrackingManager {
    private static volatile TrackingManager instance;
    private static final Set<String> KEYS = Collections.unmodifiableSet(new java.util.HashSet<>(
            java.util.Arrays.asList("work_order_id", "maintenance_type_id", "action", "error_type", "input_method", "previous_screen", "duration_ms", "exit_reason", "form_id", "field", "rule", "target", "attempts", "filter_name", "filter_value", "query_length", "result_count", "state", "feedback_type", "permission", "granted", "http_status", "method", "endpoint", "error_code")));
    private final Context context;
    private final TrackingStore store;
    private final EncryptedSessionStorage authentication;
    private final TrackingSessionManager sessions = new TrackingSessionManager();
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private volatile Scope scope;

    private static final class Scope {
        final String owner;
        final String session;
        final String screen;
        Scope(String owner, String session, String screen) {
            this.owner = owner;
            this.session = session;
            this.screen = screen;
        }
    }

    private TrackingManager(Context context) {
        this.context = context.getApplicationContext();
        this.store = new TrackingStore(this.context);
        this.authentication = new EncryptedSessionStorage(this.context);
    }

    public static void install(Application application) {
        try {
            if (instance != null) return;
            synchronized (TrackingManager.class) {
                if (instance != null) return;
                instance = new TrackingManager(application);
                application.registerActivityLifecycleCallbacks(new TrackingLifecycle());
                TrackingSyncWorker.periodic(application);
                Log.i("ApolloTracking", "Tracking installed: UX catalog v2");
            }
        } catch (RuntimeException e) {
            Log.w("ApolloTracking", "Tracking initialization unavailable");
        }
    }

    public static void screen(String screen, boolean emitView) {
        TrackingManager manager = instance;
        if (manager == null) return;
        manager.execute(() -> {
            UserSession authentication = manager.authentication.read();
            String owner = TrackingIdentity.owner(authentication);
            if (owner == null) {
                manager.scope = null;
                if (org.apollo.mobile.BuildConfig.DEBUG)
                    Log.i("ApolloTracking", "Collection paused: NO_VALID_AUTHENTICATED_SESSION");
                return;
            }
            String session = manager.sessions.touch(authentication);
            Scope snapshot = new Scope(owner, session, screen);
            manager.scope = snapshot;
            if (manager.sessions.takeOpen()) manager.persist(snapshot,
                    TrackingEvents.APP_OPENED, screen, Collections.emptyMap());
            if (emitView) manager.persist(snapshot,
                    TrackingEvents.SCREEN_VIEWED, screen, Collections.emptyMap());
            TrackingSyncWorker.schedule(manager.context);
        });
    }

    public static void track(String name, String screen, Map<String, String> properties) {
        TrackingManager manager = instance;
        if (manager == null) return;
        Scope snapshot = manager.scope;
        Map<String, String> copy = new HashMap<>(properties);
        manager.execute(() -> {
            Scope active = snapshot == null ? manager.scope : snapshot;
            if (active != null) manager.persist(active, name, screen, copy);
        });
    }

    public static void order(String name, String screen, long orderId) {
        orderEvent(name, screen, orderId);
    }

    public static Map<String, String> properties(String... pairs) {
        Map<String, String> values = new HashMap<>();
        if (pairs.length % 2 != 0) throw new IllegalArgumentException("Expected key/value pairs");
        for (int i = 0; i < pairs.length; i += 2) values.put(pairs[i], pairs[i + 1]);
        return values;
    }

    public static void trackCurrent(String name, Map<String, String> properties) {
        TrackingManager manager = instance;
        Scope snapshot = manager == null ? null : manager.scope;
        if (snapshot != null) track(name, snapshot.screen, properties);
    }

    public interface EventSink {
        void record(String name, Map<String, String> properties);
    }

    public static EventSink capture() {
        TrackingManager manager = instance;
        Scope snapshot = manager == null ? null : manager.scope;
        return (name, properties) -> {
            if (snapshot == null) return;
            Map<String, String> copy = new HashMap<>(properties);
            manager.execute(() -> manager.persist(snapshot, name, snapshot.screen, copy));
        };
    }

    private static void orderEvent(String name, String screen, long orderId) {
        track(name, screen, Collections.singletonMap("work_order_id", Long.toString(orderId)));
    }

    public static void failure(String screen, String action, String type, long orderId) {
        Map<String, String> properties = new HashMap<>();
        properties.put("action", action);
        properties.put("error_type", type);
        if (orderId > 0) properties.put("work_order_id", Long.toString(orderId));
        track(TrackingEvents.ACTION_FAILED, screen, properties);
    }

    public static void attachBusinessContext(Request.Builder request) {
        TrackingManager manager = instance;
        Scope snapshot = manager == null ? null : manager.scope;
        if (snapshot == null) return;
        request.header("X-Tracking-Session", snapshot.session)
                .header("X-Tracking-Screen", snapshot.screen);
    }

    private void persist(Scope snapshot, String name, String screen, Map<String, String> properties) {
        UserSession currentAuthentication = authentication.read();
        if (!snapshot.owner.equals(TrackingIdentity.owner(currentAuthentication))) return;
        if (properties.size() > 16) throw new IllegalArgumentException("Too many properties");
        for (var entry : properties.entrySet()) {
            if (!KEYS.contains(entry.getKey()) || entry.getValue() == null
                    || entry.getValue().length() > 128
                    || !entry.getValue().matches("[A-Za-z0-9_.:/ -]*")) {
                throw new IllegalArgumentException("Invalid tracking property");
            }
        }
        String activeSession = sessions.touch(currentAuthentication);
        if (sessions.takeOpen()) store.insert(snapshot.owner, new TrackingEvent(
                TrackingEvents.APP_OPENED, activeSession, screen, Collections.emptyMap()));
        if (scope == snapshot) scope = new Scope(snapshot.owner, activeSession, snapshot.screen);
        store.insert(snapshot.owner, new TrackingEvent(name, activeSession, screen, properties));
        if (org.apollo.mobile.BuildConfig.DEBUG) Log.d("ApolloTracking", "Queued event=" + name + " screen=" + screen);
        TrackingSyncWorker.schedule(context);
    }

    private void execute(Runnable task) {
        try {
            io.execute(() -> {
                try { task.run(); }
                catch (RuntimeException e) { Log.w("ApolloTracking", "Tracking event unavailable: " + e.getClass().getSimpleName()); }
            });
        } catch (RuntimeException e) {
            Log.w("ApolloTracking", "Tracking executor unavailable");
        }
    }
}
