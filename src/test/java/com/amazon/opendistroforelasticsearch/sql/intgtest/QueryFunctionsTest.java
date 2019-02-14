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

import com.amazon.opendistroforelasticsearch.sql.plugin.SearchDao;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.query.SqlElasticRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

public class QueryFunctionsTest {

    private static final String SELECT_ALL = "SELECT *";
    private static final String FROM_ACCOUNTS = "FROM " + TestsConstants.TEST_INDEX_ACCOUNT + "/account";
    private static final String FROM_NESTED = "FROM " + TestsConstants.TEST_INDEX_NESTED_TYPE + "/nestedType";
    private static final String FROM_PHRASE = "FROM " + TestsConstants.TEST_INDEX_PHRASE + "/phrase";

    /**
     * TODO Looks like Math/Date Functions test all use the same query() and execute() functions
     * TODO execute/featureValueOf/hits functions are the same as used in NestedFieldQueryTest, should refactor into util
     */

    @Test
    public void query() {
        assertThat(
            query(
                "SELECT state",
                FROM_ACCOUNTS,
                "WHERE QUERY('CA')"
            ),
            hits(
                hasValueForFields("CA", "state")
            )
        );
    }

    @Test
    public void matchQueryRegularField() {
        assertThat(
            query(
                "SELECT firstname",
                FROM_ACCOUNTS,
                "WHERE MATCH_QUERY(firstname, 'Ayers')"
            ),
            hits(
                hasValueForFields("Ayers", "firstname")
            )
        );
    }

    @Test
    public void matchQueryNestedField() {
        SearchHit[] hits = query("SELECT comment.data", FROM_NESTED, "WHERE MATCH_QUERY(NESTED(comment.data), 'aa')").getHits().getHits();
        Map<String, Object> source = hits[0].getSourceAsMap();
        // SearchHits innerHits = hits[0].getInnerHits().get("comment");
        assertThat(
            query(
                "SELECT comment.data",
                FROM_NESTED,
                "WHERE MATCH_QUERY(NESTED(comment.data), 'aa')"
            ),
            hits(
                //hasValueForFields("aa", "comment.data")
                hasNestedField("comment",
                    "data", "aa")
            )
        );
    }

    @Test
    public void scoreQuery() {
        assertThat(
            query(
                "SELECT firstname",
                FROM_ACCOUNTS,
                "WHERE SCORE(MATCH_QUERY(firstname, 'Ayers'), 10)"
            ),
            hits(
                hasValueForFields("Ayers", "firstname")
            )
        );
    }

    @Test
    public void scoreQueryWithNestedField() {
        assertThat(
            query(
                "SELECT comment.data",
                FROM_NESTED,
                "WHERE SCORE(MATCH_QUERY(NESTED(comment.data), 'ab'), 10)"
            ),
            hits(
                //hasValueForFields("ab", "comment.data")
                hasNestedField("comment",
                    "data", "ab")
            )
        );
    }

    @Test
    public void wildcardQuery() {
        assertThat(
            query(
                "SELECT city",
                FROM_ACCOUNTS,
                "WHERE WILDCARD_QUERY(city.keyword, 'B*')"
            ),
            hits(
                hasFieldWithPrefix("city", "B")
            )
        );
    }

    @Test
    public void matchPhraseQuery() {
        assertThat(
            query(
                "SELECT phrase",
                FROM_PHRASE,
                "WHERE MATCH_PHRASE(phrase, 'brown fox')"
            ),
            hits(
                hasValueForFields("brown fox", "phrase")
            )
        );
    }

    @Test
    public void multiMatchQuerySingleField() {
        assertThat(
            query(
                "SELECT firstname",
                FROM_ACCOUNTS,
                "WHERE MULTI_MATCH(query='Ayers', fields='firstname')"
            ),
            hits(
                hasValueForFields("Ayers", "firstname")
            )
        );
    }

    @Test
    public void multiMatchQueryWildcardField() {
        assertThat(
            query(
                "SELECT firstname, lastname",
                FROM_ACCOUNTS,
                "WHERE MULTI_MATCH(query='Bradshaw', fields='*name')"
            ),
            hits(
                hasValueForFields("Bradshaw", "firstname", "lastname")
            )
        );
    }

    private final Matcher<SearchResponse> hits(Matcher<SearchHit> subMatcher) {
        return featureValueOf("hits", everyItem(subMatcher), resp -> Arrays.asList(resp.getHits().getHits()));
    }

    private <T, U> FeatureMatcher<T, U> featureValueOf(String name, Matcher<U> subMatcher, Function<T, U> getter) {
        return new FeatureMatcher<T, U>(subMatcher, name, name) {
            @Override
            protected U featureValueOf(T actual) {
                return getter.apply(actual);
            }
        };
    }

    /**
     * Create Matchers for each field -> value
     * Only one of the Matchers need to match (per hit)
     *
     * Ex. If a query with wildcard field is made:
     *     multi_match(query="Ayers", fields="*name")
     *
     *     Then the value "Ayers" can be found in either the firstname or lastname field. Only one of these fields
     *     need to satisfy the query value to be evaluated as correct expected output.
     *
     * @param value The value to match for a field in the sourceMap
     * @param fields A list of fields to match
     */
    @SafeVarargs
    private final Matcher<SearchHit> hasValueForFields(String value, String... fields) {
        return anyOf(
                Arrays.asList(fields).
                        stream().
                        map(field -> kv(field, is(value))).
                        collect(Collectors.toList()));
    }

    private final Matcher<SearchHit> hasFieldWithPrefix(String field, String prefix) {
        return featureValueOf(field, startsWith(prefix), hit -> (String) hit.getSourceAsMap().get(field));
    }

    private final Matcher<SearchHit> hasNestedField(String path, String field, String value) {
        return featureValueOf(field, is(value), hit -> ((HashMap) hit.getSourceAsMap().get(path)).get(field));
    }

    private Matcher<SearchHit> kv(String key, Matcher<Object> valMatcher) {
        return featureValueOf(key, valMatcher, hit -> hit.getSourceAsMap().get(key));
    }

    /***********************************************************
                Query Utility to Fetch Response for SQL
     ***********************************************************/

    private SearchResponse query(String select, String from, String... statements) {
        return execute(select + " " + from + " " + String.join(" ", statements));
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
