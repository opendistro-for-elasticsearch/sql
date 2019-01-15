package org.nlpcn.es4sql.query.planner.resource.monitor;

/**
 * Interface for different monitor component
 */
public interface Monitor {

    /**
     * Is resource being monitored exhausted.
     * @return true if yes
     */
    boolean isHealthy();

}
