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
    public void test() {

    }
/*
    @Test
    public void registerMetric() {
        Metrics.clear();
        Metrics.registerMetric(new NumericMetric("test", new BasicCounter()));

        assertThat(Metrics.getAllMetrics().size(), equalTo(1));
    }

    @Test
    public void unRegisterMetric() {
        Metrics.clear();
        Metrics.registerMetric(new NumericMetric("test1", new BasicCounter()));
        Metrics.registerMetric(new NumericMetric("test2", new BasicCounter()));
        assertThat(Metrics.getAllMetrics().size(), equalTo(2));

        Metrics.unRegisterMetric("test2");
        assertThat(Metrics.getAllMetrics().size(), equalTo(1));
    }

    @Test
    public void getMetric() {
        Metrics.clear();
        Metrics.registerMetric(new NumericMetric("test1", new BasicCounter()));
        Metric metric = Metrics.getMetric("test1");

        assertThat(metric, notNullValue());
    }


    @Test
    public void getAllMetric() {
        Metrics.clear();
        Metrics.registerMetric(new NumericMetric("test1", new BasicCounter()));
        Metrics.registerMetric(new NumericMetric("test2", new BasicCounter()));
        List list = Metrics.getAllMetrics();

        assertThat(list.size(), equalTo(2));
    }

    @Test
    public void collectToJSON() {
        Metrics.clear();
        Metrics.registerMetric(new NumericMetric("test1", new BasicCounter()));
        Metrics.registerMetric(new NumericMetric("test2", new BasicCounter()));
        String res = Metrics.collectToJSON();
        JSONObject jsonObject = new JSONObject(res);

        assertThat(jsonObject.getLong("test1"), equalTo(0L));
        assertThat(jsonObject.getInt("test2"), equalTo(0));
    }
*/
}
