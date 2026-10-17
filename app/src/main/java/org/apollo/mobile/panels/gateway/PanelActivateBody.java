package org.apollo.mobile.panels.gateway;

public final class PanelActivateBody {

    private final String barcode;
    private final String installationDt;

    public PanelActivateBody(String barcode, String installationDt) {
        this.barcode = barcode;
        this.installationDt = installationDt;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getInstallationDt() {
        return installationDt;
    }
}
