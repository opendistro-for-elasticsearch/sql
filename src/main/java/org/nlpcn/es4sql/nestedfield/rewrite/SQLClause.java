package org.nlpcn.es4sql.nestedfield.rewrite;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;

import java.util.List;

/**
 * Abstract class for SQL clause domain class.
 *
 * @param <T>   concrete type of clause
 */
abstract class SQLClause<T> {

    protected final T expr;

    SQLClause(T expr) {
        this.expr = expr;
    }

    /**
     * Rewrite nested fields in query according to/fill into information in scope.
     * @param scope     Scope of current query
     */
    abstract void rewrite(Scope scope);

    /** Replace expr by nested(expr) and set pointer in parent properly */
    SQLMethodInvokeExpr replaceByNestedFunction(SQLExpr expr) {
        SQLObject parent = expr.getParent();
        SQLMethodInvokeExpr nestedFunc = wrapNestedFunction(expr);
        if (parent instanceof SQLAggregateExpr) {
            List<SQLExpr> args = ((SQLAggregateExpr) parent).getArguments();
            args.set(args.indexOf(expr), nestedFunc);
        }
        else if (parent instanceof SQLSelectItem) {
            ((SQLSelectItem) parent).setExpr(nestedFunc);
        }
        else if (parent instanceof MySqlSelectGroupByExpr) {
            ((MySqlSelectGroupByExpr) parent).setExpr(nestedFunc);
        }
        else if (parent instanceof SQLSelectOrderByItem) {
            ((SQLSelectOrderByItem) parent).setExpr(nestedFunc);
        }
        else if (parent instanceof SQLInSubQueryExpr) {
            ((SQLInSubQueryExpr) parent).setExpr(nestedFunc);
        }
        else if (parent instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr parentOp = (SQLBinaryOpExpr) parent;
            if (parentOp.getLeft() == expr) {
                parentOp.setLeft(nestedFunc);
            } else {
                parentOp.setRight(nestedFunc);
            }
        }
        else if (parent instanceof MySqlSelectQueryBlock) {
            ((MySqlSelectQueryBlock) parent).setWhere(nestedFunc);
        }
        else {
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

}
