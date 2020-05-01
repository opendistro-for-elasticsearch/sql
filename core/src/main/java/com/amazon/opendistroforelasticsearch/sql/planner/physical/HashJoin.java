/*
 *    Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Physical join operator for hash join algorithm
 */
@RequiredArgsConstructor
public class HashJoin extends PhysicalPlan {

    private final PhysicalPlan left;
    private final PhysicalPlan right;
    private final String joinType;
    private final List<String> joinFieldNames;

    private Iterator<BindingTuple> it;

    @Override
    public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
        return visitor.visitHashJoin(this, context);
    }

    @Override
    public List<PhysicalPlan> getChild() {
        return Arrays.asList(left, right);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public boolean hasNext() {
        if (it == null) {
            it = join(
                prefetch(left), prefetch(right)
            ).iterator();
        }
        return it.hasNext();
    }

    @Override
    public BindingTuple next() {
        return it.next();
    }

    private List<BindingTuple> prefetch(PhysicalPlan child) {
        List<BindingTuple> data = new ArrayList<>();
        while (child.hasNext()) {
            data.add(child.next());
        }
        return data;
    }

    private List<BindingTuple> join(List<BindingTuple> left, List<BindingTuple> right) {
        switch (joinType.toLowerCase()) {
            case "cross":
                return crossJoin(left, right);
            default:
                throw new IllegalStateException("Assertion: Invalid join type should be captured by parser");
        }
    }

    private List<BindingTuple> crossJoin(List<BindingTuple> left, List<BindingTuple> right) {
        List<BindingTuple> result = new ArrayList<>();
        for (BindingTuple row1 : left) {
            for (BindingTuple row2 : right) {
                BindingTuple combined = BindingTuple.builder().
                                                     bindingMap(row1.getBindingMap()).
                                                     bindingMap(row2.getBindingMap()).
                                                     build();
                result.add(combined);
            }
        }

        return result;
    }

}
