package org.apollo.mobile.view.auth;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import java.util.function.BooleanSupplier;

public final class AuthForm {

    private AuthForm() {
    }

    public static Runnable bind(View button, BooleanSupplier valid, TextView... fields) {
        Runnable update = () -> button.setEnabled(valid.getAsBoolean());
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                update.run();
            }
        };
        for (TextView field : fields) field.addTextChangedListener(watcher);
        update.run();
        return update;
    }

    public static boolean filled(TextView... fields) {
        for (TextView field : fields) {
            if (field.getText().toString().trim().isEmpty()) return false;
        }
        return true;
    }

    public static void requirements(TextView target, String password) {
        String value = password == null ? "" : password;
        String[] labels = {"Mínimo 8 caracteres;", "Pelo menos 1 letra maiúscula e 1 minúscula;",
                "1 número;", "1 caractere especial"};
        boolean[] ok = {
                value.length() >= 8,
                value.matches(".*[a-z].*") && value.matches(".*[A-Z].*"),
                value.matches(".*\\d.*"),
                value.matches(".*[^A-Za-z0-9].*")
        };
        SpannableStringBuilder text = new SpannableStringBuilder();
        for (int i = 0; i < labels.length; i++) {
            int start = text.length();
            text.append(ok[i] ? "✓ " : "• ").append(labels[i]);
            int color = ok[i] ? 0xFF10B981 : 0xFF716764;
            text.setSpan(new ForegroundColorSpan(color), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (i < labels.length - 1) text.append('\n');
        }
        target.setText(text);
    }
}
