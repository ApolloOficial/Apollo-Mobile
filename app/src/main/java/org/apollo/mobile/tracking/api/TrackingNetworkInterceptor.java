package org.apollo.mobile.tracking.api;

import android.os.SystemClock;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.apollo.mobile.tracking.TrackingEvents;
import org.apollo.mobile.tracking.TrackingManager;

public final class TrackingNetworkInterceptor implements Interceptor {
    @Override public Response intercept(Chain chain) throws IOException {
        String path = chain.request().url().encodedPath();
        if (path.contains("/tracking/") || path.contains("/auth/") || path.contains("/recovery/"))
            return chain.proceed(chain.request());
        long started = SystemClock.elapsedRealtime();
        TrackingManager.EventSink sink = TrackingManager.capture();
        Map<String, String> properties = new HashMap<>();
        properties.put("method", chain.request().method());
        // Keep only fixed routing categories: never IDs, search terms, queries or raw URLs.
        String category = "other";
        for (String allowed : new String[]{"maintenance-registers", "panels", "strings",
                "inverters", "branches", "users", "chat", "alerts", "sync"}) {
            if (path.contains("/" + allowed)) { category = allowed; break; }
        }
        properties.put("endpoint", category);
        try {
            Response response = chain.proceed(chain.request());
            properties.put("http_status", Integer.toString(response.code()));
            properties.put("duration_ms", Long.toString(SystemClock.elapsedRealtime() - started));
            sink.record(response.isSuccessful() ? TrackingEvents.API_REQUEST_COMPLETED
                    : TrackingEvents.API_REQUEST_FAILED, properties);
            return response;
        } catch (IOException failure) {
            properties.put("error_type", failure instanceof java.net.SocketTimeoutException
                    ? "timeout" : failure instanceof javax.net.ssl.SSLException ? "tls" : "network");
            properties.put("duration_ms", Long.toString(SystemClock.elapsedRealtime() - started));
            sink.record(TrackingEvents.API_REQUEST_FAILED, properties);
            throw failure;
        }
    }
}
