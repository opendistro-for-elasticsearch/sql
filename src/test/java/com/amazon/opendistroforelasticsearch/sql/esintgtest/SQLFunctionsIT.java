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

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.hamcrest.collection.IsMapContaining;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import java.util.stream.IntStream;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.hitAny;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvDouble;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvInt;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;


/**
 * Created by allwefantasy on 8/25/16.
 */
public class SQLFunctionsIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void functionFieldAliasAndGroupByAlias() throws Exception {
        String query = "SELECT " +
                "floor(substring(address,0,3)*20) as key," +
                "sum(age) cvalue FROM " + TEST_INDEX_ACCOUNT + "/account where address is not null " +
                "group by key order by cvalue desc limit 10  ";
        final JSONObject result = executeQuery(query);


        IntStream.rangeClosed(0, 9).forEach(i -> {
                    Assert.assertNotNull(result.query(String.format("/aggregations/key/buckets/%d/key", i)));
                    Assert.assertNotNull(result.query(String.format("/aggregations/key/buckets/%d/cvalue/value", i)));
                }
        );
    }

    /**
     * todo fix the issue.
     * @see <a href="https://github.com/opendistro-for-elasticsearch/sql/issues/59">https://github.com/opendistro-for-elasticsearch/sql/issues/59</a>
     */
    @Ignore
    public void normalFieldAlias() throws Exception {

        //here is a bug,csv field with spa
        String query = "SELECT " +
                "address as key,age from " +
                TEST_INDEX_ACCOUNT + "/account where address is not null " +
                "limit 10  ";

        assertThat(
                executeQuery(query),
                hitAny(kvString("/_source/key", not(isEmptyOrNullString())))
        );
    }

    @Test
    public void functionAlias() throws Exception {
        //here is a bug,if only script fields are included,then all fields will return; fix later
        String query = "SELECT " +
                "substring(address,0,3) as key,address from " +
                TEST_INDEX_ACCOUNT + "/account where address is not null " +
                "order by address desc limit 10  ";

        assertThat(
                executeQuery(query),
                hitAny(both(kvString("/_source/address", equalTo("863 Wythe Place"))).and(kvString("/fields/key/0",
                        equalTo("863"))))
        );
    }

    @Test
    public void caseChangeTest() throws IOException {
        String query = "SELECT LOWER(firstname) " +
                "FROM elasticsearch-sql_test_index_account/account " +
                "WHERE UPPER(lastname)='DUKE' " +
                "ORDER BY upper(lastname) ";

        assertThat(
                executeQuery(query),
                hitAny(
                        kvString("/_source/address", equalTo("880 Holmes Lane")),
                        kvString("/fields/LOWER_1/0", equalTo("amber")))
        );
    }

    @Test
    public void caseChangeTestWithLocale() throws IOException {
        // Uses Turkish locale to check if we pass correct locale for case changing functions
        // "IL".toLowerCase() in a Turkish locale returns "ıl"
        // https://stackoverflow.com/questions/11063102/using-locales-with-javas-tolowercase-and-touppercase

        String query = "SELECT LOWER(state.keyword, 'tr') " +
                "FROM elasticsearch-sql_test_index_account/account " +
                "WHERE account_number=1";

        assertThat(
                executeQuery(query),
                hitAny(
                        kvString("/fields/LOWER_1/0", equalTo("ıl")))
        );
    }

    @Test
    public void caseChangeWithAggregationTest() throws IOException {
        String query = "SELECT UPPER(e.firstname), COUNT(*)" +
                "FROM elasticsearch-sql_test_index_account/account e " +
                "WHERE LOWER(e.lastname)='duke' " +
                "GROUP BY UPPER(e.firstname) ";

        assertThat(
                executeQuery(query),
                hitAny("/aggregations/UPPER_1/buckets", kvString("/key", equalTo("AMBER"))));
    }

    @Test
    public void castIntFieldToDoubleWithoutAliasTest() throws IOException {
        String query = "SELECT CAST(age AS DOUBLE) FROM " + TestsConstants.TEST_INDEX_ACCOUNT +
                        " ORDER BY age DESC LIMIT 5";

        SearchHit[] hits = query(query).getHits();
        checkSuccessfulFieldCast(hits, "cast_age", "DOUBLE");
        for (int i = 0; i < hits.length; ++i) {
            Assert.assertThat(hits[i].getFields().get("cast_age").getValue(), is(40.0));
        }
    }

    @Test
    public void castIntFieldToDoubleWithAliasTest() throws IOException {
        String query = "SELECT CAST(age AS DOUBLE) AS test_alias FROM " + TestsConstants.TEST_INDEX_ACCOUNT +
                        " ORDER BY age LIMIT 5";

        SearchHit[] hits = query(query).getHits();
        checkSuccessfulFieldCast(hits, "test_alias", "DOUBLE");
        for (int i = 0; i < hits.length; ++i) {
            Assert.assertThat(hits[i].getFields().get("test_alias").getValue(), is(20.0));
        }
    }

    @Test
    public void castIntFieldToStringWithoutAliasTest() throws IOException {
        String query = "SELECT CAST(balance AS STRING) FROM " + TestsConstants.TEST_INDEX_ACCOUNT +
                        " ORDER BY balance LIMIT 1";

        SearchHit[] hits = query(query).getHits();
        checkSuccessfulFieldCast(hits, "cast_balance", "STRING");
        for (int i = 0; i < hits.length; ++i) {
            Assert.assertThat(hits[i].getFields().get("cast_balance").getValue(), is("1011"));
        }
    }

    @Test
    public void castIntFieldToStringWithAliasTest() throws IOException {
        String query = "SELECT CAST(balance AS STRING) AS cast_string_alias FROM " + TestsConstants.TEST_INDEX_ACCOUNT +
                        " ORDER BY cast_string_alias DESC LIMIT 1";

        SearchHit[] hits = query(query).getHits();
        checkSuccessfulFieldCast(hits, "cast_string_alias", "STRING");
        for (int i = 0; i < hits.length; ++i) {
            Assert.assertThat(hits[i].getFields().get("cast_string_alias").getValue(), is("9838"));
        }
    }

    @Test
    public void castIntFieldToFloatWithoutAliasJdbcFormatTest() {
        JSONObject response = executeJdbcRequest(
                "SELECT CAST(balance AS FLOAT) FROM " + TestsConstants.TEST_INDEX_ACCOUNT +
                        " ORDER BY balance DESC LIMIT 1");

        String float_type_cast = "{\"name\":\"cast_balance\",\"type\":\"float\"}";
        assertEquals(response.getJSONArray("schema").get(0).toString(), float_type_cast);
        Assert.assertThat(
                response.getJSONArray("datarows")
                        .getJSONArray(0).getFloat(0),
                equalTo(49989.0F));
    }

    @Test
    public void castIntFieldToFloatWithAliasJdbcFormatTest() {
        JSONObject response = executeJdbcRequest(
                "SELECT CAST(balance AS FLOAT) AS jdbc_float_alias " +
                        "FROM " + TestsConstants.TEST_INDEX_ACCOUNT + " ORDER BY jdbc_float_alias LIMIT 1");

        String float_type_cast = "{\"name\":\"jdbc_float_alias\",\"type\":\"float\"}";
        assertEquals(response.getJSONArray("schema").get(0).toString(), float_type_cast);
        Assert.assertThat(
                response.getJSONArray("datarows")
                        .getJSONArray(0).getFloat(0),
                equalTo(1011.0F));

    }

    @Test
    public void castIntFieldToDoubleWithoutAliasOrderByTest() throws IOException {
        String query = "SELECT CAST(age AS DOUBLE) FROM " + TestsConstants.TEST_INDEX_ACCOUNT + " ORDER BY age LIMIT 1";

        SearchHit[] hits = query(query).getHits();
        checkSuccessfulFieldCast(hits, "cast_age", "DOUBLE");
        for (int i = 0; i < hits.length; ++i) {
            Assert.assertThat(hits[i].getFields().get("cast_age").getValue(), is(20.0));
        }
    }

    @Test
    public void castIntFieldToDoubleWithAliasOrderByTest() throws IOException {
        String query = "SELECT CAST(age AS DOUBLE) AS alias FROM " + TestsConstants.TEST_INDEX_ACCOUNT +
                        " ORDER BY alias DESC LIMIT 1";

        SearchHit[] hits = query(query).getHits();
        checkSuccessfulFieldCast(hits, "alias", "DOUBLE");
        for (int i = 0; i < hits.length; ++i) {
            Assert.assertThat(hits[i].getFields().get("alias").getValue(), is(40.0));
        }
    }

    @Test
    public void castIntFieldToFloatWithoutAliasJdbcFormatGroupByTest() {
        JSONObject response = executeJdbcRequest(
                "SELECT CAST(balance AS FLOAT) FROM " +
                        TestsConstants.TEST_INDEX_ACCOUNT + " GROUP BY balance DESC LIMIT 5");

        String float_type_cast = "{\"name\":\"balance\",\"type\":\"long\"}";
        assertEquals(response.getJSONArray("schema").get(0).toString(), float_type_cast);
        Float[] expectedOutput = new Float[] {22026.0F, 23285.0F, 36038.0F, 39063.0F, 45493.0F};

        for (int i = 0; i < response.getJSONArray("datarows").length(); ++i) {
            Assert.assertThat(
                    response.getJSONArray("datarows")
                            .getJSONArray(i).getFloat(0),
                    equalTo(expectedOutput[i]));
        }
    }

    @Test
    public void castIntFieldToFloatWithAliasJdbcFormatGroupByTest() {
        JSONObject response = executeJdbcRequest(
                "SELECT CAST(balance AS FLOAT) AS jdbc_float_alias " +
                        "FROM " + TestsConstants.TEST_INDEX_ACCOUNT + " GROUP BY jdbc_float_alias ASC LIMIT 5");

        String float_type_cast = "{\"name\":\"jdbc_float_alias\",\"type\":\"float\"}";
        assertEquals(response.getJSONArray("schema").get(0).toString(), float_type_cast);
        Float[] expectedOutput = new Float[] {22026.0F, 23285.0F, 36038.0F, 39063.0F, 45493.0F};

        for (int i = 0; i < response.getJSONArray("datarows").length(); ++i) {
            Assert.assertThat(
                    response.getJSONArray("datarows")
                            .getJSONArray(i).getFloat(0),
                    equalTo(expectedOutput[i]));
        }
    }

    @Test
    public void castIntFieldToDoubleWithAliasJdbcFormatGroupByTest() {
        JSONObject response = executeJdbcRequest(
                "SELECT CAST(age AS DOUBLE) AS jdbc_double_alias " +
                        "FROM " + TestsConstants.TEST_INDEX_ACCOUNT + " GROUP BY jdbc_double_alias DESC LIMIT 5");

        String float_type_cast = "{\"name\":\"jdbc_double_alias\",\"type\":\"double\"}";
        assertEquals(response.getJSONArray("schema").get(0).toString(), float_type_cast);
        Double[] expectedOutput = new Double[] {31.0, 39.0, 26.0, 32.0, 35.0};

        for (int i = 0; i < response.getJSONArray("datarows").length(); ++i) {
            Assert.assertThat(
                    response.getJSONArray("datarows")
                            .getJSONArray(i).getDouble(0),
                    equalTo(expectedOutput[i]));
        }
    }

    @Test
    public void concat_ws_field_and_string() throws Exception {
        //here is a bug,csv field with spa
        String query = "SELECT " +
                " concat_ws('-',age,'-') as age,address from " +
                TEST_INDEX_ACCOUNT + "/account " +
                " limit 10  ";

        assertThat(
                executeQuery(query),
                hitAny(kvString("/fields/age/0", endsWith("--")))
        );
    }

    /**
     * Ignore this test case because painless doesn't whitelist String.split function.
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/painless/7.0/painless-api-reference.html">https://www.elastic.co/guide/en/elasticsearch/painless/7.0/painless-api-reference.html</a>
     */
    @Ignore
    public void whereConditionLeftFunctionRightVariableEqualTest() throws Exception {

        String query = "SELECT " +
                " * from " +
                TestsConstants.TEST_INDEX + "/account " +
                " where split(address,' ')[0]='806' limit 1000  ";

        assertThat(executeQuery(query).query("/hits/total"), equalTo(4));
    }

    /**
     * Ignore this test case because painless doesn't whitelist String.split function.
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/painless/7.0/painless-api-reference.html">https://www.elastic.co/guide/en/elasticsearch/painless/7.0/painless-api-reference.html</a>
     */
    @Ignore
    public void whereConditionLeftFunctionRightVariableGreatTest() throws Exception {

        String query = "SELECT " +
                " * from " +
                TestsConstants.TEST_INDEX + "/account " +
                " where floor(split(address,' ')[0]+0) > 805 limit 1000  ";

        assertThat(executeQuery(query).query("/hits/total"), equalTo(223));
    }

    @Test
    public void concat_ws_fields() throws Exception {

        //here is a bug,csv field with spa
        String query = "SELECT " +
                " concat_ws('-',age,address) as combine,address from " +
                TEST_INDEX_ACCOUNT + "/account " +
                " limit 10  ";
        assertThat(
                executeQuery(query),
                hitAny(kvString("/fields/combine/0", containsString("-")))
        );
    }

    @Test
    public void functionLogs() throws Exception {
        String query = "SELECT log10(100) as a, log(1) as b, log(2, 4) as c, log2(8) as d from "
                + TEST_INDEX_ACCOUNT + "/account limit 1";

        assertThat(
                executeQuery(query),
                hitAny(both(kvDouble("/fields/a/0", equalTo(2.0)))
                        .and(kvDouble("/fields/b/0", equalTo(0.0)))
                        .and(kvDouble("/fields/c/0", equalTo(1.0)))
                        .and(kvDouble("/fields/d/0", equalTo(3.0))))
        );
    }

    @Test
    public void functionPow() throws Exception {
        String query = "SELECT pow(account_number, 2) as key,"+
                "abs(age - 60) as new_age from " + TEST_INDEX_ACCOUNT + "/account WHERE firstname = 'Virginia' and lastname='Ayala' limit 1";

        assertThat(
                executeQuery(query),
                hitAny(both(kvDouble("/fields/new_age/0", equalTo(21.0))).and(kvDouble("/fields/key/0", equalTo(625.0))))
        );
    }

    @Test
    public void operatorSubstring() throws IOException {
        assertThat(
                executeQuery("SELECT substring('sampleName', 1, 4) AS substring FROM " + TEST_INDEX_ACCOUNT),
                hitAny(kvString("/fields/substring/0", equalTo("samp")))
        );

        assertThat(
                executeQuery("SELECT substring('sampleName', 0, 20) AS substring FROM " + TEST_INDEX_ACCOUNT),
                hitAny(kvString("/fields/substring/0", equalTo("sampleName")))
        );
    }

    @Test
    public void operatorLength() throws IOException {
        assertThat(
                executeQuery("SELECT LENGTH(lastname) FROM " + TEST_INDEX_ACCOUNT
                                + " WHERE lastname IS NOT NULL GROUP BY LENGTH(lastname) ORDER BY LENGTH(lastname)", "jdbc"),
                containsString("\"type\": \"integer\"")
        );

        assertThat(
                executeQuery("SELECT LENGTH('sampleName') AS length FROM " + TEST_INDEX_ACCOUNT),
                hitAny(kvInt("/fields/length/0", equalTo(10)))
        );

    }

    @Test
    public void operatorReplace() {
        String query = "SELECT REPLACE('elastic', 'el', 'fant') FROM " + TEST_INDEX_ACCOUNT;
        assertThat(
                executeQuery(query, "jdbc"),
                containsString("fantastic")
        );
    }

    @Test
    public void operatorLocate() throws IOException {
        String query = "SELECT LOCATE('a', lastname, 0) FROM " + TEST_INDEX_ACCOUNT
                + " WHERE lastname IS NOT NULL GROUP BY LOCATE('a', lastname, 0) ORDER BY LOCATE('a', lastname, 0)";
        assertThat(
                executeQuery(query, "jdbc"), containsString("\"type\": \"integer\"")
        );

        assertThat(
                executeQuery("SELECT LOCATE('a', 'sampleName', 3) AS locate FROM " + TEST_INDEX_ACCOUNT),
                hitAny(kvInt("/fields/locate/0", equalTo(8)))
        );
        assertThat(
                executeQuery("SELECT LOCATE('a', 'sampleName') AS locate FROM " + TEST_INDEX_ACCOUNT),
                hitAny(kvInt("/fields/locate/0", equalTo(2)))
        );
    }

    @Test
    public void rtrim() throws IOException {
        assertThat(
                executeQuery("SELECT RTRIM(' sampleName  ') AS rtrim FROM " + TEST_INDEX_ACCOUNT),
                hitAny(kvString("/fields/rtrim/0", equalTo(" sampleName")))
        );
    }

    @Test
    public void ltrim() throws IOException {
        assertThat(
                executeQuery("SELECT LTRIM(' sampleName  ') AS ltrim FROM " + TEST_INDEX_ACCOUNT),
                hitAny(kvString("/fields/ltrim/0",equalTo( "sampleName  ")))
        );
    }

    @Test
    public void ascii() throws IOException {
        assertThat(
                executeQuery("SELECT ASCII(lastname) FROM " + TEST_INDEX_ACCOUNT
                        + " WHERE lastname IS NOT NULL GROUP BY ASCII(lastname) ORDER BY ASCII(lastname) LIMIT 5",
                        "jdbc"),
                containsString("\"type\": \"integer\"")
        );
        assertThat(
                executeQuery("SELECT ASCII('sampleName') AS ascii FROM " + TEST_INDEX_ACCOUNT),
                hitAny(kvInt("/fields/ascii/0", equalTo(115)))
        );
    }

    /**
     * Ignore this test case because painless doesn't whitelist String.split function.
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/painless/7.0/painless-api-reference.html">https://www.elastic.co/guide/en/elasticsearch/painless/7.0/painless-api-reference.html</a>
     */
    @Ignore
    public void split_field() throws Exception {

        //here is a bug,csv field with spa
        String query = "SELECT " +
                " split(address,' ')[0],age from " +
                TestsConstants.TEST_INDEX + "/account where address is not null " +
                " limit 10  ";
    }

    @Test
    public void literal() throws Exception {
        String query = "SELECT 10 "+
                "from " + TEST_INDEX_ACCOUNT + "/account limit 1";
        final SearchHit[] hits = query(query).getHits();
        assertThat(hits[0].getFields(), hasValue(contains(10)));
    }

    @Test
    public void literalWithDoubleValue() throws Exception {
        String query = "SELECT 10.0 "+
                "from " + TEST_INDEX_ACCOUNT + "/account limit 1";

        final SearchHit[] hits = query(query).getHits();
        assertThat(hits[0].getFields(), hasValue(contains(10.0)));
    }

    @Test
    public void literalWithAlias() throws Exception {
        String query = "SELECT 10 as key "+
                "from " + TEST_INDEX_ACCOUNT + "/account limit 1";
        final SearchHit[] hits = query(query).getHits();

        assertThat(hits.length, is(1));
        assertThat(hits[0].getFields(), hasEntry(is("key"), contains(10)));
    }

    @Test
    public void literalMultiField() throws Exception {
        String query = "SELECT 1, 2 "+
                "from " + TEST_INDEX_ACCOUNT + "/account limit 1";
        final SearchHit[] hits = query(query).getHits();

        assertThat(hits.length, is(1));
        assertThat(hits[0].getFields(), allOf(hasValue(contains(1)), hasValue(contains(2))));
    }

    private SearchHits query(String query) throws IOException {
        final String rsp = executeQueryWithStringOutput(query);

        final XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                rsp);
        return SearchResponse.fromXContent(parser).getHits();
    }

    private void checkSuccessfulFieldCast(SearchHit[] hits, String field, String castType) {
        for (int i = 0; i < hits.length; ++i) {
            Assert.assertThat(hits[i].getFields(), IsMapContaining.hasKey(field));
            switch (castType) {
                case "FLOAT":
                    assertTrue(hits[i].getFields().get(field).getValue() instanceof Float);
                    break;
                case "DOUBLE":
                    assertTrue(hits[i].getFields().get(field).getValue() instanceof Double);
                    break;
                case "INT":
                    assertTrue(hits[i].getFields().get(field).getValue() instanceof Integer);
                    break;
                case "STRING":
                    assertTrue(hits[i].getFields().get(field).getValue() instanceof String);
                    break;
                case "DATETIME":
                    assertTrue(hits[i].getFields().get(field).getValue() instanceof Date);
                    break;
                case "LONG":
                    assertTrue(hits[i].getFields().get(field).getValue() instanceof Long);
                    break;
            }
        }
    }

    private JSONObject executeJdbcRequest(String query) {
        return new JSONObject(executeQuery(query, "jdbc"));
    }
}
