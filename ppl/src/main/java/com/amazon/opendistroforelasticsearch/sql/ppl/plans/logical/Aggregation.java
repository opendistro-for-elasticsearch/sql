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
import com.amazon.opendistroforelasticsearch.sql.ppl.node.NodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Expression;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Logical plan node of Aggregation, the interface for building aggregation actions in queries
 */
@Getter
@ToString
@EqualsAndHashCode
public class Aggregation extends UnresolvedPlan {
    private UnresolvedPlan input;
    @Setter
    private List<Expression> aggExprList;
    @Setter
    private List<Expression> sortExprList;
    @Setter
    private List<Expression> groupExprList;

    public Aggregation(List<Expression> aggExprList,
                       List<Expression> sortExprList,
                       List<Expression> groupExprList) {
        this.aggExprList = aggExprList;
        this.sortExprList = sortExprList;
        this.groupExprList = groupExprList;
    }

    @Override
    public Aggregation withInput(UnresolvedPlan input) {
        this.input = input;
        return this;
    }

    @Override
    public List<UnresolvedPlan> getChild() {
        return ImmutableList.of(input);
    }

    @Override
    public <R> R accept(NodeVisitor<R> nodeVisitor) {
        if (nodeVisitor instanceof AbstractNodeVisitor) {
            return ((AbstractNodeVisitor<R>) nodeVisitor).visitAggregation(this);
        } else {
            return nodeVisitor.visitChildren(this);
        }
    }
}
