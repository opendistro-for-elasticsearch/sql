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

package com.amazon.opendistroforelasticsearch.sql.storage.file;

import com.amazon.opendistroforelasticsearch.sql.planner.logical.AbstractPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.TableScan;
import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.storage.Metadata;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class FileTable implements Table {

    private final List<BindingTuple> dataRows;

    @Override
    public Metadata getMetaData() {
        return null;
    }

    @Override
    public PhysicalPlan find(LogicalPlan plan) {
        return plan.accept(new AbstractPlanNodeVisitor<PhysicalPlan, Object>() {
            @Override
            public PhysicalPlan visitRelation(LogicalRelation plan, Object context) {
                return new TableScan() {
                    private final Iterator<BindingTuple> it = dataRows.iterator();

                    @Override
                    public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
                        return visitor.visitTableScan(this, context);
                    }

                    @Override
                    public List<PhysicalPlan> getChild() {
                        return Collections.emptyList();
                    }

                    @Override
                    public void close() throws Exception {
                    }

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public BindingTuple next() {
                        return it.next();
                    }
                };
            }
        }, null);
    }

}
