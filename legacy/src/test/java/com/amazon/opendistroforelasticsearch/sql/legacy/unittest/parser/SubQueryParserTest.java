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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.parser;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.junit.Assert.assertEquals;

public class SubQueryParserTest {

    private static SqlParser parser = new SqlParser();

    @Test
    public void selectFromSubqueryShouldPass() throws SqlParseException {
        Select select = parseSelect(
                StringUtils.format(
                        "SELECT t.T1 as age1, t.T2 as balance1 " +
                        "FROM (SELECT age as T1, balance as T2 FROM %s/account) t",
                        TEST_INDEX_ACCOUNT));

        assertEquals(2, select.getFields().size());
        assertEquals("age", select.getFields().get(0).getName());
        assertEquals("age1", select.getFields().get(0).getAlias());
        assertEquals("balance", select.getFields().get(1).getName());
        assertEquals("balance1", select.getFields().get(1).getAlias());
    }

    @Test
    public void selectFromSubqueryWithoutAliasShouldPass() throws SqlParseException {
        Select select = parseSelect(
                StringUtils.format(
                        "SELECT t.age as finalAge, t.balance as finalBalance " +
                                "FROM (SELECT age, balance FROM %s/account) t",
                        TEST_INDEX_ACCOUNT));

        assertEquals(2, select.getFields().size());
        assertEquals("age", select.getFields().get(0).getName());
        assertEquals("finalAge", select.getFields().get(0).getAlias());
        assertEquals("balance", select.getFields().get(1).getName());
        assertEquals("finalBalance", select.getFields().get(1).getAlias());
    }

    @Test
    public void selectFromSubqueryShouldIgnoreUnusedField() throws SqlParseException {
        Select select = parseSelect(
                StringUtils.format(
                        "SELECT t.T1 as age1 " +
                        "FROM (SELECT age as T1, balance as T2 FROM %s/account) t",
                        TEST_INDEX_ACCOUNT));

        assertEquals(1, select.getFields().size());
        assertEquals("age", select.getFields().get(0).getName());
        assertEquals("age1", select.getFields().get(0).getAlias());
    }

    @Test
    public void selectFromSubqueryWithAggShouldPass() throws SqlParseException {
        Select select = parseSelect(
                StringUtils.format(
                        "SELECT t.TEMP as count " +
                        "FROM (SELECT COUNT(*) as TEMP FROM %s/account) t",
                        TEST_INDEX_ACCOUNT));
        assertEquals(1, select.getFields().size());
        assertEquals("COUNT", select.getFields().get(0).getName());
        assertEquals("count", select.getFields().get(0).getAlias());
    }

    @Test
    public void selectFromSubqueryWithWhereAndCountShouldPass() throws SqlParseException {
        Select select = parseSelect(
                StringUtils.format(
                        "SELECT t.TEMP as count " +
                        "FROM (SELECT COUNT(*) as TEMP FROM %s/account WHERE age > 30) t",
                        TEST_INDEX_ACCOUNT));

        assertEquals(1, select.getFields().size());
        assertEquals("COUNT", select.getFields().get(0).getName());
        assertEquals("count", select.getFields().get(0).getAlias());
    }

    @Test
    public void selectFromSubqueryWithCountAndGroupByAndOrderByShouldPass() throws SqlParseException {
        Select select = parseSelect(
                StringUtils.format(
                        "SELECT t.TEMP as count " +
                        "FROM (SELECT COUNT(*) as TEMP FROM %s/account GROUP BY age ORDER BY TEMP) t",
                        TEST_INDEX_ACCOUNT));

        assertEquals(1, select.getFields().size());
        assertEquals("COUNT", select.getFields().get(0).getName());
        assertEquals("count", select.getFields().get(0).getAlias());
        assertEquals(1, select.getOrderBys().size());
        assertEquals("count", select.getOrderBys().get(0).getName());
        assertEquals("count", select.getOrderBys().get(0).getSortField().getName());
    }

    @Test
    public void selectFromSubqueryWithCountAndGroupByAndHavingShouldPass() throws Exception {

        Select select = parseSelect(
                StringUtils.format("SELECT t.T1 as g, t.T2 as c " +
                                   "FROM (SELECT gender as T1, COUNT(*) as T2 " +
                                   "      FROM %s/account " +
                                   "      GROUP BY gender " +
                                   "      HAVING T2 > 500) t", TEST_INDEX_ACCOUNT));

        assertEquals(2, select.getFields().size());
        assertEquals("gender", select.getFields().get(0).getName());
        assertEquals("g", select.getFields().get(0).getAlias());
        assertEquals("COUNT", select.getFields().get(1).getName());
        assertEquals("c", select.getFields().get(1).getAlias());
        assertEquals(1, select.getHaving().getConditions().size());
        assertEquals("c", ((Condition) select.getHaving().getConditions().get(0)).getName());
    }

    @Test
    public void selectFromSubqueryCountAndSum() throws Exception {
        Select select = parseSelect(
                StringUtils.format(
                        "SELECT t.TEMP1 as count, t.TEMP2 as balance " +
                        "FROM (SELECT COUNT(*) as TEMP1, SUM(balance) as TEMP2 " +
                        "      FROM %s/account) t",
                        TEST_INDEX_ACCOUNT));
        assertEquals(2, select.getFields().size());
        assertEquals("COUNT", select.getFields().get(0).getName());
        assertEquals("count", select.getFields().get(0).getAlias());
        assertEquals("SUM", select.getFields().get(1).getName());
        assertEquals("balance", select.getFields().get(1).getAlias());
    }

    private Select parseSelect(String query) throws SqlParseException {
        return parser.parseSelect((SQLQueryExpr) new ElasticSqlExprParser(query).expr());
    }
}