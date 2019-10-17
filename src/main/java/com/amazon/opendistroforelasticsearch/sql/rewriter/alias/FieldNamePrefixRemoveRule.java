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

package com.amazon.opendistroforelasticsearch.sql.rewriter.alias;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Un-alias field name if it's using full table name as prefix.
 */
public class FieldNamePrefixRemoveRule implements RewriteRule<SQLQueryExpr> {

    /** Table names without alias in FROM clause */
    private final Set<String> unAliasedTableNames = new HashSet<>();

    @Override
    public boolean match(SQLQueryExpr root) {
        collectUnAliasedTableNames(root);
        return !unAliasedTableNames.isEmpty();
    }

    @Override
    public void rewrite(SQLQueryExpr root) {
        removeUnAliasedTableNamePrefix(root);
    }

    private void collectUnAliasedTableNames(SQLQueryExpr root) {
        visitTable(root, tableExpr -> {
            Table table = new Table(tableExpr);
            if (table.isNotAliased()) {
                unAliasedTableNames.add(table.name());
            }
        });
    }

    private void removeUnAliasedTableNamePrefix(SQLQueryExpr root) {
        visitIdentifier(root, idExpr -> {
            Identifier field = new Identifier(idExpr);
            if (field.hasPrefix() && unAliasedTableNames.contains(field.prefix())) {
                field.removePrefix();
            }
        });
    }

    private void visitTable(SQLQueryExpr root,
                            Consumer<SQLExprTableSource> visit) {
        root.accept(new MySqlASTVisitorAdapter() {
            @Override
            public void endVisit(SQLExprTableSource tableExpr) {
                visit.accept(tableExpr);
            }
        });
    }

    private void visitIdentifier(SQLQueryExpr expr,
                                 Consumer<SQLIdentifierExpr> visit) {
        expr.accept(new MySqlASTVisitorAdapter() {
            @Override
            public void endVisit(SQLIdentifierExpr idExpr) {
                visit.accept(idExpr);
            }
        });
    }

}
