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

package com.amazon.opendistroforelasticsearch.sql.parser.subquery;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.rewriter.SubqueryAliasRewriter;

/**
 * IN Subquery Rewriter.
 * For example,
 *  SELECT * FROM A WHERE a IN (SELECT b FROM B) should be rewritten to
 *  SELECT A.* FROM A JOIN B ON A.a = B.b.
 */
public class InSubqueryRewriter implements SubqueryRewriter {

    private final MySqlSelectQueryBlock rootQuery;

    private final SQLInSubQueryExpr inSubQueryExpr;

    private final MySqlSelectQueryBlock inSubQuery;

    private SQLExpr joinLeftExpr;

    private SQLExpr joinRightExpr;

    public InSubqueryRewriter(MySqlSelectQueryBlock rootQuery, SQLInSubQueryExpr inSubQueryExpr) {
        rootQuery.accept0(new SubqueryAliasRewriter());
        this.rootQuery = rootQuery;
        this.inSubQueryExpr = inSubQueryExpr;
        this.inSubQuery = (MySqlSelectQueryBlock) inSubQueryExpr.getSubQuery().getQuery();

        joinLeftExpr = inSubQueryExpr.getExpr();
        joinRightExpr = rightExpr(inSubQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SQLJoinTableSource getFrom() {
        final SQLJoinTableSource sqlJoin = new SQLJoinTableSource();
        sqlJoin.setLeft(rootQuery.getFrom());
        sqlJoin.setRight(inSubQuery.getFrom());
        sqlJoin.setCondition(new SQLBinaryOpExpr(joinLeftExpr, SQLBinaryOperator.Equality, joinRightExpr));
        sqlJoin.setJoinType(JoinType.JOIN);

        return sqlJoin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SQLExpr getWhere() {
        return rewriteSubquery(rootQuery.getWhere());
    }

    private SQLExpr rewriteSubquery(SQLExpr expr) {
        if (expr instanceof SQLInSubQueryExpr) {
            return createSubqueryReplacementCondition();
        }
        else if (expr instanceof SQLBinaryOpExpr) {
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
        /**
         * IN subquery is rewritten to JOIN, the null value from the right table should be filtered out.
         */
        SQLBinaryOpExpr replacementCond = new SQLBinaryOpExpr(joinRightExpr,
                inSubQueryExpr.isNot() ? SQLBinaryOperator.Is : SQLBinaryOperator.IsNot,
                new SQLNullExpr());
        final SQLExpr subqueryWhere = inSubQuery.getWhere();
        if (subqueryWhere == null) {
            return replacementCond;
        } else {
            return new SQLBinaryOpExpr(replacementCond, SQLBinaryOperator.BooleanAnd, subqueryWhere);
        }
    }
}
