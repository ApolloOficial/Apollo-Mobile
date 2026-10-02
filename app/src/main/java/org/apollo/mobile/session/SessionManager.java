package org.apollo.mobile.session;

import org.apollo.mobile.auth.error.AuthError;
import org.apollo.mobile.auth.securty.JwtClaimsParser;
import org.apollo.mobile.auth.policy.UserRole;
import org.apollo.mobile.auth.gateway.AuthenticationGateway;
import org.apollo.mobile.auth.gateway.LoginResponse;
import org.apollo.mobile.auth.gateway.OtpMethod;
import org.apollo.mobile.auth.gateway.OtpVerifyResponse;

import java.util.ArrayList;
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

        authenticationGateway.login(normalizedEmail, password, new AuthenticationGateway.Callback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                if (response.isOtpRequired()) {
                    if (response.getChallengeId() == null || response.getMethods().isEmpty()) {
                        callback.onFailure(new AuthError(AuthError.Type.INVALID_RESPONSE));
                        return;
                    }
                    callback.onOtpRequired(response.getChallengeId(), new ArrayList<>(response.getMethods()));
                    return;
                }
                establishSession(response.getToken(), response.getTokenType(), response.getRole(), callback);
            }

            @Override
            public void onFailure(AuthError error) {
                callback.onFailure(error);
            }
        });
    }

    /** Segundo passo do login: confere o código e, se estiver certo, abre a sessão. */
    public void completeOtpLogin(String challengeId, String code, LoginCallback callback) {
        authenticationGateway.verifyOtp(challengeId, code, new AuthenticationGateway.Callback<OtpVerifyResponse>() {
            @Override
            public void onSuccess(OtpVerifyResponse response) {
                establishSession(response.getToken(), response.getTokenType(), response.getRole(), callback);
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

    private void establishSession(String token, String tokenType, String role, LoginCallback callback) {
        if (token == null || token.trim().isEmpty() || !"Bearer".equalsIgnoreCase(tokenType)) {
            callback.onFailure(new AuthError(AuthError.Type.INVALID_RESPONSE));
            return;
        }

        try {
            JwtClaimsParser.JwtClaims claims = jwtClaimsParser.parse(token);
            // A API publicada hoje não devolve "role" no corpo do login; o cargo vem no token.
            String effectiveRole = role != null && !role.trim().isEmpty() ? role : claims.getRole();
            if (effectiveRole == null || effectiveRole.trim().isEmpty()) {
                callback.onFailure(new AuthError(AuthError.Type.INVALID_RESPONSE));
                return;
            }
            UserSession session = new UserSession(
                    token,
                    "Bearer",
                    claims.getSubject(),
                    claims.getExpiresAtEpochMillis(),
                    UserRole.fromApiValue(effectiveRole)
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

        /** A conta exige o código de segurança: abra a tela de método/código com estes dados. */
        void onOtpRequired(String challengeId, ArrayList<OtpMethod> methods);

        void onFailure(AuthError error);
    }
}
