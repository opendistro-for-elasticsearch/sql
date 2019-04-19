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

package com.amazon.opendistroforelasticsearch.sql.unittest.metrics;

import com.amazon.opendistroforelasticsearch.sql.metrics.BasicCounter;
import com.amazon.opendistroforelasticsearch.sql.metrics.Metric;
import com.amazon.opendistroforelasticsearch.sql.metrics.MetricType;
import com.amazon.opendistroforelasticsearch.sql.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.metrics.NumericMetric;

import org.json.JSONObject;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class MetricsTest {

    @Test
    public void registerMetric() {
        Metrics.registerMetric(new NumericMetric("test", new BasicCounter()));

        assertThat(Metrics.getAllMetrics().size(), equalTo(MetricType.values().length + 1));
    }

    @Test
    public void unRegisterMetric() {
        assertThat(Metrics.getAllMetrics().size(), equalTo(MetricType.values().length));

        Metrics.unRegisterMetric(MetricType.REQ_COUNT_TOTAL.getName());

        assertThat(Metrics.getAllMetrics().size(), equalTo(MetricType.values().length - 1));
    }

    @Test
    public void getMetric() {
        Metric metric = Metrics.getMetric(MetricType.FAILED_REQ_COUNT_SYS.getName());

        assertThat(metric, notNullValue());
    }

    @Test
    public void getNumericMetric() {
        NumericMetric metric = Metrics.getNumericMetric(MetricType.FAILED_REQ_COUNT_SYS);

        assertThat(metric.getName(), equalTo(MetricType.FAILED_REQ_COUNT_SYS.getName()));
    }

    @Test
    public void getAllMetric() {
        List list = Metrics.getAllMetrics();

        assertThat(list.size(), equalTo(MetricType.values().length));
    }

    @Test
    public void collectToJSON() {
        String res = Metrics.collectToJSON();
        JSONObject jsonObject = new JSONObject(res);

        assertThat(jsonObject.getLong(MetricType.REQ_COUNT_TOTAL.getName()), equalTo(0L));
        assertThat(jsonObject.getInt(MetricType.CIRCUIT_BREAKER.getName()), equalTo(0));
    }

}
