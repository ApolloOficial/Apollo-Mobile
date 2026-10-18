package org.apollo.mobile.view.common;

import android.app.Activity;
import android.content.Context;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public final class Tr {

    private static Context app;
    private static final Map<String, String> KEYS = new HashMap<>();

    private Tr() {
    }

    public static void init(Context context) {
        app = context.getApplicationContext();
    }

    public static String t(String text) {
        Activity activity = ActivityTracker.current();
        Context context = activity != null ? activity : app;
        if (context == null) return text;
        int id = context.getResources().getIdentifier(key(text), "string", context.getPackageName());
        return id == 0 ? text : context.getString(id);
    }

    public static String v(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return value;
        String translated = t(trimmed);
        if (!translated.equals(trimmed)) return translated;
        String title = trimmed.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + trimmed.substring(1).toLowerCase(java.util.Locale.ROOT);
        String alt = t(title);
        return alt.equals(title) ? trimmed : alt;
    }

    private static String key(String text) {
        String cached = KEYS.get(text);
        if (cached != null) return cached;
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder("t_");
            for (int i = 0; i < 5; i++) builder.append(String.format("%02x", hash[i]));
            cached = builder.toString();
        } catch (Exception e) {
            cached = "";
        }
        KEYS.put(text, cached);
        return cached;
    }
}
