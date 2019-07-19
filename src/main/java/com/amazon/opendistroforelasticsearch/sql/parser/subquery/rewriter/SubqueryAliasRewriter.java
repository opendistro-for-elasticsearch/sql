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

package com.amazon.opendistroforelasticsearch.sql.parser.subquery.rewriter;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Add the alias for identifier the subquery query.
 * Use the table alias if it already has one, Auto generate if it doesn't has one.
 */
public class SubqueryAliasRewriter extends MySqlASTVisitorAdapter {

    private Deque<Table> tableScope =  new ArrayDeque<>();

    private int aliasSuffix = 0;

    private final static String DOT = ".";

    @Override
    public boolean visit(MySqlSelectQueryBlock query) {
        final SQLExprTableSource from = (SQLExprTableSource) query.getFrom();
        String tableName = from.getExpr().toString().replaceAll(" ", "");

        if (from.getAlias() != null) {
            tableScope.push(new Table(tableName, from.getAlias()));
        } else {
            from.setAlias(createAlias(tableName));
            tableScope.push(new Table(tableName, from.getAlias()));
        }

        for (SQLSelectItem item : query.getSelectList()) {
            final SQLExpr itemExpr = item.getExpr();
            if (itemExpr instanceof SQLIdentifierExpr) {
                rewrite(tableScope.peek(), (SQLIdentifierExpr)itemExpr);
            } else if (itemExpr instanceof SQLAllColumnExpr){
                item.setExpr(createIdentifierExpr(tableScope.peek(), "*"));
            }
        }

        return true;
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock query) {
        tableScope.pop();
    }

    @Override
    public void endVisit(SQLBinaryOpExpr expr) {
        // ignore the es table name, index/account
        if (expr.getOperator() == SQLBinaryOperator.Divide && expr.getParent() instanceof SQLExprTableSource) {
            return;
        }

        if (expr.getLeft() instanceof SQLIdentifierExpr) {
            rewrite(tableScope.peek(), (SQLIdentifierExpr)expr.getLeft());
        }
        if (expr.getRight() instanceof SQLIdentifierExpr) {
            rewrite(tableScope.peek(), (SQLIdentifierExpr)expr.getRight());
        }
    }

    @Override
    public void endVisit(SQLInSubQueryExpr expr) {
        if (expr.getExpr() instanceof SQLIdentifierExpr) {
            rewrite(tableScope.peek(), (SQLIdentifierExpr)expr.getExpr());
        }
    }

    private void rewrite(Table table, SQLIdentifierExpr expr) {
        final String tableAlias = table.getAlias();
        final String tableName = table.getName();

        final String exprName = expr.getName();
        if (exprName.startsWith(tableName + DOT) || exprName.startsWith(tableAlias + DOT)) {
            expr.setName(exprName.replace(tableName + DOT, tableAlias+DOT));
        } else {
            expr.setName(String.join(DOT, tableAlias, exprName));
        }
    }

    private SQLIdentifierExpr createIdentifierExpr(Table table, String name) {
        final String newIdentifierName = String.join(DOT, table.getAlias(), name);
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

        public Table(String name, String alias) {
            this.name = name;
            this.alias = alias;
        }
    }
}
