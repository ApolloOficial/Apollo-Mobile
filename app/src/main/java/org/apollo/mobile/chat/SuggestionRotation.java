package org.apollo.mobile.chat;

public final class SuggestionRotation {
    private final int size;
    private int index;

    public SuggestionRotation(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("size must be positive");
        }
        this.size = size;
    }

    public int current() {
        return index;
    }

    public int next() {
        index = (index + 1) % size;
        return index;
    }

    public int size() {
        return size;
    }
}
