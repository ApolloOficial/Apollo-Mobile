package org.apollo.mobile.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class MarkdownParser {

    public enum Type {
        BOLD,
        ITALIC,
        CODE,
        HEADING
    }

    public static final class Span {
        private final Type type;
        private final int start;
        private final int end;

        Span(Type type, int start, int end) {
            this.type = type;
            this.start = start;
            this.end = end;
        }

        public Type getType() {
            return type;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }

    public static final class Result {
        private final String text;
        private final List<Span> spans;

        Result(String text, List<Span> spans) {
            this.text = text;
            this.spans = Collections.unmodifiableList(spans);
        }

        public String getText() {
            return text;
        }

        public List<Span> getSpans() {
            return spans;
        }
    }

    private static final Pattern FENCE = Pattern.compile("^\\s*```.*$");
    private static final Pattern RULE = Pattern.compile("^\\s*([-*_])(\\s*\\1){2,}\\s*$");
    private static final Pattern HEADING = Pattern.compile("^\\s{0,3}#{1,6}\\s+(.*)$");
    private static final Pattern BULLET = Pattern.compile("^(\\s*)[-*+•]\\s+(.*)$");
    private static final Pattern QUOTE = Pattern.compile("^\\s*>\\s?(.*)$");
    private static final String ESCAPABLE = "\\`*_{}[]()#+-.!>~";

    private MarkdownParser() {
    }

    public static Result parse(String markdown) {
        List<Span> spans = new ArrayList<>();
        if (markdown == null || markdown.isEmpty()) {
            return new Result("", spans);
        }
        StringBuilder out = new StringBuilder();
        boolean started = false;
        boolean inFence = false;
        String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        for (String line : lines) {
            if (FENCE.matcher(line).matches()) {
                inFence = !inFence;
                continue;
            }
            if (started) {
                out.append('\n');
            }
            started = true;
            if (inFence) {
                int start = out.length();
                out.append(line);
                spans.add(new Span(Type.CODE, start, out.length()));
                continue;
            }
            if (RULE.matcher(line).matches()) {
                continue;
            }
            var heading = HEADING.matcher(line);
            if (heading.matches()) {
                int start = out.length();
                inline(heading.group(1).trim(), out, spans);
                spans.add(new Span(Type.HEADING, start, out.length()));
                continue;
            }
            var bullet = BULLET.matcher(line);
            if (bullet.matches()) {
                int depth = bullet.group(1).length() / 2;
                for (int i = 0; i < depth; i++) {
                    out.append("  ");
                }
                out.append("• ");
                inline(bullet.group(2), out, spans);
                continue;
            }
            var quote = QUOTE.matcher(line);
            inline(quote.matches() ? quote.group(1) : line, out, spans);
        }
        int end = out.length();
        while (end > 0 && out.charAt(end - 1) == '\n') {
            end--;
        }
        out.setLength(end);
        return new Result(out.toString(), spans);
    }

    private static void inline(String s, StringBuilder out, List<Span> spans) {
        int n = s.length();
        int i = 0;
        while (i < n) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < n && ESCAPABLE.indexOf(s.charAt(i + 1)) >= 0) {
                out.append(s.charAt(i + 1));
                i += 2;
                continue;
            }
            if (c == '`') {
                int j = s.indexOf('`', i + 1);
                if (j > i + 1) {
                    int start = out.length();
                    out.append(s, i + 1, j);
                    spans.add(new Span(Type.CODE, start, out.length()));
                    i = j + 1;
                    continue;
                }
            }
            if ((c == '*' || c == '_') && i + 1 < n && s.charAt(i + 1) == c) {
                String delimiter = "" + c + c;
                int j = closingDouble(s, i + 2, delimiter);
                if (j > 0) {
                    int start = out.length();
                    inline(s.substring(i + 2, j), out, spans);
                    spans.add(new Span(Type.BOLD, start, out.length()));
                    i = j + 2;
                    continue;
                }
            }
            if (c == '*' || c == '_') {
                boolean opens = i + 1 < n && !Character.isWhitespace(s.charAt(i + 1));
                if (c == '_' && i > 0 && Character.isLetterOrDigit(s.charAt(i - 1))) {
                    opens = false;
                }
                if (opens) {
                    int j = closingSingle(s, i + 1, c);
                    if (j > 0) {
                        int start = out.length();
                        inline(s.substring(i + 1, j), out, spans);
                        spans.add(new Span(Type.ITALIC, start, out.length()));
                        i = j + 1;
                        continue;
                    }
                }
            }
            if (c == '[') {
                int close = s.indexOf(']', i + 1);
                if (close > i + 1 && close + 1 < n && s.charAt(close + 1) == '(') {
                    int end = s.indexOf(')', close + 2);
                    if (end > 0) {
                        inline(s.substring(i + 1, close), out, spans);
                        i = end + 1;
                        continue;
                    }
                }
            }
            out.append(c);
            i++;
        }
    }

    private static int closingDouble(String s, int from, String delimiter) {
        for (int j = s.indexOf(delimiter, from); j >= 0; j = s.indexOf(delimiter, j + 1)) {
            if (j > from && !Character.isWhitespace(s.charAt(j - 1))) {
                return j;
            }
        }
        return -1;
    }

    private static int closingSingle(String s, int from, char marker) {
        int n = s.length();
        for (int j = from; j < n; j++) {
            char ch = s.charAt(j);
            if (ch == '\\') {
                j++;
                continue;
            }
            if (ch != marker) {
                continue;
            }
            if (j + 1 < n && s.charAt(j + 1) == marker) {
                j++;
                continue;
            }
            boolean spaced = Character.isWhitespace(s.charAt(j - 1));
            boolean insideWord = marker == '_' && j + 1 < n && Character.isLetterOrDigit(s.charAt(j + 1));
            if (j > from && !spaced && !insideWord) {
                return j;
            }
        }
        return -1;
    }
}
