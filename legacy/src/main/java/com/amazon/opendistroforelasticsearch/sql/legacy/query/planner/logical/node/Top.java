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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.LogicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.Row;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.estimation.Cost;

import java.util.Map;

/**
 * Operator that keep only Top N rows and stop iteration.
 *
 * @param <T> data row object
 */
public class Top<T> implements LogicalOperator, PhysicalOperator<T> {

    private final PlanNode next;

    /**
     * Number of rows to return in total
     */
    private int count;

    @SuppressWarnings("unchecked")
    public Top(PlanNode next, int count) {
        this.next = next;
        this.count = count;
    }

    @Override
    public PlanNode[] children() {
        return new PlanNode[]{next};
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasNext() {
        return count > 0 && ((PhysicalOperator<T>) next).hasNext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Row<T> next() {
        count--;
        return ((PhysicalOperator<T>) next).next();
    }

    @Override
    public <U> PhysicalOperator[] toPhysical(Map<LogicalOperator, PhysicalOperator<U>> optimalOps) {
        if (!(next instanceof LogicalOperator)) {
            throw new IllegalStateException("Only logical operator can perform this toPhysical() operation");
        }
        return new PhysicalOperator[]{new Top<>(optimalOps.get(next), count)};
    }

    @Override
    public Cost estimate() {
        return new Cost();
    }

    @Override
    public String toString() {
        return "Top [ " + "count=" + count + " ]";
    }
}
