package org.apollo.mobile.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apollo.mobile.settings.format.PhoneInput;
import org.junit.Test;

public class PhoneInputTest {
    @Test
    public void stripsMaskAndCountryCode() {
        assertEquals("85987654321", PhoneInput.digits("+55 (85) 98765-4321"));
        assertEquals("8532221111", PhoneInput.digits("(85) 3222-1111"));
        assertEquals("", PhoneInput.digits(null));
    }

    @Test
    public void acceptsMobileAndLandline() {
        assertTrue(PhoneInput.isValid("(85) 98765-4321"));
        assertTrue(PhoneInput.isValid("85 3222 1111"));
        assertTrue(PhoneInput.isValid("+5585987654321"));
    }

    @Test
    public void rejectsInvalidNumbers() {
        assertFalse(PhoneInput.isValid(null));
        assertFalse(PhoneInput.isValid(""));
        assertFalse(PhoneInput.isValid("12345"));
        assertFalse(PhoneInput.isValid("(85) 88765-4321"));
        assertFalse(PhoneInput.isValid("(05) 98765-4321"));
        assertFalse(PhoneInput.isValid("(80) 98765-4321"));
    }

    @Test
    public void formatsForDisplay() {
        assertEquals("(85) 98765-4321", PhoneInput.format("85987654321"));
        assertEquals("(85) 3222-1111", PhoneInput.format("8532221111"));
        assertEquals("123", PhoneInput.format("123"));
    }
}
