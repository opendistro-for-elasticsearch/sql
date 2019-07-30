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
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.model.Subquery;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.model.SubqueryType;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.rewriter.SubqueryAliasRewriter;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.visitor.FindSubqueryInWhere;

import java.sql.SQLFeatureNotSupportedException;

/**
 * Provide Helper Function for {@link SubqueryRewriter}.
 */
public class SubqueryRewriterHelper {

    /**
     * Rewrite the {@link SQLQueryExpr}'s from and where clause.
     *
     * @param sqlExpr sqlExpr will be rewritten.
     */
    public static void rewrite(SQLQueryExpr sqlExpr, Subquery subquery) throws SQLFeatureNotSupportedException {
        if (isSubquery(subquery)) {
            MySqlSelectQueryBlock rootQuery = (MySqlSelectQueryBlock) sqlExpr.getSubQuery().getQuery();
            // add the alias for the subquery identifier if missing
            rootQuery.accept(new SubqueryAliasRewriter());
            // handle the SubqueryType.IN
            if (SubqueryType.IN == subquery.getSubqueryType()) {
                InSubqueryRewriter rewriter = new InSubqueryRewriter(subquery);
                rewriter.rewrite(rootQuery);
            }
        }
    }

    /**
     * Does the query include the supported subquery in where.
     * Currently, ONLY IN is supported.
     *
     * @return true if it is yes, otherwise false.
     */
    public static boolean isSubquery(Subquery subquery) throws SQLFeatureNotSupportedException {
        SubqueryType subqueryType = subquery.getSubqueryType();
        if (subqueryType.isNotSubquery()) return false;
        else if (subqueryType.isSupported()) {
            return true;
        } else {
            throw new SQLFeatureNotSupportedException("Unsupported subquery");
        }
    }

    /**
     * Retrieve the {@link Subquery} from the {@link SQLQueryExpr}.
     */
    public static Subquery subquery(SQLQueryExpr sqlExpr) {
        FindSubqueryInWhere whereSubquery = new FindSubqueryInWhere();
        sqlExpr.accept(whereSubquery);
        return whereSubquery.subquery();
    }
}
