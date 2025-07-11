package org.mule.pdf.extension.internal.utils;

public final class TimeUtils {

    private TimeUtils() {
    }

    public static double getElapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000.0;
    }

}
