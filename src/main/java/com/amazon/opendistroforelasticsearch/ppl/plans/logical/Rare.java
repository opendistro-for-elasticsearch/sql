/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.ppl.plans.logical;

import com.amazon.opendistroforelasticsearch.ppl.node.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.ppl.node.NodeVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;

import java.util.List;
import java.util.Optional;

@Getter
@ToString
@EqualsAndHashCode
public class Rare extends LogicalPlan{
    private LogicalPlan input;
    @Setter
    private Literal count;
    @Setter
    private List<Expression> fieldExprs;
    @Setter
    private List<Expression> groupExprs;

    public Rare(Literal count,
               List<Expression> fieldExprs,
               List<Expression> groupExprs) {
        this.count = count;
        this.fieldExprs = fieldExprs;
        this.groupExprs = groupExprs;
    }

    @Override
    public LogicalPlan withInput(LogicalPlan input) {
        this.input = input;
        return this;
    }

    public AggregationBuilder compile() {
        Optional<AggregationBuilder> groupBy = groupExprs.stream()
                .map(expr -> new GroupTermVisitor().visit(expr))
                .reduce((agg1, agg2) -> agg1.subAggregation(agg2));

        return fieldExprs.stream()
                .map(expr -> new AggTermVisitor().visit(expr))
                .reduce(groupBy.get(), (agg1, agg2) -> agg1.subAggregation(agg2));
    }

    @Override
    public List<LogicalPlan> getChild() {
        return ImmutableList.of(input);
    }

    @Override
    public <R> R accept(NodeVisitor<R> nodeVisitor) {
        if (nodeVisitor instanceof AbstractNodeVisitor) {
            return ((AbstractNodeVisitor<R>) nodeVisitor).visitRare(this);
        } else {
            return nodeVisitor.visitChildren(this);
        }
    }

    private class GroupTermVisitor extends AbstractNodeVisitor<AggregationBuilder> {
        @Override
        public AggregationBuilder visitAttributeReference(AttributeReference node) {
            return AggregationBuilders.terms(node.getAttr())
                    .field(node.getAttr());
        }
    }

    private class AggTermVisitor extends AbstractNodeVisitor<AggregationBuilder> {
        @Override
        public AggregationBuilder visitAttributeReference(AttributeReference node) {
            return AggregationBuilders.terms(node.getAttr())
                    .field(node.getAttr())
                    .order(BucketOrder.count(true))
                    .size((int) count.getValue())
                    .subAggregation(AggregationBuilders.count("count").field(node.getAttr()));
        }
    }
}
