package org.apollo.mobile.settings.format;

public final class PhoneInput {
    private PhoneInput() {
    }

    public static String digits(String raw) {
        if (raw == null) {
            return "";
        }
        String onlyDigits = raw.replaceAll("\\D", "");
        if ((onlyDigits.length() == 12 || onlyDigits.length() == 13) && onlyDigits.startsWith("55")) {
            return onlyDigits.substring(2);
        }
        return onlyDigits;
    }

    public static boolean isValid(String raw) {
        String value = digits(raw);
        if (!value.matches("\\d{10,11}")) {
            return false;
        }
        if (value.charAt(0) == '0' || value.charAt(1) == '0') {
            return false;
        }
        return value.length() == 10 || value.charAt(2) == '9';
    }

    public static String format(String raw) {
        String value = digits(raw);
        if (value.length() == 11) {
            return "(" + value.substring(0, 2) + ") " + value.substring(2, 7) + "-" + value.substring(7);
        }
        if (value.length() == 10) {
            return "(" + value.substring(0, 2) + ") " + value.substring(2, 6) + "-" + value.substring(6);
        }
        return value;
    }
}
