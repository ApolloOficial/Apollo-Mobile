package org.apollo.mobile.panels.gateway;

public final class PanelItem {

    private Long id;
    private String stringId;
    private String stringCode;
    private String inverterCode;
    private String serialNumber;
    private String barcode;
    private String manufacturer;
    private String model;
    private String operatingStats;
    private String installationDt;
    private Double stringHealthScore;
    private Long activeWarnings;

    public PanelItem() {
    }

    public PanelItem(long id, String serialNumber, String barcode, String operatingStats, long activeWarnings) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.barcode = barcode;
        this.operatingStats = operatingStats;
        this.activeWarnings = activeWarnings;
    }

    public long getId() {
        return id == null ? 0L : id;
    }

    public String getStringId() {
        return stringId;
    }

    public String getStringCode() {
        return stringCode == null ? "" : stringCode;
    }

    public String getInverterCode() {
        return inverterCode == null ? "" : inverterCode;
    }

    public String getSerialNumber() {
        return serialNumber == null ? "" : serialNumber;
    }

    public String getBarcode() {
        return barcode == null ? "" : barcode;
    }

    public String getManufacturer() {
        return manufacturer == null ? "" : manufacturer;
    }

    public String getModel() {
        return model == null ? "" : model;
    }

    public String getOperatingStats() {
        return operatingStats == null ? "" : operatingStats;
    }

    public String getInstallationDt() {
        return installationDt;
    }

    public Double getStringHealthScore() {
        return stringHealthScore;
    }

    public long getActiveWarnings() {
        return activeWarnings == null ? 0L : activeWarnings;
    }
}
