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

import java.io.IOException;
import org.elasticsearch.client.Request;
import org.junit.jupiter.api.Test;

public class IdentifierIT extends NewSQLIntegTestCase {

  @Test
  public void testHiddenIndexName() throws IOException {
    createIndex(".system_catalog");
    executeQuery("SELECT * FROM .system_catalog");
  }

  @Test
  public void testIndexNameWithDot() throws IOException {
    createIndex("logs.2020.01");
    executeQuery("SELECT * FROM `logs.2020.01`");
  }

  @Test
  public void testIndexNameWithOtherSpecialChar() throws IOException {
    createIndex("logs-2020-01", "logs_2020_01", "system logs");
    executeQuery("SELECT * FROM `logs-2020-01`");
    executeQuery("SELECT * FROM logs_2020_01");
    executeQuery("SELECT * FROM `system logs`");
  }

  private void createIndex(String... indexNames) throws IOException {
    for (String indexName : indexNames) {
      if (indexName.startsWith(".")) {
        createHiddenIndexByRestClient(client(), indexName, "");
      } else {
        executeRequest(new Request("PUT", "/" + indexName));
      }
    }
  }

}
