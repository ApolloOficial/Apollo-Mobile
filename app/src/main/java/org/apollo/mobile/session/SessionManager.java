package org.apollo.mobile.session;

import org.apollo.mobile.auth.error.AuthError;
import org.apollo.mobile.auth.securty.JwtClaimsParser;
import org.apollo.mobile.auth.policy.UserRole;
import org.apollo.mobile.auth.gateway.AuthenticationGateway;
import org.apollo.mobile.auth.gateway.LoginResponse;

import java.util.function.LongSupplier;

public final class SessionManager {

    private final AuthenticationGateway authenticationGateway;
    private final SessionStorage sessionStorage;
    private final JwtClaimsParser jwtClaimsParser;
    private final LongSupplier currentTimeMillis;

    public SessionManager(
            AuthenticationGateway authenticationGateway,
            SessionStorage sessionStorage,
            JwtClaimsParser jwtClaimsParser
    ) {
        this(authenticationGateway, sessionStorage, jwtClaimsParser, System::currentTimeMillis);
    }

    SessionManager(
            AuthenticationGateway authenticationGateway,
            SessionStorage sessionStorage,
            JwtClaimsParser jwtClaimsParser,
            LongSupplier currentTimeMillis
    ) {
        this.authenticationGateway = authenticationGateway;
        this.sessionStorage = sessionStorage;
        this.jwtClaimsParser = jwtClaimsParser;
        this.currentTimeMillis = currentTimeMillis;
    }

    public void login(String email, String password, LoginCallback callback) {
        String normalizedEmail = email == null ? "" : email.trim();
        if (normalizedEmail.isEmpty() || password == null || password.isEmpty()) {
            callback.onFailure(new AuthError(AuthError.Type.VALIDATION));
            return;
        }

        authenticationGateway.login(normalizedEmail, password, new AuthenticationGateway.Callback() {
            @Override
            public void onSuccess(LoginResponse response) {
                establishSession(response, callback);
            }

            @Override
            public void onFailure(AuthError error) {
                callback.onFailure(error);
            }
        });
    }

    public UserSession getActiveSession() {
        UserSession session = sessionStorage.read();
        if (session != null && session.isExpired(currentTimeMillis.getAsLong())) {
            sessionStorage.clear();
            return null;
        }
        return session;
    }

    public boolean hasActiveSession() {
        return getActiveSession() != null;
    }

    public void logout() {
        sessionStorage.clear();
    }

    private void establishSession(LoginResponse response, LoginCallback callback) {
        if (response == null || response.getToken() == null || response.getToken().trim().isEmpty()
                || !"Bearer".equalsIgnoreCase(response.getTokenType())
                || response.getRole() == null || response.getRole().trim().isEmpty()) {
            callback.onFailure(new AuthError(AuthError.Type.INVALID_RESPONSE));
            return;
        }

        try {
            JwtClaimsParser.JwtClaims claims = jwtClaimsParser.parse(response.getToken());
            UserSession session = new UserSession(
                    response.getToken(),
                    "Bearer",
                    claims.getSubject(),
                    claims.getExpiresAtEpochMillis(),
                    UserRole.fromApiValue(response.getRole())
            );
            if (session.isExpired(currentTimeMillis.getAsLong())) {
                callback.onFailure(new AuthError(AuthError.Type.EXPIRED_TOKEN));
                return;
            }

            sessionStorage.save(session);
            callback.onSuccess(session);
        } catch (IllegalArgumentException exception) {
            callback.onFailure(new AuthError(AuthError.Type.INVALID_RESPONSE));
        } catch (IllegalStateException exception) {
            callback.onFailure(new AuthError(AuthError.Type.STORAGE));
        }
    }

    public interface LoginCallback {
        void onSuccess(UserSession session);

        void onFailure(AuthError error);
    }
}
