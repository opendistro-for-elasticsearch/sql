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

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_DATE;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySome;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class DateTimeFunctionIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.DATE);
  }

  private void week(String date, int mode, int expectedResult) throws IOException {
    JSONObject result = executeQuery(String.format(
        "source=%s | eval f = week(date('%s'), %d) | fields f", TEST_INDEX_DATE, date, mode));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(expectedResult));
  }

  @Test
  public void testWeek() throws IOException {
    JSONObject result = executeQuery(String.format(
        "source=%s | eval f = week(date('2008-02-20')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(7));

    week("2008-02-20", 0, 7);
    week("2008-02-20", 1, 8);
    week("2008-12-31", 1, 53);
    week("2000-01-01", 0, 0);
    week("2000-01-01", 2, 52);
  }
}
