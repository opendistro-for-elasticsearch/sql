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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.monitor;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.Config;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.Stats;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.Stats.MemStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Circuit breaker for total memory usage in JVM on current ES node.
 */
public class TotalMemoryMonitor implements Monitor {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Statistic collector
     */
    private final Stats stats;

    /**
     * Upper limit for memory usage percentage
     */
    private final int limit;

    public TotalMemoryMonitor(Stats stats, Config config) {
        this.stats = stats;
        this.limit = config.circuitBreakLimit();
    }

    @Override
    public boolean isHealthy() {
        MemStats memStats = stats.collectMemStats();
        int usage = percentage(memUsage(memStats));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Memory usage and limit: {}%, {}%", usage, limit);
        }

        return usage < limit;
    }

    private int percentage(double usage) {
        return (int) Math.round(usage * 100);
    }

    private double memUsage(MemStats memStats) {
        return (1.0 * (memStats.getTotal() - memStats.getFree())) / memStats.getTotal();
    }

}
