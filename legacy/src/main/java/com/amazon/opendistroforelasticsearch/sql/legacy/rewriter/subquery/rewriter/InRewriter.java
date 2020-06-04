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
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.RewriterContext;

/**
 * IN Subquery Rewriter.
 * For example,
 * SELECT * FROM A WHERE a IN (SELECT b FROM B) and c > 10 should be rewritten to
 * SELECT A.* FROM A JOIN B ON A.a = B.b WHERE c > 10 and B.b IS NOT NULL.
 */
public class InRewriter implements Rewriter {
    private final SQLInSubQueryExpr inExpr;
    private final RewriterContext ctx;
    private final MySqlSelectQueryBlock queryBlock;

    public InRewriter(SQLInSubQueryExpr inExpr, RewriterContext ctx) {
        this.inExpr = inExpr;
        this.ctx = ctx;
        this.queryBlock = (MySqlSelectQueryBlock) inExpr.getSubQuery().getQuery();
    }

    @Override
    public boolean canRewrite() {
        return !inExpr.isNot();
    }

    /**
     * Build Where clause from input query.
     * <p>
     * With the input query.
     *         Query
     *      /    |     \
     *  SELECT  FROM  WHERE
     *  |        |    /   |  \
     *  *         A  c>10 AND INSubquery
     *                         /    \
     *                        a    Query
     *                        /      \
     *                           SELECT FROM
     *                             |     |
     *                             b     B
     * <p>
     *
     */
    @Override
    public void rewrite() {
        SQLTableSource from = queryBlock.getFrom();
        addJoinTable(from);

        SQLExpr where = queryBlock.getWhere();
        if (null == where) {
            ctx.addWhere(generateNullOp());
        } else if (where instanceof SQLBinaryOpExpr) {
            ctx.addWhere(and(generateNullOp(), (SQLBinaryOpExpr) where));
        } else {
            throw new IllegalStateException("unsupported where class type " + where.getClass());
        }
    }

    /**
     * Build the Null check expression. For example,
     * SELECT * FROM A WHERE a IN (SELECT b FROM B), should return B.b IS NOT NULL
     */
    private SQLBinaryOpExpr generateNullOp() {
        SQLBinaryOpExpr binaryOpExpr = new SQLBinaryOpExpr();
        binaryOpExpr.setLeft(fetchJoinExpr());
        binaryOpExpr.setRight(new SQLNullExpr());
        binaryOpExpr.setOperator(SQLBinaryOperator.IsNot);

        return binaryOpExpr;
    }

    /**
     * Add the {@link SQLTableSource} with {@link JoinType} and {@link SQLBinaryOpExpr} to the {@link RewriterContext}.
     */
    private void addJoinTable(SQLTableSource right) {
        SQLBinaryOpExpr binaryOpExpr = new SQLBinaryOpExpr(inExpr.getExpr(),
                SQLBinaryOperator.Equality,
                fetchJoinExpr());
        ctx.addJoin(right, JoinType.JOIN, binaryOpExpr);
    }

    private SQLExpr fetchJoinExpr() {
        if (queryBlock.getSelectList().size() > 1) {
            throw new IllegalStateException("Unsupported subquery with multiple select " + queryBlock.getSelectList());
        }
        return queryBlock.getSelectList().get(0).getExpr();
    }
}
