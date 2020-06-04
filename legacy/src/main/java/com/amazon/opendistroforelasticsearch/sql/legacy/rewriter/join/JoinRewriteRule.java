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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.join;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.mapping.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.RewriteRule;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.matchtoterm.VerificationException;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 *  Rewrite rule to add table alias to columnNames for JOIN queries without table alias.
 * <p>
 *  We use a map from columnName to tableName. This is required to remove any ambiguity
 *  while mapping fields to right table. If there is no explicit alias we create one and use that
 *  to prefix columnName.
 *
 *  Different tableName on either side of join:
 *  Case a: If columnName(without alias) present in both tables, throw error.
 *  Case b: If columnName already has some alias, and that alias is a table name,
 *          change it to explicit alias of that table.
 *  Case c: If columnName is unique to a table
 *
 *  Same tableName on either side of join:
 *  Case a: If neither has explicit alias, throw error.
 *  Case b: If any one table has explicit alias,
 *          use explicit alias of other table for columnNames with tableName as prefix. (See below example)
 * <pre>
 *       ex: SELECT table.field_a , a.field_b  | SELECT table.field_a , a.field_b
 *            FROM table a                     |  FROM table
 *             JOIN table                      |   JOIN table a
 *              ON table.field_c = a.field_d   |    ON table.field_c = a.field_d
 *
 *       both after rewrite: SELECT table_0.field_a , a.field_b
 *                            FROM table a
 *                             JOIN table table_0
 *                              ON table_0.field_c = a.field_d
 *</pre>
 * </p>
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

        // Used to handle case of same tableNames in JOIN
        final Set<String> explicitAliases = new HashSet<>();

        visitTable(root, tableExpr -> {
            // Copied from SubqueryAliasRewriter ; Removes index type name if any
            String tableName = tableExpr.getExpr().toString().replaceAll(" ", "").split("/")[0];

            if (tableExpr.getAlias() == null) {
                String alias = createAlias(tableName);
                tableExpr.setAlias(alias);
                explicitAliases.add(alias);
            }

            Table table = new Table(tableName, tableExpr.getAlias());

            tableNameToAlias.put(table.getName(), table.getAlias());

            FieldMappings fieldMappings = clusterState.getFieldMappings(
                new String[]{tableName}).firstMapping().firstMapping();
            fieldMappings.flat((fieldName, type) -> tableByFieldName.put(fieldName, table));
        });

        //Handling cases for same tableName on either side of JOIN
        if (tableNameToAlias.size() == 1) {
            String tableName = tableNameToAlias.keySet().iterator().next();
            if (explicitAliases.size() == 2) {
                // Neither table has explicit alias
                throw new VerificationException(StringUtils.format("Not unique table/alias: [%s]", tableName));
            } else if (explicitAliases.size() == 1) {
                // One table has explicit alias; use created alias for other table as alias to override fields
                // starting with actual tableName as alias to explicit alias
                tableNameToAlias.put(tableName, explicitAliases.iterator().next());
            }
        }

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