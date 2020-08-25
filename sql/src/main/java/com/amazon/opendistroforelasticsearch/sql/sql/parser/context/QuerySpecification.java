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

package com.amazon.opendistroforelasticsearch.sql.sql.parser.context;

import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.GroupByElementContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectElementContext;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.AggregateFunctionCallContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.AstExpressionBuilder;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Query specification domain that represents info for a simple query.
 */
@Getter
@RequiredArgsConstructor
public class QuerySpecification extends OpenDistroSQLParserBaseVisitor<Void> {

  private final QuerySpecification parent;

  private final AstExpressionBuilder expressionBuilder = new AstExpressionBuilder();

  private List<UnresolvedExpression> selectItems = new ArrayList<>();

  private List<UnresolvedExpression> aggregators = new ArrayList<>();

  private List<UnresolvedExpression> groupByItems = new ArrayList<>();

  public QuerySpecification() {
    this(null);
  }

  public void collect(ParseTree query) {
    query.accept(this);
  }

  @Override
  public Void visitSelectElement(SelectElementContext ctx) {
    selectItems.add(visitAstExpression(ctx));
    return super.visitSelectElement(ctx);
  }

  @Override
  public Void visitGroupByElement(GroupByElementContext ctx) {
    groupByItems.add(visitAstExpression(ctx));
    return super.visitGroupByElement(ctx);
  }

  @Override
  public Void visitAggregateFunctionCall(AggregateFunctionCallContext ctx) {
    aggregators.add(visitAstExpression(ctx));
    return super.visitAggregateFunctionCall(ctx);
  }

  /*
  @Override
  public Void visitQuerySpecification(QuerySpecificationContext ctx) {
    // Avoid collecting items on deeper level once sub-queries enabled
    return null;
  }
  */

  private UnresolvedExpression visitAstExpression(ParseTree tree) {
    return expressionBuilder.visit(tree);
  }

}
