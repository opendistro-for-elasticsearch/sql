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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Metrics {

    private static Metrics metrics = new Metrics();
    private ConcurrentHashMap<String, Metric> metricMap = new ConcurrentHashMap<>();

    public static Metrics getInstance() {
        return metrics;
    }

    public void registerDefaultMetrics() {
        for (MetricType metricType : MetricType.values()) {
            registerMetric(MetricFactory.createMetric(metricType));
        }
    }

    public void registerMetric(Metric metric) {
        metricMap.put(metric.getName(), metric);
    }

    public void unRegisterMetric(String name) {
        if (name == null || !metricMap.containsKey(name)) {
            return;
        }

        metricMap.remove(name);
    }

    public Metric getMetric(String name) {
        if (name == null || !metricMap.containsKey(name)) {
            return null;
        }

        return metricMap.get(name);
    }

    public NumericMetric getNumericMetric(MetricType metricType) {
        String name = metricType.getName();
        if (!metricType.isNumerical()) {
            name = MetricType.DEFAULT.getName();
        }

        return (NumericMetric) metricMap.get(name);
    }

    public List<Metric> getAllMetrics() {
        return new ArrayList<>(metricMap.values());
    }

    public String collectToJSON() {
        JSONObject metricsJSONObject = new JSONObject();

        for (Metric metric : metricMap.values()) {
            if (metric.getName().equals("default")) continue;
            metricsJSONObject.put(metric.getName(), metric.getValue());
        }

        return metricsJSONObject.toString();
    }

    public void clear() {
        metricMap.clear();
    }
}
