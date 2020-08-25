/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.ppl;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK;
import static org.hamcrest.Matchers.equalTo;

import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.MetricName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class MetricsIT extends PPLIntegTestCase {

  @Override
  protected void init() throws Exception {
    loadIndex(Index.BANK);
  }

  @Test
  public void requestCount() throws IOException, InterruptedException {
    int beforeQueries = pplRequestTotal();
    multiQueries(3);
    TimeUnit.SECONDS.sleep(2L);

    assertThat(pplRequestTotal(), equalTo(beforeQueries + 3));
  }

  private void multiQueries(int n) throws IOException {
    for (int i = 0; i < n; ++i) {
      executeQuery(String.format("source=%s | where age = 31 + 1 | fields age", TEST_INDEX_BANK));
    }
  }

  private Request makeStatRequest() {
    return new Request(
        "GET", "/_opendistro/_ppl/stats"
    );
  }

  private int pplRequestTotal() throws IOException {
    JSONObject jsonObject = new JSONObject(executeStatRequest(makeStatRequest()));
    return jsonObject.getInt(MetricName.PPL_REQ_TOTAL.getName());
  }

  private String executeStatRequest(final Request request) throws IOException {
    Response sqlResponse = client().performRequest(request);

    Assert.assertTrue(sqlResponse.getStatusLine().getStatusCode() == 200);

    InputStream is = sqlResponse.getEntity().getContent();
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      String line = null;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
    }

    return sb.toString();
  }

}
