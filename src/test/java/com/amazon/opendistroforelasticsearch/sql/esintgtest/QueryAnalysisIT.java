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

import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.SqlSyntaxAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_ANALYSIS_ENABLED;
import static org.elasticsearch.rest.RestStatus.BAD_REQUEST;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * Integration test for syntax and semantic analysis against query by new ANTLR parser.
 */
public class QueryAnalysisIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.BANK);
    }

    @Test
    public void missingFromClauseShouldThrowSyntaxException() {
        queryShouldThrowSyntaxException(
            "SELECT 1"
        );
    }

    @Test
    public void unsupportedOperatorShouldThrowSyntaxException() {
        queryShouldThrowSyntaxException(
            "SELECT *",
            "FROM elasticsearch-sql_test_index_bank",
            "WHERE age <=> 1"
        );
    }

    @Test
    public void unsupportedOperatorShouldThrowOtherExceptionOnceAnalyzerDisabled() {
        runWithClusterSetting(
            new ClusterSetting("transient", QUERY_ANALYSIS_ENABLED, "false"),
            () -> queryShouldThrowException(
                SqlParseException.class,
                "SELECT *",
                "FROM elasticsearch-sql_test_index_bank",
                "WHERE age <=> 1"
            )
        );
    }

    /** Run the query with cluster setting changed and cleaned after complete */
    private void runWithClusterSetting(ClusterSetting setting, Runnable query) {
        try {
            updateClusterSettings(setting);
            query.run();
        }
        catch (IOException e) {
            throw new IllegalStateException(
                StringUtils.format("Exception raised when running with cluster setting [" + setting + "]"));
        }
        finally {
            // Clean up or ES will throw java.lang.AssertionError: test leaves persistent cluster metadata behind
            try {
                updateClusterSettings(setting.nullify());
            } catch (IOException e) {
                throw new IllegalStateException(
                    StringUtils.format("Exception raised when resetting cluster setting to %s=%s", setting));
            }
        }
    }

    private void queryShouldThrowSyntaxException(String... clauses) {
        queryShouldThrowException(SqlSyntaxAnalysisException.class, clauses);
    }

    private <T> void queryShouldThrowException(Class<T> exceptionType, String... clauses) {
        String query = String.join(" ", clauses);
        try {
            explainQuery(query);
            Assert.fail("Expected ResponseException, but none was thrown for query: " + query);
        }
        catch (ResponseException e) {
            assertResponseStatusAndBody(
                e.getResponse(),
                BAD_REQUEST.getStatus(),
                "\"type\": \"" + exceptionType.getSimpleName() + "\""
            );
        }
        catch (IOException e) {
            throw new IllegalStateException(
                "Unexpected IOException raised rather than expected AnalysisException for query: " + query);
        }
    }

    private void assertResponseStatusAndBody(Response response, int status, String... contents) {
        assertResponseStatus(response, status);
        try {
            assertResponseBody(response, contents);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Unexpected IOException raised when reading response body");
        }
    }

    private void assertResponseStatus(Response response, int status) {
        assertThat(response.getStatusLine().getStatusCode(), equalTo(status));
    }

    private void assertResponseBody(Response response, String[] contents) throws IOException {
        String body = TestUtils.getResponseBody(response);
        for (String content : contents) {
            assertThat(body, containsString(content));
        }
    }



}
