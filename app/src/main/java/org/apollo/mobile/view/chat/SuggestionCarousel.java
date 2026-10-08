package org.apollo.mobile.view.chat;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.apollo.mobile.R;
import org.apollo.mobile.chat.SuggestionRotation;

final class SuggestionCarousel {
    interface OnPick {
        void onPick(String text);
    }

    private static final long INTERVAL_MS = 3500L;
    private static final long FADE_MS = 220L;

    private final TextView chip;
    private final LinearLayout dots;
    private final String[] suggestions;
    private final SuggestionRotation rotation;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable advance = this::advance;
    private boolean running;

    SuggestionCarousel(@NonNull TextView chip, @NonNull LinearLayout dots,
                       @NonNull String[] suggestions, @NonNull OnPick onPick) {
        this.chip = chip;
        this.dots = dots;
        this.suggestions = suggestions;
        this.rotation = new SuggestionRotation(suggestions.length);
        buildDots();
        show(rotation.current());
        chip.setOnClickListener(view -> onPick.onPick(this.suggestions[rotation.current()]));
    }

    void start() {
        if (running) {
            return;
        }
        running = true;
        handler.postDelayed(advance, INTERVAL_MS);
    }

    void stop() {
        running = false;
        handler.removeCallbacks(advance);
        chip.animate().cancel();
        chip.setAlpha(1f);
    }

    private void advance() {
        if (!running) {
            return;
        }
        chip.animate().alpha(0f).setDuration(FADE_MS).withEndAction(() -> {
            if (!running) {
                chip.setAlpha(1f);
                return;
            }
            show(rotation.next());
            chip.animate().alpha(1f).setDuration(FADE_MS).start();
        }).start();
        handler.postDelayed(advance, INTERVAL_MS);
    }

    private void show(int position) {
        chip.setText(suggestions[position]);
        for (int i = 0; i < dots.getChildCount(); i++) {
            dots.getChildAt(i).setBackground(dot(i == position));
        }
    }

    private void buildDots() {
        dots.removeAllViews();
        int size = dp(6);
        int gap = dp(4);
        for (int i = 0; i < suggestions.length; i++) {
            View dot = new View(dots.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(gap, 0, gap, 0);
            dot.setLayoutParams(params);
            dots.addView(dot);
        }
    }

    private GradientDrawable dot(boolean active) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(active
                ? ContextCompat.getColor(dots.getContext(), R.color.secondary_three)
                : Color.parseColor("#E0E0E0"));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                dots.getResources().getDisplayMetrics()));
    }
}
