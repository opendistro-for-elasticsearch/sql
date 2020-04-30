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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage;

import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.TableScan;
import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Elasticsearch table scan operator
 */
@RequiredArgsConstructor
public class ElasticsearchIndexScan extends TableScan {

    private final SearchRequestBuilder request;

    private Iterator<BindingTuple> it;

    @Override
    public List<PhysicalPlan> getChild() {
        return Collections.emptyList();
    }

    @Override
    public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
        return visitor.visitTableScan(this, context);
    }

    @Override
    public boolean hasNext() {
        if (it == null) {
            List<BindingTuple> result = new ArrayList<>();
            SearchHit[] hits = request.get().getHits().getHits();
            for (SearchHit hit : hits) {
                result.add(BindingTuple.from(hit.getSourceAsMap()));
            }
            it = result.iterator();
        }
        return it.hasNext();
    }

    @Override
    public BindingTuple next() {
        return it.next();
    }

    @Override
    public void close() throws Exception {

    }
}
