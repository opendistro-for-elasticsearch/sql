/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.ppl.parser;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.DataType;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Filter;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Node;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Relation;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.PPLParser;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.PPLParserBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

public class AstBuilder extends PPLParserBaseVisitor<Node> {

    /**
     * Simply return non-default value for now
     */
    @Override
    protected Node aggregateResult(Node aggregate, Node nextResult) {
        if (nextResult != defaultResult()) {
            return nextResult;
        }
        return aggregate;
    }

    @Override
    public Node visitSearchCommand(PPLParser.SearchCommandContext ctx) {
        return new Filter(
                visitExpression(ctx.logicalExpression()),
                visitLogicalPlan(ctx.fromClause())
        );
    }

    @Override
    public Node visitFromClause(PPLParser.FromClauseContext ctx) {
        return new Relation(ctx.tableSource().getText());
    }

    @Override
    public Expression visitComparisonExpression(PPLParser.ComparisonExpressionContext ctx) {
        Expression field = visitExpression(ctx.fieldExpression());
        Expression value = visitExpression(ctx.valueExpression());
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
        return new Literal(ctx.getText(), DataType.STRING);
    }

    @Override
    public Expression visitDecimalLiteral(PPLParser.DecimalLiteralContext ctx) {
        return new Literal(Integer.valueOf(ctx.getText()), DataType.INTEGER);
    }

    /* ------------------------------- */
    private Expression visitExpression(ParseTree tree) {
        return (Expression) visit(tree);
    }

    private LogicalPlan visitLogicalPlan(ParseTree tree) {
        return (LogicalPlan) (visit(tree));
    }
}
