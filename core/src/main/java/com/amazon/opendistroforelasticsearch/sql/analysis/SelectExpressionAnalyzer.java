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

import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Namespace;
import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Alias;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AllFields;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Analyze the select list in the {@link AnalysisContext} to construct the list of
 * {@link NamedExpression}.
 */
@RequiredArgsConstructor
public class SelectExpressionAnalyzer
    extends
    AbstractNodeVisitor<List<NamedExpression>, AnalysisContext> {
  private final ExpressionAnalyzer expressionAnalyzer;

  private ExpressionReferenceOptimizer optimizer;

  /**
   * Analyze Select fields.
   */
  public List<NamedExpression> analyze(List<UnresolvedExpression> selectList,
                                       AnalysisContext analysisContext,
                                       ExpressionReferenceOptimizer optimizer) {
    this.optimizer = optimizer;
    ImmutableList.Builder<NamedExpression> builder = new ImmutableList.Builder<>();
    for (UnresolvedExpression unresolvedExpression : selectList) {
      builder.addAll(unresolvedExpression.accept(this, analysisContext));
    }
    return builder.build();
  }

  @Override
  public List<NamedExpression> visitField(Field node, AnalysisContext context) {
    return Collections.singletonList(DSL.named(node.accept(expressionAnalyzer, context)));
  }

  @Override
  public List<NamedExpression> visitAlias(Alias node, AnalysisContext context) {
    Expression expr = referenceIfSymbolDefined(node, context);
    return Collections.singletonList(DSL.named(
        unqualifiedNameIfFieldOnly(node, context),
        expr,
        node.getAlias()));
  }

  /**
   * The Alias could be
   * 1. SELECT name, AVG(age) FROM s BY name ->
   * Project(Alias("name", expr), Alias("AVG(age)", aggExpr))
   * Agg(Alias("AVG(age)", aggExpr))
   * 2. SELECT length(name), AVG(age) FROM s BY length(name)
   * Project(Alias("name", expr), Alias("AVG(age)", aggExpr))
   * Agg(Alias("AVG(age)", aggExpr))
   * 3. SELECT length(name) as l, AVG(age) FROM s BY l
   * Project(Alias("name", expr, l), Alias("AVG(age)", aggExpr))
   * Agg(Alias("AVG(age)", aggExpr), Alias("length(name)", groupExpr))
   */
  private Expression referenceIfSymbolDefined(Alias expr,
                                              AnalysisContext context) {
    UnresolvedExpression delegatedExpr = expr.getDelegated();
    return optimizer.optimize(delegatedExpr.accept(expressionAnalyzer, context), context);
  }

  @Override
  public List<NamedExpression> visitAllFields(AllFields node,
                                              AnalysisContext context) {
    TypeEnvironment environment = context.peek();
    Map<String, ExprType> lookupAllFields = environment.lookupAllFields(Namespace.FIELD_NAME);
    return lookupAllFields.entrySet().stream().map(entry -> DSL.named(entry.getKey(),
        new ReferenceExpression(entry.getKey(), entry.getValue()))).collect(Collectors.toList());
  }

  /**
   * Get unqualified name if select item is just a field. For example, suppose an index
   * named "accounts", return "age" for "SELECT accounts.age". But do nothing for expression
   * in "SELECT ABS(accounts.age)".
   * Note that an assumption is made implicitly that original name field in Alias must be
   * the same as the values in QualifiedName. This is true because AST builder does this.
   * Otherwise, what unqualified() returns will override Alias's name as NamedExpression's name
   * even though the QualifiedName doesn't have qualifier.
   */
  private String unqualifiedNameIfFieldOnly(Alias node, AnalysisContext context) {
    UnresolvedExpression selectItem = node.getDelegated();
    if (selectItem instanceof QualifiedName) {
      QualifierAnalyzer qualifierAnalyzer = new QualifierAnalyzer(context);
      return qualifierAnalyzer.unqualified((QualifiedName) selectItem);
    }
    return node.getName();
  }

}
