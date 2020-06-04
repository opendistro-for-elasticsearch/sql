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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.utils;

import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor which try to find the SubQuery.
 */
public class FindSubQuery extends MySqlASTVisitorAdapter {
    private final List<SQLInSubQueryExpr> sqlInSubQueryExprs = new ArrayList<>();
    private final List<SQLExistsExpr> sqlExistsExprs = new ArrayList<>();
    private boolean continueVisit = true;

    public FindSubQuery continueVisitWhenFound(boolean continueVisit) {
        this.continueVisit = continueVisit;
        return this;
    }

    /**
     * Return true if has SubQuery.
     */
    public boolean hasSubQuery() {
        return !sqlInSubQueryExprs.isEmpty() || !sqlExistsExprs.isEmpty();
    }

    @Override
    public boolean visit(SQLInSubQueryExpr query) {
        sqlInSubQueryExprs.add(query);
        return continueVisit;
    }

    @Override
    public boolean visit(SQLExistsExpr query) {
        sqlExistsExprs.add(query);
        return continueVisit;
    }

    public List<SQLInSubQueryExpr> getSqlInSubQueryExprs() {
        return sqlInSubQueryExprs;
    }

    public List<SQLExistsExpr> getSqlExistsExprs() {
        return sqlExistsExprs;
    }
}
