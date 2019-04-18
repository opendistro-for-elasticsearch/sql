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

import java.util.function.Supplier;

public class GaugeMetric<T> extends Metric<T> {

    private T value;
    private Supplier<T> LOAD_VALUE;

    public GaugeMetric(String name, T value, Supplier<T> supplier) {
        super(name);
        this.value = value;
        this.LOAD_VALUE = supplier;
    }

    public String getName() {
        return super.getName();
    }

    public T getValue() {
        value = LOAD_VALUE.get();
        return value;
    }

}
