package org.apollo.mobile.sync;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SyncResultMapperTest {
    @Test
    public void mapsStatuses() {
        assertEquals(SendResult.SENT, SyncResultMapper.fromStatus(200));
        assertEquals(SendResult.SENT, SyncResultMapper.fromStatus(204));
        assertEquals(SendResult.UNAUTHORIZED, SyncResultMapper.fromStatus(401));
        assertEquals(SendResult.RETRY, SyncResultMapper.fromStatus(408));
        assertEquals(SendResult.RETRY, SyncResultMapper.fromStatus(429));
        assertEquals(SendResult.RETRY, SyncResultMapper.fromStatus(503));
        assertEquals(SendResult.REJECTED, SyncResultMapper.fromStatus(400));
        assertEquals(SendResult.REJECTED, SyncResultMapper.fromStatus(404));
        assertEquals(SendResult.REJECTED, SyncResultMapper.fromStatus(409));
    }
}
