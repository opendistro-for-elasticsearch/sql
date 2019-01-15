package org.nlpcn.es4sql.query.planner.resource.monitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Back off retry strategy to back off retry randomly several times to avoid all running queries exit (thundering herd)
 */
public class BackOffRetryStrategy implements Monitor {

    private final static Logger LOG = LogManager.getLogger();

    /** Interval (ms) between each retry */
    private final long[] intervals;

    /** Delta to randomize interval (ms) */
    private final long delta;

    /** Actual circuit breaker underlying */
    private final List<Monitor> monitors;


    public BackOffRetryStrategy(List<Monitor> monitors,
                                double[] intervals) {
        this.monitors = monitors;
        this.intervals = milliseconds(intervals);
        this.delta = milliseconds(0.5);
    }

    @Override
    public boolean isHealthy() {
        for (int i = 0; i < intervals.length; i++) {
            if (isAllMonitorHealthy()) {
                return true;
            }

            LOG.warn("Some monitor is unhealthy now, back off retrying: {} attempt", i);
            backOffSleep(intervals[i]);
        }
        return isAllMonitorHealthy();
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

    private boolean isAllMonitorHealthy() {
        return monitors.stream().allMatch(Monitor::isHealthy);
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
