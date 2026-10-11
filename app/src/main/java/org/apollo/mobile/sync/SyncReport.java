package org.apollo.mobile.sync;

public final class SyncReport {
    public enum Stop {
        NONE,
        NETWORK,
        UNAUTHORIZED
    }

    private final int sent;
    private final int rejected;
    private final int remaining;
    private final Stop stop;

    public SyncReport(int sent, int rejected, int remaining, Stop stop) {
        this.sent = sent;
        this.rejected = rejected;
        this.remaining = remaining;
        this.stop = stop;
    }

    public int getSent() {
        return sent;
    }

    public int getRejected() {
        return rejected;
    }

    public int getRemaining() {
        return remaining;
    }

    public Stop getStop() {
        return stop;
    }
}
