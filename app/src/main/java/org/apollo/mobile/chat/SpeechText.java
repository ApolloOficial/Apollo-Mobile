package org.apollo.mobile.chat;

import java.util.ArrayList;
import java.util.List;

public final class SpeechText {
    private SpeechText() {
    }

    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        String result = text
                .replaceAll("(?m)^\\s*[•\\-*]\\s+", "")
                .replaceAll("[*_`#>•]", "")
                .replaceAll("[ \\t]+", " ");
        StringBuilder spoken = new StringBuilder();
        for (String line : result.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (spoken.length() > 0) {
                char last = spoken.charAt(spoken.length() - 1);
                spoken.append(last == '.' || last == '!' || last == '?' || last == ':' ? " " : ". ");
            }
            spoken.append(trimmed);
        }
        return spoken.toString().trim();
    }

    public static List<String> chunks(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        String remaining = clean(text);
        while (!remaining.isEmpty()) {
            if (remaining.length() <= maxLength) {
                chunks.add(remaining);
                break;
            }
            int cut = remaining.lastIndexOf(". ", maxLength - 1);
            if (cut < maxLength / 2) {
                cut = remaining.lastIndexOf(' ', maxLength - 1);
            }
            if (cut <= 0) {
                cut = maxLength - 1;
            }
            chunks.add(remaining.substring(0, cut + 1).trim());
            remaining = remaining.substring(cut + 1).trim();
        }
        return chunks;
    }
}
