package org.apollo.mobile.tracking.session;

import android.os.SystemClock;
import org.apollo.mobile.session.UserSession;
import org.apollo.mobile.tracking.security.TrackingIdentity;

import java.util.UUID;

public final class TrackingSessionManager {
    private static final long TIMEOUT_MS = 30 * 60 * 1000L;
    private String lastToken;
    private String id;
    private long lastUse;
    private boolean pendingOpen;

    public synchronized String touch(UserSession session) {
        long now = SystemClock.elapsedRealtime();
        if (session == null || TrackingIdentity.owner(session) == null) return null;
        if (id == null || !session.getAccessToken().equals(lastToken) || now - lastUse >= TIMEOUT_MS) {
            id = UUID.randomUUID().toString();
            lastToken = session.getAccessToken();
            pendingOpen = true;
        }
        lastUse = now;
        return id;
    }

    public synchronized boolean takeOpen() {
        boolean value = pendingOpen;
        pendingOpen = false;
        return value;
    }
}