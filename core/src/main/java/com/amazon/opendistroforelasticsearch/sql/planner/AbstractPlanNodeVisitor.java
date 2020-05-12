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

package com.amazon.opendistroforelasticsearch.sql.planner;

import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;

/**
 * Abstract {@link PlanNode} Visitor.
 *
 * @param <R> return object type.
 * @param <C> context type.
 */
public abstract class AbstractPlanNodeVisitor<R, C> implements PlanNodeVisitor<R, C> {

    protected R visitNode(LogicalPlan plan, C context) {
        return null;
    }

    public R visitRelation(LogicalRelation plan, C context) {
        return visitNode(plan, context);
    }

    public R visitFilter(LogicalFilter plan, C context) {
        return visitNode(plan, context);
    }
}
