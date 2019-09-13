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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic;

import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;

import java.util.ArrayList;
import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.AggregateFunctionCallContext;
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
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.UidContext;

/**
 * ANTLR parse tree visitor to drive the semantic analysis process.
 */
public class AntlrParseTreeVisitor extends OpenDistroSqlParserBaseVisitor<Type> {

    /** Original sql query for troubleshooting information */
    private final String sql;

    private final SemanticAnalyzer analyzer = new SemanticAnalyzer();

    public AntlrParseTreeVisitor(String sql) {
        this.sql = sql;
    }

    @Override
    public Type visitQuerySpecification(QuerySpecificationContext ctx) {
        return analyzer.visitQuery(() -> {

            // Always visit FROM clause first to define symbols
            visit(ctx.fromClause());

            for (int i = 0; i < ctx.getChildCount(); i++) {
                if (ctx.getChild(i) != ctx.fromClause()) {
                    visit(ctx.getChild(i));
                }
            }
        });
    }

    @Override
    public Type visitFromClause(FromClauseContext ctx) {
        return analyzer.visitWhereClause(() -> {
            super.visitFromClause(ctx);
        });
    }

    @Override
    public Type visitSimpleTableName(SimpleTableNameContext ctx) {
        String indexName = getTextFrom(ctx.fullId().uid(0));
        return analyzer.visitIndexName(LocalClusterState.state(), indexName);
    }

    @Override
    public Type visitTableAndTypeName(TableAndTypeNameContext ctx) {
        String indexName = getTextFrom(ctx.uid(0));
        return analyzer.visitIndexName(LocalClusterState.state(), indexName);
    }

    @Override
    public Type visitFullColumnName(FullColumnNameContext ctx) {
        return analyzer.visitFieldName(getTextFrom(ctx.uid()));
    }

    // This check should be able to accomplish in grammar
    @Override
    public Type visitScalarFunctionCall(ScalarFunctionCallContext ctx) {
        Type funcType = visit(ctx.scalarFunctionName());
        List<Type> actualArgTypes = new ArrayList<>();
        for (int i = 1; i < ctx.getChildCount(); i++) {
            Type type = visit(ctx.getChild(i));
            if (type != null) {
                actualArgTypes.add(type);
            }
        }

        Type result = analyzer.visitFunctionCall(funcType, actualArgTypes.toArray(new Type[0]));
        /*if (result == TYPE_ERROR) {
            throw semanticException(
                "[%s] can not work with %s.",
                funcType.getName(), actualArgTypes
            ).at(sql, ctx).suggestion("Usage: %s.", funcType).build();
        }*/
        return result;
    }

    @Override
    public Type visitSelectElements(SelectElementsContext ctx) {
        return analyzer.visitSelectClause(() -> {
            super.visitSelectElements(ctx);
        });
    }

    @Override
    public Type visitAggregateFunctionCall(AggregateFunctionCallContext ctx) {
        //Type type = analyzer.resolve(new Symbol(FUNCTION_NAME, ctx.getStart().getText()));
        return super.visitAggregateFunctionCall(ctx);
    }

    @Override
    public Type visitFunctionNameBase(FunctionNameBaseContext ctx) {
        return analyzer.visitFunctionName(ctx.getText());
    }

    @Override
    public Type visitComparisonOperator(ComparisonOperatorContext ctx) {
        return analyzer.visitOperatorName(ctx.getText());
    }

    // Better semantic check example for overloading operator '='
    @Override
    public Type visitBinaryComparasionPredicate(BinaryComparasionPredicateContext ctx) {
        Type opType = visit(ctx.comparisonOperator());
        Type[] actualArgTypes = { visit(ctx.predicate(0)), visit(ctx.predicate(1)) };
        return analyzer.visitFunctionCall(opType, actualArgTypes);
    }

    @Override
    public Type visitStringLiteral(StringLiteralContext ctx) {
        return analyzer.visitString(ctx.getText());
    }

    @Override
    public Type visitDecimalLiteral(DecimalLiteralContext ctx) {
        return analyzer.visitNumber(ctx.getText());
    }

    @Override
    protected Type defaultResult() {
        return null;
    }

    @Override
    protected Type aggregateResult(Type aggregate, Type nextResult) {
        if (nextResult != null) { // should call Attribute method for synthesis
            return nextResult;
        }
        return aggregate;
    }

    private String getTextFrom(UidContext uid) {
        return uid.simpleId().ID().getText(); // NPE possible when ID() = null
    }

}
