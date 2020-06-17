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

package com.amazon.opendistroforelasticsearch.sql.sql.parser;

import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SimpleSelectContext;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Values;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Abstract syntax tree (AST) builder.
 */
public class AstBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedPlan> {

  private final AstExpressionBuilder expressionBuilder = new AstExpressionBuilder();

  @Override
  public UnresolvedPlan visitSimpleSelect(SimpleSelectContext ctx) {
    List<ParseTree> selectElements = ctx.querySpecification().selectElements().children;
    Project project = new Project(selectElements.stream()
                                                .map(this::visitAstExpression)
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toList()));

    // Attach an Values operator with only a empty row inside so that
    // Project operator can have a chance to evaluate its expression
    // though the evaluation doesn't have any dependency in the Values.
    Values emptyValue = new Values(ImmutableList.of(Collections.emptyList()));
    return project.attach(emptyValue);
  }

  @Override
  protected UnresolvedPlan aggregateResult(UnresolvedPlan aggregate, UnresolvedPlan nextResult) {
    return nextResult != null ? nextResult : aggregate;
  }

  private UnresolvedExpression visitAstExpression(ParseTree tree) {
    return expressionBuilder.visit(tree);
  }

}
