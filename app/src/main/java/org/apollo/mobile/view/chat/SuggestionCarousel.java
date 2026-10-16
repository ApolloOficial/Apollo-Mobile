package org.apollo.mobile.view.chat;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.apollo.mobile.R;
import org.apollo.mobile.view.common.Motion;

final class SuggestionCarousel {
    interface OnPick {
        void onPick(String text);
    }

    private final ViewGroup group;
    private boolean shown;

    SuggestionCarousel(@NonNull ViewGroup group, @NonNull String[] suggestions, @NonNull OnPick onPick) {
        this.group = group;
        group.removeAllViews();
        int height = group.getResources().getDimensionPixelSize(R.dimen.space_33);
        int padding = group.getResources().getDimensionPixelSize(R.dimen.space_16);
        for (String suggestion : suggestions) {
            TextView chip = new TextView(group.getContext());
            chip.setTextAppearance(R.style.apollo_label4);
            chip.setTextColor(ContextCompat.getColor(group.getContext(), R.color.neutral_black));
            chip.setIncludeFontPadding(false);
            chip.setText(suggestion);
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setPadding(padding, 0, padding, 0);
            chip.setBackgroundResource(R.drawable.bg_chat_chip);
            chip.setMaxLines(1);
            chip.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height));
            chip.setOnClickListener(view -> {
                Motion.bounce(view);
                onPick.onPick(suggestion);
            });
            group.addView(chip);
        }
    }

    void start() {
        if (shown) return;
        shown = true;
        for (int i = 0; i < group.getChildCount(); i++) {
            Motion.popIn(group.getChildAt(i), 120 + i * 60L);
        }
    }

    void stop() {
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).animate().cancel();
            group.getChildAt(i).setAlpha(1f);
            group.getChildAt(i).setScaleX(1f);
            group.getChildAt(i).setScaleY(1f);
        }
    }
}
