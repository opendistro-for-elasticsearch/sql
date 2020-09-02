/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN.CommandType;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Physical Plan DSL.
 */
@UtilityClass
public class PhysicalPlanDSL {

  public static AggregationOperator agg(
      PhysicalPlan input, List<Aggregator> aggregators, List<Expression> groups) {
    return new AggregationOperator(input, aggregators, groups);
  }

  public static FilterOperator filter(PhysicalPlan input, Expression condition) {
    return new FilterOperator(input, condition);
  }

  public static RenameOperator rename(
      PhysicalPlan input, Map<ReferenceExpression, ReferenceExpression> renameMap) {
    return new RenameOperator(input, renameMap);
  }

  public static ProjectOperator project(PhysicalPlan input, NamedExpression... fields) {
    return new ProjectOperator(input, Arrays.asList(fields));
  }

  public static RemoveOperator remove(PhysicalPlan input, ReferenceExpression... fields) {
    return new RemoveOperator(input, ImmutableSet.copyOf(fields));
  }

  public static EvalOperator eval(
      PhysicalPlan input, Pair<ReferenceExpression, Expression>... expressions) {
    return new EvalOperator(input, Arrays.asList(expressions));
  }

  public static SortOperator sort(PhysicalPlan input, Integer count, Pair<SortOption,
      Expression>... sorts) {
    return new SortOperator(input, count, Arrays.asList(sorts));
  }

  public static DedupeOperator dedupe(PhysicalPlan input, Expression... expressions) {
    return new DedupeOperator(input, Arrays.asList(expressions));
  }

  public static DedupeOperator dedupe(
      PhysicalPlan input,
      int allowedDuplication,
      boolean keepEmpty,
      boolean consecutive,
      Expression... expressions) {
    return new DedupeOperator(
        input, Arrays.asList(expressions), allowedDuplication, keepEmpty, consecutive);
  }

  public static RareTopNOperator rareTopN(PhysicalPlan input, CommandType commandType,
      List<Expression> groups, Expression... expressions) {
    return new RareTopNOperator(input, commandType, Arrays.asList(expressions), groups);
  }

  public static RareTopNOperator rareTopN(PhysicalPlan input, CommandType commandType,
      int noOfResults,
      List<Expression> groups, Expression... expressions) {
    return new RareTopNOperator(input, commandType, noOfResults, Arrays.asList(expressions),
        groups);
  }

  @SafeVarargs
  public ValuesOperator values(List<LiteralExpression>... values) {
    return new ValuesOperator(Arrays.asList(values));
  }

}
