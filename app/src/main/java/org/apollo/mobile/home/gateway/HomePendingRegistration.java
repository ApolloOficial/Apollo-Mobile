package org.apollo.mobile.home.gateway;

public final class HomePendingRegistration {

    private long batchId;
    private String batchCode;
    private int registered;
    private int total;

    public HomePendingRegistration() {
    }

    public long getBatchId() {
        return batchId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public int getRegistered() {
        return registered;
    }

    public int getTotal() {
        return total;
    }

    public int getRemaining() {
        return Math.max(0, total - registered);
    }

    public int getProgressPercentage() {
        if (total <= 0) {
            return 0;
        }

        return Math.min(100, registered * 100 / total);
    }
}