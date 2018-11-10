package org.nlpcn.es4sql.nestedfield.rewrite;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
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
     *
     * Assumption: there are only 2 forms of condition
     *              1) BinaryOp: Left=Identifier, right=value
     *              2) BinaryOp: Left=BinaryOp, right=BinaryOp
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
        mergeIfHaveTagAndIsRootOfWhere(scope);
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

    /** Merge anyway if the root of WHERE clause be reached */
    private void mergeIfHaveTagAndIsRootOfWhere(Scope scope) {
        if (!scope.getConditionTag(expr).isEmpty() &&
                expr.getParent() instanceof MySqlSelectQueryBlock) {
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
     *  1) For a single condition, just wrap nested() function. That's it.
     *
     *              BinaryOp
     *              /       \
     *     Identifier       Value
     *  "employees.age"      "30"
     *
     *                to
     *
     *              BinaryOp
     *              /       \
     *         Method       Value
     *        "nested"       "30"
     *          |
     *     Identifier
     *  "employees.age"
     *
     *  2) For multiple conditions, put entire BinaryOp to the parameter and add function name "nested()" first
     *
     *              BinaryOp (a)
     *             /       \
     *         BinaryOp   BinaryOp
     *            |         |
     *           ...       ...
     *
     *                 to
     *
     *               Method
     *              "nested"
     *                 |
     *               BinaryOp (a)
     *               /      \
     *              ...    ...
     */
    private void mergeNestedField(Scope scope) {
        String tag = scope.getConditionTag(expr);
        if (!tag.isEmpty()) {
            if (isLeftChildCondition()) {
                replaceByNestedFunction(expr).getParameters().add(0, new SQLCharExpr(tag));
            } else {
                replaceByNestedFunction(expr.getLeft());
            }
        }
    }

}
