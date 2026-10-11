package org.apollo.mobile.settings.gateway;

public final class NotificationPreferences {
    private Boolean predictiveMaintenance;
    private Boolean panelAlert;
    private Boolean emailSummary;

    public NotificationPreferences() {
    }

    public NotificationPreferences(Boolean predictiveMaintenance, Boolean panelAlert, Boolean emailSummary) {
        this.predictiveMaintenance = predictiveMaintenance;
        this.panelAlert = panelAlert;
        this.emailSummary = emailSummary;
    }

    public static NotificationPreferences onlyPredictiveMaintenance(boolean value) {
        return new NotificationPreferences(value, null, null);
    }

    public static NotificationPreferences onlyPanelAlert(boolean value) {
        return new NotificationPreferences(null, value, null);
    }

    public static NotificationPreferences onlyEmailSummary(boolean value) {
        return new NotificationPreferences(null, null, value);
    }

    public boolean isPredictiveMaintenance() {
        return Boolean.TRUE.equals(predictiveMaintenance);
    }

    public boolean isPanelAlert() {
        return Boolean.TRUE.equals(panelAlert);
    }

    public boolean isEmailSummary() {
        return Boolean.TRUE.equals(emailSummary);
    }
}
