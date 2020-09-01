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

package com.amazon.opendistroforelasticsearch.sql.analysis;

import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Namespace;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Symbol;
import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Alias;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.WindowFunction;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalWindow;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

@RequiredArgsConstructor
public class WindowExpressionAnalyzer extends AbstractNodeVisitor<LogicalPlan, AnalysisContext> {

  private final ExpressionAnalyzer expressionAnalyzer;

  private final LogicalPlan child;

  public LogicalPlan analyze(UnresolvedExpression projectItem, AnalysisContext context) {
    if (isWindowFunction(projectItem)) {
      return projectItem.accept(this, context);
    }
    return child;
  }

  @Override
  public LogicalPlan visitAlias(Alias node, AnalysisContext context) {
    return node.getDelegated().accept(this, context);
  }

  @Override
  public LogicalPlan visitWindowFunction(WindowFunction node, AnalysisContext context) {
    List<Expression> partitionByList = node.getPartitionByList()
                                           .stream()
                                           .map(expr -> expressionAnalyzer.analyze(expr, context))
                                           .collect(Collectors.toList());

    List<Pair<Sort.SortOption, Expression>> sortList =
        node.getSortList()
            .stream()
            .map(pair -> ImmutablePair
                .of(Sort.SortOption.PPL_ASC, expressionAnalyzer.analyze(pair.getRight(), context)))
            .collect(Collectors.toList());

    Expression windowFunction = expressionAnalyzer.analyze(node, context);
    context.peek().define(
        new Symbol(Namespace.FIELD_NAME, windowFunction.toString()), windowFunction.type());

    return new LogicalWindow(
        child,
        Collections.singletonList(windowFunction),
        new WindowDefinition(partitionByList, sortList));
  }

  private boolean isWindowFunction(UnresolvedExpression projectItem) {
    return (projectItem instanceof Alias)
        && (((Alias) projectItem).getDelegated() instanceof WindowFunction);
  }

}
