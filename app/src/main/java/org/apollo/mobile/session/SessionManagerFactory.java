package org.apollo.mobile.session;

import android.content.Context;

import org.apollo.mobile.auth.securty.JwtClaimsParser;
import org.apollo.mobile.auth.api.AuthApiClient;
import org.apollo.mobile.auth.gateway.AuthenticationGateway;
import org.apollo.mobile.auth.gateway.RetrofitAuthenticationGateway;

public final class SessionManagerFactory {

    private SessionManagerFactory() {
    }

    public static SessionManager create(Context context) {
        SessionStorage sessionStorage = new EncryptedSessionStorage(context);
        return new SessionManager(createGateway(context), sessionStorage, new JwtClaimsParser());
    }

    /** Gateway para os fluxos que não abrem sessão (recuperação de senha). */
    public static AuthenticationGateway createGateway(Context context) {
        return new RetrofitAuthenticationGateway(AuthApiClient.createAuthService(context));
    }
}
