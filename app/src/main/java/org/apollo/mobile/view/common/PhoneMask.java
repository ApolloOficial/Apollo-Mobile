package org.apollo.mobile.view.common;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;

public final class PhoneMask implements TextWatcher {

    private final EditText input;
    private boolean editing;

    private PhoneMask(EditText input) {
        this.input = input;
    }

    public static void attach(EditText input) {
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        input.addTextChangedListener(new PhoneMask(input));
    }

    public static String format(String value) {
        if (value == null) return "";
        String digits = value.replaceAll("\\D", "");
        if (digits.length() > 11) digits = digits.substring(digits.length() - 11);
        int n = digits.length();
        if (n == 0) return "";
        StringBuilder out = new StringBuilder("(");
        out.append(digits, 0, Math.min(2, n));
        if (n <= 2) return out.toString();
        out.append(") ");
        int split = n <= 10 ? 6 : 7;
        out.append(digits, 2, Math.min(split, n));
        if (n > split) out.append('-').append(digits, split, n);
        return out.toString();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (editing) return;
        editing = true;
        String formatted = format(s.toString());
        if (!formatted.equals(s.toString())) {
            s.replace(0, s.length(), formatted);
            input.setSelection(s.length());
        }
        editing = false;
    }
}
