package org.apollo.mobile.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class MarkdownParserTest {
    private static String spanText(MarkdownParser.Result r, MarkdownParser.Type type) {
        for (MarkdownParser.Span s : r.getSpans()) {
            if (s.getType() == type) {
                return r.getText().substring(s.getStart(), s.getEnd());
            }
        }
        return null;
    }

    @Test
    public void parsesBold() {
        MarkdownParser.Result r = MarkdownParser.parse("Isto é **importante** sim");
        assertEquals("Isto é importante sim", r.getText());
        assertEquals("importante", spanText(r, MarkdownParser.Type.BOLD));
    }

    @Test
    public void parsesItalic() {
        MarkdownParser.Result r = MarkdownParser.parse("Isto é *leve* sim");
        assertEquals("Isto é leve sim", r.getText());
        assertEquals("leve", spanText(r, MarkdownParser.Type.ITALIC));
    }

    @Test
    public void keepsSnakeCaseLiteral() {
        MarkdownParser.Result r = MarkdownParser.parse("valor snake_case_name aqui");
        assertEquals("valor snake_case_name aqui", r.getText());
        assertTrue(r.getSpans().isEmpty());
    }

    @Test
    public void convertsBullets() {
        MarkdownParser.Result r = MarkdownParser.parse("- um\n* dois");
        assertEquals("• um\n• dois", r.getText());
    }

    @Test
    public void parsesHeading() {
        MarkdownParser.Result r = MarkdownParser.parse("## Título\ncorpo");
        assertEquals("Título\ncorpo", r.getText());
        assertEquals("Título", spanText(r, MarkdownParser.Type.HEADING));
    }

    @Test
    public void parsesInlineCode() {
        MarkdownParser.Result r = MarkdownParser.parse("use `kWh` aqui");
        assertEquals("use kWh aqui", r.getText());
        assertEquals("kWh", spanText(r, MarkdownParser.Type.CODE));
    }

    @Test
    public void parsesFencedCode() {
        MarkdownParser.Result r = MarkdownParser.parse("antes\n```\nlinha\n```\ndepois");
        assertEquals("antes\nlinha\ndepois", r.getText());
        assertEquals("linha", spanText(r, MarkdownParser.Type.CODE));
    }

    @Test
    public void keepsLinkLabelOnly() {
        MarkdownParser.Result r = MarkdownParser.parse("veja [o manual](http://x.com) agora");
        assertEquals("veja o manual agora", r.getText());
    }

    @Test
    public void honorsEscapes() {
        MarkdownParser.Result r = MarkdownParser.parse("2 \\* 3");
        assertEquals("2 * 3", r.getText());
        assertTrue(r.getSpans().isEmpty());
    }

    @Test
    public void keepsUnmatchedMarkersLiteral() {
        MarkdownParser.Result r = MarkdownParser.parse("a ** b");
        assertEquals("a ** b", r.getText());
    }

    @Test
    public void handlesNullAndEmpty() {
        assertEquals("", MarkdownParser.parse(null).getText());
        assertEquals("", MarkdownParser.parse("").getText());
        List<MarkdownParser.Span> spans = MarkdownParser.parse("").getSpans();
        assertTrue(spans.isEmpty());
    }
}
