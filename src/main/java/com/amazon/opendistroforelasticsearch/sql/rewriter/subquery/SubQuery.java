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
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.rewriter.Rewriter;

import java.util.List;

/**
 * Base class for all the SubQuery.
 */
public abstract class SubQuery {
    private final SQLSelect query;
    private final BlackBoard bb;

    SubQuery(SQLSelect query, BlackBoard bb) {
        this.query = query;
        this.bb = bb;
    }

    abstract List<Rewriter> rewriterList();

    /**
     * Rewrite the SubQuery with {@link Rewriter}
     * If no rewriter can rewrite the SQL, throw {@link IllegalStateException}
     */
    void rewrite() {
        if (containSubQuery()) {
            SubQueryRewriter.convert(query, bb);
        } else {
            boolean supported = false;
            for (Rewriter rewriter : rewriterList()) {
                if (rewriter.canRewrite()) {
                    rewriter.rewrite();
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                throw new IllegalStateException("Unsupported subquery");
            }
        }
    }

    private boolean containSubQuery() {
        HasSuQuery hasSuQuery = new HasSuQuery();
        query.accept(hasSuQuery);
        return hasSuQuery.isHasSubQuery();
    }

    static class HasSuQuery extends MySqlASTVisitorAdapter {
        private boolean hasSubQuery = false;

        boolean isHasSubQuery() {
            return hasSubQuery;
        }

        @Override
        public boolean visit(SQLInSubQueryExpr query) {
            hasSubQuery = true;
            return false;
        }

        @Override
        public boolean visit(SQLExistsExpr query) {
            hasSubQuery = true;
            return false;
        }
    }

}
