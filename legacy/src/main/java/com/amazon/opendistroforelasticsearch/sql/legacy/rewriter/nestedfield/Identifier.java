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
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;

/**
 * Identifier expression in SELECT, FROM, WHERE, GROUP BY, ORDER BY etc.
 * <p>
 * Ex. To make concepts clear, for "e.firstname AND t.region" in "FROM team t, t.employees e":
 * parent alias (to erase): 't'
 * path:                    'e' (full path saved in Scope is 'employees')
 * name:                    'firstname'
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
                replaceByNestedFunction(expr, pathFromIdentifier(expr));
            }
        }
    }

    /**
     * return the path of the expr name. e.g.
     * expecting p returned as path in both WHERE p.name = 'A' and WHERE p IS NULL cases,
     * in which expr.name = p.name and p separately
     */
    String path() {
        return separatorIndex() == -1 ? expr.getName() : expr.getName().substring(0, separatorIndex());
    }

    String name() {
        return expr.getName().substring(separatorIndex() + 1);
    }

    private int separatorIndex() {
        return expr.getName().indexOf(SEPARATOR);
    }

    /**
     * Erase parent alias otherwise it's required to specify it everywhere even on nested
     * field (which NLPchina has problem with).
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
        expr.setName(expr.getName().replaceFirst(path(), fullPath));
    }

    private void useFullPathAsTag(Scope scope) {
        scope.addConditionTag((SQLBinaryOpExpr) expr.getParent(), path());
    }

    private boolean isInCondition() {
        return expr.getParent() instanceof SQLBinaryOpExpr;
    }

}
