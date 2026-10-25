package org.apollo.mobile.tracking.lifecycle;

import android.app.Activity;
import android.os.SystemClock;
import java.util.Map;
import java.util.WeakHashMap;
import org.apollo.mobile.tracking.TrackingManager;

/** In-memory funnel state; never stores form values. */
public final class TrackingForm {
    private static final Map<Activity, Long> forms = new WeakHashMap<>();
    private TrackingForm() {}
    public static void restore(Activity activity, android.os.Bundle state) {
        if (state != null && state.containsKey("tracking.form_at"))
            forms.put(activity, state.getLong("tracking.form_at"));
    }
    public static void save(Activity activity, android.os.Bundle state) {
        Long startedAt = forms.get(activity);
        if (startedAt != null) state.putLong("tracking.form_at", startedAt);
    }
    public static void started(Activity activity) {
        if (!forms.containsKey(activity)) {
            forms.put(activity, SystemClock.elapsedRealtime());
            event(activity, "form_started", TrackingManager.properties());
        }
    }
    public static void submitted(Activity activity) {
        started(activity);
        event(activity, "form_submitted", TrackingManager.properties());
    }
    public static void succeeded(Activity activity) {
        if (forms.remove(activity) != null) event(activity, "form_succeeded", TrackingManager.properties());
    }
    public static void invalid(Activity activity, String field, String rule) {
        started(activity);
        event(activity, "validation_failed", TrackingManager.properties("field", field, "rule", rule));
    }
    public static void destroyed(Activity activity) {
        Long at = forms.remove(activity);
        if (at != null && activity.isFinishing()) event(activity, "form_abandoned",
                TrackingManager.properties("duration_ms", Long.toString(SystemClock.elapsedRealtime() - at),
                        "exit_reason", "finished_without_success"));
    }
    private static void event(Activity activity, String name, Map<String, String> values) {
        java.util.HashMap<String, String> props = new java.util.HashMap<>(values);
        props.put("form_id", activity.getClass().getSimpleName());
        TrackingManager.track(name, activity.getClass().getSimpleName(), props);
    }
}
