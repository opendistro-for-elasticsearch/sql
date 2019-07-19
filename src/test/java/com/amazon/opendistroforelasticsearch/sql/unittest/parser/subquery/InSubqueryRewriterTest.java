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

package com.amazon.opendistroforelasticsearch.sql.unittest.parser.subquery;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.SubqueryRewriterHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class InSubqueryRewriterTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testFrom() {
        assertEquals(
                sqlString(expectFrom(
                        "SELECT TbA_0.* FROM TbA as TbA_0 JOIN TbB as TbB_1 ON TbA_0.a = TbB_1.b WHERE TbB_1.b IS NOT NULL")),
                sqlString(actualFrom(
                        "SELECT * FROM TbA WHERE a in (SELECT b FROM TbB)"))
        );
    }

    @Test
    public void testWhere() {
        assertEquals(
                sqlString(expectWhere(
                        "SELECT TbA_0.* FROM TbA as TbA_0 JOIN TbB as TbB_1 ON TbA_0.a = TbB_1.b WHERE TbB_1.b IS NOT NULL")),
                sqlString(actualWhere(
                        "SELECT * FROM TbA WHERE a in (SELECT b FROM TbB)"))
        );
    }

    @Test
    public void testFromWithInnerWhere() {
        assertEquals(
                sqlString(expectFrom(
                        "SELECT TbA_0.* FROM TbA as TbA_0 JOIN TbB as TbB_1 ON TbA_0.a = TbB_1.b WHERE TbB_1.b IS NOT NULL AND TbB_1.b > 0")),
                sqlString(actualFrom(
                        "SELECT * FROM TbA WHERE a in (SELECT b FROM TbB WHERE b > 0)"))
        );
    }

    @Test
    public void testWhereWithInnerWhere() {
        assertEquals(
                sqlString(expectWhere(
                        "SELECT TbA_0.* FROM TbA as TbA_0 JOIN TbB as TbB_1 ON TbA_0.a = TbB_1.b WHERE TbB_1.b IS NOT NULL AND TbB_1.b > 0")),
                sqlString(actualWhere(
                        "SELECT * FROM TbA WHERE a in (SELECT b FROM TbB WHERE b > 0)"))
        );
    }

    @Test
    public void testFromWithOuterWhere() {
        assertEquals(
                sqlString(expectFrom(
                        "SELECT TbA_0.* FROM TbA as TbA_0 JOIN TbB as TbB_1 ON TbA_0.a = TbB_1.b WHERE TbB_1.b IS NOT NULL AND TbA_0.a > 10")),
                sqlString(actualFrom(
                        "SELECT * FROM TbA WHERE a in (SELECT b FROM TbB) AND a > 10"))
        );
    }

    @Test
    public void testWhereWithOuterWhere() {
        assertEquals(
                sqlString(expectWhere(
                        "SELECT TbA_0.* FROM TbA as TbA_0 JOIN TbB as TbB_1 ON TbA_0.a = TbB_1.b WHERE TbB_1.b IS NOT NULL AND TbA_0.a > 10")),
                sqlString(actualWhere(
                        "SELECT * FROM TbA WHERE a in (SELECT b FROM TbB) AND a > 10"))
        );
    }

    @Test
    public void testException() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Unsupported subquery with multiple select [TbB_1.b1, TbB_1.b2]");
        actualFrom("SELECT * FROM TbA WHERE a in (SELECT b1, b2 FROM TbB) AND a > 10");
    }

    private String sqlString(SQLObject expr) {
        return SQLUtils.toMySqlString(expr).replace("\t", "");
    }

    private SQLTableSource actualFrom(String sql) {
        final SQLQueryExpr sqlExpr = query(sql);
        SubqueryRewriterHelper.rewrite(sqlExpr);
        return ((MySqlSelectQueryBlock) sqlExpr.getSubQuery().getQuery()).getFrom();
    }

    private SQLTableSource expectFrom(String sql) {
        return ((MySqlSelectQueryBlock) query(sql).getSubQuery().getQuery()).getFrom();
    }

    private SQLExpr actualWhere(String sql) {
        final SQLQueryExpr sqlExpr = query(sql);
        SubqueryRewriterHelper.rewrite(sqlExpr);
        return ((MySqlSelectQueryBlock) sqlExpr.getSubQuery().getQuery()).getWhere();
    }

    private SQLExpr expectWhere(String sql) {
        return ((MySqlSelectQueryBlock) query(sql).getSubQuery().getQuery()).getWhere();
    }

    private SQLQueryExpr query(String sql) {
        ElasticSqlExprParser parser = new ElasticSqlExprParser(sql);
        SQLQueryExpr expr = (SQLQueryExpr) parser.expr();
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("Illegal sql: " + sql);
        }
        return expr;
    }
}