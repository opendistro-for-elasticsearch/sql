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

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN.CommandType;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Logical Plan DSL.
 */
@UtilityClass
public class LogicalPlanDSL {

  public static LogicalPlan aggregation(
      LogicalPlan input, List<NamedAggregator> aggregatorList, List<NamedExpression> groupByList) {
    return new LogicalAggregation(input, aggregatorList, groupByList);
  }

  public static LogicalPlan filter(LogicalPlan input, Expression expression) {
    return new LogicalFilter(input, expression);
  }

  public static LogicalPlan relation(String tableName) {
    return new LogicalRelation(tableName);
  }

  public static LogicalPlan rename(
      LogicalPlan input, Map<ReferenceExpression, ReferenceExpression> renameMap) {
    return new LogicalRename(input, renameMap);
  }

  public static LogicalPlan project(LogicalPlan input, NamedExpression... fields) {
    return new LogicalProject(input, Arrays.asList(fields));
  }

  public LogicalPlan window(LogicalPlan input,
                            Expression windowFunction,
                            WindowDefinition windowDefinition) {
    return new LogicalWindow(input, windowFunction, windowDefinition);
  }

  public static LogicalPlan remove(LogicalPlan input, ReferenceExpression... fields) {
    return new LogicalRemove(input, ImmutableSet.copyOf(fields));
  }

  public static LogicalPlan eval(
      LogicalPlan input, Pair<ReferenceExpression, Expression>... expressions) {
    return new LogicalEval(input, Arrays.asList(expressions));
  }

  public static LogicalPlan sort(
      LogicalPlan input, Integer count, Pair<SortOption, Expression>... sorts) {
    return new LogicalSort(input, count, Arrays.asList(sorts));
  }

  public static LogicalPlan dedupe(LogicalPlan input, Expression... fields) {
    return dedupe(input, 1, false, false, fields);
  }

  public static LogicalPlan dedupe(
      LogicalPlan input,
      int allowedDuplication,
      boolean keepEmpty,
      boolean consecutive,
      Expression... fields) {
    return new LogicalDedupe(
        input, Arrays.asList(fields), allowedDuplication, keepEmpty, consecutive);
  }

  public static LogicalPlan head(
      LogicalPlan input, boolean keeplast, Expression whileExpr, int number) {
    return new LogicalHead(input, keeplast, whileExpr, number);
  }
  
  public static LogicalPlan rareTopN(LogicalPlan input, CommandType commandType,
      List<Expression> groupByList, Expression... fields) {
    return rareTopN(input, commandType, 10, groupByList, fields);
  }

  public static LogicalPlan rareTopN(LogicalPlan input, CommandType commandType, int noOfResults,
      List<Expression> groupByList, Expression... fields) {
    return new LogicalRareTopN(input, commandType, noOfResults, Arrays.asList(fields), groupByList);
  }

  public static LogicalPlan indexScan(String tableName, Expression filter) {
    return new LogicalIndexScan(tableName, filter);
  }

  public static LogicalPlan indexScanAgg(String tableName, List<NamedAggregator> aggregators,
                                         List<NamedExpression> groupByList) {
    return new LogicalIndexScanAggregation(tableName, aggregators, groupByList);
  }

  public static LogicalPlan indexScanAgg(String tableName,
                                         Expression filter,
                                         List<NamedAggregator> aggregators,
                                         List<NamedExpression> groupByList) {
    return new LogicalIndexScanAggregation(tableName, filter, aggregators, groupByList);
  }

  @SafeVarargs
  public LogicalPlan values(List<LiteralExpression>... values) {
    return new LogicalValues(Arrays.asList(values));
  }

}
