package com.amazon.opendistroforelasticsearch.ppl.parser;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AggCount;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.And;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeList;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.DataType;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Or;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.PPLParser;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.PPLParserBaseVisitor;
import java.util.stream.Collectors;

public class AstExpressionBuilder extends PPLParserBaseVisitor<Expression> {

    @Override
    public Expression visitLogicalAndBinary(PPLParser.LogicalAndBinaryContext ctx) {
        return new And(visit(ctx.left), visit(ctx.right));
    }

    @Override
    public Expression visitLogicalOrBinary(PPLParser.LogicalOrBinaryContext ctx) {
        return new Or(visit(ctx.left), visit(ctx.right));
    }

    @Override
    public Expression visitComparisonExpression(PPLParser.ComparisonExpressionContext ctx) {
        Expression field = visit(ctx.fieldExpression());
        Expression value = visit(ctx.valueExpression());
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
    public Expression visitFieldExpression(PPLParser.FieldExpressionContext ctx) {
        return new UnresolvedAttribute(ctx.getText());
    }

    @Override
    public Expression visitStringLiteral(PPLParser.StringLiteralContext ctx) {
        String token = ctx.getText();
        String identifier = token.substring(1, token.length() - 1)
                .replace("\"\"", "\"");
        return new Literal(identifier, DataType.STRING);
    }

    @Override
    public Expression visitDecimalLiteral(PPLParser.DecimalLiteralContext ctx) {
        return new Literal(Integer.valueOf(ctx.getText()), DataType.INTEGER);
    }

    @Override
    public Expression visitFieldList(PPLParser.FieldListContext ctx) {
        return new AttributeList(ctx.fieldExpression()
                .stream()
                .map(this::visitFieldExpression)
                .collect(Collectors.toList()));
    }

    @Override
    public Expression visitStatsAggTerm(PPLParser.StatsAggTermContext ctx) {
        String func = ctx.statsFunc().getText();

        switch (func.toLowerCase()) {
            case "count":
                return new AggCount(visit(ctx.fieldExpression()));
            default:
                throw new IllegalArgumentException("unsupported function: " + func.toLowerCase());
        }
    }
}
