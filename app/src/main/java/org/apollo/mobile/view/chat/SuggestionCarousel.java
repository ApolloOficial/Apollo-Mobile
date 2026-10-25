package org.apollo.mobile.view.chat;

import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.apollo.mobile.R;
import org.apollo.mobile.view.common.Motion;

final class SuggestionCarousel {
    interface OnPick {
        void onPick(String text);
    }

    private final ViewGroup group;
    private final List<TextView> chips = new ArrayList<>();
    private boolean shown;

    SuggestionCarousel(@NonNull ViewGroup group, @NonNull String[] suggestions, @NonNull OnPick onPick) {
        this.group = group;
        group.removeAllViews();
        int height = group.getResources().getDimensionPixelSize(R.dimen.space_33);
        int padding = group.getResources().getDimensionPixelSize(R.dimen.space_16);
        int spacing = group.getResources().getDimensionPixelSize(R.dimen.space_8);
        LinearLayout row = null;
        for (int i = 0; i < suggestions.length; i++) {
            String suggestion = suggestions[i];
            if (i % 2 == 0) {
                row = new LinearLayout(group.getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i > 0) rowParams.topMargin = spacing;
                group.addView(row, rowParams);
            }
            TextView chip = new TextView(group.getContext());
            chip.setTextAppearance(R.style.apollo_label4);
            chip.setTextColor(ContextCompat.getColor(group.getContext(), R.color.neutral_black));
            chip.setIncludeFontPadding(false);
            chip.setText(suggestion);
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setPadding(padding, spacing, padding, spacing);
            chip.setBackgroundResource(R.drawable.bg_chat_chip);
            chip.setSingleLine(false);
            chip.setMinHeight(height);
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i % 2 == 0 && i + 1 < suggestions.length) chipParams.setMarginEnd(spacing);
            chip.setLayoutParams(chipParams);
            chip.setOnClickListener(view -> {
                Motion.bounce(view);
                onPick.onPick(suggestion);
            });
            row.addView(chip);
            chips.add(chip);
        }
    }

    void start() {
        if (shown) return;
        shown = true;
        for (int i = 0; i < chips.size(); i++) {
            Motion.popIn(chips.get(i), 120 + i * 60L);
        }
    }

    void stop() {
        for (TextView chip : chips) {
            chip.animate().cancel();
            chip.setAlpha(1f);
            chip.setScaleX(1f);
            chip.setScaleY(1f);
        }
    }
}
