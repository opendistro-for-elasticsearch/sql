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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.rewriter.Rewriter;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.rewriter.RewriterFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.utils.FindSubQuery;

public class SubQueryRewriter {
    private final RewriterContext ctx = new RewriterContext();

    public void convert(SQLSelect query) {
        SQLSelectQuery queryExpr = query.getQuery();
        if (queryExpr instanceof MySqlSelectQueryBlock) {
            MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) queryExpr;
            ctx.addTable(queryBlock.getFrom());

            queryBlock.setWhere(convertWhere(queryBlock.getWhere()));
            queryBlock.setFrom(convertFrom(queryBlock.getFrom()));
        }
    }

    private SQLTableSource convertFrom(SQLTableSource expr) {
        SQLTableSource join = ctx.popJoin();
        if (join != null) {
            return join;
        }
        return expr;
    }

    private SQLExpr convertWhere(SQLExpr expr) {
        if (expr instanceof SQLExistsExpr) {
            ctx.setExistsSubQuery((SQLExistsExpr) expr);
            rewriteSubQuery(expr, ((SQLExistsExpr) expr).getSubQuery());
            return ctx.popWhere();
        } else if (expr instanceof SQLInSubQueryExpr) {
            ctx.setInSubQuery((SQLInSubQueryExpr) expr);
            rewriteSubQuery(expr, ((SQLInSubQueryExpr) expr).getSubQuery());
            return ctx.popWhere();
        } else if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) expr;
            SQLExpr left = convertWhere(binaryOpExpr.getLeft());
            left.setParent(binaryOpExpr);
            binaryOpExpr.setLeft(left);
            SQLExpr right = convertWhere(binaryOpExpr.getRight());
            right.setParent(binaryOpExpr);
            binaryOpExpr.setRight(right);
        }
        return expr;
    }

    private void rewriteSubQuery(SQLExpr subQueryExpr, SQLSelect subQuerySelect) {
        if (containSubQuery(subQuerySelect)) {
            convert(subQuerySelect);
        } else if (isSupportedSubQuery(ctx)){
            for (Rewriter rewriter : RewriterFactory.createRewriterList(subQueryExpr, ctx)) {
                if (rewriter.canRewrite()) {
                    rewriter.rewrite();
                    return;
                }
            }
        }
        throw new IllegalStateException("Unsupported subquery");
    }

    private boolean containSubQuery(SQLSelect query) {
        FindSubQuery findSubQuery = new FindSubQuery().continueVisitWhenFound(false);
        query.accept(findSubQuery);
        return findSubQuery.hasSubQuery();
    }

    private boolean isSupportedSubQuery(RewriterContext ctx) {
        if ((ctx.getSqlInSubQueryExprs().size() == 1 && ctx.getSqlExistsExprs().size() == 0)
            || (ctx.getSqlInSubQueryExprs().size() == 0 && ctx.getSqlExistsExprs().size() == 1)) {
            return true;
        }
        return false;
    }
}
