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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.join;

/**
 * Created by Eliran on 4/9/2015.
 */
public class MetaSearchResult {
    private long tookImMilli;
    private int totalNumOfShards;
    private int successfulShards;
    private int failedShards;
    private boolean isTimedOut;

    public MetaSearchResult() {
        totalNumOfShards = 0;
        failedShards = 0;
        successfulShards = 0;
        isTimedOut = false;
    }

    public int getTotalNumOfShards() {
        return totalNumOfShards;
    }

    public int getSuccessfulShards() {
        return successfulShards;
    }

    public int getFailedShards() {
        return failedShards;
    }

    public boolean isTimedOut() {
        return isTimedOut;
    }

    public long getTookImMilli() {
        return tookImMilli;
    }

    public void setTookImMilli(long tookImMilli) {
        this.tookImMilli = tookImMilli;
    }

    public void addFailedShards(int shards) {
        this.failedShards += shards;
    }

    public void addSuccessfulShards(int shards) {
        this.successfulShards += shards;
    }

    public void addTotalNumOfShards(int shards) {
        this.totalNumOfShards += shards;
    }

    public void updateTimeOut(boolean isTimedOut) {
        this.isTimedOut = this.isTimedOut || isTimedOut;
    }

}
