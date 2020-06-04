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

import java.util.concurrent.atomic.LongAdder;

public class BasicCounter implements Counter<Long> {

    private LongAdder count = new LongAdder();

    @Override
    public void increment() {
        count.increment();
    }

    @Override
    public void add(long n) {
        count.add(n);
    }

    @Override
    public Long getValue() {
        return count.longValue();
    }

    @Override
    public void reset() {
        count.reset();
    }
}
