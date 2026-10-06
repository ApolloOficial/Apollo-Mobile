package org.apollo.mobile.home.gateway;

public final class HomeServiceOrder {

    private long id;
    private String code;
    private String assetCode;
    private String status;
    private String priority;
    private String dueDate;

    public HomeServiceOrder() {
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public String getDueDate() {
        return dueDate;
    }
}
