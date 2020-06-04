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

public class NumericMetric<T> extends Metric<T> {

    private Counter<T> counter;

    public NumericMetric(String name, Counter counter) {
        super(name);
        this.counter = counter;
    }

    public String getName() {
        return super.getName();
    }

    public Counter<T> getCounter() {
        return counter;
    }

    public void increment() {
        counter.increment();
    }

    public void increment(long n) {
        counter.add(n);
    }

    public T getValue() {
        return counter.getValue();
    }

    public void clear() {
        counter.reset();
    }

}
