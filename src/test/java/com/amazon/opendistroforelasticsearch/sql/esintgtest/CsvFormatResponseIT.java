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
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Tests to cover requests with "?format=csv" parameter
 */
public class CsvFormatResponseIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Override
    protected Request getSqlRequest(String request, boolean explain) {

        Request sqlRequest = super.getSqlRequest(request, explain);
        sqlRequest.addParameter("format", "csv");
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
    public void specificPercentilesIntAndDouble() throws IOException  {

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
}
