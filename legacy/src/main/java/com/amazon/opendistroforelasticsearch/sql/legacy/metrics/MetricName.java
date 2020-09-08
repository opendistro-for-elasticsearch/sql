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

package com.amazon.opendistroforelasticsearch.sql.legacy.metrics;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum MetricName {

    REQ_TOTAL("request_total"),
    REQ_COUNT_TOTAL("request_count"),
    FAILED_REQ_COUNT_SYS("failed_request_count_syserr"),
    FAILED_REQ_COUNT_CUS("failed_request_count_cuserr"),
    FAILED_REQ_COUNT_CB("failed_request_count_cb"),
    DEFAULT_CURSOR_REQUEST_TOTAL("default_cursor_request_total"),
    DEFAULT_CURSOR_REQUEST_COUNT_TOTAL("default_cursor_request_count"),
    CIRCUIT_BREAKER("circuit_breaker"),
    DEFAULT("default"),

    PPL_REQ_TOTAL("ppl_request_total"),
    PPL_REQ_COUNT_TOTAL("ppl_request_count"),
    PPL_FAILED_REQ_COUNT_SYS("ppl_failed_request_count_syserr"),
    PPL_FAILED_REQ_COUNT_CUS("ppl_failed_request_count_cuserr");

    private String name;

    MetricName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> getNames() {
        return Arrays.stream(MetricName.values()).map(v -> v.name).collect(Collectors.toList());
    }


    private static Set<MetricName> NUMERICAL_METRIC = new ImmutableSet.Builder<MetricName>()
        .add(PPL_REQ_TOTAL)
        .add(PPL_REQ_COUNT_TOTAL)
        .add(PPL_FAILED_REQ_COUNT_SYS)
        .add(PPL_FAILED_REQ_COUNT_CUS)
        .build();

    public boolean isNumerical() {
        return this == REQ_TOTAL || this == REQ_COUNT_TOTAL || this == FAILED_REQ_COUNT_SYS
            || this == FAILED_REQ_COUNT_CUS || this == FAILED_REQ_COUNT_CB || this == DEFAULT
            || this == DEFAULT_CURSOR_REQUEST_TOTAL || this == DEFAULT_CURSOR_REQUEST_COUNT_TOTAL
            || NUMERICAL_METRIC.contains(this);
    }
}
