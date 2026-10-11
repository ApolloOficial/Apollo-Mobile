package org.apollo.mobile.sync;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.LongSupplier;

public final class PendingQueue {
    private final PendingStore store;
    private final LongSupplier clock;

    public PendingQueue(PendingStore store) {
        this(store, System::currentTimeMillis);
    }

    public PendingQueue(PendingStore store, LongSupplier clock) {
        this.store = store;
        this.clock = clock;
    }

    public PendingAction enqueue(String method, String path, String jsonBody) {
        String normalizedMethod = method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
        if (!normalizedMethod.equals("POST") && !normalizedMethod.equals("PUT") && !normalizedMethod.equals("PATCH")) {
            throw new IllegalArgumentException("Unsupported method");
        }
        String normalizedPath = normalizePath(path);
        PendingAction action = new PendingAction(
                UUID.randomUUID().toString(),
                normalizedMethod,
                normalizedPath,
                jsonBody == null ? "" : jsonBody,
                clock.getAsLong());
        store.add(action);
        return action;
    }

    public List<PendingAction> ordered() {
        List<PendingAction> list = new ArrayList<>(store.all());
        list.sort(Comparator.comparingLong(PendingAction::getCreatedAt));
        return list;
    }

    public int pendingCount() {
        int count = 0;
        for (PendingAction action : store.all()) {
            if (!action.isFailed()) {
                count++;
            }
        }
        return count;
    }

    public int failedCount() {
        int count = 0;
        for (PendingAction action : store.all()) {
            if (action.isFailed()) {
                count++;
            }
        }
        return count;
    }

    public long totalBytes() {
        long total = 0L;
        for (PendingAction action : store.all()) {
            total += action.sizeInBytes();
        }
        return total;
    }

    public void remove(String id) {
        store.remove(id);
    }

    public void update(PendingAction action) {
        store.update(action);
    }

    static String normalizePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path is required");
        }
        String value = path.trim();
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.isEmpty() || value.contains("://") || value.contains("..") || value.contains("\\")) {
            throw new IllegalArgumentException("Invalid path");
        }
        return value;
    }
}
