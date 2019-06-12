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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_GAME_OF_THRONES;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test new hash join algorithm by comparison with old implementation.
 */
public class HashJoinIT extends SQLIntegTestCase {

    /**
     * Hint to use old join algorithm
     */
    private static final String USE_OLD_JOIN_ALGORITHM = "/*! USE_NL*/";

    /**
     * Set limit to 100% to bypass circuit break check
     */
    private static final String BYPASS_CIRCUIT_BREAK = "/*! JOIN_CIRCUIT_BREAK_LIMIT(100)*/";

    /**
     * Enable term filter optimization
     */
    private static final String ENABLE_TERMS_FILTER = "/*! HASH_WITH_TERMS_FILTER*/";

    /**
     * Default page size > block size
     */
    private static final String PAGE_SIZE_GREATER_THAN_BLOCK_SIZE = "/*! JOIN_ALGORITHM_BLOCK_SIZE(5)*/";

    /**
     * Page size < block size
     */
    private static final String PAGE_SIZE_LESS_THAN_BLOCK_SIZE =
            "/*! JOIN_ALGORITHM_BLOCK_SIZE(5)*/ /*! JOIN_SCROLL_PAGE_SIZE(2)*/";

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.GAME_OF_THRONES);
    }

    @Test
    public void innerJoin() throws IOException {

        testJoin("INNER JOIN");
    }

    @Test
    public void leftJoin() throws IOException {

        testJoin("LEFT JOIN");
    }

    @Test
    public void innerJoinWithObjectField() throws IOException {
        testJoinWithObjectField("INNER JOIN", "");
    }

    @Test
    public void leftJoinWithObjectField() throws IOException {
        testJoinWithObjectField("LEFT JOIN", "");
    }

    @Test
    public void innerJoinWithObjectFieldAndTermsFilter() throws IOException {
        testJoinWithObjectField("INNER JOIN", ENABLE_TERMS_FILTER);
    }

    @Test
    public void leftJoinWithObjectFieldAndTermsFilter() throws IOException {
        testJoinWithObjectField("LEFT JOIN", ENABLE_TERMS_FILTER);
    }

    @Test
    public void innerJoinWithObjectFieldAndPageSizeGreaterThanBlockSize() throws IOException {
        testJoinWithObjectField("INNER JOIN", PAGE_SIZE_GREATER_THAN_BLOCK_SIZE);
    }

    @Test
    public void leftJoinWithObjectFieldAndPageSizeGreaterThanBlockSize() throws IOException {
        testJoinWithObjectField("LEFT JOIN", PAGE_SIZE_GREATER_THAN_BLOCK_SIZE);
    }

    @Test
    public void innerJoinWithObjectFieldAndPageSizeLessThanBlockSize() throws IOException {
        testJoinWithObjectField("INNER JOIN", PAGE_SIZE_LESS_THAN_BLOCK_SIZE);
    }

    @Test
    public void leftJoinWithObjectFieldAndPageSizeLessThanBlockSize() throws IOException {
        testJoinWithObjectField("LEFT JOIN", PAGE_SIZE_LESS_THAN_BLOCK_SIZE);
    }

    private void testJoin(final String join) throws IOException {

        final String queryPrefix = "SELECT";

        // TODO: reduce the balance threshold to 10000 when the memory circuit breaker issue
        //  (https://github.com/opendistro-for-elasticsearch/sql/issues/73) is fixed.
        final String querySuffixTemplate = "a.firstname, a.lastname, b.city, b.state FROM %1$s a %2$s %1$s b " +
                "ON b.age = a.age WHERE a.balance > 45000 AND b.age > 25 LIMIT 1000000";
        final String querySuffix = String.format(Locale.ROOT, querySuffixTemplate, TEST_INDEX_ACCOUNT, join);

        final String oldQuery = String.join(" ", queryPrefix, USE_OLD_JOIN_ALGORITHM, querySuffix);
        final String newQuery = String.join(" ", queryPrefix, BYPASS_CIRCUIT_BREAK, querySuffix);

        executeAndCompareOldAndNewJoins(oldQuery, newQuery);
    }

    private void testJoinWithObjectField(final String join, final String hint) throws IOException {

        final String queryPrefix = "SELECT";

        // TODO: reduce the balance threshold to 10000 when the memory circuit breaker issue
        //  (https://github.com/opendistro-for-elasticsearch/sql/issues/73) is fixed.
        final String querySuffixTemplate = "c.name.firstname, c.name.lastname, f.hname, f.seat " +
                "FROM %1$s c %2$s %1$s f ON f.gender.keyword = c.gender.keyword " +
                "AND f.house.keyword = c.house.keyword " +
                "WHERE c.gender = 'M' LIMIT 1000000";
        final String querySuffix = String.format(Locale.ROOT, querySuffixTemplate, TEST_INDEX_GAME_OF_THRONES, join);

        final String oldQuery = String.join(" ", queryPrefix, USE_OLD_JOIN_ALGORITHM, querySuffix);
        final String newQuery = String.join(" ", queryPrefix, hint, BYPASS_CIRCUIT_BREAK, querySuffix);

        executeAndCompareOldAndNewJoins(oldQuery, newQuery);
    }

    private void executeAndCompareOldAndNewJoins(final String oldQuery, final String newQuery) throws IOException {

        final JSONObject responseOld = executeQuery(oldQuery);
        final JSONObject responseNew = executeQuery(newQuery);

        Assert.assertThat(getTotalHits(responseOld), equalTo(getTotalHits(responseNew)));

        final JSONArray hitsOld = getHits(responseOld);
        final JSONArray hitsNew = getHits(responseNew);

        Assert.assertThat(hitsOld.length(), equalTo(hitsNew.length()));

        Set<String> idsOld = new HashSet<>();

        hitsOld.forEach(hitObj -> {
            JSONObject hit = (JSONObject) hitObj;
            idsOld.add(hit.getString("_id"));
        });

        hitsNew.forEach(hitObj -> {
            JSONObject hit = (JSONObject) hitObj;
            Assert.assertTrue(idsOld.contains(hit.getString("_id")));
        });
    }
}
