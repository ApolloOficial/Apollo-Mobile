package org.apollo.mobile.sync;

public final class PendingAction {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_FAILED = "FAILED";

    private String id;
    private String method;
    private String path;
    private String body;
    private long createdAt;
    private int attempts;
    private String status;

    public PendingAction() {
    }

    public PendingAction(String id, String method, String path, String body, long createdAt) {
        this.id = id;
        this.method = method;
        this.path = path;
        this.body = body;
        this.createdAt = createdAt;
        this.attempts = 0;
        this.status = STATUS_PENDING;
    }

    public String getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body == null ? "" : body;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }

    public void markAttempt() {
        attempts++;
    }

    public void markFailed() {
        status = STATUS_FAILED;
    }

    public int sizeInBytes() {
        return getBody().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
    }
}
