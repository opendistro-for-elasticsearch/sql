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

package com.amazon.opendistroforelasticsearch.sql.legacy.metrics;

import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings;

import java.time.Clock;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Rolling counter. The count is refreshed every interval. In every interval the count is cumulative.
 */
public class RollingCounter implements Counter<Long> {

    private final long capacity;
    private final long window;
    private final long interval;
    private final Clock clock;
    private final ConcurrentSkipListMap<Long, Long> time2CountWin;
    private final LongAdder count;

    public RollingCounter() {
        this(LocalClusterState.state().getSettingValue(SqlSettings.METRICS_ROLLING_WINDOW),
                LocalClusterState.state().getSettingValue(SqlSettings.METRICS_ROLLING_INTERVAL));
    }

    public RollingCounter(long window, long interval, Clock clock) {
        this.window = window;
        this.interval = interval;
        this.clock = clock;
        time2CountWin = new ConcurrentSkipListMap<>();
        count = new LongAdder();
        capacity = window / interval * 2;
    }

    public RollingCounter(long window, long interval) {
        this(window, interval, Clock.systemDefaultZone());
    }

    @Override
    public void increment() {
        add(1L);
    }

    @Override
    public void add(long n) {
        trim();
        time2CountWin.compute(getKey(clock.millis()), (k, v) -> (v == null) ? n : v + n);
    }

    @Override
    public Long getValue() {
        return getValue(getPreKey(clock.millis()));
    }

    public long getValue(long key) {
        Long res = time2CountWin.get(key);
        if (res == null) {
            return 0;
        }

        return res;
    }

    public long getSum() {
        return count.longValue();
    }

    private void trim() {
        if (time2CountWin.size() > capacity) {
            time2CountWin.headMap(getKey(clock.millis() - window * 1000)).clear();
        }
    }

    private long getKey(long millis) {
        return millis / 1000 / this.interval;
    }

    private long getPreKey(long millis) {
        return getKey(millis) - 1;
    }

    public int size() {
        return time2CountWin.size();
    }

    public void reset() {
        time2CountWin.clear();
    }

}
