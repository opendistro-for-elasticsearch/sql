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

package com.amazon.opendistroforelasticsearch.sql.rewriter.subquery;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.utils.NestedQueryDetector;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Environment for rewriting the SQL.
 */
public class BlackBoard {
    private Deque<SQLTableSource> tableStack = new ArrayDeque<>();
    private Deque<SQLBinaryOpExpr> binaryOpStack = new ArrayDeque<>();
    private NestedQueryDetector nestedQueryDetector;

    public BlackBoard(NestedQueryDetector nestedQueryDetector) {
        this.nestedQueryDetector = nestedQueryDetector;
    }

    public SQLTableSource popJoin() {
        return tableStack.pop();
    }

    public SQLExpr popWhere() {
        return binaryOpStack.pop();
    }

    public void addWhere(SQLBinaryOpExpr expr) {
        binaryOpStack.push(expr);
    }

    /**
     * Add the Join right table and {@link JoinType} and {@link SQLBinaryOpExpr} which will
     * merge the left table in the tableStack.
     */
    public void addJoin(SQLTableSource right, JoinType joinType, SQLBinaryOpExpr condition) {
        SQLTableSource left = tableStack.pop();
        SQLJoinTableSource joinTableSource = new SQLJoinTableSource();
        joinTableSource.setLeft(left);
        joinTableSource.setRight(right);
        joinTableSource.setJoinType(joinType);
        joinTableSource.setCondition(condition);
        tableStack.push(joinTableSource);
    }

    public void addJoin(SQLTableSource right, JoinType joinType) {
        addJoin(right, joinType, null);
    }

    public void addTable(SQLTableSource table) {
        tableStack.push(table);
    }

    public boolean isNestedQuery(SQLExprTableSource table) {
        return nestedQueryDetector.isNested(table);
    }
}
