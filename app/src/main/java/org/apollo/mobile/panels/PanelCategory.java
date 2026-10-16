package org.apollo.mobile.panels;

import org.apollo.mobile.panels.gateway.PanelItem;

public enum PanelCategory {
    STOCK,
    IN_USE,
    ATTENTION;

    public static PanelCategory of(PanelItem panel) {
        String status = panel.getOperatingStats();
        if ("EM_ESTOQUE".equals(status)) {
            return STOCK;
        }
        if ("FALHA".equals(status) || "MANUTENCAO".equals(status) || "INATIVO".equals(status)
                || panel.getActiveWarnings() > 0) {
            return ATTENTION;
        }
        return IN_USE;
    }
}
