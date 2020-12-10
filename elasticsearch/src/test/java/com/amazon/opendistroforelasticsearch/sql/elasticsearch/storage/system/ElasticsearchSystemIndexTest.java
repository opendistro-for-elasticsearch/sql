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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.named;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.project;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils.TABLE_INFO;
import static com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils.mappingTable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.ProjectOperator;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchSystemIndexTest {

  @Mock
  private ElasticsearchClient client;

  @Test
  void testGetFieldTypesOfMetaTable() {
    ElasticsearchSystemIndex systemIndex = new ElasticsearchSystemIndex(client, TABLE_INFO);
    final Map<String, ExprType> fieldTypes = systemIndex.getFieldTypes();
    assertThat(fieldTypes, anyOf(
        hasEntry("TABLE_CAT", STRING)
    ));
  }

  @Test
  void testGetFieldTypesOfMappingTable() {
    ElasticsearchSystemIndex systemIndex = new ElasticsearchSystemIndex(client, mappingTable(
        "test_index"));
    final Map<String, ExprType> fieldTypes = systemIndex.getFieldTypes();
    assertThat(fieldTypes, anyOf(
        hasEntry("COLUMN_NAME", STRING)
    ));
  }

  @Test
  void implement() {
    ElasticsearchSystemIndex systemIndex = new ElasticsearchSystemIndex(client, TABLE_INFO);
    NamedExpression projectExpr = named("TABLE_NAME", ref("TABLE_NAME", STRING));

    final PhysicalPlan plan = systemIndex.implement(
        project(
            relation(TABLE_INFO),
            projectExpr
        ));
    assertTrue(plan instanceof ProjectOperator);
    assertTrue(plan.getChild().get(0) instanceof ElasticsearchSystemIndexScan);
  }
}