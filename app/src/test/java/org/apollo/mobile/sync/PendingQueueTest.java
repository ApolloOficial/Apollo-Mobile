package org.apollo.mobile.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PendingQueueTest {
    private final AtomicLong clock = new AtomicLong(1000);

    private PendingQueue newQueue() {
        return new PendingQueue(new InMemoryPendingStore(), clock::incrementAndGet);
    }

    @Test
    public void enqueueNormalizesAndKeepsOrder() {
        PendingQueue queue = newQueue();
        queue.enqueue("post", "/api/v1/panels/activate", "{\"a\":1}");
        queue.enqueue("PATCH", "api/v1/x", "{}");
        List<PendingAction> list = queue.ordered();
        assertEquals(2, list.size());
        assertEquals("POST", list.get(0).getMethod());
        assertEquals("api/v1/panels/activate", list.get(0).getPath());
        assertEquals("PATCH", list.get(1).getMethod());
        assertEquals(2, queue.pendingCount());
        assertEquals(0, queue.failedCount());
    }

    @Test
    public void rejectsUnsafeInput() {
        PendingQueue queue = newQueue();
        String[] paths = {null, "", "   ", "https://evil.example/x", "api/../x", "a\\b"};
        for (String path : paths) {
            try {
                queue.enqueue("POST", path, "{}");
                fail("expected rejection for " + path);
            } catch (IllegalArgumentException expected) {
                assertTrue(true);
            }
        }
        try {
            queue.enqueue("DELETE", "api/x", "{}");
            fail("expected rejection");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        assertEquals(0, queue.pendingCount());
    }

    @Test
    public void countsBytesOfBodies() {
        PendingQueue queue = newQueue();
        queue.enqueue("POST", "api/a", "abcd");
        queue.enqueue("POST", "api/b", "çç");
        assertEquals(8, queue.totalBytes());
    }
}
