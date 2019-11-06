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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Rewrite rule for query involved JOIN.
 */
public class JoinRewriteRule implements RewriteRule<SQLQueryExpr> {

    private static final Logger logger = LogManager.getLogger();

    private static final String DOT = ".";

    private int aliasSuffix = 0;

    private final Multimap<String, Table> tableByFieldName = ArrayListMultimap.create();

    private final Set<String> tableNameAndAlias = new HashSet<>();

    private final Map<String, String> tableNameToAlias = new HashMap<>();

    private final LocalClusterState clusterState;

    public JoinRewriteRule(LocalClusterState clusterState) {
        this.clusterState = clusterState;
    }

    @Override
    public boolean match(SQLQueryExpr root) {
        // Since this Rewriter rule is executed inside JOIN block
        return true;
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

        visitTable(root, tableExpr -> {
            // Copy from SubqueryAliasRewriter, remove index type name if any
            String tableName = tableExpr.getExpr().toString().replaceAll(" ", "").split("/")[0];
            logger.warn("tableExpr.getExpr().toString():  " + tableExpr.getExpr().toString());
            logger.warn("tableName : " + tableName);
            logger.warn("===============================================");

            if (tableExpr.getAlias() == null) {
                tableExpr.setAlias(createAlias(tableName));
            }

            Table table = new Table(tableName, tableExpr.getAlias());

            tableNameAndAlias.add(table.getName());
            tableNameAndAlias.add(table.getAlias());

            tableNameToAlias.put(table.getName(), table.getAlias());

            FieldMappings fieldMappings = clusterState.getFieldMappings(
                new String[]{tableName}).firstMapping().firstMapping();
            fieldMappings.flat((fieldName, type) -> tableByFieldName.put(fieldName, table));
        });

        logger.warn("multimap: " + tableByFieldName);
        logger.warn("===============================================");
        logger.warn("tableNameAndAlias set: " + tableNameAndAlias);
        logger.warn("===============================================");
        logger.warn("tableNameToAlias map: " + tableNameToAlias);


        visitColumnName(root, idExpr -> {
            String columnName = idExpr.getName(); // TODO: Assume field doesn't have alias or table name prefix.
            Collection<Table> tables = tableByFieldName.get(columnName);
            if (tables.size() > 1) {
                throw new VerificationException(StringUtils.format(
                    "Field name [%s] is ambiguous", columnName));
            } else if (tables.isEmpty()) {
                // size() == 0, either the columnName does not exist or columnName starts with alias
                // if columnName starts with some alias, do nothing else columnName does not exist
                if (!startsWithAlias(columnName, tableNameAndAlias)) {
                    throw new VerificationException(StringUtils.format(
                        "Field name [%s] does not exist in either table", columnName));
                }
            } else {
                // fieldname is unique to one table
                Table table = tables.iterator().next();
                String tableAlias = table.getAlias();
                String tableName = table.getName();

                // Copy
                if (columnName.startsWith(tableName + DOT) || columnName.startsWith(tableAlias + DOT)) {
                    // TODO: do we need this condition?
                    idExpr.setName(columnName.replace(tableName + DOT, tableAlias + DOT));
                } else {
                    idExpr.setName(String.join(DOT, tableAlias, columnName));
                }
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
                return false; // Avoid rewriting identifier in table name
            }

            @Override
            public void endVisit(SQLIdentifierExpr idExpr) {
                visit.accept(idExpr);
            }
        });
    }


    private boolean startsWithAlias(String columnName, Collection<String> aliases) {
        boolean b = aliases.stream().map(c -> c + DOT).anyMatch(columnName::startsWith);
        logger.warn(columnName + " startsWithAlias: " + b);
        return b;
    }

    // All copy-pasted
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

        @Override
        public String toString() {
            return this.name + ":" + this.alias;
        }
    }
}