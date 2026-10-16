package org.apollo.mobile.view.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.RecyclerView;

import org.apollo.mobile.R;

import java.util.function.IntFunction;

public final class Motion {

    public static final long ENTER_MS = 360;
    private static final long STAGGER_MS = 50;
    private static final int MAX_STAGGER_STEPS = 8;

    private Motion() {}

    public static void enterScreen(Activity activity) {
        View root = activity.findViewById(android.R.id.content);
        if (!(root instanceof ViewGroup)) return;
        ViewGroup content = findContent((ViewGroup) root);
        if (content == null) return;
        int step = 0;
        for (int i = 0; i < content.getChildCount(); i++) {
            View child = content.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || isChrome(child)) continue;
            fadeUp(child, 60 + Math.min(step, MAX_STAGGER_STEPS) * STAGGER_MS);
            step++;
        }
        View nav = activity.findViewById(R.id.bottomNavigation);
        if (nav != null) slideUp(nav, 80);
        View fab = activity.findViewById(R.id.fabChatbot);
        if (fab != null) popIn(fab, 260);
    }

    private static boolean isChrome(View v) {
        int id = v.getId();
        return id == R.id.bottomNavigation || id == R.id.fabChatbot;
    }

    private static ViewGroup findContent(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if ((child instanceof NestedScrollView || child instanceof ScrollView)
                    && ((ViewGroup) child).getChildCount() > 0
                    && ((ViewGroup) child).getChildAt(0) instanceof ViewGroup) {
                return (ViewGroup) ((ViewGroup) child).getChildAt(0);
            }
            if (child instanceof ViewGroup && !(child instanceof RecyclerView)) {
                ViewGroup found = findContent((ViewGroup) child);
                if (found != null) return found;
            }
        }
        if (group.getId() == android.R.id.content && group.getChildCount() == 1
                && group.getChildAt(0) instanceof ViewGroup) {
            return (ViewGroup) group.getChildAt(0);
        }
        return null;
    }

    public static void stagger(long startDelay, View... views) {
        int step = 0;
        for (View v : views) {
            if (v == null || v.getVisibility() != View.VISIBLE) continue;
            fadeUp(v, startDelay + Math.min(step, MAX_STAGGER_STEPS) * STAGGER_MS);
            step++;
        }
    }

    public static void fadeUp(View view, long delay) {
        float distance = view.getResources().getDisplayMetrics().density * 16;
        view.setAlpha(0f);
        view.setTranslationY(distance);
        view.animate().alpha(1f).translationY(0f)
                .setStartDelay(delay)
                .setDuration(ENTER_MS)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withLayer()
                .start();
    }

    public static void slideUp(View view, long delay) {
        float distance = view.getResources().getDisplayMetrics().density * 40;
        view.setAlpha(0f);
        view.setTranslationY(distance);
        view.animate().alpha(1f).translationY(0f)
                .setStartDelay(delay)
                .setDuration(ENTER_MS + 60)
                .setInterpolator(new DecelerateInterpolator(1.8f))
                .start();
    }

    public static void popIn(View view, long delay) {
        view.setAlpha(0f);
        view.setScaleX(0.6f);
        view.setScaleY(0.6f);
        view.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setStartDelay(delay)
                .setDuration(ENTER_MS + 80)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    public static void refresh(View... views) {
        long delay = 0;
        for (View view : views) {
            if (view == null) continue;
            float distance = view.getResources().getDisplayMetrics().density * 10;
            view.setAlpha(0f);
            view.setTranslationY(distance);
            view.animate().alpha(1f).translationY(0f)
                    .setStartDelay(delay)
                    .setDuration(220)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
            delay += 40;
        }
    }

    public static void slideSwap(View view, boolean forward, Runnable swap) {
        float distance = view.getResources().getDisplayMetrics().density * 32 * (forward ? 1 : -1);
        view.animate().alpha(0f).translationX(-distance).setDuration(140)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withEndAction(() -> {
                    swap.run();
                    view.setTranslationX(distance);
                    view.animate().alpha(1f).translationX(0f).setDuration(240)
                            .setInterpolator(new DecelerateInterpolator(1.6f)).start();
                }).start();
    }

    public static void bounce(View view) {
        view.animate().cancel();
        view.setScaleX(0.82f);
        view.setScaleY(0.82f);
        view.animate().scaleX(1f).scaleY(1f).setDuration(380)
                .setInterpolator(new OvershootInterpolator(3f)).start();
    }

    public static void spin(View view) {
        view.animate().cancel();
        view.setRotation(0f);
        view.animate().rotationBy(360f).setDuration(700)
                .setInterpolator(new FastOutSlowInInterpolator()).start();
    }

    public static void growWidth(View view, long delay) {
        view.setPivotX(0f);
        view.setScaleX(0f);
        view.animate().scaleX(1f).setStartDelay(delay).setDuration(420)
                .setInterpolator(new OvershootInterpolator(1.4f)).start();
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void pressable(View... views) {
        for (View view : views) {
            if (view == null) continue;
            view.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(90)
                                .setInterpolator(new FastOutSlowInInterpolator()).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                        v.animate().scaleX(1f).scaleY(1f).setDuration(260)
                                .setInterpolator(new OvershootInterpolator(2.5f)).start();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(160).start();
                        break;
                    default:
                        break;
                }
                return false;
            });
        }
    }

    public static void countUp(TextView view, int target, IntFunction<String> format, long delay) {
        if (target <= 0) {
            view.setText(format.apply(target));
            return;
        }
        ValueAnimator animator = ValueAnimator.ofInt(0, target);
        animator.setDuration(Math.min(1100, 500 + target * 12L));
        animator.setStartDelay(delay);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(a -> view.setText(format.apply((int) a.getAnimatedValue())));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                view.setText(format.apply(target));
            }
        });
        view.setText(format.apply(0));
        animator.start();
    }

    public static void progressTo(ProgressBar bar, int target, long delay) {
        ObjectAnimator animator = ObjectAnimator.ofInt(bar, "progress", 0, target);
        animator.setDuration(900);
        animator.setStartDelay(delay);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.start();
    }

    public static void shake(View view) {
        float d = view.getResources().getDisplayMetrics().density * 8;
        ObjectAnimator tremor = ObjectAnimator.ofFloat(view, View.TRANSLATION_X,
                0f, -d, d, -d * 0.7f, d * 0.4f, 0f);
        tremor.setDuration(360);
        tremor.start();
    }

    public static void pulse(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.08f, 1f),
                ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.08f, 1f));
        set.setDuration(520);
        set.setInterpolator(new FastOutSlowInInterpolator());
        set.start();
    }
}
