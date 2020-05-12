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

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.planner.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.planner.PlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;

import java.util.Iterator;

/**
 * Physical plan
 */
public abstract class PhysicalPlan implements PlanNode<PhysicalPlan>,
        Iterator<BindingTuple>,
        AutoCloseable {

    @Override
    public final <R, C> R accept(PlanNodeVisitor<R, C> visitor, C context) {
        return accept(((PhysicalPlanNodeVisitor<R, C>) visitor), context);
    }

    public abstract <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context);

    public void open() {

    }
}
