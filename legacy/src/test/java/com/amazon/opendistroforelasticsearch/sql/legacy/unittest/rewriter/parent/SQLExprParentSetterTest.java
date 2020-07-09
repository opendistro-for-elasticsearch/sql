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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.rewriter.parent;

import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.SqlParserUtils;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SQLExprParentSetterTest {

    @Test
    public void testSQLInSubQueryExprHasParent() {
        String sql =
                "SELECT * FROM TbA " +
                "WHERE a IN (SELECT b FROM TbB)";
        SQLQueryExpr expr = SqlParserUtils.parse(sql);
        expr.accept(new SQLExprParentExistsValidator());
    }

    @Test
    public void testSQLInListExprHasParent() {
        String sql =
                "SELECT * FROM TbA " +
                "WHERE a IN (10, 20)";
        SQLQueryExpr expr = SqlParserUtils.parse(sql);
        expr.accept(new SQLExprParentExistsValidator());
    }

    static class SQLExprParentExistsValidator extends MySqlASTVisitorAdapter {
        @Override
        public boolean visit(SQLInSubQueryExpr expr) {
            assertNotNull(expr.getExpr().getParent());
            return true;
        }

        @Override
        public boolean visit(SQLInListExpr expr) {
            assertNotNull(expr.getExpr().getParent());
            return true;
        }
    }

}