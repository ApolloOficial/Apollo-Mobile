package org.apollo.mobile.view.auth;

import android.content.Context;
import android.content.Intent;

import org.apollo.mobile.auth.navigation.AppDestination;
import org.apollo.mobile.auth.policy.RoleAccessPolicy;
import org.apollo.mobile.session.RememberMeStore;
import org.apollo.mobile.session.SessionManager;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.session.UserSession;
import org.apollo.mobile.view.home.HomeActivity;

public final class StartupRouter {
    private StartupRouter() {
    }

    public static Intent destination(Context context) {
        RememberMeStore rememberMe = new RememberMeStore(context);
        SessionManager sessionManager = SessionManagerFactory.create(context.getApplicationContext());

        if (!rememberMe.isRemembered()) {
            sessionManager.logout();
            return new Intent(context, WelcomeActivity.class);
        }

        UserSession session = sessionManager.getActiveSession();
        if (session == null) {
            return new Intent(context, LoginActivity.class);
        }
        if (!RoleAccessPolicy.canAccess(session.getRole(), AppDestination.TECHNICIAN_HOME)) {
            sessionManager.logout();
            return new Intent(context, WelcomeActivity.class);
        }

        return new Intent(context, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }
}
