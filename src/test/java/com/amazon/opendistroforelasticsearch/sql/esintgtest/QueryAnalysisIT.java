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

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.SemanticAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.SyntaxAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.rest.RestStatus;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_ANALYSIS_ENABLED;
import static org.elasticsearch.common.xcontent.XContentType.JSON;
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
        queryShouldThrowSyntaxException("SELECT 1");
    }

    @Test
    public void unsupportedOperatorShouldThrowSyntaxException() {
        queryShouldThrowSyntaxException(
            "SELECT * FROM elasticsearch-sql_test_index_bank WHERE age <=> 1"
        );
    }

    @Test
    public void unsupportedOperatorShouldThrowOtherExceptionIfAnalyzerDisabled() {
        runWithClusterSetting(
            new ClusterSetting("transient", QUERY_ANALYSIS_ENABLED, "false"),
            () -> queryShouldThrowException(
                "SELECT * FROM elasticsearch-sql_test_index_bank WHERE age <=> 1",
                SqlParseException.class
            )
        );
    }

    @Test
    public void useNewAddedFieldShouldPass() throws Exception {
        // 1.Make sure new add fields not there originally
        String query = "SELECT salary FROM elasticsearch-sql_test_index_bank WHERE education = 'PhD'";
        queryShouldThrowSemanticException(query, "Field [education] cannot be found or used here.");

        // 2.Index an document with fields not present in mapping previously
        String docWithNewFields = "{\"account_number\":12345,\"education\":\"PhD\",\"salary\": \"10000\"}";
        IndexResponse resp = client().index(new IndexRequest().index("elasticsearch-sql_test_index_bank").
                                                               source(docWithNewFields, JSON)).get();
        Assert.assertEquals(RestStatus.CREATED, resp.status());

        // 3.Same query should pass
        executeQuery(query);
    }

    @Test
    public void nonExistingFieldNameShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT * FROM elasticsearch-sql_test_index_bank WHERE balance1 = 1000",
            "Field [balance1] cannot be found or used here."
            //"Did you mean [balance]?"
        );
    }

    @Test
    public void nonExistingIndexAliasShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT * FROM elasticsearch-sql_test_index_bank b WHERE a.balance = 1000",
            "Field [a.balance] cannot be found or used here."
            //"Did you mean [b.balance]?"
        );
    }

    @Test
    public void indexJoinNonNestedFieldShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT * FROM elasticsearch-sql_test_index_bank b1, b1.firstname f1",
            "Field [b1.firstname] is [TEXT] type but nested type is required."
        );
    }

    @Test
    public void scalarFunctionCallWithTypoInNameShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT * FROM elasticsearch-sql_test_index_bank WHERE ABSa(age) = 1",
            "Function [ABSA] cannot be found or used here.",
            "Did you mean [ABS]?"
        );
    }

    @Test
    public void scalarFunctionCallWithWrongTypeArgumentShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT * FROM elasticsearch-sql_test_index_bank WHERE LOG(lastname) = 1",
            "Function [LOG] cannot work with [KEYWORD].",
            "Usage: LOG(NUMBER T) -> T or LOG(NUMBER T, NUMBER) -> T"
        );
    }

    @Test
    public void aggregateFunctionCallWithWrongNumberOfArgumentShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT city FROM elasticsearch-sql_test_index_bank GROUP BY city HAVING MAX(age, birthdate) > 1",
            "Function [MAX] cannot work with [INTEGER, DATE].",
            "Usage: MAX(NUMBER T) -> T"
        );
    }

    @Test
    public void aggregateFunctionCallWithWrongScalarFunctionCallShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT MAX(LOG(firstname)) FROM elasticsearch-sql_test_index_bank GROUP BY city",
            "Function [LOG] cannot work with [TEXT]."
        );
    }

    @Test
    public void compareIntegerFieldWithBooleanShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT * FROM elasticsearch-sql_test_index_bank b WHERE b.age IS FALSE",
            "Operator [IS] cannot work with [INTEGER, BOOLEAN].",
            "Usage: Please use compatible types from each side."
        );
    }

    @Test
    public void compareNumberFieldWithStringShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT * FROM elasticsearch-sql_test_index_bank b WHERE b.age >= 'test'",
            "Operator [>=] cannot work with [INTEGER, STRING].",
            "Usage: Please use compatible types from each side."
        );
    }

    @Test
    public void compareLogFunctionCallWithNumberFieldWithStringShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT * FROM elasticsearch-sql_test_index_bank b WHERE LOG(b.balance) != 'test'",
            "Operator [!=] cannot work with [LONG, STRING].",
            "Usage: Please use compatible types from each side."
        );
    }

    @Test
    public void unionNumberFieldWithStringShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT age FROM elasticsearch-sql_test_index_bank" +
            " UNION SELECT address FROM elasticsearch-sql_test_index_bank",
            "Operator [UNION] cannot work with [INTEGER, TEXT]."
        );
    }

    @Test
    public void minusBooleanFieldWithDateShouldThrowSemanticException() {
        queryShouldThrowSemanticException(
            "SELECT male FROM elasticsearch-sql_test_index_bank" +
            " MINUS SELECT birthdate FROM elasticsearch-sql_test_index_bank",
            "Operator [MINUS] cannot work with [BOOLEAN, DATE]."
        );
    }

    @Test
    public void useInClauseWithIncompatibleFieldTypesShouldFail() {
        queryShouldThrowSemanticException(
            "SELECT * FROM elasticsearch-sql_test_index_bank WHERE male " +
            " IN (SELECT 1 FROM elasticsearch-sql_test_index_bank)",
            "Operator [IN] cannot work with [BOOLEAN, INTEGER]."
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
                StringUtils.format("Exception raised when running with cluster setting [%s]", setting));
        }
        finally {
            // Clean up or ES will throw java.lang.AssertionError: test leaves persistent cluster metadata behind
            try {
                updateClusterSettings(setting.nullify());
            }
            catch (IOException e) {
                // Ignore exception during the cleanup
            }
        }
    }

    private void queryShouldThrowSyntaxException(String query, String... expectedMsgs) {
        queryShouldThrowException(query, SyntaxAnalysisException.class, expectedMsgs);
    }

    private void queryShouldThrowSemanticException(String query, String... expectedMsgs) {
        queryShouldThrowException(query, SemanticAnalysisException.class, expectedMsgs);
    }

    private <T> void queryShouldThrowException(String query, Class<T> exceptionType, String... expectedMsgs) {
        try {
            explainQuery(query);
            Assert.fail("Expected ResponseException, but none was thrown for query: " + query);
        }
        catch (ResponseException e) {
            ResponseAssertion assertion = new ResponseAssertion(e.getResponse());
            assertion.assertStatusEqualTo(BAD_REQUEST.getStatus());
            assertion.assertBodyContains("\"type\": \"" + exceptionType.getSimpleName() + "\"");
            for (String msg : expectedMsgs) {
                assertion.assertBodyContains(msg);
            }
        }
        catch (IOException e) {
            throw new IllegalStateException(
                "Unexpected IOException raised rather than expected AnalysisException for query: " + query);
        }
    }

    private static class ResponseAssertion {
        private final Response response;
        private final String body;

        ResponseAssertion(Response response) {
            this.response = response;
            try {
                this.body = TestUtils.getResponseBody(response);
            }
            catch (IOException e) {
                throw new IllegalStateException("Unexpected IOException raised when reading response body");
            }
        }

        void assertStatusEqualTo(int expectedStatus) {
            assertThat(response.getStatusLine().getStatusCode(), equalTo(expectedStatus));
        }

        void assertBodyContains(String content) {
            assertThat(body, containsString(content));
        }
    }

}
