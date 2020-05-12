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

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class SelectOperator extends PhysicalPlan {
    private final PhysicalPlan input;
    private final List<Expression> selectList;
    private BindingTuple next = null;

    @Override
    public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
        return null;
    }

    @Override
    public List<PhysicalPlan> getChild() {
        return Arrays.asList(input);
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public boolean hasNext() {
        return input.hasNext();
    }

    @Override
    public BindingTuple next() {
        BindingTuple next = input.next();
        BindingTuple.BindingTupleBuilder builder = BindingTuple.builder();
        for (Expression select : selectList) {
            ExprValue exprValue = select.valueOf(next);
            builder.binding(select.toString(), exprValue);
        }
        return builder.build();
    }
}
