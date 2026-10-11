package org.apollo.mobile.settings.format;

import java.util.Locale;

public final class ByteSize {
    private static final long KB = 1024L;
    private static final long MB = KB * 1024L;
    private static final long GB = MB * 1024L;

    private ByteSize() {
    }

    public static String format(long bytes) {
        long value = Math.max(0L, bytes);
        Locale locale = Locale.forLanguageTag("pt-BR");
        if (value >= GB) {
            return String.format(locale, "%.1f GB", (double) value / GB);
        }
        if (value >= MB) {
            return String.format(locale, "%.1f MB", (double) value / MB);
        }
        if (value >= KB) {
            return String.format(locale, "%d KB", value / KB);
        }
        return String.format(locale, "%d B", value);
    }
}
