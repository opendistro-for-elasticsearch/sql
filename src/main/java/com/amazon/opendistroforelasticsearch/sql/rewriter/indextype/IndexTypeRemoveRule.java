package com.amazon.opendistroforelasticsearch.sql.rewriter.indextype;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Ignore and remove index type name.
 */
public class IndexTypeRemoveRule extends MySqlASTVisitorAdapter implements RewriteRule<SQLQueryExpr> {

    private Map<SQLIdentifierExpr, SQLExprTableSource> indexAndTypeNameExpr = new HashMap<>();

    private Stack<SQLExprTableSource> ancestors = new Stack<>();

    @Override
    public boolean match(SQLQueryExpr expr) {
        expr.accept(this);
        return !indexAndTypeNameExpr.isEmpty();
    }

    @Override
    public void rewrite(SQLQueryExpr expr) {
        indexAndTypeNameExpr.forEach((indexNameExpr, parentExpr) -> parentExpr.setExpr(indexNameExpr));
    }

    @Override
    public boolean visit(SQLExprTableSource expr) {
        ancestors.add(expr);
        return super.visit(expr);
    }

    @Override
    public void endVisit(SQLExprTableSource expr) {
        if (ancestors.isEmpty()) {
            throw new IllegalStateException("Cannot pop lowest ancestor from stack because it is empty");
        }
        ancestors.pop();
    }

    @Override
    public boolean visit(SQLBinaryOpExpr expr) {
        // Make sure we only touch slash (divide) in FROM, ex. cases like SELECT, WHERE, FROM(SELECT...)
        // Rather than the real division in other places.
        if (isInFromClause() && isIdentifierSlashIdentifier(expr)) {
            indexAndTypeNameExpr.put((SQLIdentifierExpr) expr.getLeft(), ancestors.peek());
        }
        return false;
    }

    private boolean isInFromClause() {
        return !ancestors.isEmpty();
    }

    private boolean isIdentifierSlashIdentifier(SQLBinaryOpExpr expr) {
        return expr.getOperator() == SQLBinaryOperator.Divide
            && expr.getLeft() instanceof SQLIdentifierExpr
            && expr.getRight() instanceof SQLIdentifierExpr;
    }
}
