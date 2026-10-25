package org.apollo.mobile.view.common;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.apollo.mobile.R;

public final class AppToast {

    public enum Type {
        SUCCESS(R.drawable.bg_toast_success, R.drawable.ic_toast_success, 2600),
        ERROR(R.drawable.bg_toast_error, R.drawable.ic_toast_error, 3400),
        INFO(R.drawable.bg_toast_info, R.drawable.ic_toast_info, 2800),
        OFFLINE(R.drawable.bg_toast_offline, R.drawable.ic_toast_offline, 4200);

        final int background;
        final int icon;
        final long duration;

        Type(int background, int icon, long duration) {
            this.background = background;
            this.icon = icon;
            this.duration = duration;
        }
    }

    private static final Object TAG = new Object();
    private static Type pendingType;
    private static CharSequence pendingMessage;

    private AppToast() {
    }

    public static void success(Context context, CharSequence message) {
        show(context, Type.SUCCESS, message);
    }

    public static void success(Context context, int message) {
        show(context, Type.SUCCESS, context.getString(message));
    }

    public static void error(Context context, CharSequence message) {
        show(context, Type.ERROR, message);
    }

    public static void error(Context context, int message) {
        show(context, Type.ERROR, context.getString(message));
    }

    public static void info(Context context, CharSequence message) {
        show(context, Type.INFO, message);
    }

    public static void info(Context context, int message) {
        show(context, Type.INFO, context.getString(message));
    }

    public static void offline(Context context) {
        show(context, Type.OFFLINE,
                org.apollo.mobile.view.common.Tr.t("Você está offline.\nNada será perdido, sincronizamos assim que a conexão voltar."));
    }

    public static void show(Context context, Type type, CharSequence message) {
        if (message == null || message.length() == 0) return;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> show(context, type, message));
            return;
        }
        Activity activity = activityOf(context);
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            Activity current = ActivityTracker.current();
            if (current != null && current != activity) {
                attach(current, type, message);
            } else {
                pendingType = type;
                pendingMessage = message;
            }
            return;
        }
        attach(activity, type, message);
    }

    static void flushPending(Activity activity) {
        if (pendingMessage == null) return;
        Type type = pendingType;
        CharSequence message = pendingMessage;
        pendingType = null;
        pendingMessage = null;
        activity.getWindow().getDecorView().postDelayed(() -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) attach(activity, type, message);
        }, 250);
    }

    private static void attach(Activity activity, Type type, CharSequence message) {
        org.apollo.mobile.tracking.TrackingManager.trackCurrent("feedback_shown",
                org.apollo.mobile.tracking.TrackingManager.properties("feedback_type", type.name()));
        ViewGroup decor = activity.findViewById(android.R.id.content);
        if (decor == null) return;
        for (int i = decor.getChildCount() - 1; i >= 0; i--) {
            View child = decor.getChildAt(i);
            if (child.getTag() == TAG) decor.removeView(child);
        }

        View toast = LayoutInflater.from(activity).inflate(R.layout.view_toast_success, decor, false);
        toast.setTag(TAG);
        toast.setBackgroundResource(type.background);
        ImageView icon = toast.findViewById(R.id.ivAppToastIcon);
        icon.setImageResource(type.icon);
        ((TextView) toast.findViewById(R.id.tvAppToast)).setText(message);

        float density = activity.getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        int side = Math.round(27 * density);
        params.setMargins(side, 0, side, bottomOffset(activity, density));
        decor.addView(toast, params);

        float distance = 90 * density;
        toast.setAlpha(0f);
        toast.setTranslationY(distance);
        toast.animate().alpha(1f).translationY(0f).setDuration(420)
                .setInterpolator(new OvershootInterpolator(1.1f)).start();
        Motion.bounce(icon);
        toast.setOnClickListener(v -> dismiss(decor, toast, distance));
        toast.postDelayed(() -> dismiss(decor, toast, distance), type.duration);
    }

    private static void dismiss(ViewGroup decor, View toast, float distance) {
        if (toast.getParent() == null) return;
        toast.animate().alpha(0f).translationY(distance).setDuration(260)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> decor.removeView(toast)).start();
    }

    private static int bottomOffset(Activity activity, float density) {
        int base = Math.round(16 * density);
        View nav = activity.findViewById(R.id.bottomNavigation);
        if (nav != null && nav.getVisibility() == View.VISIBLE && nav.isShown()) {
            int height = nav.getHeight() > 0 ? nav.getHeight() : Math.round(80 * density);
            return height + base;
        }
        return Math.round(24 * density);
    }

    private static Activity activityOf(Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) return (Activity) current;
            current = ((ContextWrapper) current).getBaseContext();
        }
        return null;
    }
}
