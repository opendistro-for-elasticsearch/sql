/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalAggregation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalProject;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRemove;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRename;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.AggregationOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.FilterOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.ProjectOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RemoveOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RenameOperator;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch table (index) implementation.
 */
@RequiredArgsConstructor
public class ElasticsearchIndex implements Table {

    /**
     * Type mapping from Elasticsearch data type to expression type in our type system in query engine.
     * TODO: date, geo, ip etc.
     */
    private final static Map<String, ExprType> ES_TYPE_TO_EXPR_TYPE_MAPPING =
        ImmutableMap.<String, ExprType>builder().put("text", ExprType.STRING).
                                                 put("keyword", ExprType.STRING).
                                                 put("integer", ExprType.INTEGER).
                                                 put("long", ExprType.LONG).
                                                 put("float", ExprType.FLOAT).
                                                 put("double", ExprType.DOUBLE).
                                                 put("boolean", ExprType.BOOLEAN).
                                                 put("nested", ExprType.ARRAY).
                                                 put("object", ExprType.STRUCT).
                                                 build();

    /**
     * Elasticsearch client connection.
     */
    private final ElasticsearchClient client;

    /**
     * Current Elasticsearch index name.
     */
    private final String indexName;


    /*
     * TODO: Assume indexName doesn't have wildcard.
     *  Need to either handle field name conflicts
     *   or lazy evaluate when query engine pulls field type.
     */
    @Override
    public Map<String, ExprType> getFieldTypes() {
        Map<String, ExprType> fieldTypes = new HashMap<>();
        Map<String, IndexMapping> indexMappings = client.getIndexMappings(indexName);
        for (IndexMapping indexMapping : indexMappings.values()) {
            fieldTypes.putAll(indexMapping.getAllFieldTypes(this::transformESTypeToExprType));
        }
        return fieldTypes;
    }

    /**
     * TODO: Push down operations to index scan operator as much as possible in future.
     */
    @Override
    public PhysicalPlan implement(LogicalPlan plan) {
        ElasticsearchIndexScan indexScan = new ElasticsearchIndexScan(client, indexName);

        /*
         * Visit logical plan with index scan as context so logical operators visited, such as aggregation, filter,
         * will accumulate (push down) Elasticsearch query and aggregation DSL on index scan.
         */
        return plan.accept(new LogicalPlanNodeVisitor<PhysicalPlan, ElasticsearchIndexScan>() {

            @Override
            public PhysicalPlan visitProject(LogicalProject node, ElasticsearchIndexScan context) {
                return new ProjectOperator(visitChild(node, context), node.getProjectList());
            }

            @Override
            public PhysicalPlan visitRemove(LogicalRemove node, ElasticsearchIndexScan context) {
                return new RemoveOperator(visitChild(node, context), node.getRemoveList());
            }

            @Override
            public PhysicalPlan visitRename(LogicalRename node, ElasticsearchIndexScan context) {
                return new RenameOperator(visitChild(node, context), node.getRenameMap());
            }

            @Override
            public PhysicalPlan visitAggregation(LogicalAggregation node, ElasticsearchIndexScan context) {
                return new AggregationOperator(visitChild(node, context), node.getAggregatorList(), node.getGroupByList());
            }

            @Override
            public PhysicalPlan visitFilter(LogicalFilter node, ElasticsearchIndexScan context) {
                return new FilterOperator(visitChild(node, context), node.getCondition());
            }

            @Override
            public PhysicalPlan visitRelation(LogicalRelation node, ElasticsearchIndexScan context) {
                return indexScan;
            }

            private PhysicalPlan visitChild(LogicalPlan node, ElasticsearchIndexScan context) {
                // Logical operators visited here can only have single child.
                return node.getChild().get(0).accept(this, context);
            }
        }, indexScan);
    }

    private ExprType transformESTypeToExprType(String esType) {
        return ES_TYPE_TO_EXPR_TYPE_MAPPING.getOrDefault(esType, ExprType.UNKNOWN);
    }
}
