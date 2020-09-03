/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.sql;

import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlAction.QUERY_API_ENDPOINT;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.featureValueOf;
import static org.elasticsearch.rest.RestStatus.BAD_REQUEST;
import static org.hamcrest.Matchers.is;

import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import java.io.IOException;
import java.util.Locale;
import java.util.function.Function;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.rest.RestStatus;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The query validation IT only covers test for error cases that not doable in comparison test.
 * For all other tests, comparison test should be favored over manual written test like this.
 */
public class QueryValidationIT extends SQLIntegTestCase {

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Override
  protected void init() throws Exception {
    loadIndex(Index.ACCOUNT);
  }

  @Ignore("Will add this validation in analyzer later")
  @Test
  public void testNonAggregatedSelectColumnMissingInGroupByClause() throws IOException {
    expectResponseException()
        .hasStatusCode(BAD_REQUEST)
        .hasErrorType("SemanticCheckException")
        .containsMessage("Expression [state] that contains non-aggregated column "
                       + "is not present in group by clause")
        .whenExecute("SELECT state FROM elasticsearch-sql_test_index_account GROUP BY age");
  }

  @Test
  public void testNonAggregatedSelectColumnPresentWithoutGroupByClause() throws IOException {
    expectResponseException()
        .hasStatusCode(BAD_REQUEST)
        .hasErrorType("SemanticCheckException")
        .containsMessage("Explicit GROUP BY clause is required because expression [state] "
                       + "contains non-aggregated column")
        .whenExecute("SELECT state, AVG(age) FROM elasticsearch-sql_test_index_account");
  }

  public ResponseExceptionAssertion expectResponseException() {
    return new ResponseExceptionAssertion(exceptionRule);
  }

  /**
   * Response exception assertion helper to assert property value in ES ResponseException
   * and Response inside. This serves as syntax sugar to improve the readability of test
   * code.
   */
  private static class ResponseExceptionAssertion {
    private final ExpectedException exceptionRule;

    private ResponseExceptionAssertion(ExpectedException exceptionRule) {
      this.exceptionRule = exceptionRule;

      exceptionRule.expect(ResponseException.class);
    }

    ResponseExceptionAssertion hasStatusCode(RestStatus code) {
      exceptionRule.expect(featureValueOf("statusCode", is(code),
          (Function<ResponseException, RestStatus>) e ->
              RestStatus.fromCode(e.getResponse().getStatusLine().getStatusCode())));
      return this;
    }

    ResponseExceptionAssertion hasErrorType(String type) {
      exceptionRule.expectMessage("\"type\": \"" + type + "\"");
      return this;
    }

    ResponseExceptionAssertion containsMessage(String... messages) {
      for (String message : messages) {
        exceptionRule.expectMessage(message);
      }
      return this;
    }

    void whenExecute(String query) throws IOException {
      execute(query);
    }
  }

  private static void execute(String query) throws IOException {
    Request request = new Request("POST", QUERY_API_ENDPOINT);
    request.setJsonEntity(String.format(Locale.ROOT, "{\n" + "  \"query\": \"%s\"\n" + "}", query));

    RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
    restOptionsBuilder.addHeader("Content-Type", "application/json");
    request.setOptions(restOptionsBuilder);

    client().performRequest(request);
  }

}
