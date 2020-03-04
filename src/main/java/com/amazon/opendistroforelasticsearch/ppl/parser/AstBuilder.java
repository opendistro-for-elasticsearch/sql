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

import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Node;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.PPLParser;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.PPLParserBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

public class AstBuilder extends PPLParserBaseVisitor<Node> {

    @Override
    public Node visitSearchCommand(PPLParser.SearchCommandContext ctx) {

        return new Project(
                null,
                visitLogicalPlan(ctx.fromClause())
        );
    }

//    @Override
//    public Node visitRoot(OpenDistroSqlParser.RootContext ctx) {
//        Node node = super.visitRoot(ctx);
//        return node;
//    }
//
//    @Override
//    protected Node aggregateResult(Node aggregate, Node nextResult) {
//        if (nextResult != defaultResult()) { // Simply return non-default value for now
//            return nextResult;
//        }
//        return aggregate;
//    }
//
//    @Override
//    public Node visitQuerySpecification(OpenDistroSqlParser.QuerySpecificationContext ctx) {
//        return new Project(
//                ctx.selectElements().selectElement().stream().map(this::visitExpression).collect(Collectors.toList()),
//                visitLogicalPlan(ctx.fromClause()));
//    }
//
//    @Override
//    public Node visitFromClause(OpenDistroSqlParser.FromClauseContext ctx) {
//        LogicalPlan relation = visitLogicalPlan(ctx.tableSources());
//        if (ctx.whereExpr != null) {
//            return new Filter(visitExpression(ctx.whereExpr), relation);
//        }
//        return relation;
//    }
//
//    @Override
//    public LogicalPlan visitAtomTableItem(OpenDistroSqlParser.AtomTableItemContext ctx) {
//        return new Relation(ctx.getText());
//    }
//
//    @Override
//    public Expression visitSelectColumnElement(OpenDistroSqlParser.SelectColumnElementContext ctx) {
//        Expression expr = visitExpression(ctx.fullColumnName());
//        if (ctx.uid() != null) {
//            return new Alias(expr, ctx.uid().getText());
//        }
//        return expr;
//    }
//
//    @Override
//    public Node visitFullColumnName(OpenDistroSqlParser.FullColumnNameContext ctx) {
//        return new UnresolvedAttribute(ctx.getText());
//    }
//
//    @Override
//    public Node visitBinaryComparisonPredicate(OpenDistroSqlParser.BinaryComparisonPredicateContext ctx) {
//        Expression left = visitExpression(ctx.left);
//        Expression right = visitExpression(ctx.right);
//        String operator = ctx.comparisonOperator().getText();
//        switch (operator) {
//            case "==":
//            case "=":
//                return new EqualTo(left, right);
//            default:
//                throw new UnsupportedOperationException(String.format("unsupported operator [%s]", operator));
//        }
//    }
//
//    @Override
//    public Expression visitStringLiteral(OpenDistroSqlParser.StringLiteralContext ctx) {
//        return new Literal(ctx.getText(), DataType.STRING);
//    }
//
//    @Override
//    public Expression visitDecimalLiteral(OpenDistroSqlParser.DecimalLiteralContext ctx) {
//        return new Literal(Integer.valueOf(ctx.getText()), DataType.INTEGER);
//    }
//
//    @Override
//    public Expression visitFullColumnNameExpressionAtom(OpenDistroSqlParser.FullColumnNameExpressionAtomContext ctx) {
//        return new UnresolvedAttribute(ctx.getText());
//    }

    /* ------------------------------- */
    private Expression visitExpression(ParseTree tree) {
        return (Expression) visit(tree);
    }

    private LogicalPlan visitLogicalPlan(ParseTree tree) {
        return (LogicalPlan) (visit(tree));
    }
}
