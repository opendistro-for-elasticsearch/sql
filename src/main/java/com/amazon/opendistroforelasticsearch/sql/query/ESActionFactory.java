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

package com.amazon.opendistroforelasticsearch.sql.query;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.domain.Delete;
import com.amazon.opendistroforelasticsearch.sql.domain.IndexStatement;
import com.amazon.opendistroforelasticsearch.sql.domain.JoinSelect;
import com.amazon.opendistroforelasticsearch.sql.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.executor.ElasticResultHandler;
import com.amazon.opendistroforelasticsearch.sql.executor.QueryActionElasticExecutor;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticLexer;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.parser.SubQueryExpression;
import com.amazon.opendistroforelasticsearch.sql.query.join.ESJoinQueryActionFactory;
import com.amazon.opendistroforelasticsearch.sql.query.multi.MultiQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.multi.MultiQuerySelect;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRuleExecutor;
import com.amazon.opendistroforelasticsearch.sql.rewriter.identifier.UnquoteIdentifierRule;
import com.amazon.opendistroforelasticsearch.sql.rewriter.alias.TableAliasPrefixRemoveRule;
import com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm.TermFieldRewriter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm.TermFieldRewriter.TermRewriterFilter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.nestedfield.NestedFieldRewriter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.ordinal.OrdinalRewriterRule;
import com.amazon.opendistroforelasticsearch.sql.rewriter.parent.SQLExprParentSetterRule;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.SubQueryRewriteRule;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.domain.IndexStatement.StatementType;

public class ESActionFactory {

    /**
     * Create the compatible Query object
     * based on the SQL query.
     *
     * @param sql The SQL query.
     * @return Query object.
     */
    public static QueryAction create(Client client, String sql) throws SqlParseException,
            SQLFeatureNotSupportedException {

        // Linebreak matcher
        sql = sql.replaceAll("\\R", " ").trim();

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
                    sqlExpr.accept(new TermFieldRewriter(client, TermRewriterFilter.MULTI_QUERY));
                    MultiQuerySelect multiSelect = new SqlParser()
                            .parseMultiSelect((SQLUnionQuery) sqlExpr.getSubQuery().getQuery());
                    return new MultiQueryAction(client, multiSelect);
                } else if (isJoin(sqlExpr, sql)) {
                    sqlExpr.accept(new TermFieldRewriter(client, TermRewriterFilter.JOIN));
                    JoinSelect joinSelect = new SqlParser().parseJoinSelect(sqlExpr);
                    return ESJoinQueryActionFactory.createJoinAction(client, joinSelect);
                } else {
                    sqlExpr.accept(new TermFieldRewriter(client));
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
        return sql
                .substring(0, endOfFirstWord > 0 ? endOfFirstWord : sql.length())
                .toUpperCase();
    }

    private static boolean isMulti(SQLQueryExpr sqlExpr) {
        return sqlExpr.getSubQuery().getQuery() instanceof SQLUnionQuery;
    }

    private static void executeAndFillSubQuery(Client client, SubQueryExpression subQueryExpression,
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

    private static SQLExpr toSqlExpr(String sql) {
        SQLExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();

        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("Illegal SQL expression : " + sql);
        }
        return expr;
    }
}
