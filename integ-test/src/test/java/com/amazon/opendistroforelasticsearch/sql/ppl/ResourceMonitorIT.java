/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.ppl;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.columnName;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyColumn;

import java.io.IOException;
import org.elasticsearch.client.ResponseException;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Test;

public class ResourceMonitorIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.DOG);
  }

  @Test
  public void queryExceedResourceLimitShouldFail() throws IOException {
    // update opendistro.ppl.query.memory_limit to 1%
    updateClusterSettings(
        new ClusterSetting("persistent", "opendistro.ppl.query.memory_limit", "1%"));
    String query = String.format("search source=%s age=20", TEST_INDEX_DOG);

    ResponseException exception =
        expectThrows(ResponseException.class, () -> executeQuery(query));
    assertEquals(503, exception.getResponse().getStatusLine().getStatusCode());
    assertThat(exception.getMessage(), Matchers.containsString("resource is not enough to run the"
        + " query, quit."));

    // update opendistro.ppl.query.memory_limit to default value 85%
    updateClusterSettings(
        new ClusterSetting("persistent", "opendistro.ppl.query.memory_limit", "85%"));
    JSONObject result = executeQuery(String.format("search source=%s", TEST_INDEX_DOG));
    verifyColumn(result, columnName("dog_name"), columnName("holdersName"), columnName("age"));
  }
}
