package org.apollo.mobile.view.chat;

final class ChatMessage {
    enum Kind {
        USER,
        BOT,
        TYPING
    }

    private final Kind kind;
    private final String text;
    private final String sources;

    private ChatMessage(Kind kind, String text, String sources) {
        this.kind = kind;
        this.text = text;
        this.sources = sources;
    }

    static ChatMessage user(String text) {
        return new ChatMessage(Kind.USER, text, "");
    }

    static ChatMessage bot(String text, String sources) {
        return new ChatMessage(Kind.BOT, text, sources == null ? "" : sources);
    }

    static ChatMessage typing() {
        return new ChatMessage(Kind.TYPING, "", "");
    }

    Kind getKind() {
        return kind;
    }

    String getText() {
        return text;
    }

    String getSources() {
        return sources;
    }
}
