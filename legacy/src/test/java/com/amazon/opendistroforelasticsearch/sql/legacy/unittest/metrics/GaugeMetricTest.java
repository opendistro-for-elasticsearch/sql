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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.metrics;

import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.GaugeMetric;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GaugeMetricTest {

    private static long x = 0;

    @Test
    public void getValue() {
        GaugeMetric gaugeMetric = new GaugeMetric<>("test", this::getSeq);

        assertThat(gaugeMetric.getValue(), equalTo(1L));
        assertThat(gaugeMetric.getValue(), equalTo(2L));

    }

    private long getSeq() {
        return ++x;
    }

}
