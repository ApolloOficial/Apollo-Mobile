package org.apollo.mobile.view.panels;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import org.apollo.mobile.R;
import org.apollo.mobile.panels.error.PanelsError;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.view.auth.LoginActivity;
import org.apollo.mobile.view.common.BottomNavigation;

final class PanelsUi {
    private PanelsUi() {
    }

    static void bindNavigation(Activity activity) {
        BottomNavigation.setup(activity, BottomNavigation.Tab.PANELS);
    }

    static boolean handleFailure(Activity activity, PanelsError error) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return true;
        }
        if (error.getType() == PanelsError.Type.UNAUTHORIZED) {
            SessionManagerFactory.create(activity.getApplicationContext()).logout();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
            return true;
        }
        Toast.makeText(activity, messageFor(error), Toast.LENGTH_LONG).show();
        return false;
    }

    static int messageFor(PanelsError error) {
        switch (error.getType()) {
            case NETWORK:
                return R.string.panels_error_network;
            case TIMEOUT:
                return R.string.panels_error_timeout;
            case SERVER:
                return R.string.panels_error_server;
            case FORBIDDEN:
                return R.string.panels_error_forbidden;
            case NOT_FOUND:
                return R.string.panels_error_not_found;
            case INVALID_REQUEST:
                return R.string.panels_error_invalid;
            default:
                return R.string.panels_error_generic;
        }
    }

    static String percent(Double value) {
        if (value == null) {
            return null;
        }
        return Math.round(value) + "%";
    }
}
