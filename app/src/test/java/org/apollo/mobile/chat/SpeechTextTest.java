package org.apollo.mobile.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class SpeechTextTest {
    @Test
    public void removesMarkdownSymbolsAndBullets() {
        String text = "**Possíveis causas:**\n• Placa molhada\n- Sombra parcial\n\nMeça de novo.";
        assertEquals("Possíveis causas: Placa molhada. Sombra parcial. Meça de novo.", SpeechText.clean(text));
    }

    @Test
    public void handlesNullAndBlankText() {
        assertEquals("", SpeechText.clean(null));
        assertEquals("", SpeechText.clean("  \n "));
        assertTrue(SpeechText.chunks(null, 100).isEmpty());
    }

    @Test
    public void splitsLongTextWithoutExceedingTheLimit() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            text.append("Frase número ").append(i).append(" da resposta. ");
        }
        List<String> chunks = SpeechText.chunks(text.toString(), 120);
        assertTrue(chunks.size() > 1);
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= 120);
            assertTrue(!chunk.isEmpty());
        }
        assertEquals(SpeechText.clean(text.toString()), String.join(" ", chunks));
    }

    @Test
    public void keepsShortTextInOneChunk() {
        assertEquals(1, SpeechText.chunks("Resposta curta.", 3000).size());
    }
}
