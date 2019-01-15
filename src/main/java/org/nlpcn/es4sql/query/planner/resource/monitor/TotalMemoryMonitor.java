package org.nlpcn.es4sql.query.planner.resource.monitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nlpcn.es4sql.query.planner.core.Config;
import org.nlpcn.es4sql.query.planner.resource.Stats;
import org.nlpcn.es4sql.query.planner.resource.Stats.MemStats;

/**
 * Circuit breaker for total memory usage in JVM on current ES node.
 */
public class TotalMemoryMonitor implements Monitor {

    private static final Logger LOG = LogManager.getLogger();

    /** Statistic collector */
    private final Stats stats;

    /** Upper limit for memory usage percentage */
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
