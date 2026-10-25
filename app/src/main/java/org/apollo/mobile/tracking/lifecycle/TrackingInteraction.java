package org.apollo.mobile.tracking.lifecycle;

import android.app.Activity;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.appcompat.view.WindowCallbackWrapper;
import java.util.Map;
import org.apollo.mobile.tracking.TrackingEvents;
import org.apollo.mobile.tracking.TrackingManager;

/** Tracks stable control identifiers, never labels, input text or touch coordinates. */
public final class TrackingInteraction extends WindowCallbackWrapper {
    private final Activity activity;
    private String lastTarget = "";
    private long lastAt;
    private int attempts;

    private TrackingInteraction(Activity activity) {
        super(activity.getWindow().getCallback());
        this.activity = activity;
    }

    public static void install(Activity activity) {
        String name = activity.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT);
        if (name.contains("login") || name.contains("password") || name.contains("auth")) return;
        if (!(activity.getWindow().getCallback() instanceof TrackingInteraction))
            activity.getWindow().setCallback(new TrackingInteraction(activity));
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        String target = null;
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            View view = find(activity.getWindow().getDecorView(), event.getRawX(), event.getRawY());
            if (view != null && view.getId() != View.NO_ID && !(view instanceof EditText)) {
                try { target = view.getResources().getResourceEntryName(view.getId()); }
                catch (android.content.res.Resources.NotFoundException ignored) { }
            }
        }
        boolean handled = super.dispatchTouchEvent(event);
        if (target != null) {
            String screen = activity.getClass().getSimpleName();
            TrackingManager.track(TrackingEvents.UI_ACTION, screen,
                    TrackingManager.properties("target", target, "input_method", "touch"));
            long now = SystemClock.elapsedRealtime();
            attempts = target.equals(lastTarget) && now - lastAt < 2000 ? attempts + 1 : 1;
            if (attempts == 3) TrackingManager.track(TrackingEvents.REPEATED_ACTION, screen,
                    TrackingManager.properties("target", target, "attempts", "3"));
            lastTarget = target;
            lastAt = now;
        }
        return handled;
    }

    private static View find(View view, float x, float y) {
        if (view.getVisibility() != View.VISIBLE || !view.isEnabled()) return null;
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        if (x < location[0] || y < location[1] || x > location[0] + view.getWidth()
                || y > location[1] + view.getHeight()) return null;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View result = find(group.getChildAt(i), x, y);
                if (result != null) return result;
            }
        }
        return view.isClickable() ? view : null;
    }
}
