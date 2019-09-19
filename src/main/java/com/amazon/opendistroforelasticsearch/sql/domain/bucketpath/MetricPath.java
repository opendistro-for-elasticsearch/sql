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

package com.amazon.opendistroforelasticsearch.sql.domain.bucketpath;

/**
 * The path of the metric aggregation which using "." as separator when build bucket path.
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-pipeline.html#buckets-path-syntax
 */
public class MetricPath implements Path {
    private final String name;
    private static String METRIC_SEPARATOR = ".";

    public MetricPath(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String separator() {
        return METRIC_SEPARATOR;
    }

    @Override
    public boolean isAggPath() {
        return false;
    }

    @Override
    public boolean isMetricPath() {
        return true;
    }
}