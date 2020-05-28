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

import com.amazon.opendistroforelasticsearch.sql.ast.expression.Compare;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.MathExpressionAtomContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.UidContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils.unquoteIdentifier;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.BinaryComparisonPredicateContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.BooleanLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.DecimalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.ScalarFunctionContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.StringLiteralContext;

/**
 * Expression builder
 */
public class AstExpressionBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedExpression> {

    @Override
    public UnresolvedExpression visitMathExpressionAtom(MathExpressionAtomContext ctx) {
        return new Function(
            ctx.mathOperator().getText(),
            Arrays.asList(
                visit(ctx.left),
                visit(ctx.right)
            )
        );
    }

    @Override
    public UnresolvedExpression visitBinaryComparisonPredicate(BinaryComparisonPredicateContext ctx) {
        return new Compare(
            ctx.comparisonOperator().getText(),
            visit(ctx.left),
            visit(ctx.right)
        );
    }

    @Override
    public UnresolvedExpression visitScalarFunction(ScalarFunctionContext ctx) {
        return new Function(
            ctx.functionNameBase().getText(),
            ctx.functionArgs().children.stream().
                                        map(this::visit).
                                        filter(argExpr -> argExpr != defaultResult()). // remove parentheses
                                        collect(Collectors.toList())
        );
    }

    @Override
    public UnresolvedExpression visitUid(UidContext ctx) {
        return new QualifiedName(StringUtils.unquoteIdentifier(ctx.getText()));
    }

    @Override
    public UnresolvedExpression visitStringLiteral(StringLiteralContext ctx) {
        return new Literal(unquoteIdentifier(ctx.getText()), DataType.STRING);
    }

    @Override
    public UnresolvedExpression visitDecimalLiteral(DecimalLiteralContext ctx) {
        return new Literal(Integer.valueOf(ctx.getText()), DataType.INTEGER);
    }

    @Override
    public UnresolvedExpression visitBooleanLiteral(BooleanLiteralContext ctx) {
        return new Literal(Boolean.valueOf(ctx.getText()), DataType.BOOLEAN);
    }

    @Override
    protected UnresolvedExpression aggregateResult(UnresolvedExpression aggregate, UnresolvedExpression nextResult) {
        return nextResult != null ? nextResult : aggregate;
    }

}
