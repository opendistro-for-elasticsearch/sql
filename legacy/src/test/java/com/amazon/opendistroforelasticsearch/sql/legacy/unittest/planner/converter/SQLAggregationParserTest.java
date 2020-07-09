/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.planner.converter;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.util.JdbcConstants;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.Expression;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.ExpressionFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.converter.SQLAggregationParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.ColumnNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.ExpressionFactory.cast;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.ExpressionFactory.of;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.ADD;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.LOG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@RunWith(MockitoJUnitRunner.class)
public class SQLAggregationParserTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void parseAggWithoutExpressionShouldPass() {
        String sql = "SELECT dayOfWeek, max(FlightDelayMin), MIN(FlightDelayMin) as min " +
                     "FROM kibana_sample_data_flights " +
                     "GROUP BY dayOfWeek";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(group("dayOfWeek", "dayOfWeek"),
                                                      agg("MAX", "FlightDelayMin", "MAX_0"),
                                                      agg("MIN", "FlightDelayMin", "min")));

        assertThat(columnNodes, containsInAnyOrder(columnNode("dayOfWeek", null, ExpressionFactory.ref("dayOfWeek")),
                                                   columnNode("MAX(FlightDelayMin)", null, ExpressionFactory
                                                           .ref("MAX_0")),
                                                   columnNode("min", "min", ExpressionFactory.ref("min"))));
    }

    @Test
    public void parseAggWithFunctioniWithoutExpressionShouldPass() {
        String sql = "SELECT dayOfWeek, max(FlightDelayMin), MIN(FlightDelayMin) as min " +
                     "FROM kibana_sample_data_flights " +
                     "GROUP BY dayOfWeek";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(group("dayOfWeek", "dayOfWeek"),
                                                      agg("MAX", "FlightDelayMin", "MAX_0"),
                                                      agg("MIN", "FlightDelayMin", "min")));

        assertThat(columnNodes, containsInAnyOrder(columnNode("dayOfWeek", null, ExpressionFactory.ref("dayOfWeek")),
                                                   columnNode("MAX(FlightDelayMin)", null, ExpressionFactory
                                                           .ref("MAX_0")),
                                                   columnNode("min", "min", ExpressionFactory.ref("min"))));
    }

    @Test
    public void parseAggWithExpressionShouldPass() {
        String sql = "SELECT dayOfWeek, max(FlightDelayMin) + MIN(FlightDelayMin) as sub " +
                     "FROM kibana_sample_data_flights " +
                     "GROUP BY dayOfWeek";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(group("dayOfWeek", "dayOfWeek"),
                                                      agg("MAX", "FlightDelayMin", "MAX_0"),
                                                      agg("MIN", "FlightDelayMin", "MIN_1")));

        assertThat(columnNodes, containsInAnyOrder(columnNode("dayOfWeek", null, ExpressionFactory.ref("dayOfWeek")),
                                                   columnNode("sub", "sub", add(ExpressionFactory.ref("MAX_0"), ExpressionFactory
                                                           .ref("MIN_1")))));
    }

    @Test
    public void parseWithRawSelectFuncnameShouldPass() {
        String sql = "SELECT LOG(FlightDelayMin) " +
                "FROM kibana_sample_data_flights " +
                "GROUP BY log(FlightDelayMin)";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(group("log(FlightDelayMin)", "log(FlightDelayMin)")));

        assertThat(
                columnNodes,
                containsInAnyOrder(
                        columnNode(
                                "LOG(FlightDelayMin)",
                                null,
                                ExpressionFactory.ref("log(FlightDelayMin)")
                        )
                )
        );
    }

    @Test
    public void functionOverFiledShouldPass() {
        String sql = "SELECT dayOfWeek, max(FlightDelayMin) + MIN(FlightDelayMin) as sub " +
                     "FROM kibana_sample_data_flights " +
                     "GROUP BY dayOfWeek";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(group("dayOfWeek", "dayOfWeek"),
                                                      agg("MAX", "FlightDelayMin", "MAX_0"),
                                                      agg("MIN", "FlightDelayMin", "MIN_1")));

        assertThat(columnNodes, containsInAnyOrder(columnNode("dayOfWeek", null, ExpressionFactory.ref("dayOfWeek")),
                                                   columnNode("sub", "sub", add(ExpressionFactory.ref("MAX_0"), ExpressionFactory
                                                           .ref("MIN_1")))));
    }

    @Test
    public void parseCompoundAggWithExpressionShouldPass() {
        String sql = "SELECT ASCII(dayOfWeek), log(max(FlightDelayMin) + MIN(FlightDelayMin)) as log " +
                     "FROM kibana_sample_data_flights " +
                     "GROUP BY ASCII(dayOfWeek)";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(group("ASCII(dayOfWeek)", "ASCII(dayOfWeek)"),
                                                      agg("MAX", "FlightDelayMin", "MAX_0"),
                                                      agg("MIN", "FlightDelayMin", "MIN_1")));

        assertThat(columnNodes, containsInAnyOrder(columnNode("ASCII(dayOfWeek)", null, ExpressionFactory
                                                           .ref("ASCII(dayOfWeek)")),
                                                   columnNode("log", "log", log(add(ExpressionFactory.ref("MAX_0"), ExpressionFactory
                                                           .ref("MIN_1"))))));
    }

    @Test
    public void parseSingleFunctionOverAggShouldPass() {
        String sql = "SELECT log(max(age)) FROM accounts";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(agg("max", "age", "max_0")));
        assertThat(columnNodes, containsInAnyOrder(columnNode("log(max(age))", null, log(
                ExpressionFactory.ref("max_0")))));
    }

    @Test
    public void parseFunctionGroupColumnOverShouldPass() {
        String sql = "SELECT CAST(balance AS FLOAT) FROM accounts GROUP BY balance";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(group("balance", "balance")));
        assertThat(columnNodes, containsInAnyOrder(columnNode("CAST(balance AS FLOAT)", null, cast(
                ExpressionFactory.ref("balance")))));
    }

    @Test
    public void withoutAggregationShouldPass() {
        String sql = "SELECT age, gender FROM accounts GROUP BY age, gender";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(
                group("age", "age"),
                group("gender", "gender")));
        assertThat(columnNodes, containsInAnyOrder(
                columnNode("age", null, ExpressionFactory.ref("age")),
                columnNode("gender", null, ExpressionFactory.ref("gender"))));
    }

    @Test
    public void groupKeyInSelectWithFunctionShouldPass() {
        String sql = "SELECT log(age), max(balance) FROM accounts GROUP BY age";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(
                group("age", "age"),
                agg("max", "balance", "max_0")));
        assertThat(columnNodes, containsInAnyOrder(
                columnNode("log(age)", null, log(ExpressionFactory.ref("age"))),
                columnNode("max(balance)", null, ExpressionFactory.ref("max_0"))));
    }

    @Test
    public void theDotInFieldNameShouldBeReplaceWithSharp() {
        String sql = "SELECT name.lastname, max(balance) FROM accounts GROUP BY name.lastname";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(
                group("name.lastname", "name#lastname"),
                agg("max", "balance", "max_0")));
        assertThat(columnNodes, containsInAnyOrder(
                columnNode("name.lastname", null, ExpressionFactory.ref("name#lastname")),
                columnNode("max(balance)", null, ExpressionFactory.ref("max_0"))));
    }

    @Test
    public void noGroupKeyInSelectShouldPass() {
        String sql = "SELECT AVG(age) FROM t GROUP BY age";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(
                agg("avg", "age", "avg_0")));
        assertThat(columnNodes, containsInAnyOrder(
                columnNode("avg(age)", null, ExpressionFactory.ref("avg_0"))));
    }

    @Test
    public void aggWithDistinctShouldPass() {
        String sql = "SELECT count(distinct gender) FROM t GROUP BY age";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
        List<SQLSelectItem> sqlSelectItems = parser.selectItemList();
        List<ColumnNode> columnNodes = parser.getColumnNodes();

        assertThat(sqlSelectItems, containsInAnyOrder(
                agg("count", "gender", "count_0")));
        assertThat(columnNodes, containsInAnyOrder(
                columnNode("count(distinct gender)", null, ExpressionFactory.ref("count_0"))));
    }

    /**
     * TermQueryExplainIT.testNestedSingleGroupBy
     */
    @Test
    public void aggregationWithNestedShouldThrowException() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("unsupported operator: nested");

        String sql = "SELECT nested(projects.name, 'projects'),id "
                     + "FROM t "
                     + "GROUP BY nested(projects.name.keyword, 'projects')";
        SQLAggregationParser parser = new SQLAggregationParser(new ColumnTypeProvider());
        parser.parse(mYSqlSelectQueryBlock(sql));
    }

    private MySqlSelectQueryBlock mYSqlSelectQueryBlock(String sql) {
        String dbType = JdbcConstants.MYSQL;
        SQLQueryExpr sqlQueryExpr = (SQLQueryExpr) SQLUtils.toSQLExpr(sql, dbType);
        return ((MySqlSelectQueryBlock) sqlQueryExpr.getSubQuery().getQuery());
    }

    private TypeSafeMatcher<ColumnNode> columnNode(String name, String alias, Expression expr) {
        return new TypeSafeMatcher<ColumnNode>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("(name=%s,alias=%s,expression=%s)", name, alias, expr));
            }

            @Override
            protected boolean matchesSafely(ColumnNode item) {
                if (name == null) {
                    return false;
                }
                if (alias == null && item.getAlias() != null) {
                    return false;
                }

                return name.equalsIgnoreCase(item.getName()) &&
                       ((alias == null && item.getAlias() == null) || alias.equals(item.getAlias())) &&
                       expr.toString().equalsIgnoreCase(item.getExpr().toString());
            }
        };
    }

    private TypeSafeMatcher<SQLSelectItem> agg(String methodName, String name, String alias) {
        return new TypeSafeMatcher<SQLSelectItem>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("(methodName=%s, name=%s, alias=%s)", methodName, name, alias));
            }

            @Override
            protected boolean matchesSafely(SQLSelectItem item) {
                if (item.getExpr() instanceof SQLAggregateExpr) {
                    return ((SQLAggregateExpr) item.getExpr()).getMethodName().equalsIgnoreCase(methodName) &&
                           ((SQLAggregateExpr) item.getExpr()).getArguments()
                                                              .get(0)
                                                              .toString()
                                                              .equalsIgnoreCase(name) &&
                           ((item.getAlias() == null && alias == null) || item.getAlias().equalsIgnoreCase(alias));
                } else {
                    return false;
                }
            }
        };
    }

    private TypeSafeMatcher<SQLSelectItem> group(String name, String alias) {
        return new TypeSafeMatcher<SQLSelectItem>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("(name=%s, alias=%s)", name, alias));
            }

            @Override
            protected boolean matchesSafely(SQLSelectItem item) {
                boolean b = item.getExpr().toString().equalsIgnoreCase(name) &&
                            ((item.getAlias() == null && alias == null) || item.getAlias().equalsIgnoreCase(alias));
                return b;
            }
        };
    }

    private Expression add(Expression... expressions) {
        return of(ADD, Arrays.asList(expressions));
    }

    private Expression log(Expression... expressions) {
        return of(LOG, Arrays.asList(expressions));
    }
}