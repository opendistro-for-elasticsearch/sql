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
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.IS_NOT_NULL;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.IS_NULL;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.LIKE;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.NOT_LIKE;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.BooleanContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.MathExpressionAtomContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.ScalarFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SignedDecimalContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SignedRealContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.StringContext;

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.IdentContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.NestedExpressionAtomContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QualifiedNameContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.TableNameContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.RuleNode;

/**
 * Expression builder to parse text to expression in AST.
 */
public class AstExpressionBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedExpression> {

  @Override
  public UnresolvedExpression visitTableName(TableNameContext ctx) {
    return new QualifiedName(visitQualifiedNameText(ctx));
  }

  @Override
  public UnresolvedExpression visitIdent(IdentContext ctx) {
    return new QualifiedName(visitQualifiedNameText(ctx));
  }

  @Override
  public UnresolvedExpression visitQualifiedName(QualifiedNameContext ctx) {
    return new QualifiedName(
        ctx.ident()
           .stream()
           .map(this::visitQualifiedNameText)
           .collect(Collectors.toList())
    );
  }

  @Override
  public UnresolvedExpression visitMathExpressionAtom(MathExpressionAtomContext ctx) {
    return new Function(
        ctx.mathOperator().getText(),
        Arrays.asList(visit(ctx.left), visit(ctx.right))
    );
  }

  @Override
  public UnresolvedExpression visitNestedExpressionAtom(NestedExpressionAtomContext ctx) {
    return visit(ctx.expression()); // Discard parenthesis around
  }

  @Override
  public UnresolvedExpression visitScalarFunctionCall(ScalarFunctionCallContext ctx) {
    return new Function(
        ctx.scalarFunctionName().getText(),
        ctx.functionArgs()
           .functionArg()
           .stream()
           .map(this::visitFunctionArg)
           .collect(Collectors.toList())
    );
  }

  @Override
  public UnresolvedExpression visitIsNullPredicate(OpenDistroSQLParser.IsNullPredicateContext ctx) {
    return new Function(
        ctx.nullNotnull().NOT() == null ? IS_NULL.getName().getFunctionName() :
            IS_NOT_NULL.getName().getFunctionName(),
        Arrays.asList(visit(ctx.predicate())));
  }

  @Override
  public UnresolvedExpression visitLikePredicate(OpenDistroSQLParser.LikePredicateContext ctx) {
    return new Function(
        ctx.NOT() == null ? LIKE.getName().getFunctionName() :
            NOT_LIKE.getName().getFunctionName(),
        Arrays.asList(visit(ctx.left), visit(ctx.right)));
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

  @Override
  public UnresolvedExpression visitNullLiteral(OpenDistroSQLParser.NullLiteralContext ctx) {
    return AstDSL.nullLiteral();
  }

  @Override
  public UnresolvedExpression visitDateLiteral(OpenDistroSQLParser.DateLiteralContext ctx) {
    return AstDSL.dateLiteral(unquoteIdentifier(ctx.date.getText()));
  }

  @Override
  public UnresolvedExpression visitTimeLiteral(OpenDistroSQLParser.TimeLiteralContext ctx) {
    return AstDSL.timeLiteral(unquoteIdentifier(ctx.time.getText()));
  }

  @Override
  public UnresolvedExpression visitTimestampLiteral(
      OpenDistroSQLParser.TimestampLiteralContext ctx) {
    return AstDSL.timestampLiteral(unquoteIdentifier(ctx.timestamp.getText()));
  }

  @Override
  public UnresolvedExpression visitBinaryComparisonPredicate(
      OpenDistroSQLParser.BinaryComparisonPredicateContext ctx) {
    String functionName = ctx.comparisonOperator().getText();
    return new Function(
        functionName.equals("<>") ? "!=" : functionName,
        Arrays.asList(visit(ctx.left), visit(ctx.right))
    );
  }

  private String visitQualifiedNameText(RuleNode node) {
    return unquoteIdentifier(node.getText());
  }

}
