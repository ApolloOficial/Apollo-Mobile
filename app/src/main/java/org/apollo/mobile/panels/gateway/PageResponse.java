package org.apollo.mobile.panels.gateway;

import java.util.Collections;
import java.util.List;

public final class PageResponse<T> {

    private List<T> content;
    private long totalElements;

    public PageResponse() {
    }

    public List<T> getContent() {
        return content == null ? Collections.<T>emptyList() : content;
    }

    public long getTotalElements() {
        return totalElements;
    }
}
