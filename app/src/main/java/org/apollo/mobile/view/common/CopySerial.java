package org.apollo.mobile.view.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import org.apollo.mobile.R;

public final class CopySerial {

    private CopySerial() {
    }

    public static void attach(TextView view) {
        CharSequence serial = view.getText();
        if (serial == null || serial.length() == 0) return;
        view.setText(withIcon(view, serial, R.drawable.selector_copy, false));
        view.setOnClickListener(v -> {
            if (copy(v, serial, false)) {
                view.setText(withIcon(view, serial, R.drawable.selector_copy, true));
                view.postDelayed(() -> view.setText(withIcon(view, serial, R.drawable.selector_copy, false)), 1600);
            }
        });
    }

    private static CharSequence withIcon(TextView view, CharSequence serial, int drawable, boolean done) {
        Context context = view.getContext();
        Drawable icon = ContextCompat.getDrawable(context, done ? R.drawable.ic_copy_done : R.drawable.ic_copy);
        if (icon == null) return serial;
        icon = DrawableCompat.wrap(icon.mutate());
        DrawableCompat.setTint(icon, ContextCompat.getColor(context, R.color.neutral_gray));
        int gap = Math.round(4 * context.getResources().getDisplayMetrics().density);
        int size = Math.round(view.getTextSize());
        Drawable spaced = new android.graphics.drawable.InsetDrawable(icon, gap, 0, 0, 0);
        spaced.setBounds(0, 0, size + gap, size);
        android.text.SpannableStringBuilder text = new android.text.SpannableStringBuilder(serial).append("\u00A0");
        text.setSpan(new android.text.style.ImageSpan(spaced, android.text.style.DynamicDrawableSpan.ALIGN_CENTER),
                text.length() - 1, text.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    public static void onLongPress(View target, CharSequence serial) {
        target.setOnLongClickListener(v -> {
            copy(v, serial);
            return true;
        });
    }

    public static void copy(View source, CharSequence serial) {
        copy(source, serial, true);
    }

    private static boolean copy(View source, CharSequence serial, boolean animate) {
        if (serial == null || serial.toString().trim().isEmpty() || "-".contentEquals(serial)) return false;
        Context context = source.getContext();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            AppToast.error(context, org.apollo.mobile.view.common.Tr.t("Não foi possível copiar o número de série."));
            return false;
        }
        try {
            clipboard.setPrimaryClip(ClipData.newPlainText("Número de série", serial.toString().trim()));
        } catch (RuntimeException e) {
            AppToast.error(context, org.apollo.mobile.view.common.Tr.t("Não foi possível copiar o número de série."));
            return false;
        }
        source.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
        AppToast.success(context, org.apollo.mobile.view.common.Tr.t("Número de série copiado!"));
        return true;
    }
}
