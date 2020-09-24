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

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class TopCommandIT  extends PPLIntegTestCase{

  @Override
  public void init() throws IOException {
    loadIndex(Index.ACCOUNT);
    setQuerySizeLimit(2000);
  }

  @Test
  public void testTopWithoutGroup() throws IOException {
    JSONObject result =
        executeQuery(String.format("source=%s | top gender", TEST_INDEX_ACCOUNT));
    verifyDataRows(
        result,
        rows("M"),
        rows("F"));
  }

  @Test
  public void testTopNWithoutGroup() throws IOException{
    JSONObject result =
        executeQuery(String.format("source=%s | top 1 gender", TEST_INDEX_ACCOUNT));
    verifyDataRows(
        result,
        rows("M"));
  }

  @Test
  public void testTopNWithGroup() throws IOException {
    JSONObject result =
        executeQuery(String.format("source=%s | top 1 state by gender", TEST_INDEX_ACCOUNT));
    verifyDataRows(
        result,
        rows("F", "TX"),
        rows("M", "MD"));
  }
}
