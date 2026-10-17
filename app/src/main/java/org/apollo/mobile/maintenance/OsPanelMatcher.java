package org.apollo.mobile.maintenance;

import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OsPanelMatcher {

    public static final int MIN_SUFFIX = 4;

    private OsPanelMatcher() {
    }

    public static String canonical(String value) {
        if (value == null) return "";
        return value.replaceAll("[\\s\\-]+", "").toUpperCase(Locale.ROOT);
    }

    public static Panel byCode(List<Panel> panels, String code) {
        String target = canonical(code);
        if (target.isEmpty()) return null;
        for (Panel panel : panels) {
            if (target.equals(canonical(panel.getBarcode())) || target.equals(canonical(panel.getSerialNumber()))) {
                return panel;
            }
        }
        return null;
    }

    public static List<Panel> bySerialEnd(List<Panel> panels, String typed) {
        List<Panel> result = new ArrayList<>();
        String suffix = canonical(typed);
        if (suffix.length() < MIN_SUFFIX) return result;
        for (Panel panel : panels) {
            if (canonical(panel.getSerialNumber()).endsWith(suffix)) result.add(panel);
        }
        return result;
    }
}
