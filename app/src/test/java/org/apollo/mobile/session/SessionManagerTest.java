package org.apollo.mobile.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.apollo.mobile.auth.error.AuthError;
import org.apollo.mobile.auth.gateway.AuthenticationGateway;
import org.apollo.mobile.auth.gateway.LoginResponse;
import org.apollo.mobile.auth.gateway.OtpMethod;
import org.apollo.mobile.auth.gateway.OtpSentResponse;
import org.apollo.mobile.auth.gateway.OtpVerifyResponse;
import org.apollo.mobile.auth.gateway.RecoveryResponse;
import org.apollo.mobile.auth.policy.UserRole;
import org.apollo.mobile.auth.securty.JwtClaimsParser;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class SessionManagerTest {
    private static final long NOW = 1_800_000_000_000L;

    private final Gson gson = new Gson();
    private FakeGateway gateway;
    private FakeStorage storage;
    private SessionManager manager;
    private Result result;

    @Before
    public void setUp() {
        gateway = new FakeGateway();
        storage = new FakeStorage();
        manager = new SessionManager(gateway, storage, new JwtClaimsParser(), () -> NOW);
        result = new Result();
    }

    private static String jwt(long expSeconds) {
        return jwt(expSeconds, null);
    }

    private static String jwt(long expSeconds, String role) {
        String payload = "{\"sub\":\"tec@apollo.local\",\"exp\":" + expSeconds
                + (role == null ? "" : ",\"role\":\"" + role + "\"") + "}";
        return "h." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + ".s";
    }

    @Test
    public void loginWithOtpRequiredDoesNotCreateSession() {
        gateway.login = gson.fromJson("{\"status\":\"OTP_REQUIRED\",\"challengeId\":\"c1\","
                + "\"methods\":[{\"method\":\"EMAIL\",\"destination\":\"t***@apollo.local\"}]}", LoginResponse.class);

        manager.login("tec@apollo.local", "Senha#123", result);

        assertEquals("c1", result.challengeId);
        assertEquals(1, result.methods.size());
        assertTrue(result.methods.get(0).isEmail());
        assertNull(storage.saved);
        assertNull(result.session);
    }

    @Test
    public void loginAuthenticatedStoresSession() {
        gateway.login = gson.fromJson("{\"status\":\"AUTHENTICATED\",\"token\":\"" + jwt(NOW / 1000 + 3600)
                + "\",\"tokenType\":\"Bearer\",\"role\":\"TECNICO\"}", LoginResponse.class);

        manager.login("tec@apollo.local", "Senha#123", result);

        assertNotNull(storage.saved);
        assertEquals(UserRole.TECHNICIAN, result.session.getRole());
        assertEquals("tec@apollo.local", result.session.getEmail());
    }

    @Test
    public void roleFromTokenIsUsedWhenTheLoginBodyHasNone() {
        gateway.login = gson.fromJson("{\"token\":\"" + jwt(NOW / 1000 + 3600, "TECHNICIAN")
                + "\",\"tokenType\":\"Bearer\"}", LoginResponse.class);

        manager.login("tec@apollo.local", "Senha#123", result);

        assertEquals(UserRole.TECHNICIAN, result.session.getRole());
    }

    @Test
    public void loginWithoutRoleAnywhereIsAnInvalidResponse() {
        gateway.login = gson.fromJson("{\"token\":\"" + jwt(NOW / 1000 + 3600)
                + "\",\"tokenType\":\"Bearer\"}", LoginResponse.class);

        manager.login("tec@apollo.local", "Senha#123", result);

        assertEquals(AuthError.Type.INVALID_RESPONSE, result.error.getType());
    }

    @Test
    public void managerRoleGetsUnknownRoleSoTheAppDeniesAccess() {
        gateway.login = gson.fromJson("{\"status\":\"AUTHENTICATED\",\"token\":\"" + jwt(NOW / 1000 + 3600)
                + "\",\"tokenType\":\"Bearer\",\"role\":\"GERENTE\"}", LoginResponse.class);

        manager.login("g@apollo.local", "Senha#123", result);

        assertEquals(UserRole.UNKNOWN, result.session.getRole());
    }

    @Test
    public void completingOtpLoginStoresSession() {
        OtpVerifyResponse verify = gson.fromJson("{\"token\":\"" + jwt(NOW / 1000 + 3600)
                + "\",\"tokenType\":\"Bearer\",\"role\":\"TECNICO\"}", OtpVerifyResponse.class);
        gateway.verify = verify;

        manager.completeOtpLogin("c1", "1234", result);

        assertEquals("c1", gateway.verifiedChallenge);
        assertEquals("1234", gateway.verifiedCode);
        assertNotNull(storage.saved);
        assertNotNull(result.session);
    }

    @Test
    public void wrongCodePropagatesTheError() {
        gateway.verifyError = new AuthError(AuthError.Type.OTP_INVALID);

        manager.completeOtpLogin("c1", "0000", result);

        assertEquals(AuthError.Type.OTP_INVALID, result.error.getType());
        assertNull(storage.saved);
    }

    @Test
    public void expiredTokenIsRejected() {
        gateway.login = gson.fromJson("{\"status\":\"AUTHENTICATED\",\"token\":\"" + jwt(NOW / 1000 - 10)
                + "\",\"tokenType\":\"Bearer\",\"role\":\"TECNICO\"}", LoginResponse.class);

        manager.login("tec@apollo.local", "Senha#123", result);

        assertEquals(AuthError.Type.EXPIRED_TOKEN, result.error.getType());
    }

    @Test
    public void blankFieldsFailValidationWithoutCallingTheApi() {
        manager.login(" ", "", result);

        assertEquals(AuthError.Type.VALIDATION, result.error.getType());
        assertFalse(gateway.loginCalled);
    }

    @Test
    public void otpRequiredWithoutMethodsIsAnInvalidResponse() {
        gateway.login = gson.fromJson("{\"status\":\"OTP_REQUIRED\",\"challengeId\":\"c1\"}", LoginResponse.class);

        manager.login("tec@apollo.local", "Senha#123", result);

        assertEquals(AuthError.Type.INVALID_RESPONSE, result.error.getType());
    }

    private static final class Result implements SessionManager.LoginCallback {
        UserSession session;
        String challengeId;
        ArrayList<OtpMethod> methods;
        AuthError error;

        @Override
        public void onSuccess(UserSession session) {
            this.session = session;
        }

        @Override
        public void onOtpRequired(String challengeId, ArrayList<OtpMethod> methods) {
            this.challengeId = challengeId;
            this.methods = methods;
        }

        @Override
        public void onFailure(AuthError error) {
            this.error = error;
        }
    }

    private static final class FakeStorage implements SessionStorage {
        UserSession saved;

        @Override
        public UserSession read() {
            return saved;
        }

        @Override
        public void save(UserSession session) {
            saved = session;
        }

        @Override
        public void clear() {
            saved = null;
        }
    }

    private static final class FakeGateway implements AuthenticationGateway {
        LoginResponse login;
        OtpVerifyResponse verify;
        AuthError verifyError;
        boolean loginCalled;
        String verifiedChallenge;
        String verifiedCode;

        @Override
        public void login(String email, String password, Callback<LoginResponse> callback) {
            loginCalled = true;
            callback.onSuccess(login);
        }

        @Override
        public void startRecovery(String email, Callback<RecoveryResponse> callback) {
        }

        @Override
        public void sendOtp(String challengeId, String method, Callback<OtpSentResponse> callback) {
        }

        @Override
        public void verifyOtp(String challengeId, String code, Callback<OtpVerifyResponse> callback) {
            verifiedChallenge = challengeId;
            verifiedCode = code;
            if (verifyError != null) {
                callback.onFailure(verifyError);
            } else {
                callback.onSuccess(verify);
            }
        }

        @Override
        public void resetPassword(String resetToken, String newPassword, Callback<Void> callback) {
        }
    }
}
