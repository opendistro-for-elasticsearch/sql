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
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Rename the binding name in {@link BindingTuple}.
 * The mapping maintain the relation between target and source.
 * it means BindingTuple.resolve(target) = BindingTuple.resolve(source).
 */
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class RenameOperator extends PhysicalPlan {
    private final PhysicalPlan input;
    private final Map<Expression, Expression> mapping;

    @Override
    public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
        return visitor.visitRename(this, context);
    }

    @Override
    public List<PhysicalPlan> getChild() {
        return Collections.singletonList(input);
    }

    @Override
    public boolean hasNext() {
        return input.hasNext();
    }

    @Override
    public BindingTuple next() {
        BindingTuple bindingTuple = input.next();
        return new BindingTuple() {
            @Override
            public ExprValue resolve(ReferenceExpression ref) {
                return bindingTuple.resolve(mapping.getOrDefault(ref, ref));
            }
        };
    }
}
