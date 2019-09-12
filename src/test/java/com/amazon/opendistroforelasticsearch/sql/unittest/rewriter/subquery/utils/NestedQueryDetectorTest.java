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

package com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.subquery.utils;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.utils.NestedQueryDetector;
import com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class NestedQueryDetectorTest {

    @Test
    public void isNested() {
        SQLQueryExpr expr = SqlParserUtils.parse(
                "SELECT e.name " +
                "FROM employee as e, e.projects as p " +
                "WHERE EXISTS (SELECT * FROM p)");
        NestedQueryDetector nestedQueryDetector = new NestedQueryDetector();
        expr.accept(nestedQueryDetector);

        assertTrue(nestedQueryDetector.isNested(new SQLExprTableSource(new SQLIdentifierExpr("e.projects"), "p")));
        assertTrue(nestedQueryDetector.isNested(new SQLExprTableSource(new SQLIdentifierExpr("p"))));
    }

    @Test
    public void hasNestedQuery() {
        SQLQueryExpr expr = SqlParserUtils.parse(
                "SELECT e.name " +
                "FROM employee as e, e.projects as p " +
                "WHERE EXISTS (SELECT * FROM p)");
        NestedQueryDetector nestedQueryDetector = new NestedQueryDetector();
        expr.accept(nestedQueryDetector);

        assertTrue(nestedQueryDetector.hasNestedQuery());
    }

    @Test
    public void notNested() {
        SQLQueryExpr expr = SqlParserUtils.parse(
                "SELECT e.name " +
                "FROM employee as e, projects as p " +
                "WHERE EXISTS (SELECT * FROM p)");
        NestedQueryDetector nestedQueryDetector = new NestedQueryDetector();
        expr.accept(nestedQueryDetector);

        assertFalse(nestedQueryDetector.isNested(new SQLExprTableSource(new SQLIdentifierExpr("e.projects"), "p")));
        assertFalse(nestedQueryDetector.isNested(new SQLExprTableSource(new SQLIdentifierExpr("p"))));
    }

    @Test
    public void noNestedQuery() {
        SQLQueryExpr expr = SqlParserUtils.parse(
                "SELECT e.name " +
                "FROM employee as e, projects as p " +
                "WHERE EXISTS (SELECT * FROM p)");
        NestedQueryDetector nestedQueryDetector = new NestedQueryDetector();
        expr.accept(nestedQueryDetector);

        assertFalse(nestedQueryDetector.hasNestedQuery());
    }
}