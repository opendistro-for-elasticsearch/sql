/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.sql;

import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.assertJsonEquals;
import static org.hamcrest.Matchers.equalTo;

import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants;
import com.amazon.opendistroforelasticsearch.sql.util.TestUtils;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.elasticsearch.client.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class AdminIT extends SQLIntegTestCase {

  @Override
  public void init() throws Exception {
    super.init();
    TestUtils.enableNewQueryEngine(client());
    loadIndex(Index.ACCOUNT);
  }

  @Test
  public void showSingleIndexAlias() throws IOException {
    String alias = "acc";
    addAlias(TestsConstants.TEST_INDEX_ACCOUNT, alias);
    JSONObject response = new JSONObject(executeQuery("SHOW TABLES LIKE acc", "jdbc"));

    /*
     * Assumed indices of fields in dataRows based on "schema" output for SHOW given above:
     * "TABLE_NAME" : 2
     */
    JSONArray row = response.getJSONArray("datarows").getJSONArray(0);
    assertThat(row.get(2), equalTo(alias));
  }

  @Test
  public void describeSingleIndexAlias() throws IOException {
    String alias = "acc";
    addAlias(TestsConstants.TEST_INDEX_ACCOUNT, alias);
    JSONObject response = new JSONObject(executeQuery("DESCRIBE TABLES LIKE acc", "jdbc"));

    /*
     * Assumed indices of fields in dataRows based on "schema" output for DESCRIBE given above:
     * "TABLE_NAME"  : 2
     */
    JSONArray row = response.getJSONArray("datarows").getJSONArray(0);
    assertThat(row.get(2), equalTo(alias));
  }

  @Test
  public void explainShow() throws Exception {
    String expected = loadFromFile("expectedOutput/sql/explain_show.json");

    final String actual = explainQuery("SHOW TABLES LIKE %");
    assertJsonEquals(
        expected,
        explainQuery("SHOW TABLES LIKE %")
    );
  }

  private void addAlias(String index, String alias) throws IOException {
    client().performRequest(new Request("PUT", StringUtils.format("%s/_alias/%s", index, alias)));
  }

  private String loadFromFile(String filename) throws Exception {
    URI uri = Resources.getResource(filename).toURI();
    return new String(Files.readAllBytes(Paths.get(uri)));
  }
}
