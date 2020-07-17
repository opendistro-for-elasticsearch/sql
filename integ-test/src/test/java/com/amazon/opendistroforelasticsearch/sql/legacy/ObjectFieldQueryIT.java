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

package com.amazon.opendistroforelasticsearch.sql.legacy;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_DEEP_NESTED;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;

import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import org.json.JSONObject;
import org.junit.Test;

public class ObjectFieldQueryIT extends SQLIntegTestCase {

  @Override
  protected void init() throws Exception {
    loadIndex(Index.DEEP_NESTED);
  }

  @Test
  public void testSelectObjectFieldItself() {
    JSONObject response = new JSONObject(query("SELECT city FROM %s"));

    verifySchema(response, schema("city", null, "object"));
    verifyDataRows(response,
        rows(new JSONObject(
            "{\n"
                + "  \"name\": \"Seattle\",\n"
                + "  \"location\": {\"latitude\": 10.5}\n"
                + "}")
        )
    );
  }

  @Test
  public void testSelectObjectInnerFields() {
    JSONObject response = new JSONObject(query(
        "SELECT city.location, city.location.latitude FROM %s"));

    verifySchema(response,
        schema("city.location", null, "object"),
        schema("city.location.latitude", null, "double")
    );
    verifyDataRows(response,
        rows(new JSONObject("{\"latitude\": 10.5}"), 10.5)
    );
  }

  private String query(String sql) {
    return executeQuery(
        StringUtils.format(sql, TEST_INDEX_DEEP_NESTED),
        "jdbc"
    );
  }

}
