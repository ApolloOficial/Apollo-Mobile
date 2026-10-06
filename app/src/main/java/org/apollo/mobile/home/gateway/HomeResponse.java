package org.apollo.mobile.home.gateway;

import java.util.Collections;
import java.util.List;

public final class HomeResponse {

    private String technicianName;
    private String profileImageUrl;
    private String siteName;
    private String siteCity;
    private String siteState;

    private int totalServiceOrders;
    private List<HomeServiceOrder> serviceOrders;

    private int totalAttentionItems;
    private List<HomeAttentionItem> attentionItems;

    private List<HomePendingRegistration> pendingRegistrations;

    public HomeResponse() {
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getSiteCity() {
        return siteCity;
    }

    public String getSiteState() {
        return siteState;
    }

    public int getTotalServiceOrders() {
        return totalServiceOrders;
    }

    public List<HomeServiceOrder> getServiceOrders() {
        return serviceOrders == null
                ? Collections.emptyList()
                : serviceOrders;
    }

    public int getTotalAttentionItems() {
        return totalAttentionItems;
    }

    public List<HomeAttentionItem> getAttentionItems() {
        return attentionItems == null
                ? Collections.emptyList()
                : attentionItems;
    }

    public List<HomePendingRegistration> getPendingRegistrations() {
        return pendingRegistrations == null
                ? Collections.emptyList()
                : pendingRegistrations;
    }

    public String getFormattedLocation() {
        StringBuilder location = new StringBuilder();

        if (siteName != null && !siteName.trim().isEmpty()) {
            location.append(siteName.trim());
        }

        if (siteCity != null && !siteCity.trim().isEmpty()) {
            if (location.length() > 0) {
                location.append(", ");
            }

            location.append(siteCity.trim());
        }

        if (siteState != null && !siteState.trim().isEmpty()) {
            location.append(" - ").append(siteState.trim());
        }

        return location.toString();
    }
}