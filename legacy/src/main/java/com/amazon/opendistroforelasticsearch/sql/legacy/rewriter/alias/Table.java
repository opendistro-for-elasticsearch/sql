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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.alias;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.google.common.base.Strings;

import static com.alibaba.druid.sql.ast.expr.SQLBinaryOperator.Divide;

/**
 * Util class for table expression parsing
 */
class Table {

    private final SQLExprTableSource tableExpr;

    Table(SQLExprTableSource tableExpr) {
        this.tableExpr = tableExpr;
    }

    boolean hasAlias() {
        return !alias().isEmpty();
    }

    String alias() {
        return Strings.nullToEmpty(tableExpr.getAlias());
    }

    void removeAlias() {
        tableExpr.setAlias(null);
    }

    /** Extract table name in table expression */
    String name() {
        SQLExpr expr = tableExpr.getExpr();
        if (expr instanceof SQLIdentifierExpr) {
            return ((SQLIdentifierExpr) expr).getName();
        } else if (isTableWithType(expr)) {
            return ((SQLIdentifierExpr) ((SQLBinaryOpExpr) expr).getLeft()).getName();
        }
        return expr.toString();
    }

    /** Return true for table name along with type name, for example 'accounts/_doc' */
    private boolean isTableWithType(SQLExpr expr) {
        return expr instanceof SQLBinaryOpExpr
            && ((SQLBinaryOpExpr) expr).getLeft() instanceof SQLIdentifierExpr
            && ((SQLBinaryOpExpr) expr).getOperator() == Divide;
    }
}
