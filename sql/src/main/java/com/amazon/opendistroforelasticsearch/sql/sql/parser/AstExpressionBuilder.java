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

import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.IS_NOT_NULL;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.IS_NULL;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.LIKE;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.NOT_LIKE;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.AggregateFunctionContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.BinaryComparisonPredicateContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.BooleanContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.DateLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.IsNullPredicateContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.LikePredicateContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.MathExpressionAtomContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.NotExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.NullLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.ScalarFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SignedDecimalContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SignedRealContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.StringContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.TimeLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.TimestampLiteralContext;

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AggregateFunction;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.And;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Interval;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.IntervalUnit;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Or;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.AndExpressionContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.ColumnNameContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.IdentContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.IntervalLiteralContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.NestedExpressionAtomContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.OrExpressionContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QualifiedNameContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.TableNameContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.RuleContext;

/**
 * Expression builder to parse text to expression in AST.
 */
public class AstExpressionBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedExpression> {

  @Override
  public UnresolvedExpression visitTableName(TableNameContext ctx) {
    return visitQualifiedName(ctx.qualifiedName());
  }

  @Override
  public UnresolvedExpression visitColumnName(ColumnNameContext ctx) {
    return visitQualifiedName(ctx.qualifiedName());
  }

  @Override
  public UnresolvedExpression visitIdent(IdentContext ctx) {
    return visitIdentifiers(Collections.singletonList(ctx));
  }

  @Override
  public UnresolvedExpression visitQualifiedName(QualifiedNameContext ctx) {
    return visitIdentifiers(ctx.ident());
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
    if (ctx.functionArgs() == null) {
      return new Function(ctx.scalarFunctionName().getText(), Collections.emptyList());
    }
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
  public UnresolvedExpression visitAggregateFunction(AggregateFunctionContext ctx) {
    return new AggregateFunction(
        ctx.functionName.getText(),
        visitFunctionArg(ctx.functionArg()));
  }

  @Override
  public UnresolvedExpression visitIsNullPredicate(IsNullPredicateContext ctx) {
    return new Function(
        ctx.nullNotnull().NOT() == null ? IS_NULL.getName().getFunctionName() :
            IS_NOT_NULL.getName().getFunctionName(),
        Arrays.asList(visit(ctx.predicate())));
  }

  @Override
  public UnresolvedExpression visitLikePredicate(LikePredicateContext ctx) {
    return new Function(
        ctx.NOT() == null ? LIKE.getName().getFunctionName() :
            NOT_LIKE.getName().getFunctionName(),
        Arrays.asList(visit(ctx.left), visit(ctx.right)));
  }

  @Override
  public UnresolvedExpression visitAndExpression(AndExpressionContext ctx) {
    return new And(visit(ctx.left), visit(ctx.right));
  }

  @Override
  public UnresolvedExpression visitOrExpression(OrExpressionContext ctx) {
    return new Or(visit(ctx.left), visit(ctx.right));
  }

  @Override
  public UnresolvedExpression visitNotExpression(NotExpressionContext ctx) {
    return new Not(visit(ctx.expression()));
  }

  @Override
  public UnresolvedExpression visitString(StringContext ctx) {
    return AstDSL.stringLiteral(StringUtils.unquoteText(ctx.getText()));
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
  public UnresolvedExpression visitNullLiteral(NullLiteralContext ctx) {
    return AstDSL.nullLiteral();
  }

  @Override
  public UnresolvedExpression visitDateLiteral(DateLiteralContext ctx) {
    return AstDSL.dateLiteral(StringUtils.unquoteText(ctx.date.getText()));
  }

  @Override
  public UnresolvedExpression visitTimeLiteral(TimeLiteralContext ctx) {
    return AstDSL.timeLiteral(StringUtils.unquoteText(ctx.time.getText()));
  }

  @Override
  public UnresolvedExpression visitTimestampLiteral(
      TimestampLiteralContext ctx) {
    return AstDSL.timestampLiteral(StringUtils.unquoteText(ctx.timestamp.getText()));
  }

  @Override
  public UnresolvedExpression visitIntervalLiteral(IntervalLiteralContext ctx) {
    return new Interval(
        visit(ctx.expression()), IntervalUnit.of(ctx.intervalUnit().getText()));
  }

  @Override
  public UnresolvedExpression visitBinaryComparisonPredicate(
      BinaryComparisonPredicateContext ctx) {
    String functionName = ctx.comparisonOperator().getText();
    return new Function(
        functionName.equals("<>") ? "!=" : functionName,
        Arrays.asList(visit(ctx.left), visit(ctx.right))
    );
  }

  private QualifiedName visitIdentifiers(List<IdentContext> identifiers) {
    return new QualifiedName(
        identifiers.stream()
                   .map(RuleContext::getText)
                   .map(StringUtils::unquoteIdentifier)
                   .collect(Collectors.toList())
    );
  }

}
