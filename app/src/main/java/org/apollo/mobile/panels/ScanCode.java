package org.apollo.mobile.panels;

public final class ScanCode {

    private ScanCode() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.replaceAll("\\s+", "");
        return value.isEmpty() ? null : value;
    }

    public static boolean isValidEan13(String value) {
        if (value == null || !value.matches("\\d{13}")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = value.charAt(i) - '0';
            sum += i % 2 == 0 ? digit : digit * 3;
        }
        return (10 - sum % 10) % 10 == value.charAt(12) - '0';
    }
}
