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
public class Aggregation extends UnresolvedPlan {
    private List<Expression> aggExprList;
    private List<Expression> sortExprList;
    private List<Expression> groupExprList;
    private List<Expression> argExprList;
    private UnresolvedPlan input;

    public Aggregation(List<Expression> aggExprList,
                       List<Expression> sortExprList,
                       List<Expression> groupExprList) {
        this.aggExprList = aggExprList;
        this.sortExprList = sortExprList;
        this.groupExprList = groupExprList;
        this.argExprList = null;
    }

    public Aggregation(List<Expression> aggExprList,
                       List<Expression> sortExprList,
                       List<Expression> groupExprList,
                       List<Expression> argExprList) {
        this.aggExprList = aggExprList;
        this.sortExprList = sortExprList;
        this.groupExprList = groupExprList;
        this.argExprList = argExprList;
    }

    @Override
    public Aggregation withInput(UnresolvedPlan input) {
        this.input = input;
        return this;
    }

    @Override
    public List<UnresolvedPlan> getChild() {
        return ImmutableList.of(this.input);
    }

    @Override
    public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
        return nodeVisitor.visitAggregation(this, context);
    }
}
