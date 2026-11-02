package org.apollo.mobile.push;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PushTextTest {

    @Test
    public void prefersPrimaryWhenFilled() {
        assertEquals("Nova OS", PushText.pick("Nova OS", "Outro"));
    }

    @Test
    public void trimsTheChosenValue() {
        assertEquals("Nova OS", PushText.pick("  Nova OS  ", null));
    }

    @Test
    public void fallsBackToSecondaryWhenPrimaryIsBlank() {
        assertEquals("Alerta", PushText.pick("   ", "Alerta"));
        assertEquals("Alerta", PushText.pick(null, "Alerta"));
    }

    @Test
    public void returnsNullWhenBothAreEmpty() {
        assertNull(PushText.pick(null, null));
        assertNull(PushText.pick("", "  "));
    }

    @Test
    public void hasContentNeedsTitleOrBody() {
        assertTrue(PushText.hasContent("t", null));
        assertTrue(PushText.hasContent(null, "b"));
        assertFalse(PushText.hasContent(null, null));
    }
}
