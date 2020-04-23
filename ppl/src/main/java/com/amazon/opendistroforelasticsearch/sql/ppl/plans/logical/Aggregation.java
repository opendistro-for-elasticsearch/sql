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

package com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical;

import com.amazon.opendistroforelasticsearch.sql.ppl.node.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.builder.UnresolvedPlanBuilder;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Logical plan node of Aggregation, the interface for building aggregation actions in queries
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class Aggregation extends UnresolvedPlan {
    private List<Expression> aggExprList;
    private List<Expression> sortExprList;
    private List<Expression> groupExprList;
    private UnresolvedPlan child;

    public Aggregation(List<Expression> aggExprList, List<Expression> sortExprList, List<Expression> groupExprList) {
        this.aggExprList = aggExprList;
        this.sortExprList = sortExprList;
        this.groupExprList = groupExprList;
    }

    @Override
    public List<UnresolvedPlan> getChild() {
        return ImmutableList.of(this.child);
    }

    @Override
    public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
        return nodeVisitor.visitAggregation(this, context);
    }

    private Aggregation(Builder builder) {
        this.child = builder.child;
    }

    /**
     * Aggregation plan builder
     */
    public static class Builder extends UnresolvedPlanBuilder<Aggregation> {
        private UnresolvedPlan child;

        public Builder(Aggregation plan) {
            super(plan);
        }

        @Override
        public UnresolvedPlanBuilder attachPlan(UnresolvedPlan attach) {
            this.child = attach;
            return this;
        }

        @Override
        public Aggregation build() {
            return new Aggregation(this);
        }
    }
}
