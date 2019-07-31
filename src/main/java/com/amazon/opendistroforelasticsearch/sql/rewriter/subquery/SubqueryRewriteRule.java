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

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.model.Subquery;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.model.SubqueryType;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.rewriter.SubqueryAliasRewriter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.visitor.FindSubqueryInWhere;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Optional;

/**
 * Subquery Optimize Rule.
 */
public class SubqueryRewriteRule implements RewriteRule<SQLQueryExpr> {
    private Optional<Subquery> subquery;

    @Override
    public boolean match(SQLQueryExpr expr) throws SQLFeatureNotSupportedException {
        subquery = subquery(expr);
        if (subquery.isPresent()) {
            SubqueryType subqueryType = subquery.get().getSubqueryType();
            if (subqueryType.isSupported()) {
                return true;
            } else {
                throw new SQLFeatureNotSupportedException("Unsupported subquery. only IN is supported.");
            }
        } else {
            return false;
        }
    }

    @Override
    public void rewrite(SQLQueryExpr expr) {
        if (subquery == null) subquery = subquery(expr);
        MySqlSelectQueryBlock rootQuery = (MySqlSelectQueryBlock) expr.getSubQuery().getQuery();
        // add the alias for the subquery identifier if missing
        rootQuery.accept(new SubqueryAliasRewriter());
        // handle the SubqueryType.IN
        if (SubqueryType.IN == subquery.get().getSubqueryType()) {
            // rootQuery is "SELECT * FROM A WHERE a IN (SELECT b FROM B)"
            // subquery is "a IN (SELECT b FROM B)"
            InSubqueryRewriter rewriter = new InSubqueryRewriter(subquery.get());
            rewriter.rewrite(rootQuery);
        }
    }

    /**
     * Retrieve the {@link Subquery} from the {@link SQLQueryExpr}.
     */
    private Optional<Subquery> subquery(SQLQueryExpr sqlExpr) {
        FindSubqueryInWhere whereSubquery = new FindSubqueryInWhere();
        sqlExpr.accept(whereSubquery);
        return whereSubquery.subquery();
    }
}


