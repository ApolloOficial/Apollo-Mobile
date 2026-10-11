package org.apollo.mobile.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PendingSyncTest {
    private final AtomicLong clock = new AtomicLong(0);

    private PendingQueue queueWith(String... paths) {
        PendingQueue queue = new PendingQueue(new InMemoryPendingStore(), clock::incrementAndGet);
        for (String path : paths) {
            queue.enqueue("POST", path, "{}");
        }
        return queue;
    }

    private static PendingSender sender(List<String> seen, SendResult... results) {
        int[] index = {0};
        return action -> {
            seen.add(action.getPath());
            SendResult result = results[Math.min(index[0], results.length - 1)];
            index[0]++;
            return result;
        };
    }

    @Test
    public void sendsAllInOrderAndEmptiesQueue() {
        PendingQueue queue = queueWith("api/a", "api/b", "api/c");
        List<String> seen = new ArrayList<>();
        SyncReport report = new PendingSync(queue, sender(seen, SendResult.SENT)).run();
        assertEquals(3, report.getSent());
        assertEquals(0, report.getRemaining());
        assertEquals(SyncReport.Stop.NONE, report.getStop());
        assertEquals("api/a", seen.get(0));
        assertEquals("api/c", seen.get(2));
        assertEquals(0, queue.pendingCount());
    }

    @Test
    public void networkFailureStopsAndKeepsOrder() {
        PendingQueue queue = queueWith("api/a", "api/b", "api/c");
        List<String> seen = new ArrayList<>();
        SyncReport report = new PendingSync(queue, sender(seen, SendResult.SENT, SendResult.RETRY)).run();
        assertEquals(1, report.getSent());
        assertEquals(2, report.getRemaining());
        assertEquals(SyncReport.Stop.NETWORK, report.getStop());
        assertEquals(2, seen.size());
        assertEquals(1, queue.ordered().get(0).getAttempts());
    }

    @Test
    public void rejectedIsMarkedFailedAndSkippedNextTime() {
        PendingQueue queue = queueWith("api/a", "api/b");
        List<String> seen = new ArrayList<>();
        SyncReport first = new PendingSync(queue, sender(seen, SendResult.REJECTED, SendResult.SENT)).run();
        assertEquals(1, first.getSent());
        assertEquals(1, first.getRejected());
        assertEquals(0, first.getRemaining());
        assertEquals(1, queue.failedCount());

        List<String> second = new ArrayList<>();
        SyncReport again = new PendingSync(queue, sender(second, SendResult.SENT)).run();
        assertEquals(0, again.getSent());
        assertTrue(second.isEmpty());
        assertEquals(1, queue.failedCount());
    }

    @Test
    public void unauthorizedStopsWithoutCountingAttempt() {
        PendingQueue queue = queueWith("api/a", "api/b");
        List<String> seen = new ArrayList<>();
        SyncReport report = new PendingSync(queue, sender(seen, SendResult.UNAUTHORIZED)).run();
        assertEquals(SyncReport.Stop.UNAUTHORIZED, report.getStop());
        assertEquals(2, report.getRemaining());
        assertEquals(1, seen.size());
        assertEquals(0, queue.ordered().get(0).getAttempts());
    }

    @Test
    public void emptyQueueIsNoop() {
        PendingQueue queue = queueWith();
        SyncReport report = new PendingSync(queue, sender(new ArrayList<>(), SendResult.SENT)).run();
        assertEquals(0, report.getSent());
        assertEquals(0, report.getRemaining());
        assertEquals(SyncReport.Stop.NONE, report.getStop());
    }
}
