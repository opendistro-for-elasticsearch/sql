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

import com.amazon.opendistroforelasticsearch.sql.planner.logical.AbstractPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.Metadata;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;

@RequiredArgsConstructor
public class ElasticsearchTable implements Table {

    private final Client client;
    private final String tableName;

    @Override
    public Metadata getMetaData() {
        return null;
    }

    @Override
    public PhysicalPlan find(LogicalPlan plan) {
        // Suppose Elasticsearch DSL doesn't support any filter or aggregation
        // so that no push down optimization can be performed.

        SearchRequestBuilder request = plan.accept(
            new AbstractPlanNodeVisitor<SearchRequestBuilder, Object>() {
                @Override
                public SearchRequestBuilder visitRelation(LogicalRelation node, Object context) {
                    return client.prepareSearch(node.getRelationName());
                }

                @Override
                public SearchRequestBuilder visitFilter(LogicalFilter plan, Object context) {
                    return super.visitFilter(plan, context);
                }
            },
            null);

        return new ElasticsearchIndexScan(request);
    }

}
