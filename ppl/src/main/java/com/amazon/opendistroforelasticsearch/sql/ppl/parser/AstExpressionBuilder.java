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
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Array;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.AttributeList;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.In;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Nest;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Or;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.UnresolvedAttribute;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.AggFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.AggFunctionNameContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.BooleanExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.CompareExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.ConstantContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.DecimalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalFunctionNameContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.FieldListContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.InExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.IntegerLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalAndContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalNotContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.LogicalOrContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.NestedFieldArrayExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.NestedFieldExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.NestedFieldLastLayerArrayExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.NestedFieldLastLayerExprContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.StringLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.WcFieldExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.WcFieldListContext;

/**
 * Class of constructed AST Expression nodes visitor
 *
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
        Expression field = visit(ctx.left);
        Expression value = visit(ctx.right);
        String operator = ctx.comparisonOperator().getText();
        switch (operator) {
            case "==":
            case "=":
                return new EqualTo(field, value);
            default:
                throw new UnsupportedOperationException(String.format("unsupported operator [%s]", operator));
        }
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
    public Expression visitNestedFieldLastLayerExpr(NestedFieldLastLayerExprContext ctx) {
        return new UnresolvedAttribute(ctx.ident().getText());
    }

    @Override
    public Expression visitNestedFieldExpr(NestedFieldExprContext ctx) {
        return new Nest(
                new UnresolvedAttribute(ctx.ident().getText()),
                visit(ctx.nestedFieldExpression())
        );
    }

    @Override
    public Expression visitNestedFieldLastLayerArrayExpr(NestedFieldLastLayerArrayExprContext ctx) {
        return new Array(
                new UnresolvedAttribute(ctx.ident().getText()),
                visit(ctx.integerLiteral())
        );
    }

    @Override
    public Expression visitNestedFieldArrayExpr(NestedFieldArrayExprContext ctx) {
        return new Nest(
                new Array(
                        new UnresolvedAttribute(ctx.ident().getText()),
                        visit(ctx.integerLiteral())
                ),
                visit(ctx.nestedFieldExpression())
        );
    }

    @Override
    public Expression visitFieldList(FieldListContext ctx) {
        return new AttributeList(
                ctx.fieldExpression()
                        .stream()
                        .map(this::visitFieldExpression)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Expression visitWcFieldExpression(WcFieldExpressionContext ctx) {
        return new UnresolvedAttribute(ctx.getText());
    }

    @Override
    public Expression visitWcFieldList(WcFieldListContext ctx) {
        return new AttributeList(
                ctx.wcFieldExpression()
                        .stream()
                        .map(this::visitWcFieldExpression)
                        .collect(Collectors.toList())
        );
    }

    /** Aggregation term */

    /** Aggregation function */
    @Override
    public Expression visitAggFunctionCall(AggFunctionCallContext ctx) {
        return new AggregateFunction(visit(ctx.aggFunctionName()), visit(ctx.fieldExpression()));
    }

    @Override
    public Expression visitAggFunctionName(AggFunctionNameContext ctx) {
        return new UnresolvedAttribute(ctx.getText());
    }

    /** Eval function */
    @Override
    public Expression visitEvalFunctionCall(EvalFunctionCallContext ctx) {
        return new Function(
                visit(ctx.evalFunctionName()),
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

    public Expression visitIntegerLiteral(IntegerLiteralContext ctx) {
        return new Literal(Integer.valueOf(ctx.getText()), DataType.INTEGER);
    }

    @Override
    public Expression visitDecimalLiteral(DecimalLiteralContext ctx) {
        return new Literal(Double.valueOf(ctx.getText()), DataType.DOUBLE);
    }

    @Override
    public Expression visitConstant(ConstantContext ctx) {
        return new UnresolvedAttribute(ctx.getText());
    }

}
