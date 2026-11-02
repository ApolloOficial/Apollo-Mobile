package org.apollo.mobile.api;

import java.util.Locale;

final class IdempotencyPolicy {
    static final String HEADER = "Idempotency-Key";

    private static final String API_PREFIX = "/api/v1/";
    private static final String PUBLIC_PATH = "/api/v1/mobile/auth/";
    private static final String LEGACY_LOGIN_PATH = "/api/v1/auth/login";

    private IdempotencyPolicy() {
    }

    static boolean shouldAttach(String method, String path, boolean alreadyHasKey) {
        if (alreadyHasKey || method == null || path == null) {
            return false;
        }
        String verb = method.toUpperCase(Locale.ROOT);
        boolean write = verb.equals("POST")
                || verb.equals("PUT")
                || verb.equals("PATCH")
                || verb.equals("DELETE");
        if (!write || !path.contains(API_PREFIX)) {
            return false;
        }
        return !path.contains(PUBLIC_PATH) && !path.endsWith(LEGACY_LOGIN_PATH);
    }
}
