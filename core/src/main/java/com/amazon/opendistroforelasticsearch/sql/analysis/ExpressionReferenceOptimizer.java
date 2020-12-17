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

package com.amazon.opendistroforelasticsearch.sql.analysis;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.conditional.cases.CaseClause;
import com.amazon.opendistroforelasticsearch.sql.expression.conditional.cases.WhenClause;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalAggregation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalWindow;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The optimizer used to replace the expression referred in the SelectClause
 * e.g. The query SELECT abs(name), sum(age)-avg(age) FROM test GROUP BY abs(name).
 * will be translated the AST
 * Project[abs(age), sub(sum(age), avg(age))
 *  Agg(agg=[sum(age), avg(age)], group=[abs(age)]]
 *   Relation
 * The sum(age) and avg(age) in the Project could be replace by the analyzed reference, the
 * LogicalPlan should be
 * LogicalProject[Ref("abs(age)"), sub(Ref("sum(age)"), Ref("avg(age)"))
 *  LogicalAgg(agg=[sum(age), avg(age)], group=[abs(age)]]
 *   LogicalRelation
 */
public class ExpressionReferenceOptimizer
    extends ExpressionNodeVisitor<Expression, AnalysisContext> {
  private final BuiltinFunctionRepository repository;

  /**
   * The map of expression and it's reference.
   * For example, The NamedAggregator should produce the map of Aggregator to Ref(name)
   */
  private final Map<Expression, Expression> expressionMap = new HashMap<>();

  public ExpressionReferenceOptimizer(
      BuiltinFunctionRepository repository, LogicalPlan logicalPlan) {
    this.repository = repository;
    logicalPlan.accept(new ExpressionMapBuilder(), null);
  }

  public Expression optimize(Expression analyzed, AnalysisContext context) {
    return analyzed.accept(this, context);
  }

  @Override
  public Expression visitNode(Expression node, AnalysisContext context) {
    return node;
  }

  @Override
  public Expression visitFunction(FunctionExpression node, AnalysisContext context) {
    if (expressionMap.containsKey(node)) {
      return expressionMap.get(node);
    } else {
      final List<Expression> args =
          node.getArguments().stream().map(expr -> expr.accept(this, context))
              .collect(Collectors.toList());
      return (Expression) repository.compile(node.getFunctionName(), args);
    }
  }

  @Override
  public Expression visitAggregator(Aggregator<?> node, AnalysisContext context) {
    return expressionMap.getOrDefault(node, node);
  }

  /**
   * Implement this because Case/When is not registered in function repository.
   */
  @Override
  public Expression visitCase(CaseClause node, AnalysisContext context) {
    if (expressionMap.containsKey(node)) {
      return expressionMap.get(node);
    }

    List<WhenClause> whenClauses = node.getWhenClauses()
                                       .stream()
                                       .map(expr -> (WhenClause) expr.accept(this, context))
                                       .collect(Collectors.toList());
    Expression defaultResult = null;
    if (node.getDefaultResult() != null) {
      defaultResult = node.getDefaultResult().accept(this, context);
    }
    return new CaseClause(whenClauses, defaultResult);
  }

  @Override
  public Expression visitWhen(WhenClause node, AnalysisContext context) {
    return new WhenClause(
        node.getCondition().accept(this, context),
        node.getResult().accept(this, context));
  }


  /**
   * Expression Map Builder.
   */
  class ExpressionMapBuilder extends LogicalPlanNodeVisitor<Void, Void> {

    @Override
    public Void visitNode(LogicalPlan plan, Void context) {
      plan.getChild().forEach(child -> child.accept(this, context));
      return null;
    }

    @Override
    public Void visitAggregation(LogicalAggregation plan, Void context) {
      // Create the mapping for all the aggregator.
      plan.getAggregatorList().forEach(namedAggregator -> expressionMap
          .put(namedAggregator.getDelegated(),
              new ReferenceExpression(namedAggregator.getName(), namedAggregator.type())));
      // Create the mapping for all the group by.
      plan.getGroupByList().forEach(groupBy -> expressionMap
          .put(groupBy.getDelegated(),
              new ReferenceExpression(groupBy.getNameOrAlias(), groupBy.type())));
      return null;
    }

    @Override
    public Void visitWindow(LogicalWindow plan, Void context) {
      Expression windowFunc = plan.getWindowFunction();
      expressionMap.put(windowFunc,
          new ReferenceExpression(windowFunc.toString(), windowFunc.type()));
      return visitNode(plan, context);
    }
  }
}
