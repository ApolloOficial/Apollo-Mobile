package org.apollo.mobile.auth.policy;

import java.text.Normalizer;
import java.util.Locale;

public enum UserRole {
    TECHNICIAN("Técnico"),
    UNKNOWN("Desconhecido");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromApiValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);

        if ("tecnico".equals(normalized) || "technician".equals(normalized)) {
            return TECHNICIAN;
        }
        return UNKNOWN;
    }
}
