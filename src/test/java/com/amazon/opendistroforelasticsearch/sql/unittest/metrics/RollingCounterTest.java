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

import com.amazon.opendistroforelasticsearch.sql.metrics.RollingCounter;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public class RollingCounterTest {

    @Test
    public void increment() throws InterruptedException {
        RollingCounter counter = new RollingCounter(3, 1);
        for (int i=0; i<5; ++i) {
            counter.increment();
        }

        assertThat(counter.getValue(), equalTo(0L));

        TimeUnit.SECONDS.sleep(1L);
        assertThat(counter.getValue(), equalTo(5L));

        counter.increment();
        counter.increment();
        TimeUnit.SECONDS.sleep(1L);
        assertThat(counter.getValue(), lessThanOrEqualTo(3L));

        TimeUnit.SECONDS.sleep(1L);
        assertThat(counter.getValue(), equalTo(0L));

    }

    @Test
    public void add() throws InterruptedException {
        RollingCounter counter = new RollingCounter(3, 1);

        counter.add(6);
        assertThat(counter.getValue(), equalTo(0L));

        TimeUnit.SECONDS.sleep(1L);
        assertThat(counter.getValue(), equalTo(6L));

        counter.add(4);
        TimeUnit.SECONDS.sleep(1L);
        assertThat(counter.getValue(), equalTo(4L));

        TimeUnit.SECONDS.sleep(1L);
        assertThat(counter.getValue(), equalTo(0L));
    }

    @Test
    public void trim() throws InterruptedException {
        RollingCounter counter = new RollingCounter(2, 1);

        for (int i=1; i<6; ++i) {
            counter.increment();
            assertThat(counter.size(), equalTo(i));
            TimeUnit.SECONDS.sleep(1L);
        }
        counter.increment();
        assertThat(counter.size(), lessThanOrEqualTo(3));
    }

}
