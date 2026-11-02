package org.apollo.mobile.push;

import android.content.Context;
import android.content.SharedPreferences;

final class PushTokenStore {
    private static final String FILE = "apollo_push";
    private static final String KEY_TOKEN = "fcm_token";
    private static final String KEY_PERMISSION_ASKED = "notification_permission_asked";

    private final SharedPreferences preferences;

    PushTokenStore(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    String token() {
        return preferences.getString(KEY_TOKEN, null);
    }

    void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    void clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply();
    }

    boolean permissionAsked() {
        return preferences.getBoolean(KEY_PERMISSION_ASKED, false);
    }

    void markPermissionAsked() {
        preferences.edit().putBoolean(KEY_PERMISSION_ASKED, true).apply();
    }
}
