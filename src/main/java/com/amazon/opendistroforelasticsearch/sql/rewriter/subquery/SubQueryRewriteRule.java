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

import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.rewriter.SubqueryAliasRewriter;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Subquery Rewriter Rule.
 */
public class SubQueryRewriteRule implements RewriteRule<SQLQueryExpr> {
    private FindAllSubQueryInWhere findAllSubQueryInWhere = new FindAllSubQueryInWhere();

    @Override
    public boolean match(SQLQueryExpr expr) throws SQLFeatureNotSupportedException {
        expr.accept(findAllSubQueryInWhere);

        if (isContainSubQuery(findAllSubQueryInWhere)) {
            if (isSupportedSubQuery(findAllSubQueryInWhere)) {
                return true;
            } else {
                throw new SQLFeatureNotSupportedException("Unsupported subquery. Only one EXISTS or IN is supported");
            }
        } else {
            return false;
        }
    }

    @Override
    public void rewrite(SQLQueryExpr expr) {
        expr.accept(new SubqueryAliasRewriter());
        new SubQueryRewriter().convert(expr.getSubQuery());
    }

    private boolean isContainSubQuery(FindAllSubQueryInWhere allSubQuery) {
        return !allSubQuery.getSqlExistsExprs().isEmpty() || !allSubQuery.getSqlInSubQueryExprs().isEmpty();
    }

    private boolean isSupportedSubQuery(FindAllSubQueryInWhere allSubQuery) {
        if ((allSubQuery.getSqlInSubQueryExprs().size() == 1 && allSubQuery.getSqlExistsExprs().size() == 0)
            || (allSubQuery.getSqlInSubQueryExprs().size() == 0 && allSubQuery.getSqlExistsExprs().size() == 1)) {
            return true;
        }
        return false;
    }

    private class FindAllSubQueryInWhere extends MySqlASTVisitorAdapter {
        private final List<SQLInSubQueryExpr> sqlInSubQueryExprs = new ArrayList<>();
        private final List<SQLExistsExpr> sqlExistsExprs = new ArrayList<>();


        @Override
        public boolean visit(SQLInSubQueryExpr query) {
            sqlInSubQueryExprs.add(query);
            return true;
        }

        @Override
        public boolean visit(SQLExistsExpr query) {
            sqlExistsExprs.add(query);
            return true;
        }

        List<SQLInSubQueryExpr> getSqlInSubQueryExprs() {
            return sqlInSubQueryExprs;
        }

        List<SQLExistsExpr> getSqlExistsExprs() {
            return sqlExistsExprs;
        }
    }

}
