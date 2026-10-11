package org.apollo.mobile.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apollo.mobile.settings.format.ByteSize;
import org.apollo.mobile.settings.format.PhotoSizing;
import org.junit.Test;

public class PhotoSizingTest {
    @Test
    public void sampleSizeKeepsLongestEdgeAtLeastTarget() {
        assertEquals(1, PhotoSizing.sampleSize(800, 600, 1024));
        assertEquals(1, PhotoSizing.sampleSize(1500, 1000, 1024));
        assertEquals(2, PhotoSizing.sampleSize(4000, 3000, 1024));
        assertEquals(4, PhotoSizing.sampleSize(8000, 6000, 1024));
        assertEquals(1, PhotoSizing.sampleSize(0, 0, 1024));
    }

    @Test
    public void scaledSizeKeepsAspectRatio() {
        int[] size = PhotoSizing.scaledSize(4000, 3000, 1024);
        assertEquals(1024, size[0]);
        assertEquals(768, size[1]);
        int[] small = PhotoSizing.scaledSize(500, 400, 1024);
        assertEquals(500, small[0]);
        assertEquals(400, small[1]);
    }

    @Test
    public void fitsWithinBackendLimit() {
        assertTrue(PhotoSizing.fits(900_000));
        assertTrue(PhotoSizing.fits(PhotoSizing.MAX_BYTES));
        assertFalse(PhotoSizing.fits(PhotoSizing.MAX_BYTES + 1));
        assertFalse(PhotoSizing.fits(0));
        assertTrue(PhotoSizing.MAX_BYTES < 1024 * 1024);
    }

    @Test
    public void qualityStepsDownAndStops() {
        assertEquals(80, PhotoSizing.nextQuality(90));
        assertEquals(40, PhotoSizing.nextQuality(50));
        assertEquals(-1, PhotoSizing.nextQuality(40));
    }

    @Test
    public void formatsByteSizes() {
        assertEquals("0 B", ByteSize.format(-5));
        assertEquals("512 B", ByteSize.format(512));
        assertEquals("2 KB", ByteSize.format(2048));
        assertEquals("1,5 MB", ByteSize.format(1572864));
        assertEquals("3,0 GB", ByteSize.format(3L * 1024 * 1024 * 1024));
    }
}
