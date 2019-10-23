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


package com.amazon.opendistroforelasticsearch.sql.rewriter.ordinal;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRuleExecutor;
import com.amazon.opendistroforelasticsearch.sql.rewriter.alias.TableAliasPrefixRemoveRule;
import com.amazon.opendistroforelasticsearch.sql.rewriter.identifier.UnquoteIdentifierRule;
import com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm.VerificationException;

import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;

/**
 * Rewrite rule for changing ordinal alias in order by and group by to actual select field.
 */
public class OrdinalRewriterRule implements RewriteRule<SQLQueryExpr> {

    private SQLQueryExpr sqlExprGroupCopy;
    private SQLQueryExpr sqlExprOrderCopy;
    private final String sql;
    private final Map<Integer, SQLExpr> groupOrdinalToExpr = new HashMap<>();
    private final Map<Integer, SQLExpr> orderOrdinalToExpr = new HashMap<>();
    private RewriteRuleExecutor<SQLQueryExpr> ruleExecutor;

    public OrdinalRewriterRule(String sql) {
        this.sql = sql;
    }

    @Override
    public boolean match(SQLQueryExpr root) throws SQLFeatureNotSupportedException {
        SQLSelectQuery sqlSelectQuery = root.getSubQuery().getQuery();
         if (!(sqlSelectQuery instanceof MySqlSelectQueryBlock)) {
             // it could be SQLUnionQuery
             return false;
         }

        MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) sqlSelectQuery;

        if (!hasGroupByWithOrdinals(query) && !hasOrderByWithOrdinals(query)) {
            return false;
        }

        // we cannot clone SQLSelectItem, so we need similar objects to assign to GroupBy and OrderBy items
        sqlExprGroupCopy = generateAST();
        sqlExprOrderCopy = generateAST();

        collectOrdinalAliases(sqlExprGroupCopy, groupOrdinalToExpr);
        collectOrdinalAliases(sqlExprOrderCopy, orderOrdinalToExpr);

        if (groupOrdinalToExpr.isEmpty()) {
            // orderOrdinalToExpr and groupOrdinalToExpr should be identical
            // because they are generated from similar syntax trees
            // checking for groupOrdinalToExpr or orderOrdinalToExpr is enough
            return false;
        }

        // we need to rewrite the generated syntax tree to match the original syntax tree Select clause
        ruleExecutor = RewriteRuleExecutor.builder()
            .withRule(new UnquoteIdentifierRule())
            .withRule(new TableAliasPrefixRemoveRule())
            .build();

        ruleExecutor.executeOn(sqlExprGroupCopy);
        ruleExecutor.executeOn(sqlExprGroupCopy);
        return true;
    }

    @Override
    public void rewrite(SQLQueryExpr root) {
        changeOrdinalAliasInGroupAndOrderBy(root);
    }

    private boolean hasGroupByWithOrdinals(MySqlSelectQueryBlock query) {
        if (query.getGroupBy() == null) {
            return false;
        } else if (query.getGroupBy().getItems().isEmpty()){
            return false;
        }

        return query.getGroupBy().getItems().stream().anyMatch(x ->
            x instanceof MySqlSelectGroupByExpr && ((MySqlSelectGroupByExpr) x).getExpr() instanceof SQLIntegerExpr
        );
    }

    private boolean hasOrderByWithOrdinals(MySqlSelectQueryBlock query) {
        if (query.getOrderBy() == null) {
            return false;
        } else if (query.getOrderBy().getItems().isEmpty()){
            return false;
        }

        return query.getOrderBy().getItems().stream().anyMatch(x ->
            x.getExpr() instanceof SQLIntegerExpr
            || (
                x.getExpr() instanceof SQLBinaryOpExpr
                && ((SQLBinaryOpExpr) x.getExpr()).getLeft() instanceof SQLIntegerExpr
            )
        );
    }

    private void collectOrdinalAliases(SQLQueryExpr root, Map<Integer, SQLExpr> map) {
        root.accept(new MySqlASTVisitorAdapter() {
            private Integer count = 0;

            @Override
            public boolean visit(SQLSelectItem select) {
                map.put(++count, select.getExpr());
                return false;
            }
        });
    }

    private void changeOrdinalAliasInGroupAndOrderBy(SQLQueryExpr root) {
        root.accept(new MySqlASTVisitorAdapter() {

            private String groupException = "Invalid ordinal [%s] specified in [GROUP BY %s]";
            private String orderException = "Invalid ordinal [%s] specified in [ORDER BY %s]";

            @Override
            public boolean visit(MySqlSelectGroupByExpr groupByExpr) {
                SQLExpr expr = groupByExpr.getExpr();
                if (expr instanceof SQLIntegerExpr) {
                    Integer ordinalValue = ((SQLIntegerExpr) expr).getNumber().intValue();
                    SQLExpr newExpr = groupOrdinalToExpr.get(ordinalValue);
                    checkInvalidOrdinal(newExpr, ordinalValue, groupException);
                    groupByExpr.setExpr(newExpr);
                    newExpr.setParent(groupByExpr);
                }
                return false;
            }

            @Override
            public boolean visit(SQLSelectOrderByItem orderByItem) {
                SQLExpr expr = orderByItem.getExpr();
                Integer ordinalValue;

                if (expr instanceof SQLIntegerExpr) {
                    ordinalValue = ((SQLIntegerExpr) expr).getNumber().intValue();
                    SQLExpr newExpr = orderOrdinalToExpr.get(ordinalValue);
                    checkInvalidOrdinal(newExpr, ordinalValue, orderException);
                    orderByItem.setExpr(newExpr);
                    newExpr.setParent(orderByItem);
                }else if (expr instanceof SQLBinaryOpExpr
                          && ((SQLBinaryOpExpr) expr).getLeft() instanceof SQLIntegerExpr) {
                    // support ORDER BY IS NULL/NOT NULL
                    SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) expr;
                    SQLIntegerExpr integerExpr = (SQLIntegerExpr) binaryOpExpr.getLeft();

                    ordinalValue = integerExpr.getNumber().intValue();
                    SQLExpr newExpr = orderOrdinalToExpr.get(ordinalValue);
                    checkInvalidOrdinal(newExpr, ordinalValue, orderException);
                    binaryOpExpr.setLeft(newExpr);
                    newExpr.setParent(binaryOpExpr);
                }

                return false;
            }
        });
    }

    private SQLQueryExpr generateAST() {
        return (SQLQueryExpr) toSqlExpr(sql);
    }

    private static SQLExpr toSqlExpr(String sql) {
        SQLExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();

        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("Illegal SQL expression : " + sql);
        }
        return expr;
    }

    private void checkInvalidOrdinal(SQLExpr expr, Integer ordinal, String exception){
        if (expr == null) {
            throw new VerificationException(String.format(exception, ordinal, ordinal));
        }
    }
}
