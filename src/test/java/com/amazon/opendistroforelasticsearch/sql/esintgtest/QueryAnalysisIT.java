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
import org.elasticsearch.client.ResponseException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

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
    public void missingFromClauseShouldThrowException() {
        queryShouldThrowSqlSyntaxAnalysisException(
            "SELECT 1"
        );
    }

    @Test
    public void illegalOperatorShouldThrowException() {
        queryShouldThrowSqlSyntaxAnalysisException(
            "SELECT * " +
            "FROM elasticsearch-sql_test_index_bank " +
            "WHERE age <=> 1"
        );
    }

    private void queryShouldThrowSqlSyntaxAnalysisException(String query) {
        queryShouldThrowException(query, SqlSyntaxAnalysisException.class);
    }

    private <T> void queryShouldThrowException(String query, Class<T> exceptionType) {
        try {
            explainQuery(query);
            Assert.fail("Expected ResponseException, but none was thrown");
        }
        catch (ResponseException e) {
            assertThat(e.getResponse().getStatusLine().getStatusCode(), equalTo(BAD_REQUEST.getStatus()));
            try {
                String body = TestUtils.getResponseBody(e.getResponse());
                assertThat(body, containsString("\"type\": \"" + exceptionType.getSimpleName() + "\""));
            }
            catch (IOException ex) {
                throw new IllegalStateException("Unexpected IOException raised when reading response body");
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException raised rather than expected AnalysisException");
        }
    }

}
