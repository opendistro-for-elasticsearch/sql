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

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.amazon.opendistroforelasticsearch.sql.planner.DefaultImplementor;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/** Elasticsearch table (index) implementation. */
@RequiredArgsConstructor
public class ElasticsearchIndex implements Table {

  /**
   * Type mapping from Elasticsearch data type to expression type in our type system in query
   * engine. TODO: date, geo, ip etc.
   */
  private static final Map<String, ExprCoreType> ES_TYPE_TO_EXPR_TYPE_MAPPING =
      ImmutableMap.<String, ExprCoreType>builder()
          .put("text", ExprCoreType.STRING)
          .put("keyword", ExprCoreType.STRING)
          .put("integer", ExprCoreType.INTEGER)
          .put("long", ExprCoreType.LONG)
          .put("float", ExprCoreType.FLOAT)
          .put("double", ExprCoreType.DOUBLE)
          .put("boolean", ExprCoreType.BOOLEAN)
          .put("nested", ExprCoreType.ARRAY)
          .put("object", ExprCoreType.STRUCT)
          .put("date", ExprCoreType.TIMESTAMP)
          .build();

  /** Elasticsearch client connection. */
  private final ElasticsearchClient client;

  /** Current Elasticsearch index name. */
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

  /** TODO: Push down operations to index scan operator as much as possible in future. */
  @Override
  public PhysicalPlan implement(LogicalPlan plan) {
    ElasticsearchIndexScan indexScan = new ElasticsearchIndexScan(client, indexName,
        new ElasticsearchExprValueFactory(getFieldTypes()));

    /*
     * Visit logical plan with index scan as context so logical operators visited, such as
     * aggregation, filter, will accumulate (push down) Elasticsearch query and aggregation DSL on
     * index scan.
     */
    return plan.accept(new DefaultImplementor<ElasticsearchIndexScan>() {
          @Override
          public PhysicalPlan visitRelation(LogicalRelation node, ElasticsearchIndexScan context) {
            return indexScan;
          }
        },
        indexScan);
  }

  private ExprCoreType transformESTypeToExprType(String esType) {
    return ES_TYPE_TO_EXPR_TYPE_MAPPING.getOrDefault(esType, ExprCoreType.UNKNOWN);
  }
}
