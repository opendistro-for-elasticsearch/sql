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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.blocksize.AdaptiveBlockSize;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.blocksize.BlockSize;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.blocksize.BlockSize.FixedBlockSize;

/**
 * Query planner configuration
 */
public class Config {

    public static final int DEFAULT_BLOCK_SIZE = 10000;
    public static final int DEFAULT_SCROLL_PAGE_SIZE = 10000;
    public static final int DEFAULT_CIRCUIT_BREAK_LIMIT = 85;
    public static final double[] DEFAULT_BACK_OFF_RETRY_INTERVALS = {4, 8 + 4, 16 + 4};
    public static final int DEFAULT_TIME_OUT = 60;

    /**
     * Block size for join algorithm
     */
    private BlockSize blockSize = new FixedBlockSize(DEFAULT_BLOCK_SIZE);

    /**
     * Page size for scroll on each index
     */
    private Integer[] scrollPageSizes = {DEFAULT_SCROLL_PAGE_SIZE, DEFAULT_SCROLL_PAGE_SIZE};

    /**
     * Circuit breaker trigger limit (percentage)
     */
    private Integer circuitBreakLimit = DEFAULT_CIRCUIT_BREAK_LIMIT;

    /**
     * Intervals for back off retry
     */
    private double[] backOffRetryIntervals = DEFAULT_BACK_OFF_RETRY_INTERVALS;

    /**
     * Total number of rows in final result specified by LIMIT
     */
    private int totalLimit;

    /**
     * Number of rows fetched from each table specified by JOIN_TABLES_LIMIT hint
     */
    private int tableLimit1;
    private int tableLimit2;

    /**
     * Push down column values in ON of first table to query against second table
     */
    private boolean isUseTermsFilterOptimization = false;

    /**
     * Total time out (seconds) for the execution
     */
    private int timeout = DEFAULT_TIME_OUT;


    public BlockSize blockSize() {
        return blockSize;
    }

    public void configureBlockSize(Object[] params) {
        if (params.length > 0) {
            Integer size = (Integer) params[0];
            if (size > 0) {
                blockSize = new FixedBlockSize(size);
            } else {
                blockSize = new AdaptiveBlockSize(0);
            }
        }
    }

    public Integer[] scrollPageSize() {
        return scrollPageSizes;
    }

    public void configureScrollPageSize(Object[] params) {
        if (params.length == 1) {
            scrollPageSizes = new Integer[]{
                    (Integer) params[0],
                    (Integer) params[0]
            };
        } else if (params.length >= 2) {
            scrollPageSizes = (Integer[]) params;
        }
    }

    public int circuitBreakLimit() {
        return circuitBreakLimit;
    }

    public void configureCircuitBreakLimit(Object[] params) {
        if (params.length > 0) {
            circuitBreakLimit = (Integer) params[0];
        }
    }

    public double[] backOffRetryIntervals() {
        return backOffRetryIntervals;
    }

    public void configureBackOffRetryIntervals(Object[] params) {
        backOffRetryIntervals = new double[params.length];
        for (int i = 0; i < params.length; i++) {
            backOffRetryIntervals[i] = (Integer) params[i]; //Only support integer interval for now
        }
    }

    public void configureLimit(Integer totalLimit, Integer tableLimit1, Integer tableLimit2) {
        if (totalLimit != null) {
            this.totalLimit = totalLimit;
        }
        if (tableLimit1 != null) {
            this.tableLimit1 = tableLimit1;
        }
        if (tableLimit2 != null) {
            this.tableLimit2 = tableLimit2;
        }
    }

    public int totalLimit() {
        return totalLimit;
    }

    public int tableLimit1() {
        return tableLimit1;
    }

    public int tableLimit2() {
        return tableLimit2;
    }

    public void configureTermsFilterOptimization(boolean isUseTermFiltersOptimization) {
        this.isUseTermsFilterOptimization = isUseTermFiltersOptimization;
    }

    public boolean isUseTermsFilterOptimization() {
        return isUseTermsFilterOptimization;
    }

    public void configureTimeOut(Object[] params) {
        if (params.length > 0) {
            timeout = (Integer) params[0];
        }
    }

    public int timeout() {
        return timeout;
    }
}
