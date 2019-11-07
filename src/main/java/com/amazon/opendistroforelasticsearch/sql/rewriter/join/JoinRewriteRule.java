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

package com.amazon.opendistroforelasticsearch.sql.rewriter.join;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;
import com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm.VerificationException;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Rewrite rule for query involved JOIN.
 */
public class JoinRewriteRule implements RewriteRule<SQLQueryExpr> {

    private static final String DOT = ".";
    private int aliasSuffix = 0;
    private final LocalClusterState clusterState;

    public JoinRewriteRule(LocalClusterState clusterState) {
        this.clusterState = clusterState;
    }

    @Override
    public boolean match(SQLQueryExpr root) {
        return isJoin(root);
    }

    private boolean isJoin(SQLQueryExpr sqlExpr) {
        SQLSelectQuery sqlSelectQuery = sqlExpr.getSubQuery().getQuery();

        if (!(sqlSelectQuery instanceof MySqlSelectQueryBlock)) {
            return false;
        }

        MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) sqlSelectQuery;
        return query.getFrom() instanceof SQLJoinTableSource
            && ((SQLJoinTableSource) query.getFrom()).getJoinType() != SQLJoinTableSource.JoinType.COMMA;
    }

    @Override
    public void rewrite(SQLQueryExpr root) {

        final Multimap<String, Table> tableByFieldName = ArrayListMultimap.create();
        final Map<String, String> tableNameToAlias = new HashMap<>();

        visitTable(root, tableExpr -> {
            // Copied from SubqueryAliasRewriter ; Removes index type name if any
            String tableName = tableExpr.getExpr().toString().replaceAll(" ", "").split("/")[0];

            if (tableExpr.getAlias() == null) {
                // Could we not directly use table name as alias?
                tableExpr.setAlias(createAlias(tableName));
            }

            Table table = new Table(tableName, tableExpr.getAlias());

            tableNameToAlias.put(table.getName(), table.getAlias());

            FieldMappings fieldMappings = clusterState.getFieldMappings(
                new String[]{tableName}).firstMapping().firstMapping();
            fieldMappings.flat((fieldName, type) -> tableByFieldName.put(fieldName, table));
        });

        visitColumnName(root, idExpr -> {
            String columnName = idExpr.getName();
            Collection<Table> tables = tableByFieldName.get(columnName);
            if (tables.size() > 1) {
                // columnName without alias present in both tables
                throw new VerificationException(StringUtils.format("Field name [%s] is ambiguous", columnName));
            } else if (tables.isEmpty()) {
                // size() == 0?
                // 1. Either the columnName does not exist (handled by SemanticAnalyzer [SemanticAnalysisException])
                // 2. Or column starts with tableName as alias or explicit alias
                //    If starts with tableName as alias change to explicit alias
                tableNameToAlias.keySet().stream().forEach(tableName -> {
                    if (columnName.startsWith(tableName + DOT)) {
                        idExpr.setName(columnName.replace(tableName + DOT, tableNameToAlias.get(tableName) + DOT));
                    }
                });
            } else {
                // columnName with any alias and unique to one table
                Table table = tables.iterator().next();
                idExpr.setName(String.join(DOT, table.getAlias(), columnName));
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

    private void visitColumnName(SQLQueryExpr expr,
                                 Consumer<SQLIdentifierExpr> visit) {
        expr.accept(new MySqlASTVisitorAdapter() {
            @Override
            public boolean visit(SQLExprTableSource x) {
                // Avoid rewriting identifier in table name
                return false;
            }

            @Override
            public void endVisit(SQLIdentifierExpr idExpr) {
                visit.accept(idExpr);
            }
        });
    }

    private String createAlias(String alias) {
        return String.format("%s_%d", alias, next());
    }

    private Integer next() {
        return aliasSuffix++;
    }

    private static class Table {

        public String getName() {
            return name;
        }

        public String getAlias() {
            return alias;
        }

        /**
         * Table Name.
         */
        private String name;

        /**
         * Table Alias.
         */
        private String alias;

        Table(String name, String alias) {
            this.name = name;
            this.alias = alias;
        }

        // Added for debugging
        @Override
        public String toString() {
            return this.name + "-->" + this.alias;
        }
    }
}