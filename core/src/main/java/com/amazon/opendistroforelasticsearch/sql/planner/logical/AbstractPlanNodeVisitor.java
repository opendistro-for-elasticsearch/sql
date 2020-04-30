/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.planner.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.planner.PlanNodeVisitor;

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

    public R visitJoin(LogicalJoin plan, C context) {
        return visitNode(plan, context);
    }

}
