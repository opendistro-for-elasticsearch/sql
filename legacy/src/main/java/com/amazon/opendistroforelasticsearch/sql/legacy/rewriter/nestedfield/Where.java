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

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;

/**
 * Condition expression in WHERE statement.
 */
class Where extends SQLClause<SQLBinaryOpExpr> {

    Where(SQLBinaryOpExpr expr) {
        super(expr);
    }

    /**
     * Rewrite if left and right tag is different (or reach root of WHERE).
     * Otherwise continue delaying the rewrite.
     * <p>
     * Assumption: there are only 2 forms of condition
     * 1) BinaryOp: Left=Identifier, right=value
     * 2) BinaryOp: Left=BinaryOp, right=BinaryOp
     */
    @Override
    void rewrite(Scope scope) {
        if (isLeftChildCondition()) {
            if (isChildTagEquals(scope)) {
                useAnyChildTag(scope);
            } else {
                left().mergeNestedField(scope);
                right().mergeNestedField(scope);
            }
        }
        mergeIfHaveTagAndIsRootOfWhereOrNot(scope);
    }

    private boolean isLeftChildCondition() {
        return expr.getLeft() instanceof SQLBinaryOpExpr;
    }

    private boolean isChildTagEquals(Scope scope) {
        String left = scope.getConditionTag((SQLBinaryOpExpr) expr.getLeft());
        String right = scope.getConditionTag((SQLBinaryOpExpr) expr.getRight());
        return left.equals(right);
    }

    private void useAnyChildTag(Scope scope) {
        scope.addConditionTag(expr, scope.getConditionTag((SQLBinaryOpExpr) expr.getLeft()));
    }

    /**
     * Merge anyway if the root of WHERE clause or {@link SQLNotExpr} be reached.
     */
    private void mergeIfHaveTagAndIsRootOfWhereOrNot(Scope scope) {
        if (scope.getConditionTag(expr).isEmpty()) {
            return;
        }
        if (expr.getParent() instanceof MySqlSelectQueryBlock
            || expr.getParent() instanceof SQLNotExpr) {
            mergeNestedField(scope);
        }
    }

    private Where left() {
        return new Where((SQLBinaryOpExpr) expr.getLeft());
    }

    private Where right() {
        return new Where((SQLBinaryOpExpr) expr.getRight());
    }

    /**
     * There are 2 cases:
     * 1) For a single condition, just wrap nested() function. That's it.
     * <p>
     * BinaryOp
     * /       \
     * Identifier       Value
     * "employees.age"      "30"
     * <p>
     * to
     * <p>
     * BinaryOp
     * /       \
     * Method       Value
     * "nested"       "30"
     * |
     * Identifier
     * "employees.age"
     * <p>
     * 2) For multiple conditions, put entire BinaryOp to the parameter and add function name "nested()" first
     * <p>
     * BinaryOp (a)
     * /       \
     * BinaryOp   BinaryOp
     * |         |
     * ...       ...
     * <p>
     * to
     * <p>
     * Method
     * "nested"
     * |
     * BinaryOp (a)
     * /      \
     * ...    ...
     */
    private void mergeNestedField(Scope scope) {
        String tag = scope.getConditionTag(expr);
        if (!tag.isEmpty()) {
            if (isLeftChildCondition()) {
                replaceByNestedFunction(expr).getParameters().add(0, new SQLCharExpr(tag));
            } else {
                replaceByNestedFunction(expr.getLeft(), pathFromIdentifier(expr.getLeft()));
            }
        }
    }

}
