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
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;

public class SubQueryRewriter {

    public static void convert(SQLSelect query, BlackBoard bb) {
        SQLSelectQuery queryExpr = query.getQuery();
        if (queryExpr instanceof MySqlSelectQueryBlock) {
            MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) queryExpr;
            bb.addTable(queryBlock.getFrom());

            queryBlock.setWhere(convertWhere(queryBlock.getWhere(), bb));
            queryBlock.setFrom(convertFrom(queryBlock.getFrom(), bb));
        }
    }

    private static SQLTableSource convertFrom(SQLTableSource expr, BlackBoard bb) {
        SQLTableSource join = bb.popJoin();
        if (join != null) {
            return join;
        }
        return expr;
    }

    private static SQLExpr convertWhere(SQLExpr expr, BlackBoard bb) {
        if (expr instanceof SQLExistsExpr) {
            new Exists((SQLExistsExpr) expr, bb).rewrite();
            return bb.popWhere();
        } else if (expr instanceof SQLInSubQueryExpr) {
            new In((SQLInSubQueryExpr) expr, bb).rewrite();
            return bb.popWhere();
        } else if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) expr;
            binaryOpExpr.setLeft(convertWhere(binaryOpExpr.getLeft(), bb));
            binaryOpExpr.setRight(convertWhere(binaryOpExpr.getRight(), bb));
        }
        return expr;
    }
}
