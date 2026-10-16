package org.apollo.mobile.panels;

import org.apollo.mobile.panels.gateway.PanelItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum PanelFilter {
    ALL(null),
    STOCK(PanelCategory.STOCK),
    IN_USE(PanelCategory.IN_USE),
    ATTENTION(PanelCategory.ATTENTION);

    private final PanelCategory category;

    PanelFilter(PanelCategory category) {
        this.category = category;
    }

    public List<PanelItem> apply(List<PanelItem> panels, String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<PanelItem> result = new ArrayList<>();
        for (PanelItem panel : panels) {
            if (category != null && PanelCategory.of(panel) != category) {
                continue;
            }
            if (!needle.isEmpty() && !panel.getSerialNumber().toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            result.add(panel);
        }
        return result;
    }
}
