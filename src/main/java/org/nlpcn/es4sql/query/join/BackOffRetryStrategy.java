package org.nlpcn.es4sql.query.join;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Runtime.getRuntime;

public class BackOffRetryStrategy {

    private final static Logger LOG = LogManager.getLogger();

    /** Interval (ms) between each retry */
    private final long[] intervals;

    /** Delta to randomize interval (ms) */
    private final long delta;

    private final int threshold = 75;

    public BackOffRetryStrategy(double[] intervals) {
        this.intervals = milliseconds(intervals);
        this.delta = milliseconds(0.5);
    }

    public boolean isMemoryHealthy(long allocateMemory) {
        final long freeMemory = Runtime.getRuntime().freeMemory();
        final long totalMemory = Runtime.getRuntime().totalMemory();
        final int memoryUsage = (int)Math.round((double)(totalMemory - freeMemory + allocateMemory) / (double)totalMemory * 100);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Memory total, free, allocate: {}, {}, {}", totalMemory, freeMemory, allocateMemory);
            LOG.debug("Memory usage and limit: {}%, {}%", memoryUsage, threshold);
        }

        return memoryUsage < threshold;
    }

    public boolean isHealthy() {
        return isHealthy(0L);
    }

    public boolean isHealthy(long allocateMemory) {
        for (int i = 0; i < intervals.length; i++) {
            if (isMemoryHealthy(allocateMemory)) {
                return true;
            }

            LOG.warn("Memory monitor is unhealthy now, back off retrying: {} attempt", i);
            backOffSleep(intervals[i]);
        }
        return isMemoryHealthy(allocateMemory);
    }

    private void backOffSleep(long interval) {
        try {
            long millis = randomize(interval);

            LOG.debug("Back off sleeping: {} ms", millis);
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            LOG.error("Sleep interrupted", e);
        }
    }

    /** Generate random interval in [interval-delta, interval+delta) */
    private long randomize(long interval) {
        // Random number within range generator for JDK 7+
        return ThreadLocalRandom.current().nextLong(
                lowerBound(interval), upperBound(interval)
        );
    }

    private long lowerBound(long interval) {
        return Math.max(0, interval - delta);
    }

    private long upperBound(long interval) {
        return interval + delta;
    }

    private long[] milliseconds(double[] seconds) {
        return Arrays.stream(seconds).
                mapToLong(this::milliseconds).
                toArray();
    }

    private long milliseconds(double seconds) {
        return (long) (1000 * seconds);
    }
}
