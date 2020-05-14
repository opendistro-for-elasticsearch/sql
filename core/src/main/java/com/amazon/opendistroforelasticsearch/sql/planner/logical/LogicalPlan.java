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

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.planner.PlanNode;

/**
 * The abstract base class for all the Logical Plan node.
 */
public abstract class LogicalPlan implements PlanNode<LogicalPlan> {
    /**
     * Accept the {@link LogicalPlanNodeVisitor}.
     *
     * @param visitor visitor.
     * @param context visitor context.
     * @param <R>     returned object type.
     * @param <C>     context type.
     * @return returned object.
     */
    public abstract <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context);
}
