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
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.IntStream;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.hitAny;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvDouble;
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
}
