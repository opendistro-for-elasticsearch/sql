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

import com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Integration test cases for both rewriting and projection logic.
 *
 * Test result:
 *  1) SELECT * or any field or aggregate function on any field
 *  2) WHERE single or multiple conditions on nested type
 *  3) GROUP BY regular field and aggregate on any field
 *  4) GROUP BY nested field and COUNT(*)
 *  5) UNION/MINUS but only SELECT nested field
 *
 * Does NOT support:
 *  1) GROUP BY nested field and aggregate other than COUNT
 *  2) UNION/MINUS and SELECT nested field (need to flatten during set computation)
 *  3) JOIN (doesn't work if put alias before nested field name and may have similar problem as UNION/MINUS during computation)
 *  4) Subquery
 *  5) HAVING
 *  6) Verification for conditions mixed with regular and nested fields
 */
public class NestedFieldQueryIT extends SQLIntegTestCase {

    private static final String FROM = "FROM " + TestsConstants.TEST_INDEX_NESTED_TYPE + "/nestedType n, n.message m";

    @Override
    protected void init() throws Exception {
        loadIndex(Index.NESTED);
    }

    @Test
    public void selectAll() throws IOException {
        queryAll("SELECT *");
    }

    @Test
    public void noCondition() throws IOException {
        queryAll("SELECT myNum, someField, m.author, m.info");
    }

    private void queryAll(String sql) throws IOException {
        assertThat(
            query(sql),
            hits(
                hit(
                    myNum(1),
                    someField("b"),
                    innerHits("message",
                        hit(
                            author("e"),
                            info("a")
                        )
                    )
                ),
                hit(
                    myNum(2),
                    someField("a"),
                    innerHits("message",
                        hit(
                            author("f"),
                            info("b")
                        )
                    )
                ),
                hit(
                    myNum(3),
                    someField("a"),
                    innerHits("message",
                        hit(
                            author("g"),
                            info("c")
                        )
                    )
                ),
                hit(
                    myNum(4),
                    someField("b"),
                    innerHits("message",
                        hit(
                            author("h"),
                            info("c")
                        ),
                        hit(
                            author("i"),
                            info("a")
                        )
                    )
                ),
                hit(
                    myNum(new int[]{3, 4}),
                    someField("a"),
                    innerHits("message",
                        hit(
                            author("zz"),
                            info("zz")
                        )
                    )
                )
            )
        );
    }

    @Test
    public void singleCondition() throws IOException {
        assertThat(
            query(
                "SELECT myNum, m.author, m.info",
                "WHERE m.info = 'c'"
            ),
            hits(
                hit(
                    myNum(3),
                    innerHits("message",
                        hit(
                            author("g"),
                            info("c")
                        )
                    )
                ),
                hit(
                    myNum(4),
                    innerHits("message",
                        hit(
                            author("h"),
                            info("c")
                        )
                    )
                )
            )
        );
    }

    @Test
    public void multipleConditionsOfNestedField() throws IOException {
        assertThat(
            query(
                "SELECT someField, m.author, m.info",
                "WHERE m.info = 'c' AND m.author = 'h'"
            ),
            hits(
                hit(
                    someField("b"),
                    innerHits("message",
                        hit(
                            author("h"),
                            info("c")
                        )
                    )
                )
            )
        );
    }

    @Test
    public void multipleConditionsOfNestedFieldNoMatch() throws IOException {
        assertThat(
            query(
                "SELECT someField, m.author, m.info",
                "WHERE m.info = 'c' AND m.author = 'i'"
            ),
            hits()
        );
    }

    @Test
    public void multipleConditionsOfRegularAndNestedField() throws IOException {
        assertThat(
            query(
                "SELECT myNum, m.author, m.info",
                "WHERE myNum = 3 AND m.info = 'c'"
            ),
            hits(
                hit(
                    myNum(3),
                    innerHits("message",
                        hit(
                            author("g"),
                            info("c")
                        )
                    )
                )
            )
        );
    }

    @Test
    public void multipleConditionsOfRegularOrNestedField() throws IOException {
        assertThat(
            query(
                "SELECT myNum, m.author, m.info",
                "WHERE myNum = 2 OR m.info = 'c'"
            ),
            hits(
                hit(
                    myNum(2)
                ), // Note: no inner hit here because of no match in nested field
                hit(
                    myNum(3),
                    innerHits("message",
                        hit(
                            author("g"),
                            info("c")
                        )
                    )
                ),
                hit(
                    myNum(4),
                    innerHits("message",
                        hit(
                            author("h"),
                            info("c")
                        )
                    )
                )
            )
        );
    }

    @Test
    public void aggregationWithoutGroupBy() throws IOException {
        String sql = "SELECT AVG(m.dayOfWeek) AS avgDay " + FROM;

        JSONObject result = executeQuery(sql);
        JSONObject aggregation = getAggregation(result, "message.dayOfWeek@NESTED");

        Assert.assertThat((Double) aggregation.query("/avgDay/value"), closeTo(3.166666666, 0.01));
    }

    @Test
    public void groupByNestedFieldAndCount() throws IOException {
        final String sql = "SELECT m.info, COUNT(*) " + FROM + " GROUP BY m.info";

        JSONObject result = executeQuery(sql);
        JSONObject aggregation = getAggregation(result, "message.info@NESTED");
        JSONArray msgInfoBuckets = (JSONArray)aggregation.optQuery("/message.info/buckets");

        Assert.assertNotNull(msgInfoBuckets);
        Assert.assertThat(msgInfoBuckets.length(), equalTo(4));
        Assert.assertThat(msgInfoBuckets.query("/0/key"), equalTo("a"));
        Assert.assertThat(msgInfoBuckets.query("/0/COUNT(*)/value"), equalTo(2));
        Assert.assertThat(msgInfoBuckets.query("/1/key"), equalTo("c"));
        Assert.assertThat(msgInfoBuckets.query("/1/COUNT(*)/value"), equalTo(2));
        Assert.assertThat(msgInfoBuckets.query("/2/key"), equalTo("b"));
        Assert.assertThat(msgInfoBuckets.query("/2/COUNT(*)/value"), equalTo(1));
        Assert.assertThat(msgInfoBuckets.query("/3/key"), equalTo("zz"));
        Assert.assertThat(msgInfoBuckets.query("/3/COUNT(*)/value"), equalTo(1));
    }

    @Test
    public void groupByRegularFieldAndSum() throws IOException {
        final String sql = "SELECT *, SUM(m.dayOfWeek) AS sumDay " + FROM + " GROUP BY someField";

        JSONObject result = executeQuery(sql);
        JSONObject aggregation = getAggregation(result, "someField");
        JSONArray msgInfoBuckets = (JSONArray)aggregation.optQuery("/buckets");

        Assert.assertNotNull(msgInfoBuckets);
        Assert.assertThat(msgInfoBuckets.length(), equalTo(2));
        Assert.assertThat(msgInfoBuckets.query("/0/key"), equalTo("a"));
        Assert.assertThat((Double) msgInfoBuckets.query("/0/message.dayOfWeek@NESTED/sumDay/value"), closeTo(9.0, 0.01));
        Assert.assertThat(msgInfoBuckets.query("/1/key"), equalTo("b"));
        Assert.assertThat((Double) msgInfoBuckets.query("/1/message.dayOfWeek@NESTED/sumDay/value"), closeTo(10.0, 0.01));
    }

    // Doesn't support: aggregate function other than COUNT()
    @SuppressWarnings("unused")
    public void groupByNestedFieldAndAvg() throws IOException {
        query(
            "SELECT m.info, AVG(m.dayOfWeek)",
            "GROUP BY m.info"
        );
        query(
            "SELECT m.info, AVG(myNum)",
            "GROUP BY m.info"
        );
    }

    @Test
    public void groupByNestedAndRegularField() throws IOException {
        final String sql = "SELECT someField, m.info, COUNT(*) " + FROM + " GROUP BY someField, m.info";

        JSONObject result = executeQuery(sql);
        JSONObject aggregation = getAggregation(result, "someField");
        JSONArray msgInfoBuckets = (JSONArray)aggregation.optQuery("/buckets");

        Assert.assertNotNull(msgInfoBuckets);
        Assert.assertThat(msgInfoBuckets.length(), equalTo(2));
        Assert.assertThat(msgInfoBuckets.query("/0/key"), equalTo("a"));
        Assert.assertThat(msgInfoBuckets.query("/1/key"), equalTo("b"));

        JSONArray innerBuckets = (JSONArray)msgInfoBuckets.optQuery("/0/message.info@NESTED/message.info/buckets");
        Assert.assertThat(innerBuckets.query("/0/key"), equalTo("b"));
        Assert.assertThat(innerBuckets.query("/0/COUNT(*)/value"), equalTo(1));
        Assert.assertThat(innerBuckets.query("/1/key"), equalTo("c"));
        Assert.assertThat(innerBuckets.query("/1/COUNT(*)/value"), equalTo(1));
        Assert.assertThat(innerBuckets.query("/2/key"), equalTo("zz"));
        Assert.assertThat(innerBuckets.query("/2/COUNT(*)/value"), equalTo(1));

        innerBuckets = (JSONArray)msgInfoBuckets.optQuery("/1/message.info@NESTED/message.info/buckets");
        Assert.assertThat(innerBuckets.query("/0/key"), equalTo("a"));
        Assert.assertThat(innerBuckets.query("/0/COUNT(*)/value"), equalTo(2));
        Assert.assertThat(innerBuckets.query("/1/key"), equalTo("c"));
        Assert.assertThat(innerBuckets.query("/1/COUNT(*)/value"), equalTo(1));
    }


    /***********************************************************
                Matchers for Non-Aggregation Testing
     ***********************************************************/

    @SafeVarargs
    private final Matcher<SearchResponse> hits(Matcher<SearchHit>... subMatchers) {
        return featureValueOf("hits", arrayContainingInAnyOrder(subMatchers), resp -> resp.getHits().getHits());
    }

    @SafeVarargs
    private final Matcher<SearchHit> hit(Matcher<SearchHit>... subMatchers) {
        return allOf(subMatchers);
    }

    private Matcher<SearchHit> myNum(int value) {
        return kv("myNum", is(value));
    }

    private Matcher<SearchHit> myNum(int[] values) {

        return new BaseMatcher<SearchHit>() {

            @Override
            public boolean matches(Object item) {

                if (item instanceof SearchHit) {
                    final SearchHit hit = (SearchHit) item;
                    ArrayList<Integer> actualValues = (ArrayList<Integer>) hit.getSourceAsMap().get("myNum");

                    if (actualValues.size() != values.length) {
                        return false;
                    }
                    for (int i = 0; i < values.length; ++i) {
                        if (values[i] != actualValues.get(i)) {
                            return false;
                        }
                    }
                    return true;
                }

                return false;
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    private Matcher<SearchHit> someField(String value) {
        return kv("someField", is(value));
    }

    private Matcher<SearchHit> author(String value) {
        return kv("author", is(value));
    }

    private Matcher<SearchHit> info(String value) {
        return kv("info", is(value));
    }

    private Matcher<SearchHit> kv(String key, Matcher<Object> valMatcher) {
        return featureValueOf(key, valMatcher, hit -> hit.getSourceAsMap().get(key));
    }

    @SafeVarargs
    private final Matcher<SearchHit> innerHits(String path, Matcher<SearchHit>... innerHitMatchers) {
        return featureValueOf(
            "innerHits",
            arrayContainingInAnyOrder(innerHitMatchers),
            hit -> hit.getInnerHits().get(path).getHits()
        );
    }

    /***********************************************************
                Matchers for Aggregation Testing
     ***********************************************************/

    private <T, U> FeatureMatcher<T, U> featureValueOf(String name, Matcher<U> subMatcher, Function<T, U> getter) {
        return new FeatureMatcher<T, U>(subMatcher, name, name) {
            @Override
            protected U featureValueOf(T actual) {
                return getter.apply(actual);
            }
        };
    }

    /***********************************************************
                Query Utility to Fetch Response for SQL
     ***********************************************************/

    private SearchResponse query(String select, String... statements) throws IOException {
        return execute(select + " " + FROM + " " + String.join(" ", statements));
    }

    private SearchResponse execute(String sql) throws IOException {
        final JSONObject jsonObject = executeQuery(sql);

        final XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                jsonObject.toString());
        return SearchResponse.fromXContent(parser);
    }

    private JSONObject getAggregation(final JSONObject queryResult, final String aggregationName)
    {
        final String aggregationsObjName = "aggregations";
        Assert.assertTrue(queryResult.has(aggregationsObjName));

        final JSONObject aggregations = queryResult.getJSONObject(aggregationsObjName);
        Assert.assertTrue(aggregations.has(aggregationName));
        return aggregations.getJSONObject(aggregationName);
    }

}
