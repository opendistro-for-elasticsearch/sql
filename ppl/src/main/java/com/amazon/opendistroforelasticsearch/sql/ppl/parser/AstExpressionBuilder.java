/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Compare;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AggregateFunction;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.And;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.In;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Or;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;

import static com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils.unquoteIdentifier;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.BooleanLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.CompareExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.DecimalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.FieldExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.InExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.IntegerLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalAndContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalNotContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalOrContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.PercentileAggFunctionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.QualifiedNameContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.SortFieldContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.StatsFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.StringLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.WcFieldExpressionContext;

/**
 * Class of building AST Expression nodes
 */
public class AstExpressionBuilder extends OpenDistroPPLParserBaseVisitor<Expression> {
    /** Logical expression excluding boolean, eval, comparison */
    @Override
    public Expression visitLogicalNot(LogicalNotContext ctx) {
        return new Not(visit(ctx.logicalExpression()));
    }

    @Override
    public Expression visitLogicalOr(LogicalOrContext ctx) {
        return new Or(visit(ctx.left), visit(ctx.right));
    }

    @Override
    public Expression visitLogicalAnd(LogicalAndContext ctx) {
        return new And(visit(ctx.left), visit(ctx.right));
    }


    /** Eval expression */
    @Override
    public Expression visitEvalExpression(EvalExpressionContext ctx) {
        Expression field = visit(ctx.fieldExpression());
        Expression evalFunctionCall = visit(ctx.evalFunctionCall());
        return new EqualTo(field, evalFunctionCall);
    }

    /** Comparison expression */
    @Override
    public Expression visitCompareExpr(CompareExprContext ctx) {
        Expression right = ctx.field != null ? visit(ctx.field) : visit(ctx.literal);
        return new Compare(ctx.comparisonOperator().getText(), visit(ctx.left), right);
    }

    @Override
    public Expression visitInExpr(InExprContext ctx) {
        return new In(
                visit(ctx.fieldExpression()),
                ctx.valueList()
                        .literalValue()
                        .stream()
                        .map(this::visitLiteralValue)
                        .collect(Collectors.toList()));
    }

    /** Field expression */
    @Override
    public Expression visitFieldExpression(FieldExpressionContext ctx) {
        return new Field((QualifiedName) visit(ctx.qualifiedName()));
    }

    @Override
    public Expression visitWcFieldExpression(WcFieldExpressionContext ctx) {
        return new Field((QualifiedName) visit(ctx.wcQualifiedName()));
    }

    @Override
    public Expression visitSortField(SortFieldContext ctx) {
        return new Field(
                ctx.sortFieldExpression().fieldExpression().getText(),
                Arrays.asList(
                        ctx.MINUS() != null
                                ? new Argument("exclude", new Literal(true, DataType.BOOLEAN))
                                : new Argument("exclude", new Literal(false, DataType.BOOLEAN)),
                        ctx.sortFieldExpression().AUTO() != null
                                ? new Argument("type", new Literal("auto", DataType.STRING))
                                : ctx.sortFieldExpression().IP() != null
                                ? new Argument("type", new Literal("ip", DataType.STRING))
                                : ctx.sortFieldExpression().NUM() != null
                                ? new Argument("type", new Literal("num", DataType.STRING))
                                : ctx.sortFieldExpression().STR() != null
                                ? new Argument("type", new Literal("str", DataType.STRING))
                                : new Argument("type", new Literal(null, DataType.NULL))
                )
        );
    }

    /** Aggregation function */
    @Override
    public Expression visitStatsFunctionCall(StatsFunctionCallContext ctx) {
        return new AggregateFunction(ctx.statsFunctionName().getText(), visit(ctx.fieldExpression()));
    }

    @Override
    public Expression visitPercentileAggFunction(PercentileAggFunctionContext ctx) {
        return new AggregateFunction(ctx.PERCENTILE().getText(), visit(ctx.aggField),
                Collections.singletonList(new Argument("rank", visit(ctx.value))));
    }

    /** Eval function */
    @Override
    public Expression visitEvalFunctionCall(EvalFunctionCallContext ctx) {
        return new Function(
                ctx.evalFunctionName().getText(),
                ctx.functionArgs()
                        .functionArg()
                        .stream()
                        .map(this::visitFunctionArg)
                        .collect(Collectors.toList()));
    }

    /** Literal and value */
    @Override
    public Expression visitQualifiedName(QualifiedNameContext ctx) {
        return new QualifiedName(
                ctx.ident()
                        .stream()
                        .map(ParserRuleContext::getText)
                        .map(StringUtils::unquoteIdentifier)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Expression visitWcQualifiedName(OpenDistroPPLParser.WcQualifiedNameContext ctx) {
        return new QualifiedName(
                ctx.wildcard()
                        .stream()
                        .map(ParserRuleContext::getText)
                        .map(StringUtils::unquoteIdentifier)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Expression visitStringLiteral(StringLiteralContext ctx) {
        return new Literal(unquoteIdentifier(ctx.getText()), DataType.STRING);
    }

    @Override
    public Expression visitIntegerLiteral(IntegerLiteralContext ctx) {
        return new Literal(Integer.valueOf(ctx.getText()), DataType.INTEGER);
    }

    @Override
    public Expression visitDecimalLiteral(DecimalLiteralContext ctx) {
        return new Literal(Double.valueOf(ctx.getText()), DataType.DOUBLE);
    }

    @Override
    public Expression visitBooleanLiteral(BooleanLiteralContext ctx) {
        return new Literal(Boolean.valueOf(ctx.getText()), DataType.BOOLEAN);
    }

}
