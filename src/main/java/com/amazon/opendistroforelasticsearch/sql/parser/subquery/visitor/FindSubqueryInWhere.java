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

package com.amazon.opendistroforelasticsearch.sql.parser.subquery.visitor;

import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.model.Subquery;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.model.SubqueryType;

import java.util.ArrayList;
import java.util.List;

/**
 * Find the {@link SQLInSubQueryExpr} and {@link SQLExistsExpr} in the {@link SQLQueryExpr}
 */
public class FindSubqueryInWhere extends MySqlASTVisitorAdapter {
    private final List<SQLInSubQueryExpr> sqlInSubQueryExprs = new ArrayList<>();
    private final List<SQLExistsExpr> sqlExistsExprs = new ArrayList<>();

    /**
     * Return Subquery
     * @return {@link Subquery}
     */
    public Subquery subquery() {
        if (sqlExistsExprs.isEmpty() && sqlInSubQueryExprs.isEmpty()) return new Subquery(SubqueryType.NOT_SUBQUERY);
        if (sqlInSubQueryExprs.size() == 1 &&
                sqlExistsExprs.isEmpty() &&
                !sqlInSubQueryExprs.get(0).isNot()) {
            return new Subquery(SubqueryType.IN, sqlInSubQueryExprs.get(0));
        }
        return new Subquery(SubqueryType.UNSUPPORTED);
    }

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
}
