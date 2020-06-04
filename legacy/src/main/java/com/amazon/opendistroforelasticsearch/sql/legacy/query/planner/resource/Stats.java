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

import org.elasticsearch.client.Client;

/**
 * Statistics collector collects from ES stats, JVM etc for other components:
 * <p>
 * 1) Resource monitor
 * 2) Cost estimation
 * 3) Block size calculation
 */
public class Stats {

    /**
     * Client connection to ES cluster (unused now)
     */
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

    /**
     * Statistics data class for memory usage
     */
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
