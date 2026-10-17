package org.apollo.mobile.view.chat;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import org.apollo.mobile.chat.MarkdownParser;

final class MarkdownRenderer {
    private static final int CODE_BACKGROUND = 0x1F000000;
    private static final float HEADING_SIZE = 1.1f;

    private MarkdownRenderer() {
    }

    static CharSequence render(String markdown) {
        MarkdownParser.Result result = MarkdownParser.parse(flattenTables(markdown));
        SpannableString output = new SpannableString(result.getText());
        for (MarkdownParser.Span span : result.getSpans()) {
            if (span.getStart() >= span.getEnd()) {
                continue;
            }
            switch (span.getType()) {
                case BOLD:
                    apply(output, new StyleSpan(Typeface.BOLD), span);
                    break;
                case ITALIC:
                    apply(output, new StyleSpan(Typeface.ITALIC), span);
                    break;
                case CODE:
                    apply(output, new TypefaceSpan("monospace"), span);
                    apply(output, new BackgroundColorSpan(CODE_BACKGROUND), span);
                    break;
                case HEADING:
                    apply(output, new StyleSpan(Typeface.BOLD), span);
                    apply(output, new RelativeSizeSpan(HEADING_SIZE), span);
                    break;
                default:
                    break;
            }
        }
        return output;
    }

    private static String flattenTables(String markdown) {
        if (markdown == null || markdown.indexOf('|') < 0) return markdown;
        String[] lines = markdown.split("\n", -1);
        StringBuilder out = new StringBuilder();
        String[] header = null;
        for (String line : lines) {
            String trimmed = line.trim();
            boolean row = trimmed.startsWith("|") && trimmed.length() > 1;
            if (!row) {
                header = null;
                out.append(line).append('\n');
                continue;
            }
            String[] cells = cells(trimmed);
            if (isSeparator(cells)) continue;
            if (header == null) {
                header = cells;
                continue;
            }
            for (int i = 0; i < cells.length; i++) {
                if (cells[i].isEmpty()) continue;
                String label = i < header.length ? header[i] : "";
                if (!label.isEmpty()) out.append("**").append(label).append(":** ");
                out.append(cells[i]).append('\n');
            }
            out.append('\n');
        }
        return out.toString().replaceAll("\n{3,}", "\n\n").trim();
    }

    private static String[] cells(String row) {
        String inner = row.replaceAll("^\\|", "").replaceAll("\\|$", "");
        String[] parts = inner.split("\\|", -1);
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

    private static boolean isSeparator(String[] cells) {
        for (String cell : cells) {
            if (!cell.matches(":?-{2,}:?")) return false;
        }
        return cells.length > 0;
    }

    private static void apply(SpannableString output, Object style, MarkdownParser.Span span) {
        output.setSpan(style, span.getStart(), span.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
