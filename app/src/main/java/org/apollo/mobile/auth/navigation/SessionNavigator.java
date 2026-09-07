package org.apollo.mobile.auth.navigation;

import android.app.Activity;
import android.content.Intent;

import org.apollo.mobile.auth.policy.RoleAccessPolicy;
import org.apollo.mobile.session.SessionManager;
import org.apollo.mobile.session.UserSession;
import org.apollo.mobile.view.access.AccessDeniedActivity;
import org.apollo.mobile.view.auth.LoginActivity;
import org.apollo.mobile.view.home.TechnicianHomeActivity;
import org.apollo.mobile.view.auth.WelcomeActivity;

public final class SessionNavigator {

    private SessionNavigator() {
    }

    public static void openInitialScreen(Activity activity, SessionManager sessionManager) {
        UserSession session = sessionManager.getActiveSession();
        if (session == null) {
            open(activity, WelcomeActivity.class, true);
            return;
        }
        openHome(activity, session);
    }

    public static void openHome(Activity activity, UserSession session) {
        if (RoleAccessPolicy.canAccess(session.getRole(), AppDestination.TECHNICIAN_HOME)) {
            open(activity, TechnicianHomeActivity.class, true);
            return;
        }
        open(activity, AccessDeniedActivity.class, true);
    }

    public static boolean enforceAccess(
            Activity activity,
            SessionManager sessionManager,
            AppDestination destination
    ) {
        UserSession session = sessionManager.getActiveSession();
        if (session == null) {
            openLogin(activity);
            return false;
        }
        if (!RoleAccessPolicy.canAccess(session.getRole(), destination)) {
            open(activity, AccessDeniedActivity.class, true);
            return false;
        }
        return true;
    }

    public static void openLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void open(Activity activity, Class<? extends Activity> destination, boolean closeCurrent) {
        activity.startActivity(new Intent(activity, destination));
        if (closeCurrent) {
            activity.finish();
        }
    }
}
