package org.apollo.mobile.view.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import org.apollo.mobile.R;

public final class CopySerial {

    private CopySerial() {
    }

    public static void attach(TextView view) {
        Context context = view.getContext();
        CharSequence serial = view.getText();
        Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_copypaste);
        if (icon != null && serial != null && serial.length() > 0) {
            icon = DrawableCompat.wrap(icon.mutate());
            DrawableCompat.setTint(icon, ContextCompat.getColor(context, R.color.neutral_gray));
            int gap = Math.round(4 * context.getResources().getDisplayMetrics().density);
            int size = Math.round(view.getTextSize());
            Drawable spaced = new android.graphics.drawable.InsetDrawable(icon, gap, 0, 0, 0);
            spaced.setBounds(0, 0, size + gap, size);
            android.text.SpannableStringBuilder text = new android.text.SpannableStringBuilder(serial).append("\u00A0");
            text.setSpan(new android.text.style.ImageSpan(spaced, android.text.style.DynamicDrawableSpan.ALIGN_CENTER),
                    text.length() - 1, text.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            view.setText(text);
        }
        view.setOnClickListener(v -> copy(v, serial));
    }

    public static void onLongPress(View target, CharSequence serial) {
        target.setOnLongClickListener(v -> {
            copy(v, serial);
            return true;
        });
    }

    public static void copy(View source, CharSequence serial) {
        if (serial == null || serial.toString().trim().isEmpty() || "-".contentEquals(serial)) return;
        Context context = source.getContext();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) return;
        clipboard.setPrimaryClip(ClipData.newPlainText("Número de série", serial.toString().trim()));
        source.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
        Motion.bounce(source);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Número de série copiado", Toast.LENGTH_SHORT).show();
        }
    }
}
