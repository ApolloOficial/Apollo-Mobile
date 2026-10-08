package org.apollo.mobile.chat;

import static org.junit.Assert.assertEquals;

import org.apollo.mobile.chat.gateway.ChatReply;
import org.apollo.mobile.chat.gateway.ChatSource;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class ChatSourcesFormatterTest {
    @Test
    public void returnsEmptyTextWithoutSources() {
        assertEquals("", ChatSourcesFormatter.format(null));
        assertEquals("", ChatSourcesFormatter.format(Collections.emptyList()));
    }

    @Test
    public void joinsDocumentsWithPagesAndDropsDuplicates() {
        String text = ChatSourcesFormatter.format(Arrays.asList(
                new ChatSource("NREL", 12),
                new ChatSource("NREL", 12),
                new ChatSource("IEA PVPS", null)
        ));
        assertEquals("NREL (p. 12); IEA PVPS", text);
    }

    @Test
    public void ignoresBlankDocumentsAndLimitsToThree() {
        String text = ChatSourcesFormatter.format(Arrays.asList(
                new ChatSource(" ", 1),
                new ChatSource(null, 2),
                new ChatSource("A", 1),
                new ChatSource("B", 2),
                new ChatSource("C", 3),
                new ChatSource("D", 4)
        ));
        assertEquals("A (p. 1); B (p. 2); C (p. 3)", text);
    }

    @Test
    public void replyExposesSafeDefaults() {
        ChatReply reply = new ChatReply();
        assertEquals("", reply.getAnswer());
        assertEquals(0, reply.getSources().size());
        assertEquals(0, reply.getAgents().size());
    }
}
