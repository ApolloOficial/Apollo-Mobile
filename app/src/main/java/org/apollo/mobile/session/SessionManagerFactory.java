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
        AuthenticationGateway authenticationGateway = new RetrofitAuthenticationGateway(
                AuthApiClient.createAuthService(context)
        );
        return new SessionManager(authenticationGateway, sessionStorage, new JwtClaimsParser());
    }
}
