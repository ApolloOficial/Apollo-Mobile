package org.apollo.mobile.view.common;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public final class ActivityTracker implements Application.ActivityLifecycleCallbacks {

    private static final ActivityTracker INSTANCE = new ActivityTracker();
    private static WeakReference<Activity> current = new WeakReference<>(null);
    private static boolean registered;

    private ActivityTracker() {
    }

    public static void register(Application application) {
        if (registered) return;
        registered = true;
        application.registerActivityLifecycleCallbacks(INSTANCE);
    }

    @Nullable
    public static Activity current() {
        Activity activity = current.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return null;
        return activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        current = new WeakReference<>(activity);
        AppToast.flushPending(activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle state) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (current.get() == activity) current = new WeakReference<>(null);
    }
}
