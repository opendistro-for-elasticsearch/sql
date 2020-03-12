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

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AggCount;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor.AbstractExprVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode
public class Aggregation extends LogicalPlan {
    private LogicalPlan input;
    @Setter
    private List<Expression> aggExprs;
    @Setter
    private List<Expression> groupExprs;

    public Aggregation(List<Expression> aggExprs,
                       List<Expression> groupExprs) {
        this.aggExprs = aggExprs;
        this.groupExprs = groupExprs;
    }

    @Override
    public Aggregation withInput(LogicalPlan input) {
        this.input = input;
        return this;
    }

    public AggregationBuilder compile() {
        Optional<AggregationBuilder> groupBy = groupExprs.stream()
                .map(expr -> new GroupTermVisitor().visit(expr))
                .reduce((agg1, agg2) -> agg1.subAggregation(agg2));

        return aggExprs.stream()
                .map(expr -> new AggTermVisitor().visit(expr))
                .reduce(groupBy.get(), (agg1, agg2) -> agg1.subAggregation(agg2));
    }

    private class GroupTermVisitor extends AbstractExprVisitor<AggregationBuilder> {
        @Override
        public AggregationBuilder visitAttributeReference(AttributeReference node) {
            return AggregationBuilders.terms(node.getAttr()).field(node.getAttr());
        }
    }

    private static class AggTermVisitor extends AbstractExprVisitor<AggregationBuilder> {
        @Override
        public AggregationBuilder visitAggCount(AggCount node) {
            if (node.getField() instanceof AttributeReference) {
                return AggregationBuilders.count("count").field(((AttributeReference) node.getField()).getAttr());
            } else {
                throw new IllegalStateException("unsupported node");
            }
        }
    }
}
