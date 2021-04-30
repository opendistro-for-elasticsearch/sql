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
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;

import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants;
import org.json.JSONObject;
import org.junit.Test;

public class WindowFunctionIT extends SQLIntegTestCase {

  @Override
  protected void init() throws Exception {
    loadIndex(Index.BANK_WITH_NULL_VALUES);
  }

  @Test
  public void testOrderByNullFirst() {
    JSONObject response = new JSONObject(
        executeQuery("SELECT age, ROW_NUMBER() OVER(ORDER BY age DESC NULLS FIRST) "
            + "FROM " + TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES, "jdbc"));

    verifyDataRows(response,
        rows(null, 1),
        rows(36, 2),
        rows(36, 3),
        rows(34, 4),
        rows(33, 5),
        rows(32, 6),
        rows(28, 7));
  }

  @Test
  public void testOrderByNullLast() {
    JSONObject response = new JSONObject(
        executeQuery("SELECT age, ROW_NUMBER() OVER(ORDER BY age NULLS LAST) "
            + "FROM " + TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES, "jdbc"));

    verifyDataRows(response,
        rows(28, 1),
        rows(32, 2),
        rows(33, 3),
        rows(34, 4),
        rows(36, 5),
        rows(36, 6),
        rows(null, 7));
  }

}
