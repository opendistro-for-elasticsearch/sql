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

package com.amazon.opendistroforelasticsearch.sql.unittest.parser.subquery.visitor;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.SubqueryType;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.visitor.FindSubqueryInWhere;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FindSubqueryInWhereTest {

    @Test
    public void findIN() {
        String sql = "SELECT * FROM A WHERE a IN (SELECT b FROM B)";
        final FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        parse(sql).accept(subqueryInWhere);

        assertEquals(SubqueryType.IN, subqueryInWhere.subqueryType());
        assertEquals(1, subqueryInWhere.getSqlInSubQueryExprs().size());
        assertThat(subqueryInWhere.getSqlInSubQueryExprs().get(0), is(instanceOf(SQLInSubQueryExpr.class)));
        assertFalse(subqueryInWhere.getSqlInSubQueryExprs().get(0).isNot());
        assertEquals(0, subqueryInWhere.getSqlExistsExprs().size());
    }

    @Test
    public void findNOTIN() {
        String sql = "SELECT * FROM A WHERE a NOT IN (SELECT b FROM B)";
        final FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        parse(sql).accept(subqueryInWhere);

        assertEquals(SubqueryType.UNSUPPORTED, subqueryInWhere.subqueryType());
        assertEquals(1, subqueryInWhere.getSqlInSubQueryExprs().size());
        assertThat(subqueryInWhere.getSqlInSubQueryExprs().get(0), is(instanceOf(SQLInSubQueryExpr.class)));
        assertTrue(subqueryInWhere.getSqlInSubQueryExprs().get(0).isNot());
        assertEquals(0, subqueryInWhere.getSqlExistsExprs().size());
    }

    @Test
    public void findEXIST() {
        String sql = "SELECT * FROM A WHERE EXISTS (SELECT 1 FROM B WHERE A.a_v = B.b_v)";
        final FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        parse(sql).accept(subqueryInWhere);

        assertEquals(SubqueryType.UNSUPPORTED, subqueryInWhere.subqueryType());
        assertEquals(1, subqueryInWhere.getSqlExistsExprs().size());
        assertThat(subqueryInWhere.getSqlExistsExprs().get(0), is(instanceOf(SQLExistsExpr.class)));
        assertFalse(subqueryInWhere.getSqlExistsExprs().get(0).isNot());
        assertEquals(0, subqueryInWhere.getSqlInSubQueryExprs().size());
    }

    @Test
    public void findNOTEXIST() {
        String sql = "SELECT * FROM A WHERE NOT EXISTS (SELECT 1 FROM B WHERE A.a_v = B.b_v)";
        final FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        parse(sql).accept(subqueryInWhere);

        assertEquals(SubqueryType.UNSUPPORTED, subqueryInWhere.subqueryType());
        assertEquals(1, subqueryInWhere.getSqlExistsExprs().size());
        assertThat(subqueryInWhere.getSqlExistsExprs().get(0), is(instanceOf(SQLExistsExpr.class)));
        assertTrue(subqueryInWhere.getSqlExistsExprs().get(0).isNot());
        assertEquals(0, subqueryInWhere.getSqlInSubQueryExprs().size());
    }

    @Test
    public void findINatSelect() {
        String sql = "SELECT A.v, (SELECT  MAX(b) FROM B WHERE A.id = B.id) max_age  FROM A";
        final FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        final SQLQueryExpr parse = parse(sql);
        parse(sql).accept(subqueryInWhere);

        assertEquals(SubqueryType.UNSUPPORTED, subqueryInWhere.subqueryType());
        assertEquals(0, subqueryInWhere.getSqlInSubQueryExprs().size());
        assertEquals(0, subqueryInWhere.getSqlExistsExprs().size());
    }

    private SQLQueryExpr parse(String sql) {
        ElasticSqlExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("Illegal sql: " + sql);
        }
        return (SQLQueryExpr) expr;
    }
}