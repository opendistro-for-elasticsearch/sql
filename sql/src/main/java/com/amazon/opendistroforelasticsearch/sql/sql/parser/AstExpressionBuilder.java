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
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.BooleanContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SignedDecimalContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SignedRealContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.StringContext;

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QualifiedNameContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Expression builder to parse text to expression in AST.
 */
public class AstExpressionBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedExpression> {

  @Override
  public UnresolvedExpression visitQualifiedName(QualifiedNameContext ctx) {
    return new QualifiedName(
        ctx.ident()
           .stream()
           .map(ParserRuleContext::getText)
           .map(StringUtils::unquoteIdentifier)
           .collect(Collectors.toList())
    );
  }

  @Override
  public UnresolvedExpression visitString(StringContext ctx) {
    return AstDSL.stringLiteral(unquoteIdentifier(ctx.getText()));
  }

  @Override
  public UnresolvedExpression visitSignedDecimal(SignedDecimalContext ctx) {
    return AstDSL.intLiteral(Integer.valueOf(ctx.getText()));
  }

  @Override
  public UnresolvedExpression visitSignedReal(SignedRealContext ctx) {
    return AstDSL.doubleLiteral(Double.valueOf(ctx.getText()));
  }

  @Override
  public UnresolvedExpression visitBoolean(BooleanContext ctx) {
    return AstDSL.booleanLiteral(Boolean.valueOf(ctx.getText()));
  }

}
