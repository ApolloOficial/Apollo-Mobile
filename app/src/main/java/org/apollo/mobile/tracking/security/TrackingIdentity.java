package org.apollo.mobile.tracking.security;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apollo.mobile.session.UserSession;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class TrackingIdentity {
    private TrackingIdentity() { }

    public static String owner(UserSession session) {
        if (session == null || session.isExpired(System.currentTimeMillis())) return null;
        try {
            String[] parts = session.getAccessToken().split("\\.");
            JsonObject claims = JsonParser.parseString(new String(
                    Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)).getAsJsonObject();
            String user = UUID.fromString(claims.get("userId").getAsString()).toString();
            long company = claims.get("companyId").getAsLong();
            return company + ":" + user;
        } catch (RuntimeException e) {
            return null;
        }
    }
}