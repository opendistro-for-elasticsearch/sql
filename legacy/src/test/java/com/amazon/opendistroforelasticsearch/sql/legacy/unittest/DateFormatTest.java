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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Order;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.AggregationQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.maker.QueryMaker;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.hamcrest.Matcher;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.HasFieldWithValue.hasFieldWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class DateFormatTest {

    private static final String SELECT_CNT_FROM_DATE = "SELECT COUNT(*) AS c FROM dates ";

    @Test
    public void simpleFormatCondition() {
        List<QueryBuilder> q = query(SELECT_CNT_FROM_DATE + "WHERE date_format(creationDate, 'YYYY') < '2018'");

        assertThat(q, hasQueryWithValue("fieldName", equalTo("creationDate")));
        assertThat(q, hasQueryWithValueGetter(MatcherUtils.featureValueOf("has format", equalTo("YYYY"), f->((RangeQueryBuilder)f).format())));
    }

    @Test
    public void equalCondition() {
        List<QueryBuilder> q = query(SELECT_CNT_FROM_DATE + "WHERE date_format(creationDate, 'YYYY-MM-dd') = '2018-04-02'");

        assertThat(q, hasQueryWithValueGetter(MatcherUtils.featureValueOf("has format", equalTo("YYYY-MM-dd"), f->((RangeQueryBuilder)f).format())));

        // Equality query for date_format is created with a rangeQuery where the 'from' and 'to' values are equal to the value we are equating to
        assertThat(q, hasQueryWithValue("from", equalTo(BytesRefs.toBytesRef("2018-04-02")))); // converting string to bytes ref as RangeQueryBuilder stores it this way
        assertThat(q, hasQueryWithValue("to", equalTo(BytesRefs.toBytesRef("2018-04-02"))));
    }

    @Test
    public void orderByTest() {
        String query = "SELECT agent, ip, date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY date_format(utc_time, 'dd-MM-YYYY') desc, ip";

        Select select = getSelect(query);

        List<Order> orderBys = select.getOrderBys();
        assertThat(orderBys.size(), equalTo(2));

        Order formula = orderBys.get(0);

        assertThat(formula.isScript(), is(true));
        assertThat(formula.getType(), is("DESC"));
        assertThat(formula.getName(), containsString("DateTimeFormatter.ofPattern"));

        Order ip = orderBys.get(1);

        assertThat(ip.isScript(), is(false));
        assertThat(ip.getName(), is("ip"));
        assertThat(ip.getType(), is("ASC"));
    }

    @Test
    public void groupByWithDescOrder() throws SqlParseException {
        String query = "SELECT date_format(utc_time, 'dd-MM-YYYY'), count(*) " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date_format(utc_time, 'dd-MM-YYYY') " +
                "ORDER BY date_format(utc_time, 'dd-MM-YYYY') DESC";

        JSONObject aggregation = getAggregation(query);
        assertThat(aggregation.getInt("size"), is(getSelect(query).getRowCount()));
        assertThat(aggregation.getJSONObject("order").getString("_key"), is("desc"));
    }

    @Test
    public void groupByWithAscOrder() throws SqlParseException {
        String query = "SELECT date_format(utc_time, 'dd-MM-YYYY'), count(*) " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date_format(utc_time, 'dd-MM-YYYY') " +
                "ORDER BY date_format(utc_time, 'dd-MM-YYYY')";

        JSONObject aggregation = getAggregation(query);

        assertThat(aggregation.getJSONObject("order").getString("_key"), is("asc"));
    }

    @Test
    @Ignore("https://github.com/opendistro-for-elasticsearch/sql/issues/158")
    public void groupByWithAndAlias() throws SqlParseException {
        String query = "SELECT date_format(utc_time, 'dd-MM-YYYY') x, count(*) " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY x " +
                "ORDER BY x";

        JSONObject aggregation = getAggregation(query);
        assertThat(aggregation.getJSONObject("order").getString("_key"), is("asc"));
    }

    public JSONObject getAggregation(String query) throws SqlParseException {
        Select select = getSelect(query);

        Client client = mock(Client.class);
        AggregationQueryAction queryAction = new AggregationQueryAction(client, select);

        String elasticDsl = queryAction.explain().explain();
        JSONObject elasticQuery = new JSONObject(elasticDsl);

        JSONObject aggregations = elasticQuery.getJSONObject("aggregations");
        String dateFormatAggregationKey = getScriptAggregationKey(aggregations, "date_format");

        return aggregations.getJSONObject(dateFormatAggregationKey).getJSONObject("terms");
    }

    public static String getScriptAggregationKey(JSONObject aggregation, String prefix) {
        return aggregation.keySet()
                .stream()
                .filter(x -> x.startsWith(prefix))
                .findFirst()
                .orElseThrow(()-> new RuntimeException("Can't find key" + prefix + " in aggregation " + aggregation));
    }

    @Test
    public void notEqualCondition() {
        List<QueryBuilder> q = query(SELECT_CNT_FROM_DATE + "WHERE date_format(creationDate, 'YYYY-MM-dd') <> '2018-04-02'");

        assertThat(q, hasNotQueryWithValue("from", equalTo(BytesRefs.toBytesRef("2018-04-02"))));
        assertThat(q, hasNotQueryWithValue("to", equalTo(BytesRefs.toBytesRef("2018-04-02"))));
    }

    @Test
    public void greaterThanCondition() {
        List<QueryBuilder> q = query(SELECT_CNT_FROM_DATE + "WHERE date_format(creationDate, 'YYYY-MM-dd') > '2018-04-02'");

        assertThat(q, hasQueryWithValue("from", equalTo(BytesRefs.toBytesRef("2018-04-02"))));
        assertThat(q, hasQueryWithValue("includeLower", equalTo(false)));
        assertThat(q, hasQueryWithValue("includeUpper", equalTo(true)));
    }

    @Test
    public void greaterThanOrEqualToCondition() {
        List<QueryBuilder> q = query(SELECT_CNT_FROM_DATE + "WHERE date_format(creationDate, 'YYYY-MM-dd') >= '2018-04-02'");

        assertThat(q, hasQueryWithValue("from", equalTo(BytesRefs.toBytesRef("2018-04-02"))));
        assertThat(q, hasQueryWithValue("to", equalTo(null)));
        assertThat(q, hasQueryWithValue("includeLower", equalTo(true)));
        assertThat(q, hasQueryWithValue("includeUpper", equalTo(true)));
    }

    @Test
    public void timeZoneCondition() {
        List<QueryBuilder> q = query(SELECT_CNT_FROM_DATE + "WHERE date_format(creationDate, 'YYYY-MM-dd', 'America/Phoenix') > '2018-04-02'");

        // Used hasProperty here as getter followed convention for obtaining ID and Feature Matcher was having issues with generic type to obtain value
        assertThat(q, hasQueryWithValue("timeZone", hasProperty("id", equalTo("America/Phoenix"))));
    }

    private List<QueryBuilder> query(String sql) {
        return translate(parseSql(sql));
    }

    private List<QueryBuilder> translate(SQLQueryExpr expr) {
        try {
            Select select = new SqlParser().parseSelect(expr);
            QueryBuilder whereQuery = QueryMaker.explain(select.getWhere(), select.isQuery);
            return ((BoolQueryBuilder) whereQuery).filter();
        } catch (SqlParseException e) {
            throw new ParserException("Illegal sql expr: " + expr.toString());
        }
    }

    private SQLQueryExpr parseSql(String sql) {
        ElasticSqlExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("Illegal sql: " + sql);
        }
        return (SQLQueryExpr) expr;
    }

    private Select getSelect(String query) {
        try {
            Select select = new SqlParser().parseSelect(parseSql(query));
            if (select.getRowCount() == null){
                select.setRowCount(Select.DEFAULT_LIMIT);
            }
            return select;
        } catch (SqlParseException e) {
            throw new RuntimeException(e);
        }
    }

    private <T, U> Matcher<Iterable<? super T>> hasQueryWithValue(String name, Matcher<? super U> matcher) {
        return hasItem(
                hasFieldWithValue("mustClauses", "has mustClauses",
                        hasItem(hasFieldWithValue(name, "has " + name, matcher))));
    }

    private <T, U> Matcher<Iterable<? super T>> hasNotQueryWithValue(String name, Matcher<? super U> matcher) {
        return hasItem(
                hasFieldWithValue("mustClauses", "has mustClauses",
                        hasItem(hasFieldWithValue("mustNotClauses", "has mustNotClauses",
                                hasItem(hasFieldWithValue(name, "has " + name, matcher))))));
    }

    private <T, U> Matcher<Iterable<? super T>> hasQueryWithValueGetter(Matcher<? super U> matcher) {
        return hasItem(
                hasFieldWithValue("mustClauses", "has mustClauses",
                        hasItem(matcher)));
    }
}