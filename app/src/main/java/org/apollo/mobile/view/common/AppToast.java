package org.apollo.mobile.view.common;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.apollo.mobile.R;

public final class AppToast {

    private static final long VISIBLE_MS = 2600;

    private AppToast() {}

    public static void success(Activity activity, CharSequence message) {
        ViewGroup decor = activity.findViewById(android.R.id.content);
        if (decor == null) return;
        View toast = LayoutInflater.from(activity).inflate(R.layout.view_toast_success, decor, false);
        ((TextView) toast.findViewById(R.id.tvAppToast)).setText(message);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP);
        int side = activity.getResources().getDimensionPixelSize(R.dimen.space_27);
        params.setMargins(side, activity.getResources().getDimensionPixelSize(R.dimen.space_48), side, 0);
        decor.addView(toast, params);
        float distance = -activity.getResources().getDisplayMetrics().density * 80;
        toast.setAlpha(0f);
        toast.setTranslationY(distance);
        toast.animate().alpha(1f).translationY(0f).setDuration(420)
                .setInterpolator(new OvershootInterpolator(1.2f)).start();
        Motion.bounce(toast.findViewById(R.id.ivAppToastIcon));
        toast.postDelayed(() -> toast.animate().alpha(0f).translationY(distance).setDuration(260)
                .withEndAction(() -> decor.removeView(toast)).start(), VISIBLE_MS);
    }
}
