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

package com.amazon.opendistroforelasticsearch.sql.legacy.query;

import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Delete;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.IndexStatement;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.JoinSelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.QueryActionRequest;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.ElasticResultHandler;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.Format;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.QueryActionElasticExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.adapter.QueryPlanQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.adapter.QueryPlanRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.ElasticLexer;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SubQueryExpression;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.ESJoinQueryActionFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.multi.MultiQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.multi.MultiQuerySelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.BindingTupleQueryPlanner;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.RewriteRuleExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.alias.TableAliasPrefixRemoveRule;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.identifier.UnquoteIdentifierRule;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.join.JoinRewriteRule;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.matchtoterm.TermFieldRewriter;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.matchtoterm.TermFieldRewriter.TermRewriterFilter;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.nestedfield.NestedFieldRewriter;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.ordinal.OrdinalRewriterRule;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.parent.SQLExprParentSetterRule;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.SubQueryRewriteRule;
import com.google.common.annotations.VisibleForTesting;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.legacy.domain.IndexStatement.StatementType;
import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util.toSqlExpr;

public class ESActionFactory {

    public static QueryAction create(Client client, String sql)
            throws SqlParseException, SQLFeatureNotSupportedException {
        return create(client, new QueryActionRequest(sql, new ColumnTypeProvider(), Format.JSON));
    }

    /**
     * Create the compatible Query object
     * based on the SQL query.
     *
     * @param request The SQL query.
     * @return Query object.
     */
    public static QueryAction create(Client client, QueryActionRequest request)
            throws SqlParseException, SQLFeatureNotSupportedException {
        String sql = request.getSql();
        // Remove line breaker anywhere and semicolon at the end
        sql = sql.replaceAll("\\R", " ").trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        switch (getFirstWord(sql)) {
            case "SELECT":
                SQLQueryExpr sqlExpr = (SQLQueryExpr) toSqlExpr(sql);

                RewriteRuleExecutor<SQLQueryExpr> ruleExecutor = RewriteRuleExecutor.builder()
                        .withRule(new SQLExprParentSetterRule())
                        .withRule(new OrdinalRewriterRule(sql))
                        .withRule(new UnquoteIdentifierRule())
                        .withRule(new TableAliasPrefixRemoveRule())
                        .withRule(new SubQueryRewriteRule())
                        .build();
                ruleExecutor.executeOn(sqlExpr);
                sqlExpr.accept(new NestedFieldRewriter());

                if (isMulti(sqlExpr)) {
                    sqlExpr.accept(new TermFieldRewriter(TermRewriterFilter.MULTI_QUERY));
                    MultiQuerySelect multiSelect =
                            new SqlParser().parseMultiSelect((SQLUnionQuery) sqlExpr.getSubQuery().getQuery());
                    return new MultiQueryAction(client, multiSelect);
                } else if (isJoin(sqlExpr, sql)) {
                    new JoinRewriteRule(LocalClusterState.state()).rewrite(sqlExpr);
                    sqlExpr.accept(new TermFieldRewriter(TermRewriterFilter.JOIN));
                    JoinSelect joinSelect = new SqlParser().parseJoinSelect(sqlExpr);
                    return ESJoinQueryActionFactory.createJoinAction(client, joinSelect);
                } else {
                    sqlExpr.accept(new TermFieldRewriter());
                    // migrate aggregation to query planner framework.
                    if (shouldMigrateToQueryPlan(sqlExpr, request.getFormat())) {
                        return new QueryPlanQueryAction(new QueryPlanRequestBuilder(
                                new BindingTupleQueryPlanner(client, sqlExpr, request.getTypeProvider())));
                    }
                    Select select = new SqlParser().parseSelect(sqlExpr);
                    return handleSelect(client, select);
                }
            case "DELETE":
                SQLStatementParser parser = createSqlStatementParser(sql);
                SQLDeleteStatement deleteStatement = parser.parseDeleteStatement();
                Delete delete = new SqlParser().parseDelete(deleteStatement);
                return new DeleteQueryAction(client, delete);
            case "SHOW":
                IndexStatement showStatement = new IndexStatement(StatementType.SHOW, sql);
                return new ShowQueryAction(client, showStatement);
            case "DESCRIBE":
                IndexStatement describeStatement = new IndexStatement(StatementType.DESCRIBE, sql);
                return new DescribeQueryAction(client, describeStatement);
            default:
                throw new SQLFeatureNotSupportedException(
                        String.format("Query must start with SELECT, DELETE, SHOW or DESCRIBE: %s", sql));
        }
    }

    private static String getFirstWord(String sql) {
        int endOfFirstWord = sql.indexOf(' ');
        return sql.substring(0, endOfFirstWord > 0 ? endOfFirstWord : sql.length()).toUpperCase();
    }

    private static boolean isMulti(SQLQueryExpr sqlExpr) {
        return sqlExpr.getSubQuery().getQuery() instanceof SQLUnionQuery;
    }

    private static void executeAndFillSubQuery(Client client,
                                               SubQueryExpression subQueryExpression,
                                               QueryAction queryAction) throws SqlParseException {
        List<Object> values = new ArrayList<>();
        Object queryResult;
        try {
            queryResult = QueryActionElasticExecutor.executeAnyAction(client, queryAction);
        } catch (Exception e) {
            throw new SqlParseException("could not execute SubQuery: " + e.getMessage());
        }

        String returnField = subQueryExpression.getReturnField();
        if (queryResult instanceof SearchHits) {
            SearchHits hits = (SearchHits) queryResult;
            for (SearchHit hit : hits) {
                values.add(ElasticResultHandler.getFieldValue(hit, returnField));
            }
        } else {
            throw new SqlParseException("on sub queries only support queries that return Hits and not aggregations");
        }
        subQueryExpression.setValues(values.toArray());
    }

    private static QueryAction handleSelect(Client client, Select select) {
        if (select.isAggregate) {
            return new AggregationQueryAction(client, select);
        } else {
            return new DefaultQueryAction(client, select);
        }
    }

    private static SQLStatementParser createSqlStatementParser(String sql) {
        ElasticLexer lexer = new ElasticLexer(sql);
        lexer.nextToken();
        return new MySqlStatementParser(lexer);
    }

    private static boolean isJoin(SQLQueryExpr sqlExpr, String sql) {
        MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) sqlExpr.getSubQuery().getQuery();
        return query.getFrom() instanceof SQLJoinTableSource
               && ((SQLJoinTableSource) query.getFrom()).getJoinType() != SQLJoinTableSource.JoinType.COMMA;
    }

    @VisibleForTesting
    public static boolean shouldMigrateToQueryPlan(SQLQueryExpr expr, Format format) {
        // The JSON format will return the Elasticsearch aggregation result, which is not supported by the QueryPlanner.
        if (format == Format.JSON) {
            return false;
        }
        QueryPlannerScopeDecider decider = new QueryPlannerScopeDecider();
        return decider.isInScope(expr);
    }

    private static class QueryPlannerScopeDecider extends MySqlASTVisitorAdapter {
        private boolean hasAggregationFunc = false;
        private boolean hasNestedFunction = false;
        private boolean hasGroupBy = false;
        private boolean hasAllColumnExpr = false;

        public boolean isInScope(SQLQueryExpr expr) {
            expr.accept(this);
            return !hasAllColumnExpr && !hasNestedFunction && (hasGroupBy || hasAggregationFunc);
        }

        @Override
        public boolean visit(SQLSelectItem expr) {
            if (expr.getExpr() instanceof SQLAllColumnExpr) {
                hasAllColumnExpr = true;
            }
            return super.visit(expr);
        }

        @Override
        public boolean visit(SQLSelectGroupByClause expr) {
            hasGroupBy = true;
            return super.visit(expr);
        }

        @Override
        public boolean visit(SQLAggregateExpr expr) {
            hasAggregationFunc = true;
            return super.visit(expr);
        }

        @Override
        public boolean visit(SQLMethodInvokeExpr expr) {
            if (expr.getMethodName().equalsIgnoreCase("nested")) {
                hasNestedFunction = true;
            }
            return super.visit(expr);
        }
    }
}
