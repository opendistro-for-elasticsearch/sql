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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.STRUCT;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Rename the binding name in {@link BindingTuple}.
 * The mapping maintain the relation between source and target.
 * it means BindingTuple.resolve(target) = BindingTuple.resolve(source).
 */
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class RenameOperator extends PhysicalPlan {
    private final PhysicalPlan input;
    private final Map<ReferenceExpression, ReferenceExpression> mapping;

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
    public ExprValue next() {
        ExprValue inputValue = input.next();
        if (STRUCT == inputValue.type()) {
            Map<String, ExprValue> tupleValue = ExprValueUtils.getTupleValue(inputValue);
            ImmutableMap.Builder<String, ExprValue> mapBuilder = new Builder<>();
            for (String bindName : tupleValue.keySet()) {
                if (mapping.containsKey(DSL.ref(bindName))) {
                    mapBuilder.put(mapping.get(DSL.ref(bindName)).getAttr(), tupleValue.get(bindName));
                } else {
                    mapBuilder.put(bindName, tupleValue.get(bindName));
                }
            }
            return ExprTupleValue.fromExprValueMap(mapBuilder.build());
        } else {
            return inputValue;
        }
    }
}
