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

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.named;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.In;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN.CommandType;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Todo, testing purpose, delete later.
 */
@ExtendWith(MockitoExtension.class)
class PhysicalPlanNodeVisitorTest extends PhysicalPlanTestBase {
  @Mock
  PhysicalPlan plan;
  @Mock
  ReferenceExpression ref;

  @Test
  public void print_physical_plan() {
    PhysicalPlan plan =
        PhysicalPlanDSL.remove(
            PhysicalPlanDSL.project(
                PhysicalPlanDSL.rename(
                    PhysicalPlanDSL.agg(
                        PhysicalPlanDSL.rareTopN(
                            PhysicalPlanDSL.filter(
                                new TestScan(),
                                dsl.equal(DSL.ref("response", INTEGER), DSL.literal(10))),
                            CommandType.TOP,
                            ImmutableList.of(),
                            DSL.ref("response", INTEGER)),
                        ImmutableList
                            .of(DSL.named("avg(response)", dsl.avg(DSL.ref("response", INTEGER)))),
                        ImmutableList.of()),
                    ImmutableMap.of(DSL.ref("ivalue", INTEGER), DSL.ref("avg(response)", DOUBLE))),
                named("ref", ref)),
            ref);

    PhysicalPlanPrinter printer = new PhysicalPlanPrinter();
    assertEquals(
        "Remove->\n"
            + "\tProject->\n"
            + "\t\tRename->\n"
            + "\t\t\tAggregation->\n"
            + "\t\t\t\tRareTopN->\n"
            + "\t\t\t\t\tFilter->",
        printer.print(plan));
  }

  @Test
  public void test_PhysicalPlanVisitor_should_return_null() {
    PhysicalPlan filter =
        PhysicalPlanDSL.filter(
            new TestScan(), dsl.equal(DSL.ref("response", INTEGER), DSL.literal(10)));
    assertNull(filter.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    PhysicalPlan aggregation =
        PhysicalPlanDSL.agg(
            filter, ImmutableList.of(DSL.named("avg(response)",
                dsl.avg(DSL.ref("response", INTEGER)))), ImmutableList.of());
    assertNull(aggregation.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    PhysicalPlan rename =
        PhysicalPlanDSL.rename(
            aggregation, ImmutableMap.of(DSL.ref("ivalue", INTEGER), DSL.ref("avg(response)",
                DOUBLE)));
    assertNull(rename.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    PhysicalPlan project = PhysicalPlanDSL.project(plan, named("ref", ref));
    assertNull(project.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    PhysicalPlan remove = PhysicalPlanDSL.remove(plan, ref);
    assertNull(remove.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    PhysicalPlan eval = PhysicalPlanDSL.eval(plan, Pair.of(ref, ref));
    assertNull(eval.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    PhysicalPlan sort = PhysicalPlanDSL.sort(plan, 100, Pair.of(SortOption.PPL_ASC, ref));
    assertNull(sort.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    PhysicalPlan dedupe = PhysicalPlanDSL.dedupe(plan, ref);
    assertNull(dedupe.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    PhysicalPlan values = PhysicalPlanDSL.values(Collections.emptyList());
    assertNull(values.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    PhysicalPlan rareTopN =
        PhysicalPlanDSL.rareTopN(plan, CommandType.TOP, 5, ImmutableList.of(), ref);
    assertNull(rareTopN.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
    }, null));
  }

  public static class PhysicalPlanPrinter extends PhysicalPlanNodeVisitor<String, Integer> {

    public String print(PhysicalPlan node) {
      return node.accept(this, 0);
    }

    @Override
    public String visitFilter(FilterOperator node, Integer tabs) {
      return name(node, "Filter->", tabs);
    }

    @Override
    public String visitAggregation(AggregationOperator node, Integer tabs) {
      return name(node, "Aggregation->", tabs);
    }

    @Override
    public String visitRename(RenameOperator node, Integer tabs) {
      return name(node, "Rename->", tabs);
    }

    @Override
    public String visitProject(ProjectOperator node, Integer tabs) {
      return name(node, "Project->", tabs);
    }

    @Override
    public String visitRemove(RemoveOperator node, Integer tabs) {
      return name(node, "Remove->", tabs);
    }

    @Override
    public String visitRareTopN(RareTopNOperator node, Integer tabs) {
      return name(node, "RareTopN->", tabs);
    }

    private String name(PhysicalPlan node, String current, int tabs) {
      String child = node.getChild().get(0).accept(this, tabs + 1);
      StringBuilder sb = new StringBuilder();
      for (Integer i = 0; i < tabs; i++) {
        sb.append("\t");
      }
      sb.append(current);
      if (!Strings.isNullOrEmpty(child)) {
        sb.append("\n");
        sb.append(child);
      }
      return sb.toString();
    }
  }
}
