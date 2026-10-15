package org.apollo.mobile.view.panels;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import org.apollo.mobile.R;
import org.apollo.mobile.panels.error.PanelsError;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.view.auth.LoginActivity;
import org.apollo.mobile.view.chat.ChatbotActivity;
import org.apollo.mobile.view.home.HomeActivity;
import org.apollo.mobile.view.scanner.ScannerActivity;
import org.apollo.mobile.view.settings.SettingsActivity;

final class PanelsUi {
    private PanelsUi() {
    }

    static void bindNavigation(Activity activity) {
        View home = activity.findViewById(R.id.ivHome);
        if (home != null) {
            home.setOnClickListener(view -> open(activity, HomeActivity.class, true));
        }
        View panels = activity.findViewById(R.id.ivPanels);
        if (panels != null && !(activity instanceof PanelsActivity)) {
            panels.setOnClickListener(view -> open(activity, PanelsActivity.class, true));
        }
        View scan = activity.findViewById(R.id.cvScanContainer);
        if (scan != null) {
            scan.setOnClickListener(view -> activity.startActivity(new Intent(activity, ScannerActivity.class)));
        }
        View settings = activity.findViewById(R.id.ivSettings);
        if (settings != null) {
            settings.setOnClickListener(view -> open(activity, SettingsActivity.class, false));
        }
        View chat = activity.findViewById(R.id.fabChatbot);
        if (chat != null) {
            chat.setOnClickListener(view -> activity.startActivity(new Intent(activity, ChatbotActivity.class)));
        }
    }

    private static void open(Activity activity, Class<?> target, boolean clearTop) {
        Intent intent = new Intent(activity, target);
        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        activity.startActivity(intent, ActivityOptions.makeCustomAnimation(
                activity, R.anim.nav_fade_in, R.anim.nav_fade_out).toBundle());
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
