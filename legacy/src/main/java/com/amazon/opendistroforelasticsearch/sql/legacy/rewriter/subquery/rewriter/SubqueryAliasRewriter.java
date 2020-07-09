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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.rewriter;

import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Add the alias for identifier the subquery query.
 * Use the table alias if it already has one, Auto generate if it doesn't has one.
 * <p>
 * The following table demonstrate how the rewriter works with scope and query.
 * +-----------------------+-------------+-----------------------------------------------------------------------------------------------------+
 * | Rewrite               | TableScope  | Query                                                                                               |
 * +-----------------------+-------------+-----------------------------------------------------------------------------------------------------+
 * | (Start)               | ()          | SELECT * FROM TbA WHERE a IN (SELECT b FROM TbB) and c > 10                                         |
 * +-----------------------+-------------+-----------------------------------------------------------------------------------------------------+
 * | MySqlSelectQueryBlock | (TbA,TbA_0) | SELECT * FROM TbA as TbA_0 WHERE a IN (SELECT b FROM TbB) and c > 10                                |
 * +-----------------------+-------------+-----------------------------------------------------------------------------------------------------+
 * | Identifier in Select  | (TbA,TbA_0) | SELECT TbA.* FROM TbA as TbA_0 WHERE a IN (SELECT b FROM TbB) and c > 10                            |
 * +-----------------------+-------------+-----------------------------------------------------------------------------------------------------+
 * | Identifier in Where   | (TbA,TbA_0) | SELECT TbA.* FROM TbA as TbA_0 WHERE TbA_0.a IN (SELECT b FROM TbB) and TbA_0.c > 10                |
 * +-----------------------+-------------+-----------------------------------------------------------------------------------------------------+
 * | MySqlSelectQueryBlock | (TbA,TbA_0) | SELECT TbA.* FROM TbA as TbA_0 WHERE TbA_0.a IN (SELECT b FROM TbB as TbB_0) and TbA_0.c > 10       |
 * |                       | (TbB,TbB_0) |                                                                                                     |
 * +-----------------------+-------------+-----------------------------------------------------------------------------------------------------+
 * | Identifier in Select  | (TbA,TbA_0) | SELECT TbA.* FROM TbA as TbA_0 WHERE TbA_0.a IN (SELECT TbB_0.b FROM TbB as TbB_0) and TbA_0.c > 10 |
 * |                       | (TbB,TbB_0) |                                                                                                     |
 * +-----------------------+-------------+-----------------------------------------------------------------------------------------------------+
 */
public class SubqueryAliasRewriter extends MySqlASTVisitorAdapter {
    private final Deque<Table> tableScope = new ArrayDeque<>();
    private int aliasSuffix = 0;
    private static final String DOT = ".";

    @Override
    public boolean visit(MySqlSelectQueryBlock query) {
        SQLTableSource from = query.getFrom();
        if (from instanceof SQLExprTableSource) {
            SQLExprTableSource expr = (SQLExprTableSource) from;
            String tableName = expr.getExpr().toString().replaceAll(" ", "");

            if (expr.getAlias() != null) {
                tableScope.push(new Table(tableName, expr.getAlias()));
            } else {
                expr.setAlias(createAlias(tableName));
                tableScope.push(new Table(tableName, expr.getAlias()));
            }
        }
        return true;
    }

    @Override
    public boolean visit(SQLIdentifierExpr expr) {
        if (!tableScope.isEmpty() && (inSelect(expr) || inWhere(expr) || inSubquery(expr))) {
            rewrite(tableScope.peek(), expr);
        }
        return true;
    }

    @Override
    public boolean visit(SQLAllColumnExpr expr) {
        if (!tableScope.isEmpty() && inSelect(expr)) {
            ((SQLSelectItem) expr.getParent()).setExpr(createIdentifierExpr(tableScope.peek()));
        }
        return true;
    }

    private boolean inSelect(SQLIdentifierExpr expr) {
        return expr.getParent() instanceof SQLSelectItem;
    }

    private boolean inSelect(SQLAllColumnExpr expr) {
        return expr.getParent() instanceof SQLSelectItem;
    }

    private boolean inWhere(SQLIdentifierExpr expr) {
        return expr.getParent() instanceof SQLBinaryOpExpr && !isESTable((SQLBinaryOpExpr) expr.getParent());
    }

    /**
     * The table name in elasticsearch could be "index/type". Which represent as SQLBinaryOpExpr in AST.
     */
    private boolean isESTable(SQLBinaryOpExpr expr) {
        return expr.getOperator() == SQLBinaryOperator.Divide && expr.getParent() instanceof SQLExprTableSource;
    }

    private boolean inSubquery(SQLIdentifierExpr expr) {
        return expr.getParent() instanceof SQLInSubQueryExpr;
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock query) {
        if (!tableScope.isEmpty()) {
            tableScope.pop();
        }
    }

    private void rewrite(Table table, SQLIdentifierExpr expr) {
        String tableAlias = table.getAlias();
        String tableName = table.getName();

        String exprName = expr.getName();
        if (exprName.startsWith(tableName + DOT) || exprName.startsWith(tableAlias + DOT)) {
            expr.setName(exprName.replace(tableName + DOT, tableAlias + DOT));
        } else {
            expr.setName(String.join(DOT, tableAlias, exprName));
        }
    }

    private SQLIdentifierExpr createIdentifierExpr(Table table) {
        String newIdentifierName = String.join(DOT, table.getAlias(), "*");
        return new SQLIdentifierExpr(newIdentifierName);
    }

    private String createAlias(String alias) {
        return String.format("%s_%d", alias, next());
    }

    private Integer next() {
        return aliasSuffix++;
    }

    /**
     * Table Bean.
     */
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
    }
}
