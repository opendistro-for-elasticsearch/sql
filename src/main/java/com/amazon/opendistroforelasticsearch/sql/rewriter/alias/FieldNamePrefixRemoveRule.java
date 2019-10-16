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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;
import com.google.common.base.Strings;

import java.util.HashSet;
import java.util.Set;

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
        root.accept(new MySqlASTVisitorAdapter() {
            @Override
            public void endVisit(SQLIdentifierExpr idExpr) {
                Identifier field = new Identifier(idExpr);
                if (unAliasedTableNames.contains(field.prefix())) {
                    field.removePrefix();
                }
            }
        });
    }

    private void collectUnAliasedTableNames(SQLQueryExpr expr) {
        expr.accept(new MySqlASTVisitorAdapter() {
            @Override
            public boolean visit(SQLExprTableSource tableExpr) {
                Table table = new Table(tableExpr);
                if (table.isUnAliased()) {
                    unAliasedTableNames.add(table.name());
                }
                return true;
            }
        });
    }

    private static class Table {

        private final SQLExprTableSource tableExpr;

        private Table(SQLExprTableSource tableExpr) {
            this.tableExpr = tableExpr;
        }

        public boolean isUnAliased() {
            return Strings.isNullOrEmpty(tableExpr.getAlias());
        }

        public String name() {
            SQLExpr expr = tableExpr.getExpr();
            if (expr instanceof SQLIdentifierExpr) {
                return ((SQLIdentifierExpr) expr).getName();
            }
            return expr.toString();
        }
    }

    private static class Identifier {

        private final SQLIdentifierExpr idExpr;

        private Identifier(SQLIdentifierExpr idExpr) {
            this.idExpr = idExpr;
        }

        public String name() {
            return idExpr.getName();
        }

        public String prefix() {
            int firstDotIndex = name().indexOf('.');
            if (firstDotIndex != -1) {
                return name().substring(0, firstDotIndex);
            }
            return "";
        }

        public void removePrefix() {
            idExpr.setName(name().substring(prefix().length() + 1));
        }
    }

}
