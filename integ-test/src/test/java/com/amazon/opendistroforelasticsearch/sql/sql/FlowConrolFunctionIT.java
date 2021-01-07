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

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.hitAny;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvInt;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvString;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;


import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import com.amazon.opendistroforelasticsearch.sql.util.TestUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;

import org.junit.Assume;
import org.junit.Test;


/**
 * Created by allwefantasy on 8/25/16.
 */
public class FlowConrolFunctionIT extends SQLIntegTestCase {

  @Override
  public void init() throws Exception {
    super.init();
    TestUtils.enableNewQueryEngine(client());
    loadIndex(Index.BANK);
    loadIndex(Index.ACCOUNT);
    loadIndex(Index.ONLINE);
    loadIndex(Index.DATE);
  }

  @Test
  public void ifnullShouldPassJDBC() throws IOException {
    JSONObject response = executeJdbcRequest(
        "SELECT IFNULL(lastname, 'unknown') AS name FROM " + TEST_INDEX_ACCOUNT
            + " GROUP BY name");
    assertEquals("IFNULL(lastname, \'unknown\')", response.query("/schema/0/name"));
    assertEquals("name", response.query("/schema/0/alias"));
    assertEquals("keyword", response.query("/schema/0/type"));
  }

  @Test
  public void ifnullWithNotNullInputTest() throws IOException {
    assertThat(
        executeQuery("SELECT IFNULL('sample', 'IsNull') AS ifnull FROM " + TEST_INDEX_ACCOUNT),
        hitAny(kvString("/fields/ifnull/0", equalTo("sample")))
    );
  }

  @Test
  public void ifnullWithNullInputTest() throws IOException {
    assertThat(
        executeQuery("SELECT IFNULL(null, 10) AS ifnull FROM " + TEST_INDEX_ACCOUNT),
        hitAny(kvInt("/fields/ifnull/0", equalTo(10)))
    );
    assertThat(
        executeQuery("SELECT IFNULL('', 10) AS ifnull FROM " + TEST_INDEX_ACCOUNT),
        hitAny(kvString("/fields/ifnull/0", equalTo("")))
    );
  }

  @Test
  public void nullifShouldPassJDBC() throws IOException {
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = executeJdbcRequest(
            "SELECT NULLIF(lastname, 'unknown') AS name FROM " + TEST_INDEX_ACCOUNT);
    assertEquals("NULLIF(lastname, \'unknown\')", response.query("/schema/0/name"));
    assertEquals("name", response.query("/schema/0/alias"));
    assertEquals("keyword", response.query("/schema/0/type"));
  }

  @Test
  public void isnullShouldPassJDBC() throws IOException {
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = executeJdbcRequest(
            "SELECT ISNULL(lastname) AS name FROM " + TEST_INDEX_ACCOUNT);
    assertEquals("ISNULL(lastname)", response.query("/schema/0/name"));
    assertEquals("name", response.query("/schema/0/alias"));
    assertEquals("boolean", response.query("/schema/0/type"));
  }

  private SearchHits query(String query) throws IOException {
    final String rsp = executeQueryWithStringOutput(query);

    final XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
        NamedXContentRegistry.EMPTY,
        LoggingDeprecationHandler.INSTANCE,
        rsp);
    return SearchResponse.fromXContent(parser).getHits();
  }

  private JSONObject executeJdbcRequest(String query) {
    return new JSONObject(executeQuery(query, "jdbc"));
  }
}
