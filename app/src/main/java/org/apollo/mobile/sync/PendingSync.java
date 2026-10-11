package org.apollo.mobile.sync;

public final class PendingSync {
    private final PendingQueue queue;
    private final PendingSender sender;

    public PendingSync(PendingQueue queue, PendingSender sender) {
        this.queue = queue;
        this.sender = sender;
    }

    public synchronized SyncReport run() {
        int sent = 0;
        int rejected = 0;
        SyncReport.Stop stop = SyncReport.Stop.NONE;

        for (PendingAction action : queue.ordered()) {
            if (action.isFailed()) {
                continue;
            }
            SendResult result = sender.send(action);
            if (result == SendResult.SENT) {
                queue.remove(action.getId());
                sent++;
            } else if (result == SendResult.REJECTED) {
                action.markAttempt();
                action.markFailed();
                queue.update(action);
                rejected++;
            } else if (result == SendResult.UNAUTHORIZED) {
                stop = SyncReport.Stop.UNAUTHORIZED;
                break;
            } else {
                action.markAttempt();
                queue.update(action);
                stop = SyncReport.Stop.NETWORK;
                break;
            }
        }
        return new SyncReport(sent, rejected, queue.pendingCount(), stop);
    }
}
