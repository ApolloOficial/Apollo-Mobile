package org.apollo.mobile.panels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScanCodeTest {

    @Test
    public void normalizeStripsWhitespace() {
        assertEquals("7890101000015", ScanCode.normalize(" 7890101000015\n"));
        assertEquals("ABC123", ScanCode.normalize("AB C 123"));
    }

    @Test
    public void normalizeRejectsEmpty() {
        assertNull(ScanCode.normalize(null));
        assertNull(ScanCode.normalize("  \n"));
    }

    @Test
    public void ean13Checksum() {
        assertTrue(ScanCode.isValidEan13("7890101000015"));
        assertFalse(ScanCode.isValidEan13("7890101000016"));
        assertFalse(ScanCode.isValidEan13("789010100001"));
        assertFalse(ScanCode.isValidEan13(null));
    }
}
