package org.nlpcn.es4sql.query.planner.resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.plugin.nlpcn.MetaSearchResult;
import org.nlpcn.es4sql.query.planner.core.Config;
import org.nlpcn.es4sql.query.planner.resource.monitor.BackOffRetryStrategy;
import org.nlpcn.es4sql.query.planner.resource.monitor.Monitor;
import org.nlpcn.es4sql.query.planner.resource.monitor.TotalMemoryMonitor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated resource monitor
 */
public class ResourceManager {

    private static final Logger LOG = LogManager.getLogger();

    /** Actual resource monitor list */
    private final List<Monitor> monitors = new ArrayList<>();

    /** Wrapper for underlying monitors to exit with strategy */
    private final Monitor monitorStrategy;

    /** Time out for the execution */
    private final int timeout;
    private final Instant startTime;

    /** Meta result of the execution */
    private final MetaSearchResult metaResult;

    public ResourceManager(Stats stats, Config config) {
        this.monitors.add(new TotalMemoryMonitor(stats, config));
        this.monitorStrategy = new BackOffRetryStrategy(monitors, config.backOffRetryIntervals());
        this.timeout = config.timeout();
        this.startTime = Instant.now();
        this.metaResult = new MetaSearchResult();
    }

    /**
     * Is all resource monitor healthy with strategy.
     *
     * @return  true for yes
     */
    public boolean isHealthy() {
        return monitorStrategy.isHealthy();
    }

    /**
     * Is current execution time out?
     *
     * @return  true for yes
     */
    public boolean isTimeout() {
        return Duration.between(startTime, Instant.now()).getSeconds() >= timeout;
    }

    public MetaSearchResult getMetaResult() {
        return metaResult;
    }
}
