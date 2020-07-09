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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.parent;

import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

/**
 * Add the parent for the node in {@link SQLQueryExpr} if it is missing.
 */
public class SQLExprParentSetter extends MySqlASTVisitorAdapter {

    /**
     * Fix null parent problem which is required by SQLIdentifier.visit()
     */
    @Override
    public boolean visit(SQLInSubQueryExpr subQuery) {
        subQuery.getExpr().setParent(subQuery);
        return true;
    }

    /**
     * Fix null parent problem which is required by SQLIdentifier.visit()
     */
    @Override
    public boolean visit(SQLInListExpr expr) {
        expr.getExpr().setParent(expr);
        return true;
    }

    /**
     * Fix the expr in {@link SQLNotExpr} without parent.
     */
    @Override
    public boolean visit(SQLNotExpr notExpr) {
        notExpr.getExpr().setParent(notExpr);
        return true;
    }
}
