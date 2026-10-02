package org.apollo.mobile.view.auth;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import org.apollo.mobile.R;
import org.apollo.mobile.auth.navigation.AppDestination;
import org.apollo.mobile.auth.policy.RoleAccessPolicy;
import org.apollo.mobile.session.SessionManager;
import org.apollo.mobile.session.UserSession;

/** Decide para onde ir depois que a sessão foi aberta (com ou sem código de segurança). */
final class PostLoginRouter {

    private PostLoginRouter() {
    }

    static void route(Activity activity, SessionManager sessionManager, UserSession session) {
        if (RoleAccessPolicy.canAccess(session.getRole(), AppDestination.TECHNICIAN_HOME)) {
            /*
             * A TechnicianHomeActivity ainda não existe. Quando ela entrar, é só abri-la aqui.
             */
            Toast.makeText(activity, R.string.login_success, Toast.LENGTH_SHORT).show();
            return;
        }

        // Perfil sem acesso ao app (ex.: gerente): não deixa token guardado.
        sessionManager.logout();
        Intent intent = new Intent(activity, AccessDeniedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
