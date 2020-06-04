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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.rewriter;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.RewriterContext;

/**
 * Nested EXISTS SQL Rewriter.
 * The EXISTS clause will be remove from the SQL. The translated SQL will use ElasticSearch's nested query logic.
 *
 * For example,
 * <p>
 * SELECT e.name
 * FROM employee as e, e.projects as p
 * WHERE EXISTS (SELECT * FROM p)
 * should be rewritten to
 * SELECT e.name
 * FROM employee as e, e.projects as p
 * WHERE p is not null
 * </p>
 */
public class NestedExistsRewriter implements Rewriter {
    private final SQLExistsExpr existsExpr;
    private final RewriterContext ctx;
    private final SQLExprTableSource from;
    private final SQLExpr where;

    public NestedExistsRewriter(SQLExistsExpr existsExpr, RewriterContext board) {
        this.existsExpr = existsExpr;
        this.ctx = board;
        MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) existsExpr.getSubQuery().getQuery();
        if (queryBlock.getFrom() instanceof SQLExprTableSource) {
            this.from = (SQLExprTableSource) queryBlock.getFrom();
        } else {
            throw new IllegalStateException("unsupported expression in from " + queryBlock.getFrom().getClass());
        }
        this.where = queryBlock.getWhere();
    }

    /**
     * The from table must be nested field.
     */
    @Override
    public boolean canRewrite() {
        return ctx.isNestedQuery(from);
    }

    @Override
    public void rewrite() {
        ctx.addJoin(from, JoinType.COMMA);
        ctx.addWhere(rewriteExistsWhere());
    }

    private SQLExpr rewriteExistsWhere() {
        SQLBinaryOpExpr translatedWhere;
        SQLBinaryOpExpr notMissingOp = buildNotMissingOp();
        if (null == where) {
            translatedWhere = notMissingOp;
        } else if (where instanceof SQLBinaryOpExpr) {
            translatedWhere = and(notMissingOp, (SQLBinaryOpExpr) where);
        } else {
            throw new IllegalStateException("unsupported expression in where " + where.getClass());
        }

        if (existsExpr.isNot()) {
            SQLNotExpr sqlNotExpr = new SQLNotExpr(translatedWhere);
            translatedWhere.setParent(sqlNotExpr);
            return sqlNotExpr;
        } else {
            return translatedWhere;
        }
    }

    private SQLBinaryOpExpr buildNotMissingOp() {
        SQLBinaryOpExpr binaryOpExpr = new SQLBinaryOpExpr();
        binaryOpExpr.setLeft(new SQLIdentifierExpr(from.getAlias()));
        binaryOpExpr.setRight(new SQLIdentifierExpr("MISSING"));
        binaryOpExpr.setOperator(SQLBinaryOperator.IsNot);

        return binaryOpExpr;
    }
}
