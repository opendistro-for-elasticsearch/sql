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

package com.amazon.opendistroforelasticsearch.sql.unittest;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.rewriter.nestedfield.NestedFieldRewriter;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import org.junit.Test;

import java.util.List;

import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;

public class NestedFieldRewriterTest {

    @Test
    public void regression() {
        noImpact("SELECT * FROM team");
        noImpact("SELECT region FROM team/test, employees/test");
        noImpact("SELECT manager.name FROM team WHERE region = 'US' ORDER BY COUNT(*)");
        noImpact("SELECT COUNT(*) FROM team GROUP BY region");
    }

    @Test
    public void selectWithoutFrom() {
        // Expect no exception thrown
        query("SELECT now()");
    }

    @Test
    public void selectAll() {
        same(
            query("SELECT * FROM team t, t.employees"),
            query("SELECT *, nested(employees.*) FROM team")
        );
    }

    @Test
    public void selectAllWithGroupBy() {
        same(
            query("SELECT * FROM team t, t.employees e GROUP BY e.firstname"),
            query("SELECT * FROM team GROUP BY nested(employees.firstname)")
        );
    }

    @Test
    public void selectAllWithCondition() {
        same(
            query("SELECT * FROM team t, t.employees e WHERE e.age = 26"),
            query("SELECT *, nested(employees.*) FROM team WHERE nested(employees.age) = 26")
        );
    }

    @Test
    public void singleCondition() {
        same(
            query("SELECT region FROM team t, t.employees e WHERE e.age = 26"),
            query("SELECT region FROM team WHERE nested(employees.age) = 26")
        );
    }

    @Test
    public void mixedWithObjectType() {
        same(
            query("SELECT region FROM team t, t.employees e WHERE e.age > 30 OR manager.age = 50"),
            query("SELECT region FROM team WHERE nested(employees.age) > 30 OR manager.age = 50")
        );
    }

    @Test
    public void noAlias() {
        same(
            query("SELECT region FROM team t, t.employees WHERE employees.age = 26"),
            query("SELECT region FROM team WHERE nested(employees.age) = 26")
        );
    }

    @Test(expected = AssertionError.class)
    public void multipleRegularTables() {
        same(
            query("SELECT region FROM team t, t.employees e, company c WHERE e.age = 26"),
            query("SELECT region FROM team, company WHERE nested(employees.age) = 26")
        );
    }

    @Test
    public void eraseParentAlias() {
        same(
            query("SELECT t.age FROM team t, t.employees e WHERE t.region = 'US' AND age > 26"),
            query("SELECT age FROM team WHERE region = 'US' AND age > 26")
        );
        noImpact("SELECT t.age FROM team t WHERE t.region = 'US'");
    }

    @Test
    public void select() {
        same(
            query("SELECT e.age FROM team t, t.employees e"),
            query("SELECT nested(employees.age) FROM team")
        );
    }

    @Test
    public void aggregationInSelect() {
        same(
            query("SELECT AVG(e.age) FROM team t, t.employees e"),
            query("SELECT AVG(nested(employees.age)) FROM team")
        );
    }

    @Test
    public void multipleAggregationsInSelect() {
        same(
            query("SELECT COUNT(*), AVG(e.age) FROM team t, t.employees e"),
            query("SELECT COUNT(*), AVG(nested(employees.age)) FROM team")
        );
    }

    @Test
    public void groupBy() {
        same(
            query("SELECT e.firstname, COUNT(*) FROM team t, t.employees e GROUP BY e.firstname"),
            query("SELECT nested(employees.firstname), COUNT(*) FROM team GROUP BY nested(employees.firstname)")
        );
    }

    @Test
    public void multipleFieldsInGroupBy() {
        same(
            query("SELECT COUNT(*) FROM team t, t.employees e GROUP BY t.manager, e.age"),
            query("SELECT COUNT(*) FROM team GROUP BY manager, nested(employees.age)")
        );
    }

    @Test
    public void orderBy() {
        same(
            query("SELECT region FROM team t, t.employees e ORDER BY e.age"),
            query("SELECT region FROM team ORDER BY nested(employees.age)")
        );
    }

    @Test
    public void multipleConditions() {
        same(
            query("SELECT region " +
                  "FROM team t, t.manager m, t.employees e " +
                  "WHERE t.department = 'IT' AND " +
                  "      (e.age = 26 OR (e.firstname = 'John' AND e.lastname = 'Smith')) AND " +
                  "      t.region = 'US' AND " +
                  "      (m.name = 'Alice' AND m.age = 50)"),
            query("SELECT region " +
                  "FROM team " +
                  "WHERE department = 'IT' AND " +
                  "      nested(\"employees\", employees.age = 26 OR (employees.firstname = 'John' AND employees.lastname = 'Smith')) AND " +
                  "      region = 'US' AND " +
                  "      nested(\"manager\", manager.name = 'Alice' AND manager.age = 50)")
        );
    }

    @Test
    public void multipleFieldsInFrom() {
        same(
            query("SELECT region FROM team/test t, t.manager m, t.employees e WHERE m.age = 30 AND e.age = 26"),
            query("SELECT region FROM team/test WHERE nested(manager.age) = 30 AND nested(employees.age) = 26")
        );
    }

    @Test
    public void unionAll() {
        // NLPchina doesn't support UNION (intersection)
        same(
            query("SELECT region FROM team t, t.employees e WHERE e.age = 26 " +
                  "UNION ALL " +
                  "SELECT region FROM team t, t.employees e WHERE e.firstname = 'John'"),
            query("SELECT region FROM team WHERE nested(employees.age) = 26 " +
                  "UNION ALL " +
                  "SELECT region FROM team WHERE nested(employees.firstname) = 'John'")
        );
    }

    @Test
    public void minus() {
        same(
            query("SELECT region FROM team t, t.employees e WHERE e.age = 26 " +
                  "MINUS " +
                  "SELECT region FROM team t, t.employees e WHERE e.firstname = 'John'"),
            query("SELECT region FROM team WHERE nested(employees.age) = 26 " +
                  "MINUS " +
                  "SELECT region FROM team WHERE nested(employees.firstname) = 'John'")
        );
    }

    public void join() {
        // TODO
    }

    @Test
    public void subQuery() {
        // Subquery only support IN and TERMS
        same(
            query("SELECT region FROM team t, t.employees e " +
                  "  WHERE e.age IN " +
                  "    (SELECT t1.manager.age FROM team t1, t1.employees e1 WHERE e1.age > 0)"),
            query("SELECT region FROM team " +
                  "  WHERE nested(employees.age) IN " +
                  "    (SELECT manager.age FROM team WHERE nested(employees.age) > 0)")
        );
    }

    @Test
    public void subQueryWitSameAlias() {
        // Inner alias e shadow outer alias e of nested field
        same(
            query("SELECT name FROM team t, t.employees e " +
                  "  WHERE e.age IN " +
                  "    (SELECT e.age FROM team e, e.manager m WHERE e.age > 0 OR m.name = 'Alice')"),
            query("SELECT name FROM team " +
                  "  WHERE nested(employees.age) IN " +
                  "    (SELECT age FROM team WHERE age > 0 OR nested(manager.name) = 'Alice')")
        );
    }

    private void noImpact(String sql) {
        same(parse(sql), rewrite(parse(sql)));
    }

    /**
     * The intention for this assert method is:
     *
     * 1) MySqlSelectQueryBlock.equals() doesn't call super.equals().
     *    But select items, from, where and group by are all held by parent class SQLSelectQueryBlock.
     *
     * 2) SQLSelectGroupByClause doesn't implement equals() at all..
     *    MySqlSelectGroupByExpr compares identity of expression..
     *
     * 3) MySqlUnionQuery doesn't implement equals() at all
     */
    private void same(SQLQueryExpr actual, SQLQueryExpr expected) {
        assertEquals(expected.getClass(), actual.getClass());

        SQLSelect expectedQuery = expected.getSubQuery();
        SQLSelect actualQuery = actual.getSubQuery();
        assertEquals(expectedQuery.getOrderBy(), actualQuery.getOrderBy());
        assertQuery(expectedQuery, actualQuery);
    }

    private void assertQuery(SQLSelect expected, SQLSelect actual) {
        SQLSelectQuery expectedQuery = expected.getQuery();
        SQLSelectQuery actualQuery = actual.getQuery();
        if (actualQuery instanceof SQLSelectQueryBlock) {
            assertQueryBlock(
                (SQLSelectQueryBlock) expectedQuery,
                (SQLSelectQueryBlock) actualQuery
            );
        }
        else if (actualQuery instanceof SQLUnionQuery) {
            assertQueryBlock(
                (SQLSelectQueryBlock) ((SQLUnionQuery) expectedQuery).getLeft(),
                (SQLSelectQueryBlock) ((SQLUnionQuery) actualQuery).getLeft()
            );
            assertQueryBlock(
                (SQLSelectQueryBlock) ((SQLUnionQuery) expectedQuery).getRight(),
                (SQLSelectQueryBlock) ((SQLUnionQuery) actualQuery).getRight()
            );
            assertEquals(
                ((SQLUnionQuery) expectedQuery).getOperator(),
                ((SQLUnionQuery) actualQuery).getOperator()
            );
        }
        else {
            throw new IllegalStateException("Unsupported test SQL");
        }
    }

    private void assertQueryBlock(SQLSelectQueryBlock expected, SQLSelectQueryBlock actual) {
        assertEquals("SELECT", expected.getSelectList(), actual.getSelectList());
        assertEquals("INTO", expected.getInto(), actual.getInto());
        assertEquals("WHERE", expected.getWhere(), actual.getWhere());
        if (actual.getWhere() instanceof SQLInSubQueryExpr) {
            assertQuery(
                ((SQLInSubQueryExpr) expected.getWhere()).getSubQuery(),
                ((SQLInSubQueryExpr) actual.getWhere()).getSubQuery()
            );
        }
        assertEquals("PARENTHESIZED", expected.isParenthesized(), actual.isParenthesized());
        assertEquals("DISTION", expected.getDistionOption(), actual.getDistionOption());
        assertFrom(expected, actual);
        if (!(expected.getGroupBy() == null && actual.getGroupBy() == null)) {
            assertGroupBy(expected.getGroupBy(), actual.getGroupBy());
        }
    }

    private void assertFrom(SQLSelectQueryBlock expected, SQLSelectQueryBlock actual) {
        // Only 2 tables JOIN at most is supported
        if (expected.getFrom() instanceof SQLExprTableSource) {
            assertTable(expected.getFrom(), actual.getFrom());
        } else {
            assertEquals(actual.getFrom().getClass(), SQLJoinTableSource.class);
            assertTable(
                ((SQLJoinTableSource) expected.getFrom()).getLeft(),
                ((SQLJoinTableSource) actual.getFrom()).getLeft()
            );
            assertTable(
                ((SQLJoinTableSource) expected.getFrom()).getRight(),
                ((SQLJoinTableSource) actual.getFrom()).getRight()
            );
            assertEquals(
                ((SQLJoinTableSource) expected.getFrom()).getJoinType(),
                ((SQLJoinTableSource) actual.getFrom()).getJoinType()
            );
        }
    }

    private void assertGroupBy(SQLSelectGroupByClause expected, SQLSelectGroupByClause actual) {
        assertEquals("HAVING", expected.getHaving(), actual.getHaving());

        List<SQLExpr> expectedGroupby = expected.getItems();
        List<SQLExpr> actualGroupby = actual.getItems();
        assertEquals(expectedGroupby.size(), actualGroupby.size());
        range(0, expectedGroupby.size()).
            forEach(i -> assertEquals(
                ((MySqlSelectGroupByExpr) expectedGroupby.get(i)).getExpr(),
                ((MySqlSelectGroupByExpr) actualGroupby.get(i)).getExpr())
            );
    }

    private void assertTable(SQLTableSource expect, SQLTableSource actual) {
        assertEquals(SQLExprTableSource.class, expect.getClass());
        assertEquals(SQLExprTableSource.class, actual.getClass());
        assertEquals(((SQLExprTableSource) expect).getExpr(), ((SQLExprTableSource) actual).getExpr());
        assertEquals(expect.getAlias(), actual.getAlias());
    }

    /**
     * Walk through extra rewrite logic if NOT found "nested" in SQL query statement.
     * Otherwise return as before so that original logic be compared with result of rewrite.
     *
     * @param sql  Test sql
     * @return     Node parsed out of sql
     */
    private SQLQueryExpr query(String sql) {
        SQLQueryExpr expr = parse(sql);
        if (sql.contains("nested")) {
            return expr;
        }
        return rewrite(expr);
    }

    private SQLQueryExpr parse(String sql) {
        ElasticSqlExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("Illegal sql: " + sql);
        }
        return (SQLQueryExpr) expr;
    }

    private SQLQueryExpr rewrite(SQLQueryExpr expr) {
        expr.accept(new NestedFieldRewriter());
        return expr;
    }

}
