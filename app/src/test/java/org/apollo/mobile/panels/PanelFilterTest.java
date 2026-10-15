package org.apollo.mobile.panels;

import static org.junit.Assert.assertEquals;

import org.apollo.mobile.panels.gateway.PanelItem;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PanelFilterTest {
    private final List<PanelItem> panels = Arrays.asList(
            new PanelItem(1, "CS-2025-00841", "b1", "OPERACIONAL", 0),
            new PanelItem(2, "CS-2026-00932", "b2", "EM_ESTOQUE", 0),
            new PanelItem(3, "CS-2023-00724", "b3", "OPERACIONAL", 2),
            new PanelItem(4, "CS-2024-00820", "b4", "FALHA", 0),
            new PanelItem(5, "CS-2025-00836", "b5", "MANUTENCAO", 0)
    );

    @Test
    public void allKeepsEverything() {
        assertEquals(5, PanelFilter.ALL.apply(panels, "").size());
    }

    @Test
    public void stockOnlyReturnsStock() {
        List<PanelItem> result = PanelFilter.STOCK.apply(panels, "");
        assertEquals(1, result.size());
        assertEquals("CS-2026-00932", result.get(0).getSerialNumber());
    }

    @Test
    public void inUseExcludesAttentionAndStock() {
        List<PanelItem> result = PanelFilter.IN_USE.apply(panels, "");
        assertEquals(1, result.size());
        assertEquals("CS-2025-00841", result.get(0).getSerialNumber());
    }

    @Test
    public void attentionIncludesWarningsFailureAndMaintenance() {
        assertEquals(3, PanelFilter.ATTENTION.apply(panels, "").size());
    }

    @Test
    public void filtersAreMutuallyExclusive() {
        int total = PanelFilter.STOCK.apply(panels, "").size()
                + PanelFilter.IN_USE.apply(panels, "").size()
                + PanelFilter.ATTENTION.apply(panels, "").size();
        assertEquals(panels.size(), total);
    }

    @Test
    public void searchMatchesSerialNumberIgnoringCase() {
        List<PanelItem> result = PanelFilter.ALL.apply(panels, " cs-2023 ");
        assertEquals(1, result.size());
        assertEquals("CS-2023-00724", result.get(0).getSerialNumber());
    }

    @Test
    public void searchCombinesWithFilter() {
        assertEquals(0, PanelFilter.STOCK.apply(panels, "2025").size());
        assertEquals(1, PanelFilter.STOCK.apply(panels, "2026").size());
    }

    @Test
    public void nullQueryIsIgnored() {
        assertEquals(5, PanelFilter.ALL.apply(panels, null).size());
    }
}
