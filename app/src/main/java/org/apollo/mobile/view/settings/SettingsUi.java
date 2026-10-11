package org.apollo.mobile.view.settings;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import org.apollo.mobile.R;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.settings.error.SettingsError;
import org.apollo.mobile.settings.format.ElapsedTime;
import org.apollo.mobile.view.auth.LoginActivity;

final class SettingsUi {
    private SettingsUi() {
    }

    static boolean handleFailure(Activity activity, SettingsError error) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return true;
        }
        if (error.getType() == SettingsError.Type.SESSION_EXPIRED) {
            goToLogin(activity);
            return true;
        }
        Toast.makeText(activity, messageFor(error), Toast.LENGTH_LONG).show();
        return false;
    }

    static void goToLogin(Activity activity) {
        SessionManagerFactory.create(activity.getApplicationContext()).logout();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    static int messageFor(SettingsError error) {
        switch (error.getType()) {
            case NETWORK:
                return R.string.settings_error_network;
            case TIMEOUT:
                return R.string.settings_error_timeout;
            case UNAVAILABLE:
                return R.string.settings_error_unavailable;
            case SERVER:
                return R.string.settings_error_server;
            case FORBIDDEN:
                return R.string.settings_error_forbidden;
            case INVALID_REQUEST:
                return R.string.settings_error_invalid;
            default:
                return R.string.settings_error_generic;
        }
    }

    static String elapsedText(Activity activity, ElapsedTime elapsed, int minutes, int hours, int days,
                              int months, int years, int nowText) {
        if (elapsed == null) {
            return null;
        }
        switch (elapsed.getUnit()) {
            case MINUTES:
                return activity.getResources().getQuantityString(minutes, elapsed.getAmount(), elapsed.getAmount());
            case HOURS:
                return activity.getResources().getQuantityString(hours, elapsed.getAmount(), elapsed.getAmount());
            case DAYS:
                return activity.getResources().getQuantityString(days, elapsed.getAmount(), elapsed.getAmount());
            case MONTHS:
                return activity.getResources().getQuantityString(months, elapsed.getAmount(), elapsed.getAmount());
            case YEARS:
                return activity.getResources().getQuantityString(years, elapsed.getAmount(), elapsed.getAmount());
            default:
                return activity.getString(nowText);
        }
    }
}
