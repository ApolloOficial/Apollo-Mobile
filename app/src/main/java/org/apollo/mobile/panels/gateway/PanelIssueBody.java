package org.apollo.mobile.panels.gateway;

public final class PanelIssueBody {

    private final String barcode;
    private final String type;
    private final String severity;
    private final String message;

    public PanelIssueBody(String barcode, String type, String severity, String message) {
        this.barcode = barcode;
        this.type = type;
        this.severity = severity;
        this.message = message;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getType() {
        return type;
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }
}
