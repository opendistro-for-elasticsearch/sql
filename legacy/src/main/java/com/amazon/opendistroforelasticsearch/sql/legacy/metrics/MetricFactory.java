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

import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.BackOffRetryStrategy;

public class MetricFactory {

    public static Metric createMetric(MetricName name) {

        switch (name) {
            case REQ_TOTAL:
            case DEFAULT_CURSOR_REQUEST_TOTAL:
            case DEFAULT:
            case PPL_REQ_TOTAL:
                return new NumericMetric<>(name.getName(), new BasicCounter());
            case CIRCUIT_BREAKER:
                return new GaugeMetric<>(name.getName(), BackOffRetryStrategy.GET_CB_STATE);
            case REQ_COUNT_TOTAL:
            case DEFAULT_CURSOR_REQUEST_COUNT_TOTAL:
            case FAILED_REQ_COUNT_CUS:
            case FAILED_REQ_COUNT_SYS:
            case FAILED_REQ_COUNT_CB:
            case PPL_REQ_COUNT_TOTAL:
            case PPL_FAILED_REQ_COUNT_CUS:
            case PPL_FAILED_REQ_COUNT_SYS:
                return new NumericMetric<>(name.getName(), new RollingCounter());
            default:
                return new NumericMetric<>(name.getName(), new BasicCounter());
        }
    }
}
