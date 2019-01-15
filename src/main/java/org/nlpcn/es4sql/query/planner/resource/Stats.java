package org.nlpcn.es4sql.query.planner.resource;

import org.elasticsearch.client.Client;

/**
 * Statistics collector collects from ES stats, JVM etc for other components:
 *
 *  1) Resource monitor
 *  2) Cost estimation
 *  3) Block size calculation
 */
public class Stats {

    /** Client connection to ES cluster (unused now) */
    private Client client;

    public Stats(Client client) {
        this.client = client;
    }

    public MemStats collectMemStats() {
        return new MemStats(
            Runtime.getRuntime().freeMemory(),
            Runtime.getRuntime().totalMemory()
        );
    }

    /** Statistics data class for memory usage */
    public static class MemStats {
        private long free;
        private long total;

        public MemStats(long free, long total) {
            this.free = free;
            this.total = total;
        }

        public long getFree() {
            return free;
        }

        public long getTotal() {
            return total;
        }
    }

    /*
    public class IndexStats {
        private long size;
        private long docNum;

        public IndexStats(long size, long docNum) {
            this.size = size;
            this.docNum = docNum;
        }
    }
    */

}
