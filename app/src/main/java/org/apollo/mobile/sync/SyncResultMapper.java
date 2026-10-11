package org.apollo.mobile.sync;

public final class SyncResultMapper {
    private SyncResultMapper() {
    }

    public static SendResult fromStatus(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return SendResult.SENT;
        }
        if (statusCode == 401) {
            return SendResult.UNAUTHORIZED;
        }
        if (statusCode == 408 || statusCode == 429 || statusCode >= 500) {
            return SendResult.RETRY;
        }
        return SendResult.REJECTED;
    }
}
