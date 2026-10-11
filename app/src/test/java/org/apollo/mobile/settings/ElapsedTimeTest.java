package org.apollo.mobile.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apollo.mobile.settings.format.ElapsedTime;
import org.junit.Test;

import java.time.LocalDateTime;

public class ElapsedTimeTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 9, 12, 0, 0);

    @Test
    public void returnsNullForBlankOrInvalid() {
        assertNull(ElapsedTime.between(null, NOW));
        assertNull(ElapsedTime.between("  ", NOW));
        assertNull(ElapsedTime.between("ontem", NOW));
    }

    @Test
    public void recentIsNow() {
        assertEquals(ElapsedTime.Unit.NOW, ElapsedTime.between("2026-10-09T11:59:30", NOW).getUnit());
        assertEquals(ElapsedTime.Unit.NOW, ElapsedTime.between("2026-10-09T12:05:00", NOW).getUnit());
    }

    @Test
    public void picksTheLargestUnit() {
        ElapsedTime minutes = ElapsedTime.between("2026-10-09T11:15:00", NOW);
        assertEquals(ElapsedTime.Unit.MINUTES, minutes.getUnit());
        assertEquals(45, minutes.getAmount());

        ElapsedTime hours = ElapsedTime.between("2026-10-09T07:52:00", NOW);
        assertEquals(ElapsedTime.Unit.HOURS, hours.getUnit());
        assertEquals(4, hours.getAmount());

        ElapsedTime days = ElapsedTime.between("2026-10-02T12:00:00", NOW);
        assertEquals(ElapsedTime.Unit.DAYS, days.getUnit());
        assertEquals(7, days.getAmount());

        ElapsedTime months = ElapsedTime.between("2026-07-09T12:00:00", NOW);
        assertEquals(ElapsedTime.Unit.MONTHS, months.getUnit());
        assertEquals(3, months.getAmount());

        ElapsedTime years = ElapsedTime.between("2024-10-09T12:00:00", NOW);
        assertEquals(ElapsedTime.Unit.YEARS, years.getUnit());
        assertEquals(2, years.getAmount());
    }

    @Test
    public void parsesFractionsAndOffsets() {
        assertNotNull(ElapsedTime.parse("2026-10-09T11:15:00.123456"));
        assertNotNull(ElapsedTime.parse("2026-10-09T11:15:00Z"));
    }
}
