package org.apollo.mobile.sync;

import java.util.List;

public interface PendingStore {
    List<PendingAction> all();

    void add(PendingAction action);

    void update(PendingAction action);

    void remove(String id);
}
