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

package com.amazon.opendistroforelasticsearch.sql.antlr.visitor;

import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParserBaseVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.AggregateFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.AtomTableItemContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.BinaryComparasionPredicateContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.ComparisonOperatorContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.DecimalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.FromClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.FullColumnNameContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.FunctionNameBaseContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.ScalarFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.SelectElementsContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.SimpleTableNameContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.StringLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.TableAndTypeNameContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.TableNameContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.UidContext;

/**
 * ANTLR parse tree visitor to drive the semantic analysis process.
 */
public class AntlrParseTreeVisitor<T extends Aggregator> extends OpenDistroSqlParserBaseVisitor<T> {

    private final ParseTreeVisitor<T> visitor;

    public AntlrParseTreeVisitor(ParseTreeVisitor<T> visitor) {
        this.visitor = visitor;
    }

    @Override
    public T visitQuerySpecification(QuerySpecificationContext ctx) {
        visitor.visitQuery();

        // Enforce visit order because ANTLR is generic and unaware.
        // Always visit FROM clause first to define symbols
        visitFromClause(ctx.fromClause());
        visitSelectElements(ctx.selectElements());

        if (ctx.orderByClause() != null) {
            visitOrderByClause(ctx.orderByClause());
        }
        if (ctx.limitClause() != null) {
            visitLimitClause(ctx.limitClause());
        }

        return visitor.endVisitQuery();
    }

    @Override
    public T visitFromClause(FromClauseContext ctx) {
        visitor.visitFrom();
        //visitor.visitWhere();
        super.visitFromClause(ctx);
        return visitor.endVisitFrom();
    }

    @Override
    public T visitAtomTableItem(AtomTableItemContext ctx) {
        Optional<String> alias = Optional.ofNullable(ctx.alias == null ? null : getTextFromUid(ctx.alias));
        TableNameContext tableName = ctx.tableName();
        if (tableName instanceof SimpleTableNameContext) {
            visitor.visitIndexName(
                getTextFromUid(((SimpleTableNameContext) tableName).fullId().uid(0)), alias
            );
        } else if (tableName instanceof TableAndTypeNameContext) {
            visitor.visitIndexName(
                getTextFromUid(((TableAndTypeNameContext) tableName).uid(0)), alias
            );
        } // else TODO: skip all analysis if TableNamePattern
        return defaultResult();
    }

    @Override
    public T visitFullColumnName(FullColumnNameContext ctx) {
        return visitor.visitFieldName(getTextFromUid(ctx.uid()));
    }

    // This check should be able to accomplish in grammar
    @Override
    public T visitScalarFunctionCall(ScalarFunctionCallContext ctx) {
        T func = visit(ctx.scalarFunctionName());
        List<T> actualArgs = new ArrayList<>();
        for (int i = 1; i < ctx.getChildCount(); i++) {
            T arg = visit(ctx.getChild(i));
            if (arg != null) {
                actualArgs.add(arg);
            }
        }

        //Type result = visitor.visitFunctionCall(funcType, actualArgTypes.toArray(new Type[0]));
        /*if (result == TYPE_ERROR) {
            throw semanticException(
                "[%s] can not work with %s.",
                funcType.getName(), actualArgTypes
            ).at(sql, ctx).suggestion("Usage: %s.", funcType).build();
        }*/
        return func.aggregate(actualArgs);
    }

    @Override
    public T visitSelectElements(SelectElementsContext ctx) {
        //return visitor.visitSelectClause(() -> {
            super.visitSelectElements(ctx);
        //});
        return defaultResult();
    }

    @Override
    public T visitAggregateFunctionCall(AggregateFunctionCallContext ctx) {
        //Type type = analyzer.resolve(new Symbol(FUNCTION_NAME, ctx.getStart().getText()));
        return super.visitAggregateFunctionCall(ctx);
    }

    @Override
    public T visitFunctionNameBase(FunctionNameBaseContext ctx) {
        return visitor.visitFunctionName(ctx.getText());
    }

    @Override
    public T visitComparisonOperator(ComparisonOperatorContext ctx) {
        //return visitor.visitOperatorName(ctx.getText());
        return defaultResult();
    }

    // Better semantic check example for overloading operator '='
    @Override
    public T visitBinaryComparasionPredicate(BinaryComparasionPredicateContext ctx) {
        //T opType = visit(ctx.comparisonOperator());
        //List<T> actualArgTypes = Arrays.asList(visit(ctx.predicate(0)), visit(ctx.predicate(1)));
        //return opType.aggregate(actualArgTypes);
        return super.visitBinaryComparasionPredicate(ctx);
    }

    @Override
    public T visitStringLiteral(StringLiteralContext ctx) {
        return visitor.visitString(ctx.getText());
    }

    @Override
    public T visitDecimalLiteral(DecimalLiteralContext ctx) {
        return visitor.visitNumber(ctx.getText());
    }

    @Override
    protected T defaultResult() {
        return null;
    }

    @Override
    protected T aggregateResult(T aggregate, T nextResult) {
        if (nextResult != null) { // should call Attribute method for synthesis
            return nextResult;
        }
        return aggregate;
    }

    private String getTextFromUid(UidContext uid) {
        return uid.simpleId().ID().getText(); // NPE possible when ID() = null
    }

}
