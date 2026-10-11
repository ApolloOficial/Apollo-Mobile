package org.apollo.mobile.sync;

public interface PendingSender {
    SendResult send(PendingAction action);
}
