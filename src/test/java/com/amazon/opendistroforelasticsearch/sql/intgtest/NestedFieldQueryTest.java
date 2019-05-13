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

package com.amazon.opendistroforelasticsearch.sql.intgtest;

import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.plugin.SearchDao;
import com.amazon.opendistroforelasticsearch.sql.query.SqlElasticRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
public class NestedFieldQueryTest {

    private static final String FROM = "FROM " + TestsConstants.TEST_INDEX_NESTED_TYPE + "/nestedType n, n.message m";

    @Test
    public void selectAll() {
        queryAll("SELECT *");
    }

    @Test
    public void noCondition() {
        queryAll("SELECT myNum, someField, m.author, m.info");
    }

    private void queryAll(String sql) {
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
    public void singleCondition() {
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
    public void multipleConditionsOfNestedField() {
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
    public void multipleConditionsOfNestedFieldNoMatch() {
        assertThat(
            query(
                "SELECT someField, m.author, m.info",
                "WHERE m.info = 'c' AND m.author = 'i'"
            ),
            hits()
        );
    }

    @Test
    public void multipleConditionsOfRegularAndNestedField() {
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
    public void multipleConditionsOfRegularOrNestedField() {
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
    public void aggregationWithoutGroupBy() {
        SearchResponse resp = query("SELECT AVG(m.dayOfWeek) AS avgDay");
        Avg avgDay = getNestedAgg(resp, "message.dayOfWeek", "avgDay");
        assertThat(avgDay.getValue(), isCloseTo(3.166666666));
    }

    @Test
    public void groupByNestedFieldAndCount() {
        SearchResponse resp = query(
            "SELECT m.info, COUNT(*)", "GROUP BY m.info"
        );
        assertThat(
            getNestedAgg(resp, "message.info", "message.info"),
            buckets(
                bucket("a", count(equalTo(2))),
                bucket("c", count(equalTo(2))),
                bucket("b", count(equalTo(1))),
                bucket("zz", count(equalTo(1)))
            )
        );
    }

    @Test
    public void groupByRegularFieldAndSum() {
        SearchResponse resp = query(
            "SELECT *, SUM(m.dayOfWeek) AS sumDay",
            "GROUP BY someField"
        );
        assertThat(
            getAgg(resp, "someField"),
            buckets(
                bucket("a", sum("message.dayOfWeek", "sumDay", isCloseTo(9.0))),
                bucket("b", sum("message.dayOfWeek", "sumDay", isCloseTo(10.0)))
            )
        );
    }

    // Doesn't support: aggregate function other than COUNT()
    @SuppressWarnings("unused")
    public void groupByNestedFieldAndAvg() {
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
    public void groupByNestedAndRegularField() {
        SearchResponse resp = query(
            "SELECT someField, m.info, COUNT(*)",
            "GROUP BY someField, m.info"
        );
        Terms agg = getAgg(resp, "someField");
        assertThat(
            getNestedAgg(agg.getBucketByKey("a").getAggregations(), "message.info", "message.info"),
            buckets(
                bucket("b", count(equalTo(1))),
                bucket("c", count(equalTo(1))),
                bucket("zz", count(equalTo(1)))
            )
        );
        assertThat(
            getNestedAgg(agg.getBucketByKey("b").getAggregations(), "message.info", "message.info"),
            buckets(
                bucket("a", count(equalTo(2))),
                bucket("c", count(equalTo(1)))
            )
        );

        //doesn't support: group by nested field first
        /*query(
            "SELECT *, SUM(m.dayOfWeek)",
            "GROUP BY m.author, someField"
        );*/
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
                    List<Integer> actualValues = (ArrayList<Integer>) hit.getSourceAsMap().get("myNum");

                    if (actualValues.size() != values.length) {
                        return false;
                    }
                    for (int value : values) {
                        if (!actualValues.contains(value)) {
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

    private <T extends Aggregation> T getAgg(SearchResponse resp,
                                             String fieldName) {
        return resp.getAggregations().get(fieldName);
    }

    private <T extends Aggregation> T getNestedAgg(SearchResponse resp,
                                                   String nestedFieldName,
                                                   String aggAlias) {
        return getNestedAgg(resp.getAggregations(), nestedFieldName, aggAlias);
    }

    private <T extends Aggregation> T getNestedAgg(Aggregations aggs,
                                                   String nestedFieldName,
                                                   String aggAlias) {
        return aggs.<InternalNested>get(nestedFieldName + "@NESTED").
                    getAggregations().
                    get(aggAlias);
    }

    @SafeVarargs
    private final Matcher<Terms> buckets(Matcher<Terms.Bucket>... subMatchers) {
        return featureValueOf("buckets", containsInAnyOrder(subMatchers), Terms::getBuckets);
    }

    private Matcher<Terms.Bucket> bucket(String key, Matcher<Aggregations> valMatcher) {
        return featureValueOf(key, valMatcher, Terms.Bucket::getAggregations);
    }

    private Matcher<Aggregations> count(Matcher<Long> subMatcher) {
        return featureValueOf("COUNT(*)", subMatcher, aggs -> ((ValueCount) aggs.get("COUNT(*)")).getValue());
    }

    private Matcher<Aggregations> sum(String nestedFieldName, String name, Matcher<Double> subMatcher) {
        return featureValueOf(name, subMatcher, aggs -> ((Sum) getNestedAgg(aggs, nestedFieldName, name)).getValue());
    }

    private Matcher<Double> isCloseTo(double expected) {
        return closeTo(expected, 0.01);
    }

    private Matcher<Long> equalTo(int expected) {
        return is((long) expected);
    }

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

    private SearchResponse query(String select, String... statements) {
        return execute(select + " " + FROM + " " + String.join(" ", statements));
    }

    private SearchResponse execute(String sql) {
        SearchDao searchDao = MainTestSuite.getSearchDao();
        try {
            SqlElasticRequestBuilder result = searchDao.explain(sql).explain();
            return (SearchResponse) result.get();
        }
        catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new IllegalStateException("Query failed: " + sql, e);
        }
    }

}
