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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.nestedfield;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util;

import java.util.List;

/**
 * Abstract class for SQL clause domain class.
 *
 * @param <T> concrete type of clause
 */
abstract class SQLClause<T> {

    protected final T expr;

    SQLClause(T expr) {
        this.expr = expr;
    }

    /**
     * Rewrite nested fields in query according to/fill into information in scope.
     *
     * @param scope Scope of current query
     */
    abstract void rewrite(Scope scope);

    SQLMethodInvokeExpr replaceByNestedFunction(SQLExpr expr, String nestedPath) {
        final int nestedPathIndex = 1;
        SQLMethodInvokeExpr nestedFunc = replaceByNestedFunction(expr);
        nestedFunc.getParameters().add(nestedPathIndex, new SQLCharExpr(nestedPath));
        return nestedFunc;
    }

    /**
     * Replace expr by nested(expr) and set pointer in parent properly
     */
    SQLMethodInvokeExpr replaceByNestedFunction(SQLExpr expr) {
        SQLObject parent = expr.getParent();
        SQLMethodInvokeExpr nestedFunc = wrapNestedFunction(expr);
        if (parent instanceof SQLAggregateExpr) {
            List<SQLExpr> args = ((SQLAggregateExpr) parent).getArguments();
            args.set(args.indexOf(expr), nestedFunc);
        } else if (parent instanceof SQLSelectItem) {
            ((SQLSelectItem) parent).setExpr(nestedFunc);
        } else if (parent instanceof MySqlSelectGroupByExpr) {
            ((MySqlSelectGroupByExpr) parent).setExpr(nestedFunc);
        } else if (parent instanceof SQLSelectOrderByItem) {
            ((SQLSelectOrderByItem) parent).setExpr(nestedFunc);
        } else if (parent instanceof SQLInSubQueryExpr) {
            ((SQLInSubQueryExpr) parent).setExpr(nestedFunc);
        } else if (parent instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr parentOp = (SQLBinaryOpExpr) parent;
            if (parentOp.getLeft() == expr) {
                parentOp.setLeft(nestedFunc);
            } else {
                parentOp.setRight(nestedFunc);
            }
        } else if (parent instanceof MySqlSelectQueryBlock) {
            ((MySqlSelectQueryBlock) parent).setWhere(nestedFunc);
        } else if (parent instanceof SQLNotExpr) {
              ((SQLNotExpr) parent).setExpr(nestedFunc);
        } else {
            throw new IllegalStateException("Unsupported place to use nested field under parent: " + parent);
        }
        return nestedFunc;
    }

    private SQLMethodInvokeExpr wrapNestedFunction(SQLExpr expr) {
        SQLMethodInvokeExpr nestedFunc = new SQLMethodInvokeExpr("nested");
        nestedFunc.setParent(expr.getParent());
        nestedFunc.addParameter(expr);  // this will auto set parent of expr
        return nestedFunc;
    }

    String pathFromIdentifier(SQLExpr identifier) {
        String field = Util.extendedToString(identifier);
        int lastDot = field.lastIndexOf(".");
        return lastDot == -1 ? field :field.substring(0, lastDot);
    }

}
