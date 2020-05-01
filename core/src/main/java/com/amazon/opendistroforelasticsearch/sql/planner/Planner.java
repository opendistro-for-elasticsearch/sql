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

package com.amazon.opendistroforelasticsearch.sql.planner;

import com.amazon.opendistroforelasticsearch.sql.planner.logical.AbstractPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalJoin;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.HashJoin;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Planner extends AbstractPlanNodeVisitor<PhysicalPlan, Object> {

    private final StorageEngine storageEngine;

    public PhysicalPlan plan(LogicalPlan logicalPlan) {
        return logicalPlan.accept(this, null);
    }

    @Override
    public PhysicalPlan visitJoin(LogicalJoin join, Object context) {
        return new HashJoin(
            join.getLeft().accept(this, context),
            join.getRight().accept(this, context),
            join.getJoinType(),
            join.getJoinFieldNames()
        );
    }

    @Override
    public PhysicalPlan visitRelation(LogicalRelation relation, Object context) {
        Table table = storageEngine.getTable(relation.getRelationName());
        return table.find(relation);
    }

}
