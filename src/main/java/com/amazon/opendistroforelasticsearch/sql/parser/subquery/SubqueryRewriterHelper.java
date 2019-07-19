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

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.visitor.FindSubqueryInWhere;

/**
 * {@link SubqueryRewriter} Helper Function.
 */
public class SubqueryRewriterHelper {

    /**
     * Rewrite the {@link SQLQueryExpr}'s from and where clause.
     *
     * @param sqlExpr sqlExpr will be rewritten.
     */
    public static void rewrite(SQLQueryExpr sqlExpr) {
        MySqlSelectQueryBlock rootQuery = (MySqlSelectQueryBlock) sqlExpr.getSubQuery().getQuery();
        final FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        sqlExpr.accept(subqueryInWhere);

        if (subqueryInWhere.subqueryType() == SubqueryType.IN) {
            final InSubqueryRewriter rewriter = new InSubqueryRewriter(rootQuery,
                    subqueryInWhere.getSqlInSubQueryExprs().get(0));
            rootQuery.setFrom(rewriter.getFrom());
            rootQuery.setWhere(rewriter.getWhere());
        }
    }

    /**
     * Does the query include the supported subquery in where.
     * Currently, ONLY IN is supported.
     *
     * @return true if it is yes, otherwise false.
     */
    public static boolean isSubquery(SQLQueryExpr sqlExpr) {
        final FindSubqueryInWhere whereSubquery = new FindSubqueryInWhere();
        sqlExpr.accept(whereSubquery);
        return SubqueryType.isSupported(whereSubquery.subqueryType());
    }
}
