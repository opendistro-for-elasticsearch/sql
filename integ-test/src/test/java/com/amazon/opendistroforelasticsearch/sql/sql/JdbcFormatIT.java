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

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;

import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class JdbcFormatIT extends SQLIntegTestCase {

  @Override
  protected void init() throws Exception {
    loadIndex(Index.BANK);
  }

  @Test
  public void testSimpleDataTypesInSchema() {
    JSONObject response = new JSONObject(executeQuery(
        "SELECT account_number, address, age, birthdate, city, male, state "
            + "FROM " + TEST_INDEX_BANK, "jdbc"));

    verifySchema(response,
        schema("account_number", "long"),
        schema("address", "text"),
        schema("age", "integer"),
        schema("birthdate", "timestamp"),
        schema("city", "keyword"),
        schema("male", "boolean"),
        schema("state", "text"));
  }

  @Test
  public void testAliasInSchema() {
    JSONObject response = new JSONObject(executeQuery(
        "SELECT account_number AS acc FROM " + TEST_INDEX_BANK, "jdbc"));

    verifySchema(response, schema("account_number", "acc", "long"));
  }

}
