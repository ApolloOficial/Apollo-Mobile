package org.apollo.mobile.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class GreetingNameTest {
    @Test
    public void usesFirstNameFromDottedEmail() {
        assertEquals("Enzo", GreetingName.firstName("enzo.mota@empresa.dominio"));
        assertEquals("Maria", GreetingName.firstName("MARIA_silva@empresa.com"));
        assertEquals("José", GreetingName.firstName("josé-santos@empresa.com"));
    }

    @Test
    public void returnsNullWhenEmailDoesNotLookLikeAName() {
        assertNull(GreetingName.firstName(null));
        assertNull(GreetingName.firstName(""));
        assertNull(GreetingName.firstName("admin@empresa.com"));
        assertNull(GreetingName.firstName("tecnico01.sp@empresa.com"));
        assertNull(GreetingName.firstName("a.b@empresa.com"));
        assertNull(GreetingName.firstName("sem-arroba"));
        assertNull(GreetingName.firstName("@empresa.com"));
    }
}
