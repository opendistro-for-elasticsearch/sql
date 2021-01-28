/*
 *    Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import static com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase.Index.DEEP_NESTED;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_DEEP_NESTED;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.Test;

public class ObjectFieldOperateIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(DEEP_NESTED);
  }

  @Test
  public void select_object_field() throws IOException {
    JSONObject result = executeQuery(
        String.format("source=%s | "
                + "fields city.name, city.location.latitude",
            TEST_INDEX_DEEP_NESTED));
    verifySchema(result,
        schema("city.name", "string"),
        schema("city.location.latitude", "double"));
    verifyDataRows(result,
        rows("Seattle", 10.5));
  }

  @Test
  public void compare_object_field_in_where() throws IOException {
    JSONObject result = executeQuery(
        String.format("source=%s "
                + "| where city.name = 'Seattle' "
                + "| fields city.name, city.location.latitude",
            TEST_INDEX_DEEP_NESTED));
    verifySchema(result,
        schema("city.name", "string"),
        schema("city.location.latitude", "double"));
    verifyDataRows(result,
        rows("Seattle", 10.5));
  }

  @Test
  public void group_object_field_in_stats() throws IOException {
    JSONObject result = executeQuery(
        String.format("source=%s "
                + "| stats count() by city.name",
            TEST_INDEX_DEEP_NESTED));
    verifySchema(result,
        schema("count()", "integer"),
        schema("city.name", "string"));
    verifyDataRows(result,
        rows(1, "Seattle"));
  }

  @Test
  public void sort_by_object_field() throws IOException {
    JSONObject result = executeQuery(
        String.format("source=%s "
                + "| sort city.name"
                + "| fields city.name, city.location.latitude",
            TEST_INDEX_DEEP_NESTED));
    verifySchema(result,
        schema("city.name", "string"),
        schema("city.location.latitude", "double"));
    verifyDataRows(result,
        rows("Seattle", 10.5));
  }
}
