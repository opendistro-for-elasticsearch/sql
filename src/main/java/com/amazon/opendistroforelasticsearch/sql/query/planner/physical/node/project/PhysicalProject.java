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

package com.amazon.opendistroforelasticsearch.sql.query.planner.physical.node.project;

import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.node.scroll.BindingTupleRow;
import com.amazon.opendistroforelasticsearch.sql.query.planner.core.ColumnNode;
import com.amazon.opendistroforelasticsearch.sql.query.planner.core.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.Row;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.estimation.Cost;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The definition of Project Operator.
 */
@RequiredArgsConstructor
public class PhysicalProject implements PhysicalOperator<BindingTuple> {
    private final PhysicalOperator<BindingTuple> next;
    private final List<ColumnNode> fields;

    @Override
    public Cost estimate() {
        return null;
    }

    @Override
    public PlanNode[] children() {
        return new PlanNode[]{next};
    }

    @Override
    public boolean hasNext() {
        return next.hasNext();
    }

    @Override
    public Row<BindingTuple> next() {
        BindingTuple input = next.next().data();
        Map<String, ExprValue> output = new HashMap<>();
        for (ColumnNode field : fields) {
            ExprValue exprValue = field.getExpr().valueOf(input);
            output.put(field.getName(), exprValue);
        }
        return new BindingTupleRow(BindingTuple.builder().bindingMap(output).build());
    }
}
