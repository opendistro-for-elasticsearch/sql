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

import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.RollingCounter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RollingCounterTest {
    @Mock
    Clock clock;

    @Test
    public void increment() {
        RollingCounter counter = new RollingCounter(3, 1, clock);
        for (int i=0; i<5; ++i) {
            counter.increment();
        }

        assertThat(counter.getValue(), equalTo(0L));

        when(clock.millis()).thenReturn(1000L); // 1 second passed
        assertThat(counter.getValue(), equalTo(5L));

        counter.increment();
        counter.increment();

        when(clock.millis()).thenReturn(2000L); // 1 second passed
        assertThat(counter.getValue(), lessThanOrEqualTo(3L));

        when(clock.millis()).thenReturn(3000L); // 1 second passed
        assertThat(counter.getValue(), equalTo(0L));

    }

    @Test
    public void add() {
        RollingCounter counter = new RollingCounter(3, 1, clock);

        counter.add(6);
        assertThat(counter.getValue(), equalTo(0L));

        when(clock.millis()).thenReturn(1000L); // 1 second passed
        assertThat(counter.getValue(), equalTo(6L));

        counter.add(4);
        when(clock.millis()).thenReturn(2000L); // 1 second passed
        assertThat(counter.getValue(), equalTo(4L));

        when(clock.millis()).thenReturn(3000L); // 1 second passed
        assertThat(counter.getValue(), equalTo(0L));
    }

    @Test
    public void trim() {
        RollingCounter counter = new RollingCounter(2, 1, clock);

        for (int i=1; i<6; ++i) {
            counter.increment();
            assertThat(counter.size(), equalTo(i));
            when(clock.millis()).thenReturn(i * 1000L); // i seconds passed
        }
        counter.increment();
        assertThat(counter.size(), lessThanOrEqualTo(3));
    }

}
