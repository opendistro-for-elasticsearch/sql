/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.ppl.parser;

import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.BinaryArithmeticContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.BooleanLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.CompareExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.DecimalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.FieldExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.IdentsAsQualifiedNameContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.IdentsAsWildcardQualifiedNameContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.InExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.IntegerLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.IntervalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.KeywordsAsQualifiedNameContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.KeywordsAsWildcardQualifiedNameContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalAndContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalNotContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalOrContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalXorContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.ParentheticBinaryArithmeticContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.PercentileAggFunctionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.SortFieldContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.StatsFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.StringLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.WcFieldExpressionContext;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.AggregateFunction;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AllFields;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.And;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Compare;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.In;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Interval;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.IntervalUnit;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Let;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Or;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Xor;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.ppl.utils.ArgumentFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Class of building AST Expression nodes.
 */
public class AstExpressionBuilder extends OpenDistroPPLParserBaseVisitor<UnresolvedExpression> {
  /**
   * Eval clause.
   */
  @Override
  public UnresolvedExpression visitEvalClause(EvalClauseContext ctx) {
    return new Let((Field) visit(ctx.fieldExpression()), visit(ctx.expression()));
  }

  /**
   * Logical expression excluding boolean, comparison.
   */
  @Override
  public UnresolvedExpression visitLogicalNot(LogicalNotContext ctx) {
    return new Not(visit(ctx.logicalExpression()));
  }

  @Override
  public UnresolvedExpression visitLogicalOr(LogicalOrContext ctx) {
    return new Or(visit(ctx.left), visit(ctx.right));
  }

  @Override
  public UnresolvedExpression visitLogicalAnd(LogicalAndContext ctx) {
    return new And(visit(ctx.left), visit(ctx.right));
  }

  @Override
  public UnresolvedExpression visitLogicalXor(LogicalXorContext ctx) {
    return new Xor(visit(ctx.left), visit(ctx.right));
  }

  /**
   * Comparison expression.
   */
  @Override
  public UnresolvedExpression visitCompareExpr(CompareExprContext ctx) {
    return new Compare(ctx.comparisonOperator().getText(), visit(ctx.left), visit(ctx.right));
  }

  @Override
  public UnresolvedExpression visitInExpr(InExprContext ctx) {
    return new In(
        visit(ctx.valueExpression()),
        ctx.valueList()
            .literalValue()
            .stream()
            .map(this::visitLiteralValue)
            .collect(Collectors.toList()));
  }

  /**
   * Value Expression.
   */
  @Override
  public UnresolvedExpression visitBinaryArithmetic(BinaryArithmeticContext ctx) {
    return new Function(
        ctx.binaryOperator().getText(),
        Arrays.asList(visit(ctx.left), visit(ctx.right))
    );
  }

  @Override
  public UnresolvedExpression visitParentheticBinaryArithmetic(
      ParentheticBinaryArithmeticContext ctx) {
    return new Function(
        ctx.binaryOperator().getText(),
        Arrays.asList(visit(ctx.left), visit(ctx.right))
    );
  }

  /**
   * Field expression.
   */
  @Override
  public UnresolvedExpression visitFieldExpression(FieldExpressionContext ctx) {
    return new Field((QualifiedName) visit(ctx.qualifiedName()));
  }

  @Override
  public UnresolvedExpression visitWcFieldExpression(WcFieldExpressionContext ctx) {
    return new Field((QualifiedName) visit(ctx.wcQualifiedName()));
  }

  @Override
  public UnresolvedExpression visitSortField(SortFieldContext ctx) {
    return new Field(
        ctx.sortFieldExpression().fieldExpression().getText(),
        ArgumentFactory.getArgumentList(ctx)
    );
  }

  /**
   * Aggregation function.
   */
  @Override
  public UnresolvedExpression visitStatsFunctionCall(StatsFunctionCallContext ctx) {
    return new AggregateFunction(ctx.statsFunctionName().getText(), visit(ctx.valueExpression()));
  }

  @Override
  public UnresolvedExpression visitCountAllFunctionCall(
      OpenDistroPPLParser.CountAllFunctionCallContext ctx) {
    return new AggregateFunction("count", AllFields.of());
  }

  @Override
  public UnresolvedExpression visitPercentileAggFunction(PercentileAggFunctionContext ctx) {
    return new AggregateFunction(ctx.PERCENTILE().getText(), visit(ctx.aggField),
        Collections.singletonList(new Argument("rank", (Literal) visit(ctx.value))));
  }

  /**
   * Eval function.
   */
  @Override
  public UnresolvedExpression visitEvalFunctionCall(EvalFunctionCallContext ctx) {
    return new Function(
        ctx.evalFunctionName().getText(),
        ctx.functionArgs()
            .functionArg()
            .stream()
            .map(this::visitFunctionArg)
            .collect(Collectors.toList()));
  }

  /**
   * Literal and value.
   */
  @Override
  public UnresolvedExpression visitIdentsAsQualifiedName(IdentsAsQualifiedNameContext ctx) {
    return new QualifiedName(
        ctx.ident()
            .stream()
            .map(ParserRuleContext::getText)
            .map(StringUtils::unquoteText)
            .collect(Collectors.toList())
    );
  }

  @Override
  public UnresolvedExpression visitKeywordsAsQualifiedName(KeywordsAsQualifiedNameContext ctx) {
    return new QualifiedName(ctx.keywordsCanBeId().getText());
  }

  @Override
  public UnresolvedExpression visitIdentsAsWildcardQualifiedName(
      IdentsAsWildcardQualifiedNameContext ctx) {
    return new QualifiedName(
        ctx.wildcard()
            .stream()
            .map(ParserRuleContext::getText)
            .map(StringUtils::unquoteText)
            .collect(Collectors.toList())
    );
  }

  @Override
  public UnresolvedExpression visitKeywordsAsWildcardQualifiedName(
      KeywordsAsWildcardQualifiedNameContext ctx) {
    return new QualifiedName(ctx.keywordsCanBeId().getText());
  }

  @Override
  public UnresolvedExpression visitIntervalLiteral(IntervalLiteralContext ctx) {
    return new Interval(
        visit(ctx.valueExpression()), IntervalUnit.of(ctx.intervalUnit().getText()));
  }

  @Override
  public UnresolvedExpression visitStringLiteral(StringLiteralContext ctx) {
    return new Literal(StringUtils.unquoteText(ctx.getText()), DataType.STRING);
  }

  @Override
  public UnresolvedExpression visitIntegerLiteral(IntegerLiteralContext ctx) {
    return new Literal(Integer.valueOf(ctx.getText()), DataType.INTEGER);
  }

  @Override
  public UnresolvedExpression visitDecimalLiteral(DecimalLiteralContext ctx) {
    return new Literal(Double.valueOf(ctx.getText()), DataType.DOUBLE);
  }

  @Override
  public UnresolvedExpression visitBooleanLiteral(BooleanLiteralContext ctx) {
    return new Literal(Boolean.valueOf(ctx.getText()), DataType.BOOLEAN);
  }

}
