package org.apollo.mobile.tracking.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.apollo.mobile.tracking.TrackingEvents;
import org.apollo.mobile.tracking.TrackingManager;

public final class TrackingLifecycle implements Application.ActivityLifecycleCallbacks {
    private static final String VIEWED = "apollo.tracking.viewed";
    private final Set<Activity> skipResume = Collections.newSetFromMap(new WeakHashMap<>());
    private final Map<Activity, Long> started = new WeakHashMap<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int active;
    private boolean foreground;
    private String previous = "";
    private final Runnable background = () -> {
        if (active == 0 && foreground) {
            foreground = false;
            TrackingManager.track(TrackingEvents.APP_BACKGROUNDED, previous, Collections.emptyMap());
        }
    };

    @Override public void onActivityCreated(Activity activity, Bundle state) {
        TrackingForm.restore(activity, state);
        if (state != null && state.getBoolean(VIEWED, false)) skipResume.add(activity);
    }
    @Override public void onActivityStarted(Activity activity) {
        active++;
        handler.removeCallbacks(background);
    }
    @Override public void onActivityResumed(Activity activity) {
        String screen = activity.getClass().getSimpleName();
        TrackingManager.screen(screen, !skipResume.remove(activity));
        if (!foreground) {
            foreground = true;
            TrackingManager.track(TrackingEvents.APP_FOREGROUNDED, screen, Collections.emptyMap());
        }
        started.put(activity, SystemClock.elapsedRealtime());
        TrackingInteraction.install(activity);
        previous = screen;
    }
    @Override public void onActivityPaused(Activity activity) {
        Long start = started.remove(activity);
        if (start != null && !activity.isChangingConfigurations()) {
            String reason = activity.isFinishing() ? "finished" : "paused";
            TrackingManager.track(TrackingEvents.SCREEN_EXITED, activity.getClass().getSimpleName(),
                    TrackingManager.properties("duration_ms", Long.toString(SystemClock.elapsedRealtime() - start),
                            "exit_reason", reason));
        }
    }
    @Override public void onActivityStopped(Activity activity) {
        active = Math.max(0, active - 1);
        if (!activity.isChangingConfigurations()) handler.postDelayed(background, 700);
    }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) {
        TrackingForm.save(activity, state);
        state.putBoolean(VIEWED, true);
    }
    @Override public void onActivityDestroyed(Activity activity) {
        TrackingForm.destroyed(activity);
        started.remove(activity);
        skipResume.remove(activity);
    }
}
