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

public enum MetricType {

    REQ_TOTAL("request_total", 0),
    REQ_COUNT_TOTAL("request_count", 1),
    FAILED_REQ_COUNT_SYS("failed_request_count_syserr", 1),
    FAILED_REQ_COUNT_CUS("failed_request_count_cuserr", 1),
    FAILED_REQ_COUNT_CB("failed_request_count_cb", 1),
    CIRCUIT_BREAKER("circuit_breaker", 2),
    DEFAULT("default", 0);

    private String name;
    private int type;

    private MetricType(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public static List<String> getNames() {
        return Arrays.stream(MetricType.values()).map(v -> v.name).collect(Collectors.toList());
    }

    public boolean isNumerical() {
        return type < 2;
    }
}
