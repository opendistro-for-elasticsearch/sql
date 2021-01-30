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

import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;

import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import org.json.JSONObject;
import org.junit.Test;

/**
 * This manual IT for NULL literal cannot be replaced with comparison test because other database
 * has different type for expression with NULL involved, such as NULL rather than concrete type
 * inferred like what we do in core engine.
 */
public class NullLiteralIT extends SQLIntegTestCase {

  @Test
  public void testNullLiteralSchema() {
    verifySchema(
        query("SELECT NULL, ABS(NULL), 1 + NULL, NULL + 1.0"),
        schema("NULL", "undefined"),
        schema("ABS(NULL)", "byte"),
        schema("1 + NULL", "integer"),
        schema("NULL + 1.0", "double"));
  }

  @Test
  public void testNullLiteralInOperator() {
    verifyDataRows(
        query("SELECT NULL = NULL, NULL AND TRUE"),
        rows(null, null));
  }

  @Test
  public void testNullLiteralInFunction() {
    verifyDataRows(
        query("SELECT ABS(NULL), POW(2, FLOOR(NULL))"),
        rows(null, null));
  }

  @Test
  public void testNullLiteralInInterval() {
    verifyDataRows(
        query("SELECT INTERVAL NULL DAY, INTERVAL 60 * 60 * 24 * (NULL - FLOOR(NULL)) SECOND"),
        rows(null, null)
    );
  }

  private JSONObject query(String sql) {
    return new JSONObject(executeQuery(sql, "jdbc"));
  }

}
