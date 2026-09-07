package org.apollo.mobile.auth.gateway;

import org.apollo.mobile.auth.error.AuthError;

public interface AuthenticationGateway {

    void login(String email, String password, Callback callback);

    interface Callback {
        void onSuccess(LoginResponse response);

        void onFailure(AuthError error);
    }
}
