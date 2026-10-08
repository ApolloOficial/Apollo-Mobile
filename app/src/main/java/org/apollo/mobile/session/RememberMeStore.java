package org.apollo.mobile.session;

import android.content.Context;
import android.content.SharedPreferences;

public final class RememberMeStore {
    private static final String PREFERENCES_NAME = "auth_remember";
    private static final String REMEMBER_KEY = "remember";
    private static final String EMAIL_KEY = "email";

    private final SharedPreferences preferences;

    public RememberMeStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public boolean isRemembered() {
        return preferences.getBoolean(REMEMBER_KEY, false);
    }

    public String getEmail() {
        return isRemembered() ? preferences.getString(EMAIL_KEY, "") : "";
    }

    public void remember(String email) {
        preferences.edit()
                .putBoolean(REMEMBER_KEY, true)
                .putString(EMAIL_KEY, email)
                .apply();
    }

    public void forget() {
        preferences.edit()
                .remove(REMEMBER_KEY)
                .remove(EMAIL_KEY)
                .apply();
    }
}
