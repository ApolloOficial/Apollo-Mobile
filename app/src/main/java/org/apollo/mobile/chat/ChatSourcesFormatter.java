package org.apollo.mobile.chat;

import org.apollo.mobile.chat.gateway.ChatSource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ChatSourcesFormatter {
    private static final int MAX_SOURCES = 3;

    private ChatSourcesFormatter() {
    }

    public static String format(List<ChatSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return "";
        }
        Set<String> labels = new LinkedHashSet<>();
        for (ChatSource source : sources) {
            if (source == null || source.getDocument() == null || source.getDocument().trim().isEmpty()) {
                continue;
            }
            String label = source.getDocument().trim();
            if (source.getPage() != null) {
                label += " (p. " + source.getPage() + ")";
            }
            labels.add(label);
            if (labels.size() == MAX_SOURCES) {
                break;
            }
        }
        return String.join("; ", labels);
    }
}
