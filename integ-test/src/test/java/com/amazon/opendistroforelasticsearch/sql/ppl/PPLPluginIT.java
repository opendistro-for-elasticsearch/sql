/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.ppl;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import com.amazon.opendistroforelasticsearch.sql.util.TestUtils;
import java.io.IOException;
import java.util.Locale;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PPLPluginIT extends PPLIntegTestCase {
  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  private static final String PERSISTENT = "persistent";

  @Override
  protected void init() throws Exception {
    loadIndex(Index.BANK);
  }

  @Test
  public void testQueryEndpointShouldOK() throws IOException {
    Request request = new Request("PUT", "/a/_doc/1?refresh=true");
    request.setJsonEntity("{\"name\": \"hello\"}");
    client().performRequest(request);

    JSONObject response = executeQuery("search source=a");
    verifySchema(response, schema("name", null, "string"));
    verifyDataRows(response, rows("hello"));
  }

  @Test
  public void testQueryEndpointShouldFail() throws IOException {
    exceptionRule.expect(ResponseException.class);
    exceptionRule.expect(hasProperty("response", statusCode(400)));

    client().performRequest(makePPLRequest("search invalid"));
  }

  @Test
  public void sqlEnableSettingsTest() throws IOException {
    String query =
        String.format("search source=%s firstname='Hattie' | fields firstname", TEST_INDEX_BANK);
    // enable by default
    JSONObject result = executeQuery(query);
    verifyDataRows(result, rows("Hattie"));

    // disable
    updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.ppl.enabled", "false"));
    Response response = null;
    try {
      result = executeQuery(query);
    } catch (ResponseException ex) {
      response = ex.getResponse();
    }

    result = new JSONObject(TestUtils.getResponseBody(response));
    assertThat(result.getInt("status"), equalTo(400));
    JSONObject error = result.getJSONObject("error");
    assertThat(error.getString("reason"), equalTo("Invalid Query"));
    assertThat(error.getString("details"), equalTo(
        "Either opendistro.ppl.enabled or rest.action.multi.allow_explicit_index setting is "
            + "false"));
    assertThat(error.getString("type"), equalTo("IllegalAccessException"));

    // reset the setting
    updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.ppl.enabled", null));
  }

  protected Request makePPLRequest(String query) {
    Request post = new Request("POST", "/_opendistro/_ppl");
    post.setJsonEntity(String.format(Locale.ROOT, "{\n" + "  \"query\": \"%s\"\n" + "}", query));
    return post;
  }

  private TypeSafeMatcher<Response> statusCode(int statusCode) {
    return new TypeSafeMatcher<Response>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(String.format(Locale.ROOT, "statusCode=%d", statusCode));
      }

      @Override
      protected boolean matchesSafely(Response resp) {
        return resp.getStatusLine().getStatusCode() == statusCode;
      }
    };
  }
}
