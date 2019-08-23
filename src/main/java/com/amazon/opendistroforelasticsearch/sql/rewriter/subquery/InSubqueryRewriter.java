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

package com.amazon.opendistroforelasticsearch.sql.rewriter.subquery;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.model.Subquery;

/**
 * IN Subquery Rewriter.
 * For example,
 * SELECT * FROM A WHERE a IN (SELECT b FROM B) and c > 10 should be rewritten to
 * SELECT A.* FROM A JOIN B ON A.a = B.b WHERE c > 10 and B.b IS NOT NULL.
 */
public class InSubqueryRewriter implements SubqueryRewriter {
    private final SQLInSubQueryExpr inSubQueryExpr;
    private final MySqlSelectQueryBlock inSubQuery;
    private final SQLExpr joinLeftExpr;
    private final SQLExpr joinRightExpr;

    public InSubqueryRewriter(Subquery subquery) {
        this.inSubQueryExpr = (SQLInSubQueryExpr) subquery.getSubQueryExpr();
        this.inSubQuery = (MySqlSelectQueryBlock) inSubQueryExpr.getSubQuery().getQuery();
        joinLeftExpr = inSubQueryExpr.getExpr();
        joinRightExpr = rightExpr(inSubQuery);
    }

    /**
     * Rewrite the subquery with JOIN clause. This API is NOT idempotent.
     *
     * @param query Subquery will be written.
     */
    @Override
    public void rewrite(MySqlSelectQueryBlock query) {
        query.setFrom(buildFrom(query));
        query.setWhere(buildWhere(query));
    }

    /**
     * Build From clause from input query.
     * <p>
     * With the input query.
     * Query
     * /   |   \
     * SELECT FROM     WHERE
     * |    |     /   |  \
     * *    A  c>10 AND INSubquery
     * /    \
     * a    Query
     * /    \
     * SELECT FROM
     * |     |
     * b     B
     * <p>
     * The FROM logic should be
     * JOIN
     * / | \
     * A  B A.a == B.b
     */
    private SQLJoinTableSource buildFrom(MySqlSelectQueryBlock rootQuery) {
        SQLJoinTableSource sqlJoin = new SQLJoinTableSource();
        sqlJoin.setLeft(rootQuery.getFrom());
        sqlJoin.setRight(inSubQuery.getFrom());
        sqlJoin.setCondition(new SQLBinaryOpExpr(joinLeftExpr, SQLBinaryOperator.Equality, joinRightExpr));
        sqlJoin.setJoinType(JoinType.JOIN);

        return sqlJoin;
    }

    /**
     * Build Where clause from input query.
     * <p>
     * With the input query.
     * Query
     * /   |   \
     * SELECT FROM     WHERE
     * |    |     /   |  \
     * *    A  c>10 AND INSubquery
     * /    \
     * a    Query
     * /    \
     * SELECT FROM
     * |     |
     * b     B
     * <p>
     * <p>
     * WHERE
     * /  |   \
     * c>10 AND B.b is NOT NULL
     */
    private SQLExpr buildWhere(MySqlSelectQueryBlock rootQuery) {
        return rewriteSubquery(rootQuery.getWhere());
    }

    private SQLExpr rewriteSubquery(SQLExpr expr) {
        if (expr instanceof SQLInSubQueryExpr) {
            return createSubqueryReplacementCondition();
        } else if (expr instanceof SQLBinaryOpExpr) {
            ((SQLBinaryOpExpr) expr).setLeft(rewriteSubquery(((SQLBinaryOpExpr) expr).getLeft()));
            ((SQLBinaryOpExpr) expr).setRight(rewriteSubquery(((SQLBinaryOpExpr) expr).getRight()));
            return expr;
        } else {
            return expr;
        }
    }

    private SQLExpr rightExpr(MySqlSelectQueryBlock rightQuery) {
        if (rightQuery.getSelectList().size() > 1) {
            throw new IllegalStateException("Unsupported subquery with multiple select " + rightQuery.getSelectList());
        }
        return rightQuery.getSelectList().get(0).getExpr();
    }

    private SQLBinaryOpExpr createSubqueryReplacementCondition() {
        // IN subquery is rewritten to JOIN, the null value from the right table should be filtered out.
        SQLBinaryOpExpr replacementCond = new SQLBinaryOpExpr(joinRightExpr,
                inSubQueryExpr.isNot() ? SQLBinaryOperator.Is : SQLBinaryOperator.IsNot,
                new SQLNullExpr());
        SQLExpr subqueryWhere = inSubQuery.getWhere();
        if (subqueryWhere == null) {
            return replacementCond;
        } else {
            return new SQLBinaryOpExpr(replacementCond, SQLBinaryOperator.BooleanAnd, subqueryWhere);
        }
    }
}
