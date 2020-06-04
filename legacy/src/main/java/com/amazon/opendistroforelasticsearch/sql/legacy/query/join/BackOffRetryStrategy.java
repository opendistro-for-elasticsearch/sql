/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.query.join;

import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.MetricName;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.Metrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.collect.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BackOffRetryStrategy {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Interval (ms) between each retry
     */
    private static final long[] intervals = milliseconds(new double[]{4, 8 + 4, 16 + 4});

    /**
     * Delta to randomize interval (ms)
     */
    private static final long delta = 4 * 1000;

    private static final int threshold = 85;

    private static IdentityHashMap<Object, Tuple<Long, Long>> memUse = new IdentityHashMap<>();

    private static AtomicLong mem = new AtomicLong(0L);

    private static long lastTimeoutCleanTime = System.currentTimeMillis();

    private static final long RELTIMEOUT = 1000 * 60 * 30;

    private static final int MAXRETRIES = 999;

    private static final Object obj = new Object();

    public static final Supplier<Integer> GET_CB_STATE = () -> isMemoryHealthy() ? 0 : 1;

    private BackOffRetryStrategy() {

    }

    private static boolean isMemoryHealthy() {
        final long freeMemory = Runtime.getRuntime().freeMemory();
        final long totalMemory = Runtime.getRuntime().totalMemory();
        final int memoryUsage = (int) Math.round((double) (totalMemory - freeMemory + mem.get())
                / (double) totalMemory * 100);

        LOG.debug("[MCB1] Memory total, free, allocate: {}, {}, {}", totalMemory, freeMemory, mem.get());
        LOG.debug("[MCB1] Memory usage and limit: {}%, {}%", memoryUsage, threshold);

        return memoryUsage < threshold;
    }

    public static boolean isHealthy() {
        for (int i = 0; i < intervals.length; i++) {
            if (isMemoryHealthy()) {
                return true;
            }

            LOG.warn("[MCB1] Memory monitor is unhealthy now, back off retrying: {} attempt, thread id = {}",
                    i, Thread.currentThread().getId());
            if (ThreadLocalRandom.current().nextBoolean()) {
                Metrics.getInstance().getNumericalMetric(MetricName.FAILED_REQ_COUNT_CB).increment();
                LOG.warn("[MCB1] Directly abort on idx {}.", i);
                return false;
            }
            backOffSleep(intervals[i]);
        }

        boolean isHealthy = isMemoryHealthy();
        if (!isHealthy) {
            Metrics.getInstance().getNumericalMetric(MetricName.FAILED_REQ_COUNT_CB).increment();
        }

        return isHealthy;
    }

    private static boolean isMemoryHealthy(long allocateMemory, int idx, Object key) {
        long logMem = mem.get();

        releaseTimeoutMemory();
        if (idx == 0 && allocateMemory > 0) {
            logMem = mem.addAndGet(allocateMemory);
            synchronized (BackOffRetryStrategy.class) {
                if (memUse.containsKey(key)) {
                    memUse.put(key, Tuple.tuple(memUse.get(key).v1(), memUse.get(key).v2() + allocateMemory));
                } else {
                    memUse.put(key, Tuple.tuple(System.currentTimeMillis(), allocateMemory));
                }
            }
        }

        final long freeMemory = Runtime.getRuntime().freeMemory();
        final long totalMemory = Runtime.getRuntime().totalMemory();
        final int memoryUsage = (int) Math.round((double) (totalMemory - freeMemory + logMem)
                / (double) totalMemory * 100);

        LOG.debug("[MCB] Idx is {}", idx);
        LOG.debug("[MCB] Memory total, free, allocate: {}, {}, {}, {}", totalMemory, freeMemory,
                allocateMemory, logMem);
        LOG.debug("[MCB] Memory usage and limit: {}%, {}%", memoryUsage, threshold);

        return memoryUsage < threshold;

    }

    public static boolean isHealthy(long allocateMemory, Object key) {
        if (key == null) {
            key = obj;
        }

        for (int i = 0; i < intervals.length; i++) {
            if (isMemoryHealthy(allocateMemory, i, key)) {
                return true;
            }

            LOG.warn("[MCB] Memory monitor is unhealthy now, back off retrying: {} attempt, "
                    + "executor = {}, thread id = {}", i, key, Thread.currentThread().getId());
            if (ThreadLocalRandom.current().nextBoolean()) {
                LOG.warn("[MCB] Directly abort on idx {}, executor is {}.", i, key);
                return false;
            }
            backOffSleep(intervals[i]);
        }
        return isMemoryHealthy(allocateMemory, MAXRETRIES, key);
    }

    public static void backOffSleep(long interval) {
        try {
            long millis = randomize(interval);

            LOG.info("[MCB] Back off sleeping: {} ms", millis);
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.error("[MCB] Sleep interrupted", e);
        }
    }

    /**
     * Generate random interval in [interval-delta, interval+delta)
     */
    private static long randomize(long interval) {
        // Random number within range generator for JDK 7+
        return ThreadLocalRandom.current().nextLong(
                lowerBound(interval), upperBound(interval)
        );
    }

    private static long lowerBound(long interval) {
        return Math.max(0, interval - delta);
    }

    private static long upperBound(long interval) {
        return interval + delta;
    }

    private static long[] milliseconds(double[] seconds) {
        return Arrays.stream(seconds).
                mapToLong((second) -> (long) (1000 * second)).
                toArray();
    }

    public static void releaseMem(Object key) {
        LOG.debug("[MCB] mem is {} before release", mem);
        long v = 0L;
        synchronized (BackOffRetryStrategy.class) {
            if (memUse.containsKey(key)) {
                v = memUse.get(key).v2();
                memUse.remove(key);
            }
        }
        if (v > 0) {
            atomicMinusLowBoundZero(mem, v);
        }
        LOG.debug("[MCB] mem is {} after release", mem);
    }

    private static void releaseTimeoutMemory() {
        long cur = System.currentTimeMillis();
        if (cur - lastTimeoutCleanTime < RELTIMEOUT) {
            return;
        }

        List<Long> bulks = new ArrayList<>();
        Predicate<Tuple<Long, Long>> isTimeout = t -> cur - t.v1() > RELTIMEOUT;
        synchronized (BackOffRetryStrategy.class) {
            memUse.values().stream().filter(isTimeout).forEach(v -> bulks.add(v.v2()));
            memUse.values().removeIf(isTimeout);
        }

        for (long v : bulks) {
            atomicMinusLowBoundZero(mem, v);
        }

        lastTimeoutCleanTime = cur;
    }

    private static void atomicMinusLowBoundZero(AtomicLong x, Long y) {
        long memRes = x.addAndGet(-y);
        if (memRes < 0) {
            x.compareAndSet(memRes, 0L);
        }
    }
}
