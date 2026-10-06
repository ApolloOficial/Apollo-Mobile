package org.apollo.mobile.home.gateway;

public final class HomeAttentionItem {

    private long id;
    private String severity;
    private String title;
    private String metricLabel;
    private double metricValue;
    private long serviceOrderId;

    public HomeAttentionItem() {
    }

    public long getId() {
        return id;
    }

    public String getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getMetricLabel() {
        return metricLabel;
    }

    public double getMetricValue() {
        return metricValue;
    }

    public long getServiceOrderId() {
        return serviceOrderId;
    }

    public int getProgressValue() {
        return (int) Math.max(0, Math.min(100, Math.round(metricValue)));
    }
}