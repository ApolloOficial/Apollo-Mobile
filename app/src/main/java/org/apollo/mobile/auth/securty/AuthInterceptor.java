package org.apollo.mobile.auth.securty;

import org.apollo.mobile.session.SessionStorage;
import org.apollo.mobile.session.UserSession;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class AuthInterceptor implements Interceptor {

    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final SessionStorage sessionStorage;

    public AuthInterceptor(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        if (LOGIN_PATH.equals(request.url().encodedPath())) {
            return chain.proceed(request);
        }

        UserSession session = sessionStorage.read();
        if (session == null) {
            return chain.proceed(request);
        }

        if (session.isExpired(System.currentTimeMillis())) {
            sessionStorage.clear();
            return chain.proceed(request);
        }

        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", session.getTokenType() + " " + session.getAccessToken())
                .build();
        Response response = chain.proceed(authenticatedRequest);
        if (response.code() == 401) {
            sessionStorage.clear();
        }
        return response;
    }
}
