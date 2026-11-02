package org.apollo.mobile.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IdempotencyPolicyTest {

    @Test
    public void attachesToAuthenticatedWrites() {
        assertTrue(IdempotencyPolicy.shouldAttach("POST", "/api/v1/panels/report-issue", false));
        assertTrue(IdempotencyPolicy.shouldAttach("PUT", "/api/v1/settings/photo", false));
        assertTrue(IdempotencyPolicy.shouldAttach("PATCH", "/api/v1/maintenance-registers/12/start", false));
        assertTrue(IdempotencyPolicy.shouldAttach("DELETE", "/api/v1/devices/push-tokens", false));
    }

    @Test
    public void isCaseInsensitiveOnMethod() {
        assertTrue(IdempotencyPolicy.shouldAttach("post", "/api/v1/panels/report-issue", false));
    }

    @Test
    public void skipsReads() {
        assertFalse(IdempotencyPolicy.shouldAttach("GET", "/api/v1/notifications", false));
        assertFalse(IdempotencyPolicy.shouldAttach("HEAD", "/api/v1/notifications", false));
    }

    @Test
    public void keepsExistingKey() {
        assertFalse(IdempotencyPolicy.shouldAttach("POST", "/api/v1/panels/report-issue", true));
    }

    @Test
    public void skipsPublicAuthRoutes() {
        assertFalse(IdempotencyPolicy.shouldAttach("POST", "/api/v1/mobile/auth/login", false));
        assertFalse(IdempotencyPolicy.shouldAttach("POST", "/api/v1/auth/login", false));
    }

    @Test
    public void skipsRoutesOutsideTheApi() {
        assertFalse(IdempotencyPolicy.shouldAttach("POST", "/actuator/health", false));
    }

    @Test
    public void toleratesNulls() {
        assertFalse(IdempotencyPolicy.shouldAttach(null, "/api/v1/x", false));
        assertFalse(IdempotencyPolicy.shouldAttach("POST", null, false));
    }
}
