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

import org.elasticsearch.client.Request;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_NESTED_TYPE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringStartsWith.startsWith;

/**
 * Tests to cover requests with "?format=csv" parameter
 */
public class CsvFormatResponseIT extends SQLIntegTestCase {

    private boolean flatOption = false;

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.NESTED);
    }

    @Override
    protected Request getSqlRequest(String request, boolean explain) {

        Request sqlRequest = super.getSqlRequest(request, explain);
        sqlRequest.addParameter("format", "csv");
        sqlRequest.addParameter("flat", flatOption ? "true" : "false");
        return sqlRequest;
    }

    @Test
    public void allPercentilesByDefault() throws IOException  {

        final String query = String.format(Locale.ROOT, "SELECT PERCENTILES(age) FROM %s", TEST_INDEX_ACCOUNT);
        final String result = executeQueryWithStringOutput(query);

        final String expectedHeaders = "PERCENTILES(age).1.0,PERCENTILES(age).5.0,PERCENTILES(age).25.0," +
                "PERCENTILES(age).50.0,PERCENTILES(age).75.0,PERCENTILES(age).95.0,PERCENTILES(age).99.0";
        Assert.assertThat(result, containsString(expectedHeaders));
    }

    @Test
    public void specificPercentilesIntAndDouble() throws IOException {

        final String query = String.format(Locale.ROOT, "SELECT PERCENTILES(age,10,49.0) FROM %s",
                TEST_INDEX_ACCOUNT);
        final String result = executeQueryWithStringOutput(query);

        final String[] unexpectedPercentiles = {"1.0", "5.0", "25.0", "50.0", "75.0", "95.0", "99.0"};
        final String expectedHeaders = "PERCENTILES(age,10,49.0).10.0,PERCENTILES(age,10,49.0).49.0";
        Assert.assertThat(result, containsString(expectedHeaders));
        for (final String unexpectedPercentile : unexpectedPercentiles) {
            Assert.assertThat(result, not(containsString("PERCENTILES(age,10,49.0)." + unexpectedPercentile)));
        }
    }

    @Test
    public void nestedObjectsAndArraysAreQuoted() throws IOException {

        final String query = String.format(Locale.ROOT, "SELECT * FROM %s WHERE _id = 5",
                TEST_INDEX_NESTED_TYPE);
        final String result = executeQueryWithStringOutput(query);

        final String expectedMyNum = "\"[3, 4]\"";
        final String expectedComment = "\"{data=[aa, bb], likes=10}\"";
        final String expectedMessage = "\"[{dayOfWeek=6, author=zz, info=zz}]\"";

        Assert.assertThat(result, containsString(expectedMyNum));
        Assert.assertThat(result, containsString(expectedComment));
        Assert.assertThat(result, containsString(expectedMessage));
    }

    @Test
    public void arraysAreQuotedInFlatMode() throws IOException {

        setFlatOption(true);

        final String query = String.format(Locale.ROOT, "SELECT * FROM %s WHERE _id = 5",
                TEST_INDEX_NESTED_TYPE);
        final String result = executeQueryWithStringOutput(query);

        final String expectedMyNum = "\"[3, 4]\"";
        final String expectedCommentData = "\"[aa, bb]\"";
        final String expectedMessage = "\"[{dayOfWeek=6, author=zz, info=zz}]\"";

        Assert.assertThat(result, containsString(expectedMyNum));
        Assert.assertThat(result, containsString(expectedCommentData));
        Assert.assertThat(result, containsString(expectedMessage));

        setFlatOption(false);
    }

    @Test
    public void fieldOrder() throws IOException {

        final String[] expectedFields = {"age", "firstname", "address", "gender", "email"};

        verifyFieldOrder(expectedFields);
    }

    @Test
    public void fieldOrderOther() throws IOException {

        final String[] expectedFields = {"email", "firstname", "age", "gender", "address"};

        verifyFieldOrder(expectedFields);
    }

    @Ignore("Painless script not supported") // TODO: remove the ignore line once the issue is fixed
    @Test
    public void fieldOrderWithScriptFields() throws IOException {

        final String[] expectedFields = {"email", "script1", "script2", "gender", "address"};
        final String query = String.format(Locale.ROOT, "SELECT email, " +
                "script(script1, \"doc['balance'].value * 2\"), " +
                "script(script2, painless, \"doc['balance'].value + 10\"), gender, address " +
                "FROM %s WHERE email='amberduke@pyrami.com'", TEST_INDEX_ACCOUNT);

        verifyFieldOrder(expectedFields, query);
    }

    private void verifyFieldOrder(final String[] expectedFields) throws IOException {

        final String fields = String.join(", ", expectedFields);
        final String query = String.format(Locale.ROOT, "SELECT %s FROM %s " +
                "WHERE email='amberduke@pyrami.com'", fields, TEST_INDEX_ACCOUNT);

        verifyFieldOrder(expectedFields, query);
    }

    private void verifyFieldOrder(final String[] expectedFields, final String query) throws IOException {

        final String result = executeQueryWithStringOutput(query);

        final String expectedHeader = String.join(",", expectedFields);
        Assert.assertThat(result, startsWith(expectedHeader));
    }

    private void setFlatOption(boolean flat) {

        this.flatOption = flat;
    }
}
