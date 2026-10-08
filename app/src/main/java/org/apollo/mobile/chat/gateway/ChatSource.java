package org.apollo.mobile.chat.gateway;

public final class ChatSource {
    private String document;
    private Integer page;
    private String section;
    private String url;
    private String excerpt;
    private Double score;

    public ChatSource() {
    }

    public ChatSource(String document, Integer page) {
        this.document = document;
        this.page = page;
    }

    public String getDocument() {
        return document;
    }

    public Integer getPage() {
        return page;
    }

    public String getSection() {
        return section;
    }

    public String getUrl() {
        return url;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public Double getScore() {
        return score;
    }
}
