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

import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import java.io.IOException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.*;
import static org.hamcrest.Matchers.*;


public class InIT extends SQLIntegTestCase {

  @Override
  public void init() throws Exception {
    super.init();
    loadIndex(Index.ACCOUNT);
    loadIndex(Index.DATETIME);
    loadIndex(Index.EMPLOYEE_NESTED);
  }

  @Test
  public void inWithLong() throws IOException {
    String sql = "SELECT * FROM " + TEST_INDEX_ACCOUNT
        + " WHERE age IN (36, 1 ,2,4) AND firstname < lastname";
    JSONObject results = executeQuery(sql);
    Assert.assertThat(getTotalHits(results), equalTo(29));
  }

  @Test
  public void inWithString() throws IOException {
    String sql = "SELECT * FROM " + TEST_INDEX_ACCOUNT
        + " WHERE firstname IN ('Baker','Ina','Bryan','Alford', 'Mike') AND firstname < lastname";
    JSONObject results = executeQuery(sql);
    Assert.assertThat(getTotalHits(results), equalTo(3));
  }

  @Test
  public void inWithDate() throws IOException {
    String sql = "SELECT * FROM " + TEST_INDEX_DATE_TIME
        + " WHERE login_time in ('2020-04-08T11:10:30+05:00')";
    JSONObject results = executeQuery(sql);
    Assert.assertThat(getTotalHits(results), equalTo(1));
  }

  @Test
  public void inNestedString() throws IOException {
    String sql = "SELECT * FROM " + TEST_INDEX_EMPLOYEE_NESTED
        + " WHERE nested(comments, comments.message in ('comment_2_1') AND comments.likes > 1)";
    JSONObject results = executeQuery(sql);
    Assert.assertThat(getTotalHits(results), equalTo(1));
  }

}
