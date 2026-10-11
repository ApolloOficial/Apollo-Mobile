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
        MarkdownParser.Result result = MarkdownParser.parse(markdown);
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

    private static void apply(SpannableString output, Object style, MarkdownParser.Span span) {
        output.setSpan(style, span.getStart(), span.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
