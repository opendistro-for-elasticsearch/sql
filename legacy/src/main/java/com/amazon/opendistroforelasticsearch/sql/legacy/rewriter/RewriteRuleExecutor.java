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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Query RewriteRuleExecutor which will execute the {@link RewriteRule} with registered order.
 */
public class RewriteRuleExecutor<T extends SQLQueryExpr> {
    private final List<RewriteRule<T>> rewriteRules;

    public RewriteRuleExecutor(List<RewriteRule<T>> rewriteRules) {
        this.rewriteRules = rewriteRules;
    }

    /**
     * Execute the registered {@link RewriteRule} in order on the Query.
     */
    public void executeOn(T expr) throws SQLFeatureNotSupportedException {
        for (RewriteRule<T> rule : rewriteRules) {
            if (rule.match(expr)) {
                rule.rewrite(expr);
            }
        }
    }

    /**
     * Build {@link RewriteRuleExecutor}
     */
    public static <T extends SQLQueryExpr> BuilderOptimizer<T> builder() {
        return new BuilderOptimizer<T>();
    }

    /**
     * Builder of {@link RewriteRuleExecutor}
     */
    public static class BuilderOptimizer<T extends SQLQueryExpr> {
        private List<RewriteRule<T>> rewriteRules;

        public BuilderOptimizer<T> withRule(RewriteRule<T> rule) {
            if (rewriteRules == null) {
                rewriteRules = new ArrayList<>();
            }
            rewriteRules.add(rule);
            return this;
        }

        public RewriteRuleExecutor<T> build() {
            return new RewriteRuleExecutor<T>(rewriteRules);
        }
    }
}
