package org.apollo.mobile.view.auth;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.core.content.ContextCompat;

import org.apollo.mobile.R;

final class PasswordVisibilityToggle {
    private static final int TOUCH_SLOP_DP = 12;

    private PasswordVisibilityToggle() {
    }

    @SuppressLint("ClickableViewAccessibility")
    static void attach(EditText field) {
        field.setOnTouchListener((view, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP || !hitsEndIcon(field, event)) {
                return false;
            }
            toggle(field);
            view.performClick();
            return true;
        });
    }

    private static boolean hitsEndIcon(EditText field, MotionEvent event) {
        Drawable icon = field.getCompoundDrawablesRelative()[2];
        if (icon == null) {
            return false;
        }
        float slop = TOUCH_SLOP_DP * field.getResources().getDisplayMetrics().density;
        float iconSpan = icon.getIntrinsicWidth() + field.getPaddingEnd() + slop;
        if (field.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            return event.getX() <= iconSpan;
        }
        return event.getX() >= field.getWidth() - iconSpan;
    }

    private static void toggle(EditText field) {
        boolean wasVisible = (field.getInputType() & InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        Typeface typeface = field.getTypeface();
        int selection = field.getSelectionEnd();

        field.setInputType(InputType.TYPE_CLASS_TEXT | (wasVisible
                ? InputType.TYPE_TEXT_VARIATION_PASSWORD
                : InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
        field.setTypeface(typeface);
        field.setSelection(Math.max(selection, 0));

        Drawable[] icons = field.getCompoundDrawablesRelative();
        Drawable endIcon = ContextCompat.getDrawable(field.getContext(),
                wasVisible ? R.drawable.ic_eye : R.drawable.ic_eye_open);
        field.setCompoundDrawablesRelativeWithIntrinsicBounds(icons[0], icons[1], endIcon, icons[3]);
    }
}
