/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.system;

import static com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils.systemTable;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.system.ElasticsearchCatIndicesRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.system.ElasticsearchDescribeIndexRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.system.ElasticsearchSystemRequest;
import com.amazon.opendistroforelasticsearch.sql.planner.DefaultImplementor;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils;
import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Elasticsearch System Index Table Implementation.
 */
public class ElasticsearchSystemIndex implements Table {
  /**
   * System Index Name.
   */
  private final Pair<ElasticsearchSystemIndexSchema, ElasticsearchSystemRequest> systemIndexBundle;

  public ElasticsearchSystemIndex(
      ElasticsearchClient client, String indexName) {
    this.systemIndexBundle = buildIndexBundle(client, indexName);
  }

  @Override
  public Map<String, ExprType> getFieldTypes() {
    return systemIndexBundle.getLeft().getMapping();
  }

  @Override
  public PhysicalPlan implement(LogicalPlan plan) {
    return plan.accept(new ElasticsearchSystemIndexDefaultImplementor(), null);
  }

  @VisibleForTesting
  @RequiredArgsConstructor
  public class ElasticsearchSystemIndexDefaultImplementor
      extends DefaultImplementor<Object> {

    @Override
    public PhysicalPlan visitRelation(LogicalRelation node, Object context) {
      return new ElasticsearchSystemIndexScan(systemIndexBundle.getRight());
    }
  }

  /**
   * Constructor of ElasticsearchSystemIndexName.
   *
   * @param indexName index name;
   */
  private Pair<ElasticsearchSystemIndexSchema, ElasticsearchSystemRequest> buildIndexBundle(
      ElasticsearchClient client, String indexName) {
    SystemIndexUtils.SystemTable systemTable = systemTable(indexName);
    if (systemTable.isSystemInfoTable()) {
      return Pair.of(ElasticsearchSystemIndexSchema.SYS_TABLE_TABLES,
          new ElasticsearchCatIndicesRequest(client));
    } else {
      return Pair.of(ElasticsearchSystemIndexSchema.SYS_TABLE_MAPPINGS,
          new ElasticsearchDescribeIndexRequest(client, systemTable.getTableName()));
    }
  }
}
