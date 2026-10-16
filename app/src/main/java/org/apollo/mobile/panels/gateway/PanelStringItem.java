package org.apollo.mobile.panels.gateway;

public final class PanelStringItem {

    private String id;
    private String code;
    private String inverterCode;
    private Double nominalPowerKwp;
    private Long installedPanels;
    private Long panelsOperational;
    private Long panelsInMaintenance;
    private Long panelsInStock;
    private Long activeWarnings;
    private Double healthScore;

    public PanelStringItem() {
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code == null ? "" : code;
    }

    public String getInverterCode() {
        return inverterCode == null ? "" : inverterCode;
    }

    public double getNominalPowerKwp() {
        return nominalPowerKwp == null ? 0d : nominalPowerKwp;
    }

    public long getInstalledPanels() {
        return installedPanels == null ? 0L : installedPanels;
    }

    public long getPanelsOperational() {
        return panelsOperational == null ? 0L : panelsOperational;
    }

    public long getPanelsInMaintenance() {
        return panelsInMaintenance == null ? 0L : panelsInMaintenance;
    }

    public long getPanelsInStock() {
        return panelsInStock == null ? 0L : panelsInStock;
    }

    public long getActiveWarnings() {
        return activeWarnings == null ? 0L : activeWarnings;
    }

    public Double getHealthScore() {
        return healthScore;
    }
}
