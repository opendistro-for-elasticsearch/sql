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

import static com.amazon.opendistroforelasticsearch.sql.util.TestUtils.createHiddenIndexByRestClient;
import static com.amazon.opendistroforelasticsearch.sql.util.TestUtils.performRequest;

import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import java.io.IOException;
import org.elasticsearch.client.Request;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for identifiers including index and field name symbol.
 */
public class IdentifierIT extends SQLIntegTestCase {

  @Test
  public void testIndexNames() throws IOException {
    createIndexWithOneDoc("logs", "logs_2020_01");
    queryAndAssertTheDoc("SELECT * FROM logs");
    queryAndAssertTheDoc("SELECT * FROM logs_2020_01");
  }

  @Test
  public void testSpecialIndexNames() throws IOException {
    createIndexWithOneDoc(".system", "logs-2020-01");
    queryAndAssertTheDoc("SELECT * FROM .system");
    queryAndAssertTheDoc("SELECT * FROM logs-2020-01");
  }

  @Test
  public void testQuotedIndexNames() throws IOException {
    createIndexWithOneDoc("logs+2020+01", "logs.2020.01");
    queryAndAssertTheDoc("SELECT * FROM `logs+2020+01`");
    queryAndAssertTheDoc("SELECT * FROM \"logs.2020.01\"");
  }

  @Test
  public void testSpecialFieldName() throws IOException {
    new Index("test")
        .addDoc("{\"@timestamp\": 10, \"dimensions:major_version\": 30}");

    assertEquals(
        "{\n"
            + "  \"schema\": [\n"
            + "    {\n"
            + "      \"name\": \"@timestamp\",\n"
            + "      \"type\": \"long\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"dimensions:major_version\",\n"
            + "      \"type\": \"long\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"total\": 1,\n"
            + "  \"datarows\": [[\n"
            + "    10,\n"
            + "    30\n"
            + "  ]],\n"
            + "  \"size\": 1\n"
            + "}\n",
        executeQuery("SELECT @timestamp, `dimensions:major_version` FROM test", "jdbc")
    );
  }

  private void createIndexWithOneDoc(String... indexNames) throws IOException {
    for (String indexName : indexNames) {
      new Index(indexName).addDoc("{\"age\": 30}");
    }
  }

  private void queryAndAssertTheDoc(String sql) {
    assertEquals(
        "{\n"
            + "  \"schema\": [{\n"
            + "    \"name\": \"age\",\n"
            + "    \"type\": \"long\"\n"
            + "  }],\n"
            + "  \"total\": 1,\n"
            + "  \"datarows\": [[30]],\n"
            + "  \"size\": 1\n"
            + "}\n",
        executeQuery(sql.replace("\"", "\\\""), "jdbc")
    );
  }

  /**
   * Index abstraction for test code readability.
   */
  private static class Index {

    private final String indexName;

    Index(String indexName) throws IOException {
      this.indexName = indexName;

      if (indexName.startsWith(".")) {
        createHiddenIndexByRestClient(client(), indexName, "");
      } else {
        executeRequest(new Request("PUT", "/" + indexName));
      }
    }

    void addDoc(String doc) {
      Request indexDoc = new Request("POST", String.format("/%s/_doc?refresh=true", indexName));
      indexDoc.setJsonEntity(doc);
      performRequest(client(), indexDoc);
    }
  }

}
