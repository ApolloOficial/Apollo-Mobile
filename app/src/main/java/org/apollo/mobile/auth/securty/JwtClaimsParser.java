package org.apollo.mobile.auth.securty;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class JwtClaimsParser {

    public JwtClaims parse(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token é obrigatório");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Token precisa conter três partes");
        }

        try {
            String payload = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8
            );
            JsonObject claims = JsonParser.parseString(payload).getAsJsonObject();
            String subject = claims.get("sub").getAsString();
            if (subject.trim().isEmpty()) {
                throw new IllegalArgumentException("O conteúdo do Token é obrigatório");
            }
            long expirationInSeconds = claims.get("exp").getAsLong();
            long expirationInMillis = Math.multiplyExact(expirationInSeconds, 1_000L);
            return new JwtClaims(subject, expirationInMillis);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Payload do Token é inválido", exception);
        }
    }

    public static final class JwtClaims {

        private final String subject;
        private final long expiresAtEpochMillis;

        private JwtClaims(String subject, long expiresAtEpochMillis) {
            this.subject = subject;
            this.expiresAtEpochMillis = expiresAtEpochMillis;
        }

        public String getSubject() {
            return subject;
        }

        public long getExpiresAtEpochMillis() {
            return expiresAtEpochMillis;
        }
    }
}
