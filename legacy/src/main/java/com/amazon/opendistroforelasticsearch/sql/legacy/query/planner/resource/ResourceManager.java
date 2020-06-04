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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.join.MetaSearchResult;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.BackOffRetryStrategy;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.Config;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.monitor.Monitor;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.monitor.TotalMemoryMonitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated resource monitor
 */
public class ResourceManager {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Actual resource monitor list
     */
    private final List<Monitor> monitors = new ArrayList<>();

    /**
     * Time out for the execution
     */
    private final int timeout;
    private final Instant startTime;

    /**
     * Meta result of the execution
     */
    private final MetaSearchResult metaResult;

    public ResourceManager(Stats stats, Config config) {
        this.monitors.add(new TotalMemoryMonitor(stats, config));
        this.timeout = config.timeout();
        this.startTime = Instant.now();
        this.metaResult = new MetaSearchResult();
    }

    /**
     * Is all resource monitor healthy with strategy.
     *
     * @return true for yes
     */
    public boolean isHealthy() {
        return BackOffRetryStrategy.isHealthy();
    }

    /**
     * Is current execution time out?
     *
     * @return true for yes
     */
    public boolean isTimeout() {
        return Duration.between(startTime, Instant.now()).getSeconds() >= timeout;
    }

    public MetaSearchResult getMetaResult() {
        return metaResult;
    }
}
