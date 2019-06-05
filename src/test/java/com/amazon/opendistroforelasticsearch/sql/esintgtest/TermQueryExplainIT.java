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

import org.elasticsearch.client.ResponseException;
import org.elasticsearch.rest.RestStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_BANK_TWO;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ONLINE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class TermQueryExplainIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {

        loadIndex(Index.ACCOUNT);
        loadIndex(Index.ONLINE);
        loadIndex(Index.BANK);
        loadIndex(Index.BANK_TWO);
        loadIndex(Index.DOG);
    }

    @Test
    public void testNonExistingIndex() throws IOException {
        try {
            explainQuery("SELECT firstname, lastname " +
                    "FROM elasticsearch_sql_test_fake_index " +
                    "WHERE firstname = 'Leo'");
            Assert.fail("Expected ResponseException, but none was thrown");

        } catch (ResponseException e) {
            assertThat(e.getResponse().getStatusLine().getStatusCode(), equalTo(RestStatus.BAD_REQUEST.getStatus()));
            final String entity = TestUtils.getResponseBody(e.getResponse());
            assertThat(entity, containsString("no such index"));
            assertThat(entity, containsString("\"type\": \"IndexNotFoundException\""));
        }
    }

    @Test
    public void testNonResolvingIndexPattern() throws IOException {
        try {
            explainQuery("SELECT * " +
                    "FROM elasticsearch_sql_test_blah_blah* " +
                    "WHERE firstname = 'Leo'");
            Assert.fail("Expected ResponseException, but none was thrown");

        } catch (ResponseException e) {
            assertThat(e.getResponse().getStatusLine().getStatusCode(), equalTo(RestStatus.BAD_REQUEST.getStatus()));
            final String entity = TestUtils.getResponseBody(e.getResponse());
            assertThat(entity, containsString("Unknown index"));
            assertThat(entity, containsString("\"type\": \"VerificationException\""));
        }
    }

    @Test
    public void testNonResolvingIndexPatternWithExistingIndex() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT * " +
                "FROM elasticsearch_sql_test_blah_blah*, %s " +
                "WHERE state = 'DC'", TEST_INDEX_BANK));
        assertThat(result, containsString("\"term\":{\"state.keyword\""));
    }

    @Test
    public void testNonResolvingIndexPatternWithNonExistingIndex() throws IOException {
        try {
            explainQuery("SELECT firstname, lastname " +
                    "FROM elasticsearch_sql_test_blah_blah*, another_fake_index " +
                    "WHERE firstname = 'Leo'");
            Assert.fail("Expected ResponseException, but none was thrown");
        } catch (ResponseException e) {
            assertThat(e.getResponse().getStatusLine().getStatusCode(), equalTo(RestStatus.BAD_REQUEST.getStatus()));
            final String entity = TestUtils.getResponseBody(e.getResponse());
            assertThat(entity, containsString("no such index"));
            assertThat(entity, containsString("\"type\": \"IndexNotFoundException\""));
        }
    }

    @Test
    public void testNonIdenticalMappings() throws IOException {
        try {
            explainQuery(String.format(Locale.ROOT, "SELECT firstname, birthdate FROM %s, %s " +
                            "WHERE firstname = 'Leo' OR male = 'true'",
                    TEST_INDEX_BANK, TEST_INDEX_ONLINE));
        } catch (ResponseException e) {
            assertThat(e.getResponse().getStatusLine().getStatusCode(), equalTo(RestStatus.BAD_REQUEST.getStatus()));
            final String entity = TestUtils.getResponseBody(e.getResponse());
            assertThat(entity, containsString("When using multiple indices, the mappings must be identical"));
            assertThat(entity, containsString("\"type\": \"VerificationException\""));
        }
    }

    @Test
    public void testIdenticalMappings() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT firstname, birthdate, state " +
                        "FROM %s, %s WHERE state = 'WA' OR male = 'true'",
                com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_BANK,
                com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_BANK_TWO));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
        assertThat(result, containsString("_source"));
    }

    @Test
    public void testIdenticalMappingsWithTypes() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT firstname, birthdate, state FROM %s, %s WHERE state = 'WA' OR male = 'true'",
                com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_BANK + "/account",
                com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_BANK_TWO + "/account_two"));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
        assertThat(result, containsString("_source"));
    }


    @Test
    public void testIdenticalMappingsWithPartialType() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT firstname, birthdate, state " +
                        "FROM %s, %s WHERE state = 'WA' OR male = 'true'",
                TEST_INDEX_BANK + "/account", TEST_INDEX_BANK_TWO));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
        assertThat(result, containsString("_source"));
    }

    @Test
    public void testTextFieldOnly() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT firstname, birthdate, state " +
                "FROM %s WHERE firstname = 'Abbas'", TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, not(containsString("firstname.")));
    }

    @Test
    public void testTextAndKeywordAppendsKeywordAlias() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT firstname, birthdate, state " +
                "FROM %s WHERE state = 'WA' OR lastname = 'Chen'", TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
        assertThat(result, not(containsString("lastname.")));
    }

    @Test
    public void testBooleanFieldNoKeywordAlias() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT * FROM %s WHERE male = 'false'",
                TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, not(containsString("male.")));
    }

    @Test
    public void testDateFieldNoKeywordAlias() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT * FROM %s WHERE birthdate = '2018-08-19'",
                TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, not(containsString("birthdate.")));
    }

    @Test
    public void testNumberNoKeywordAlias() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT * FROM %s WHERE age = 32",
                TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, not(containsString("age.")));
    }

    @Test
    public void inTestInWhere() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "select * from %s " +
                "where state IN ('WA' , 'PA' , 'TN')", TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
    }

    @Test
    @Ignore // TODO: enable when subqueries are fixed
    public void inTestInWhereSubquery() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "select * from %s where " +
                        "state IN (select state from %s where city = 'Nicholson')",
                TEST_INDEX_BANK + "/account", TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
    }

    @Test
    public void testKeywordAliasGroupBy() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT firstname, state FROM %s " +
                "GROUP BY firstname, state", TEST_INDEX_BANK + "/account"));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
        assertThat(result, not(containsString("lastname.")));
    }

    @Test
    public void testKeywordAliasOrderBy() throws IOException {

        String result = explainQuery(String.format(Locale.ROOT, "SELECT * FROM %s ORDER BY state, lastname ",
                TEST_INDEX_BANK));
        assertThat(result, containsString("\"state.keyword\":{\"order\":\"asc\""));
        assertThat(result, containsString("\"lastname\":{\"order\":\"asc\"}"));
    }

    @Test
    @Ignore // TODO: verify the returned query is correct and fix the expected output
    public void testJoinWhere() throws IOException {

        String expectedOutput = TestUtils.fileToString("src/test/resources/expectedOutput/term_join_where", true);
        String result = explainQuery(String.format(Locale.ROOT, "SELECT a.firstname, a.lastname , b.city " +
                "FROM %s a JOIN %s b ON a.city = b.city where a.city IN ('Nicholson', 'Yardville')",
                TEST_INDEX_ACCOUNT, TEST_INDEX_ACCOUNT ));

        assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void testJoinAliasMissing() throws IOException {
        try {
            explainQuery(String.format(Locale.ROOT, "SELECT a.firstname, a.lastname , b.city " +
                    "FROM %s a JOIN %s b ON a.city = b.city where city IN ('Nicholson', 'Yardville')",
                    TEST_INDEX_ACCOUNT, TEST_INDEX_ACCOUNT));
            Assert.fail("Expected ResponseException, but none was thrown");
        } catch (ResponseException e) {
            assertThat(e.getResponse().getStatusLine().getStatusCode(), equalTo(RestStatus.BAD_REQUEST.getStatus()));
            final String entity = TestUtils.getResponseBody(e.getResponse());
            assertThat(entity, containsString("table alias or field name missing"));
            assertThat(entity, containsString("\"type\": \"VerificationException\""));
        }

    }

    @Test
    @Ignore // TODO: enable when subqueries are fixed
    public void testMultiQuery() throws IOException {

        String expectedOutput = TestUtils.fileToString("src/test/resources/expectedOutput/term_union_where", true);
        String result = explainQuery(String.format(Locale.ROOT, "SELECT firstname FROM %s/account " +
                        "WHERE firstname = 'Amber' UNION ALL SELECT dog_name as firstname FROM %s/dog " +
                        "WHERE holdersName = 'Hattie' OR dog_name = 'rex'", TEST_INDEX_ACCOUNT, TEST_INDEX_DOG));
        assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }
}
