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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.elasticsearch;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.QueryRunner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;

/**
 * Query runner for Elasticsearch databases.
 */
public class ElasticsearchQueryRunner extends QueryRunner {

  private CloseableHttpClient client;
  private HttpPost httpPost;
  private CloseableHttpResponse response;
  private String query;

  /**
   * Function to run queries against Elasticsearch database.
   */
  @Override
  public void runQuery() throws Exception {
    response = client.execute(httpPost);
  }

  /**
   * Function to prepare query runner against Elasticsearch database.
   *
   * @param query Query to run against Elasticsearch database.
   */
  @Override
  public void prepareQueryRunner(final String query) throws Exception {
    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
          public X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          public void checkClientTrusted(X509Certificate[] certs, String authType) {
          }

          public void checkServerTrusted(X509Certificate[] certs, String authType) {
          }
        }
    };

    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new SecureRandom());
    client = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
        .setSSLContext(sc).build();

    httpPost = new HttpPost("https://localhost:9200/_opendistro/_sql");

    JSONObject queryJSON = new JSONObject();
    queryJSON.put("query", query);
    httpPost.setEntity(new StringEntity(queryJSON.toJSONString()));
    this.query = query;

    httpPost.setHeader("Accept", "application/json");
    httpPost.setHeader("Content-type", "application/json");

    UsernamePasswordCredentials creds
        = new UsernamePasswordCredentials("admin", "admin");
    httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
  }

  /**
   * Function to check query execution status against Elasticsearch database.
   */
  @Override
  public void checkQueryExecutionStatus(final String benchmarkPath)
      throws Exception {
    if (response.getStatusLine().getStatusCode() != 200) {
      File benchmarkDirectory = new File(benchmarkPath);
      if (benchmarkDirectory.exists() && benchmarkDirectory.isDirectory()) {
        BufferedWriter bufferedWriter = new BufferedWriter(
            new FileWriter(benchmarkPath + "/elasticsearch_failed_queries.txt", true));
        bufferedWriter.write(query);
        bufferedWriter.newLine();
        bufferedWriter.close();
      } else {
        throw new FileNotFoundException("Invalid Directory");
      }
    }
    client.close();
  }
}
