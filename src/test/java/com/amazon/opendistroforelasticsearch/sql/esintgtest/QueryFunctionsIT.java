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

import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
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
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_NESTED_TYPE;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_PHRASE;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

public class QueryFunctionsIT extends SQLIntegTestCase {

    private static final String SELECT_ALL = "SELECT *";
    private static final String FROM_ACCOUNTS = "FROM " + TEST_INDEX_ACCOUNT + "/account";
    private static final String FROM_NESTED = "FROM " + TEST_INDEX_NESTED_TYPE + "/nestedType";
    private static final String FROM_PHRASE = "FROM " + TEST_INDEX_PHRASE + "/phrase";

    /**
     * TODO Looks like Math/Date Functions test all use the same query() and execute() functions
     * TODO execute/featureValueOf/hits functions are the same as used in NestedFieldQueryIT, should refactor into util
     */

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.NESTED);
        loadIndex(Index.PHRASE);
    }

    @Test
    public void query() throws IOException {
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
    public void matchQueryRegularField() throws IOException {
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
    public void matchQueryNestedField() throws IOException {
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
                anyOf(hasNestedField("comment", "data", "aa"),
                        hasNestedArrayField("comment", "data", "aa"))
            )
        );
    }

    @Test
    public void scoreQuery() throws IOException {
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
    public void scoreQueryWithNestedField() throws IOException {
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
    public void wildcardQuery() throws IOException {
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
    public void matchPhraseQuery() throws IOException {
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
    public void multiMatchQuerySingleField() throws IOException {
        assertThat(
            query(
                "SELECT firstname",
                FROM_ACCOUNTS,
                "WHERE MULTI_MATCH('query'='Ayers', 'fields'='firstname')"
            ),
            hits(
                hasValueForFields("Ayers", "firstname")
            )
        );
    }

    @Test
    public void multiMatchQueryWildcardField() throws IOException {
        assertThat(
            query(
                "SELECT firstname, lastname",
                FROM_ACCOUNTS,
                "WHERE MULTI_MATCH('query'='Bradshaw', 'fields'='*name')"
            ),
            hits(
                hasValueForFields("Bradshaw", "firstname", "lastname")
            )
        );
    }

    @Test
    public void numberLiteralInSelectField() {
        assertTrue(
                executeQuery(StringUtils.format("SELECT 234234 AS number from %s", TEST_INDEX_ACCOUNT), "jdbc")
                .contains("234234")
        );

        assertTrue(
                executeQuery(StringUtils.format("SELECT 2.34234 AS number FROM %s", TEST_INDEX_ACCOUNT), "jdbc")
                .contains("2.34234")
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

    private final Matcher<SearchHit> hasNestedArrayField(String path, String field, String value) {

        return new BaseMatcher<SearchHit>() {
            @Override
            public void describeTo(Description description) {

            }

            @Override
            public boolean matches(Object item) {

                SearchHit hit = (SearchHit)item;
                List<Object> elements = (List<Object>) ((HashMap) hit.getSourceAsMap().get(path)).get(field);
                return elements.contains(value);
            }
        };
    }

    private Matcher<SearchHit> kv(String key, Matcher<Object> valMatcher) {
        return featureValueOf(key, valMatcher, hit -> hit.getSourceAsMap().get(key));
    }

    /***********************************************************
                Query Utility to Fetch Response for SQL
     ***********************************************************/

    private SearchResponse query(String select, String from, String... statements) throws IOException {
        return execute(select + " " + from + " " + String.join(" ", statements));
    }

    private SearchResponse execute(String sql) throws IOException {
        final JSONObject jsonObject = executeQuery(sql);

        final XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                jsonObject.toString());
        return SearchResponse.fromXContent(parser);
    }
}
