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

package com.amazon.opendistroforelasticsearch.sql.ppl;

import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.assertJsonEquals;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ExplainIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.ACCOUNT);
  }

  @Test
  public void testExplain() throws Exception {
    String expected = loadFromFile("expectedOutput/ppl/explain_output.json");
    assertJsonEquals(
        expected,
        explainQueryToString(
            "source=elasticsearch-sql_test_index_account"
                + "| where age > 30 "
                + "| stats avg(age) AS avg_age by state, city "
                + "| sort state "
                + "| fields - city "
                + "| eval age2 = avg_age + 2 "
                + "| dedup age2 "
                + "| fields age2")
    );
  }

  @Test
  public void testFilterPushDownExplain() throws Exception {
    String expected = loadFromFile("expectedOutput/ppl/explain_filter_push.json");

    assertJsonEquals(
        expected,
        explainQueryToString(
            "source=elasticsearch-sql_test_index_account"
                + "| where age > 30 "
                + "| where age < 40 "
                + "| where balance > 10000 ")
    );
  }

  @Test
  public void testFilterAndAggPushDownExplain() throws Exception {
    String expected = loadFromFile("expectedOutput/ppl/explain_filter_agg_push.json");

    assertJsonEquals(
        expected,
        explainQueryToString(
            "source=elasticsearch-sql_test_index_account"
                + "| where age > 30 "
                + "| stats avg(age) AS avg_age by state, city")
    );
  }

  @Test
  public void testSortPushDownExplain() throws Exception {
    String expected = loadFromFile("expectedOutput/ppl/explain_sort_push.json");

    String actual = explainQueryToString(
        "source=elasticsearch-sql_test_index_account"
            + "| sort age "
            + "| where age > 30");
    assertJsonEquals(
        expected,
        explainQueryToString(
            "source=elasticsearch-sql_test_index_account"
                + "| sort age "
                + "| where age > 30")
    );
  }

  String loadFromFile(String filename) throws Exception {
    URI uri = Resources.getResource(filename).toURI();
    return new String(Files.readAllBytes(Paths.get(uri)));
  }
}
