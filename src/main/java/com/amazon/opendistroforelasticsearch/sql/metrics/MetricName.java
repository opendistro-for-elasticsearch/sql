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

package com.amazon.opendistroforelasticsearch.sql.metrics;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MetricName {

    REQ_TOTAL("request_total"),
    REQ_COUNT_TOTAL("request_count"),
    FAILED_REQ_COUNT_SYS("failed_request_count_syserr"),
    FAILED_REQ_COUNT_CUS("failed_request_count_cuserr"),
    FAILED_REQ_COUNT_CB("failed_request_count_cb"),
    CIRCUIT_BREAKER("circuit_breaker"),
    DEFAULT("default");

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

    public boolean isNumerical() {
        return this == REQ_TOTAL || this == REQ_COUNT_TOTAL || this == FAILED_REQ_COUNT_SYS
                || this == FAILED_REQ_COUNT_CUS || this == FAILED_REQ_COUNT_CB || this == DEFAULT;
    }

}
