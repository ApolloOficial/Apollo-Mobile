package org.apollo.mobile.sync;

import java.util.ArrayList;
import java.util.List;

final class InMemoryPendingStore implements PendingStore {
    private final List<PendingAction> items = new ArrayList<>();

    @Override
    public List<PendingAction> all() {
        return new ArrayList<>(items);
    }

    @Override
    public void add(PendingAction action) {
        items.add(action);
    }

    @Override
    public void update(PendingAction action) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(action.getId())) {
                items.set(i, action);
            }
        }
    }

    @Override
    public void remove(String id) {
        items.removeIf(item -> item.getId().equals(id));
    }
}
