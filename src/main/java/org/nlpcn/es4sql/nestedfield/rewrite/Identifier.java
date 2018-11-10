package org.nlpcn.es4sql.nestedfield.rewrite;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;

/**
 * Identifier expression in SELECT, FROM, WHERE, GROUP BY, ORDER BY etc.
 *
 * Ex. To make concepts clear, for "e.firstname AND t.region" in "FROM team t, t.employees e":
 *  parent alias (to erase): 't'
 *  path:                    'e' (full path saved in Scope is 'employees')
 *  name:                    'firstname'
 */
class Identifier extends SQLClause<SQLIdentifierExpr> {

    private static final String SEPARATOR = ".";

    Identifier(SQLIdentifierExpr expr) {
        super(expr);
    }

    /**
     * Erase parent alias for all identifiers but only rewrite those (nested field identifier) NOT in WHERE.
     * For identifier in conditions in WHERE, use full path as tag and delay the rewrite in Where.rewrite().
     */
    @Override
    void rewrite(Scope scope) {
        eraseParentAlias(scope);
        if (isNestedField(scope)) {
            renameByFullPath(scope);
            if (isInCondition()) {
                useFullPathAsTag(scope);
            } else {
                replaceByNestedFunction(expr);
            }
        }
    }

    String path() {
        return separatorIndex() == -1 ? "" : expr.getName().substring(0, separatorIndex());
    }

    String name() {
        return expr.getName().substring(separatorIndex() + 1);
    }

    private int separatorIndex() {
        return expr.getName().indexOf(SEPARATOR);
    }

    /**
     * Erase parent alias otherwise it's required to specify it everywhere even on nested field (which NLPchina has problem with).
     * Sample: "FROM team t, t.employees e WHERE t.region = 'US'" => "WHERE region = 'US'"
     */
    private void eraseParentAlias(Scope scope) {
        if (isStartWithParentAlias(scope)) {
            expr.setName(name());
        }
    }

    private boolean isStartWithParentAlias(Scope scope) {
        return path().equals(scope.getParentAlias());
    }

    private boolean isNestedField(Scope scope) {
        return !scope.getFullPath(path()).isEmpty();
    }

    private void renameByFullPath(Scope scope) {
        String fullPath = scope.getFullPath(path());
        if (fullPath.isEmpty()) {
            throw new IllegalStateException("Full path not found for identifier:" + expr.getName());
        }
        expr.setName(fullPath + SEPARATOR + name());
    }

    private void useFullPathAsTag(Scope scope) {
        scope.addConditionTag((SQLBinaryOpExpr) expr.getParent(), path());
    }

    private boolean isInCondition() {
        return expr.getParent() instanceof SQLBinaryOpExpr;
    }

}
