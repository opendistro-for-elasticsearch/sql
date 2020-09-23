/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.planner;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalAggregation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRename;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.AggregationOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.FilterOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanTestBase;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RenameOperator;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PlannerTest extends PhysicalPlanTestBase {
  @Mock
  private PhysicalPlan scan;

  @Mock
  private StorageEngine storageEngine;

  @BeforeEach
  public void setUp() {
    when(storageEngine.getTable(any())).thenReturn(new MockTable());
  }

  @Test
  public void planner_test() {
    assertPhysicalPlan(
        PhysicalPlanDSL.rename(
            PhysicalPlanDSL.agg(
                PhysicalPlanDSL.filter(
                    scan,
                    dsl.equal(DSL.ref("response", INTEGER), DSL.literal(10))
                ),
                ImmutableList.of(dsl.avg(DSL.ref("response", INTEGER))),
                ImmutableList.of()
            ),
            ImmutableMap.of(DSL.ref("ivalue", INTEGER), DSL.ref("avg(response)", DOUBLE))
        ),
        LogicalPlanDSL.rename(
            LogicalPlanDSL.aggregation(
                LogicalPlanDSL.filter(
                    LogicalPlanDSL.relation("schema"),
                    dsl.equal(DSL.ref("response", INTEGER), DSL.literal(10))
                ),
                ImmutableList.of(dsl.avg(DSL.ref("response", INTEGER))),
                ImmutableList.of()
            ),
            ImmutableMap.of(DSL.ref("ivalue", INTEGER), DSL.ref("avg(response)", DOUBLE))
        )
    );
  }

  @Test
  public void plan_a_query_without_relation_involved() {
    // Storage engine mock is not needed here since no relation involved.
    Mockito.reset(storageEngine);

    assertPhysicalPlan(
        PhysicalPlanDSL.project(
            PhysicalPlanDSL.values(emptyList()),
            DSL.named("123", DSL.literal(123)),
            DSL.named("hello", DSL.literal("hello")),
            DSL.named("false", DSL.literal(false))
        ),
        LogicalPlanDSL.project(
            LogicalPlanDSL.values(emptyList()),
            DSL.named("123", DSL.literal(123)),
            DSL.named("hello", DSL.literal("hello")),
            DSL.named("false", DSL.literal(false))
        )
    );
  }

  protected void assertPhysicalPlan(PhysicalPlan expected, LogicalPlan logicalPlan) {
    assertEquals(expected, analyze(logicalPlan));
  }

  protected PhysicalPlan analyze(LogicalPlan logicalPlan) {
    return new Planner(storageEngine).plan(logicalPlan);
  }

  protected class MockTable extends LogicalPlanNodeVisitor<PhysicalPlan, Object> implements Table {

    @Override
    public Map<String, ExprType> getFieldTypes() {
      throw new UnsupportedOperationException();
    }

    @Override
    public PhysicalPlan implement(LogicalPlan plan) {
      return plan.accept(this, null);
    }

    @Override
    public PhysicalPlan visitRelation(LogicalRelation plan, Object context) {
      return scan;
    }

    @Override
    public PhysicalPlan visitFilter(LogicalFilter plan, Object context) {
      return new FilterOperator(plan.getChild().get(0).accept(this, context), plan.getCondition());
    }

    @Override
    public PhysicalPlan visitAggregation(LogicalAggregation plan, Object context) {
      return new AggregationOperator(plan.getChild().get(0).accept(this, context),
          plan.getAggregatorList(), plan.getGroupByList()
      );
    }

    @Override
    public PhysicalPlan visitRename(LogicalRename plan, Object context) {
      return new RenameOperator(plan.getChild().get(0).accept(this, context),
          plan.getRenameMap());
    }
  }
}
