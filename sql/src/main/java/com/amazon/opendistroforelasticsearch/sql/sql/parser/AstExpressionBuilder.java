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

import static com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils.unquoteIdentifier;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.BooleanLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.DecimalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.StringLiteralContext;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;

/**
 * Expression builder to parse text to expression in AST.
 */
public class AstExpressionBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedExpression> {

  @Override
  public UnresolvedExpression visitStringLiteral(StringLiteralContext ctx) {
    return new Literal(unquoteIdentifier(ctx.getText()), DataType.STRING);
  }

  @Override
  public UnresolvedExpression visitDecimalLiteral(DecimalLiteralContext ctx) {
    return new Literal(Integer.valueOf(ctx.getText()), DataType.INTEGER);
  }

  @Override
  public UnresolvedExpression visitBooleanLiteral(BooleanLiteralContext ctx) {
    return new Literal(Boolean.valueOf(ctx.getText()), DataType.BOOLEAN);
  }

  @Override
  protected UnresolvedExpression aggregateResult(UnresolvedExpression aggregate, UnresolvedExpression nextResult) {
    return nextResult != null ? nextResult : aggregate;
  }

}
