package org.apollo.mobile.notifications;

import com.google.gson.JsonElement;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class NotificationItem {
    private String id;
    private String maintenanceId;
    private String type;
    private String title;
    private String body;
    private Boolean read;
    private JsonElement createdAt;

    public String getId() { return id; }
    public String getMaintenanceId() { return maintenanceId; }
    public String getType() { return type == null ? "" : type; }
    public String getTitle() { return title == null ? "" : title; }
    public String getBody() { return body == null ? "" : body; }
    public boolean isRead() { return read != null && read; }
    public void markRead() { read = true; }

    public LocalDateTime createdAtLocal() {
        if (createdAt == null || createdAt.isJsonNull()) return null;
        try {
            if (createdAt.isJsonPrimitive() && createdAt.getAsJsonPrimitive().isNumber()) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt.getAsLong()), ZoneId.systemDefault());
            }
            String raw = createdAt.getAsString();
            try {
                return OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            } catch (Exception ignored) {
                return LocalDateTime.parse(raw.replace(' ', 'T'));
            }
        } catch (Exception e) {
            return null;
        }
    }
}
