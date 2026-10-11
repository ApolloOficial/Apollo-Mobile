package org.apollo.mobile.settings.format;

public final class PhotoSizing {
    public static final int MAX_BYTES = 1_000_000;
    public static final int MAX_EDGE = 1024;
    public static final int START_QUALITY = 90;
    public static final int MIN_QUALITY = 40;
    private static final int QUALITY_STEP = 10;

    private PhotoSizing() {
    }

    public static int sampleSize(int width, int height, int maxEdge) {
        if (width <= 0 || height <= 0 || maxEdge <= 0) {
            return 1;
        }
        int sample = 1;
        while (Math.max(width, height) / (sample * 2) >= maxEdge) {
            sample *= 2;
        }
        return sample;
    }

    public static int[] scaledSize(int width, int height, int maxEdge) {
        int longest = Math.max(width, height);
        if (longest <= maxEdge || longest <= 0) {
            return new int[]{width, height};
        }
        double ratio = (double) maxEdge / longest;
        int scaledWidth = Math.max(1, (int) Math.round(width * ratio));
        int scaledHeight = Math.max(1, (int) Math.round(height * ratio));
        return new int[]{scaledWidth, scaledHeight};
    }

    public static boolean fits(long length) {
        return length > 0 && length <= MAX_BYTES;
    }

    public static int nextQuality(int quality) {
        int next = quality - QUALITY_STEP;
        return next < MIN_QUALITY ? -1 : next;
    }
}
