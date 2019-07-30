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

package com.amazon.opendistroforelasticsearch.sql.optimizer;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Query optimizer which will optimize the input query with the {@link OptimizeRule} with registered order.
 */
public class Optimizer<T> {
    private final List<OptimizeRule<T>> optimizerRules;

    public Optimizer(List<OptimizeRule<T>> optimizerRules) {
        this.optimizerRules = optimizerRules;
    }

    /**
     * Optimize the query with registered {@link OptimizeRule} in order.
     */
    public void optimize(T expr) throws SQLFeatureNotSupportedException {
        for (OptimizeRule<T> rule : optimizerRules) {
            if (rule.match(expr)) rule.optimize(expr);
        }
    }

    /**
     * Build {@link Optimizer}
     */
    public static <T> BuilderOptimizer<T> builder() {
        return new BuilderOptimizer<T>();
    }

    /**
     * Builder of {@link Optimizer}
     */
    public static class BuilderOptimizer<T> {
        private List<OptimizeRule<T>> optimizerRules;

        public BuilderOptimizer<T> withRule(OptimizeRule<T> rule) {
            if (optimizerRules == null) {
                optimizerRules = new ArrayList<>();
            }
            optimizerRules.add(rule);
            return this;
        }

        public Optimizer<T> build() {
            return new Optimizer<>(optimizerRules);
        }
    }
}
