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

import org.hamcrest.Matcher;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class HavingIT extends SQLIntegTestCase {

    private static final String SELECT_FROM_WHERE_GROUP_BY =
            "SELECT state, COUNT(*) cnt " +
            "FROM " + TestsConstants.TEST_INDEX_ACCOUNT + " " +
            "WHERE age = 30 " +
            "GROUP BY state ";

    private static final Set<Matcher<Object[]>> states1 = rowSet(1, Arrays.asList(
            "AK", "AR", "CT", "DE", "HI", "IA", "IL", "IN", "LA", "MA", "MD", "MN",
            "MO", "MT", "NC", "ND", "NE", "NH", "NJ", "NV", "SD", "VT", "WV", "WY"
    ));
    private static final Set<Matcher<Object[]>> states2 = rowSet(2, Arrays.asList("AZ", "DC", "KS", "ME"));
    private static final Set<Matcher<Object[]>> states3 = rowSet(3, Arrays.asList("AL", "ID", "KY", "OR", "TN"));

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void equalsTo() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt = 2"),
            resultSet(
                states2
            )
        );
    }

    @Test
    public void lessThanOrEqual() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt <= 2"),
            resultSet(
                states1,
                states2
            )
        );
    }

    @Test
    public void notEqualsTo() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt <> 2"),
            resultSet(
                states1,
                states3
            )
        );
    }

    @Test
    public void between() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt BETWEEN 1 AND 2"),
            resultSet(
                states1,
                states2
            )
        );
    }

    @Test
    public void notBetween() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt NOT BETWEEN 1 AND 2"),
            resultSet(
                states3
            )
        );
    }

    @Test
    public void in() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt IN (2, 3)"),
            resultSet(
                states2,
                states3
            )
        );
    }

    @Test
    public void notIn() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt NOT IN (2, 3)"),
            resultSet(
                states1
            )
        );
    }

    @Test
    public void and() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt >= 1 AND cnt < 3"),
            resultSet(
                states1,
                states2
            )
        );
    }

    @Test
    public void or() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt = 1 OR cnt = 3"),
            resultSet(
                states1,
                states3
            )
        );
    }

    @Test
    public void not() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING NOT cnt >= 2"),
            resultSet(
                states1
            )
        );
    }

    @Test
    public void notAndOr() throws IOException {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING NOT (cnt > 0 AND cnt <= 2)"),
            resultSet(
                states3
            )
        );
    }

    private Set<Object[]> query(String query) throws IOException {
        JSONObject response = executeQuery(query);
        return getResult(response, "state.keyword", "cnt");
    }

    private Set<Object[]> getResult(JSONObject response, String aggName, String aggFunc) {

        String bucketsPath = String.format(Locale.ROOT, "/aggregations/%s/buckets", aggName);
        JSONArray buckets = (JSONArray) response.query(bucketsPath);

        Set<Object[]> result = new HashSet<>();
        for (int i = 0; i < buckets.length(); i++) {
            JSONObject bucket = buckets.getJSONObject(i);
            result.add(new Object[]{
                bucket.get("key"),
                ((JSONObject) bucket.get(aggFunc)).getLong("value")
            });
        }

        return result;
    }

    @SafeVarargs
    private final Matcher<Iterable<? extends Object[]>> resultSet(Set<Matcher<Object[]>>... rowSets) {
        return containsInAnyOrder(Arrays.stream(rowSets)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }

    private static Set<Matcher<Object[]>> rowSet(long count, List<String> states) {
        return states.stream()
                .map(state -> row(state, count))
                .collect(Collectors.toSet());
    }

    private static Matcher<Object[]> row(String state, long count) {
        return arrayContaining(is(state), is(count));
    }
}
