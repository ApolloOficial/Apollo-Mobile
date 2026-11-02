package org.apollo.mobile.push;

final class PushText {

    private PushText() {
    }

    static String pick(String primary, String secondary) {
        if (primary != null && !primary.trim().isEmpty()) {
            return primary.trim();
        }
        if (secondary != null && !secondary.trim().isEmpty()) {
            return secondary.trim();
        }
        return null;
    }

    static boolean hasContent(String title, String body) {
        return title != null || body != null;
    }
}
