package org.apollo.mobile.chat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SuggestionRotationTest {
    @Test
    public void startsAtFirstItem() {
        assertEquals(0, new SuggestionRotation(5).current());
    }

    @Test
    public void advancesAndWrapsAround() {
        SuggestionRotation rotation = new SuggestionRotation(3);
        assertEquals(1, rotation.next());
        assertEquals(2, rotation.next());
        assertEquals(0, rotation.next());
    }

    @Test
    public void singleItemStaysInPlace() {
        SuggestionRotation rotation = new SuggestionRotation(1);
        assertEquals(0, rotation.next());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyList() {
        new SuggestionRotation(0);
    }
}
