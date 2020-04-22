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

import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.AggregateFunction;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.And;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Compare;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.In;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Or;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.UnresolvedAttribute;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.AggFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.AggFunctionNameContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.BooleanExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.BooleanLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.CompareExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.DecimalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalFunctionNameContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.FieldExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.InExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.IntegerLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalAndContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalNotContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalOrContext;
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

    /** Boolean expression */
    @Override
    public Expression visitBooleanExpression(BooleanExpressionContext ctx) {
        return new Literal(ctx.booleanLiteral().getText(), DataType.BOOLEAN);
    }


    /** Field expression */
    @Override
    public Expression visitFieldExpression(FieldExpressionContext ctx) {
        return new UnresolvedAttribute(ctx.getText());
    }

    @Override
    public Expression visitWcFieldExpression(WcFieldExpressionContext ctx) {
        return new UnresolvedAttribute(ctx.getText());
    }

    /** Aggregation term */

    /** Aggregation function */
    @Override
    public Expression visitAggFunctionCall(AggFunctionCallContext ctx) {
        return new AggregateFunction(ctx.aggFunctionName().getText(), visit(ctx.fieldExpression()));
    }

    @Override
    public Expression visitAggFunctionName(AggFunctionNameContext ctx) {
        return new UnresolvedAttribute(ctx.getText());
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

    @Override
    public Expression visitEvalFunctionName(EvalFunctionNameContext ctx) {
        return new UnresolvedAttribute(ctx.getText());
    }

    /** Literal and value */
    @Override
    public Expression visitStringLiteral(StringLiteralContext ctx) {
        String token = ctx.getText();
        String identifier = token.substring(1, token.length() - 1)
                .replace("\"\"", "\"");
        return new Literal(identifier, DataType.STRING);
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
