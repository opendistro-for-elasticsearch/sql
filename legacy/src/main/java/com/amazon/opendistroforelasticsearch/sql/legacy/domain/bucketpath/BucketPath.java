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

package com.amazon.opendistroforelasticsearch.sql.legacy.domain.bucketpath;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The bucket path syntax
 * <AGG_NAME> [ <AGG_SEPARATOR>, <AGG_NAME> ]* [ <METRIC_SEPARATOR>, <METRIC> ]
 *
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-pipeline.html#buckets-path-syntax
 */
public class BucketPath {
    private Deque<Path> pathStack = new ArrayDeque<>();

    public BucketPath add(Path path) {
        if (pathStack.isEmpty()) {
            assert path.isMetricPath() : "The last path in the bucket path must be Metric";
        } else {
            assert path.isAggPath() : "All the other path in the bucket path must be Agg";
        }
        pathStack.push(path);
        return this;
    }

    /**
     * Return the bucket path.
     * Return "", if there is no agg or metric available
     */
    public String getBucketPath() {
        String bucketPath = pathStack.isEmpty() ? "" : pathStack.pop().getPath();
        return pathStack.stream()
                .map(path -> path.getSeparator() + path.getPath())
                .reduce(bucketPath, String::concat);
    }
}
