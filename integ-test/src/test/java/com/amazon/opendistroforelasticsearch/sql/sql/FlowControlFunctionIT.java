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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_FALSE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.*;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.Locale;


import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants;
import com.amazon.opendistroforelasticsearch.sql.util.TestUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;

import org.junit.Assume;
import org.junit.Test;

public class FlowControlFunctionIT extends SQLIntegTestCase {

  @Override
  public void init() throws Exception {
    super.init();
    TestUtils.enableNewQueryEngine(client());
    loadIndex(Index.ACCOUNT);
    loadIndex(Index.BANK_WITH_NULL_VALUES);
  }

  @Test
  public void ifnullShouldPassJDBC() throws IOException {
    JSONObject response = executeJdbcRequest(
        "SELECT IFNULL(lastname, 'unknown') AS name FROM " + TEST_INDEX_ACCOUNT
            + " GROUP BY name");
    assertEquals("IFNULL(lastname, \'unknown\')", response.query("/schema/0/name"));
    assertEquals("name", response.query("/schema/0/alias"));
    assertEquals("keyword", response.query("/schema/0/type"));
  }

  @Test
  public void ifnullWithNullInputTest() {
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = new JSONObject(executeQuery(
            "SELECT IFNULL(1/0, firstname) as IFNULL1 ,"
                    + " IFNULL(firstname, 1/0) as IFNULL2 ,"
                    + " IFNULL(1/0, 1/0) as IFNULL3 "
                    + " FROM " + TEST_INDEX_BANK_WITH_NULL_VALUES
                    + " WHERE balance is null limit 2", "jdbc"));
    verifySchema(response,
            schema("IFNULL(1/0, firstname)", "IFNULL1", "keyword"),
            schema("IFNULL(firstname, 1/0)", "IFNULL2", "integer"),
            schema("IFNULL(1/0, 1/0)", "IFNULL3", "integer"));
    verifyDataRows(response,
            rows("Hattie", "Hattie", LITERAL_NULL.value()),
            rows( "Elinor", "Elinor", LITERAL_NULL.value())
    );
  }

  @Test
  public void ifnullWithMissingInputTest() {
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = new JSONObject(executeQuery(
            "SELECT IFNULL(balance, firstname) as IFNULL1 ,"
                    + " IFNULL(firstname, balance) as IFNULL2 ,"
                    + " IFNULL(balance, balance) as IFNULL3 "
                    + " FROM " + TEST_INDEX_BANK_WITH_NULL_VALUES
                    + " WHERE balance is null limit 2", "jdbc"));
    verifySchema(response,
            schema("IFNULL(balance, firstname)", "IFNULL1", "keyword"),
            schema("IFNULL(firstname, balance)", "IFNULL2", "long"),
            schema("IFNULL(balance, balance)", "IFNULL3", "long"));
    verifyDataRows(response,
            rows("Hattie", "Hattie", LITERAL_NULL.value()),
            rows( "Elinor", "Elinor", LITERAL_NULL.value())
    );
  }

  @Test
  public void nullifShouldPassJDBC() throws IOException {
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = executeJdbcRequest(
            "SELECT NULLIF(lastname, 'unknown') AS name FROM " + TEST_INDEX_ACCOUNT);
    assertEquals("NULLIF(lastname, \'unknown\')", response.query("/schema/0/name"));
    assertEquals("name", response.query("/schema/0/alias"));
    assertEquals("keyword", response.query("/schema/0/type"));
  }

  @Test
  public void nullifWithNotNullInputTestOne(){
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = new JSONObject(executeQuery(
            "SELECT NULLIF(firstname, 'Amber JOHnny') as testnullif "
                    + "FROM " + TEST_INDEX_BANK_WITH_NULL_VALUES
                    + " limit 2 ", "jdbc"));
    verifySchema(response,
            schema("NULLIF(firstname, 'Amber JOHnny')", "testnullif", "keyword"));
    verifyDataRows(response,
            rows(LITERAL_NULL.value()),
            rows("Hattie")
            );
  }

  @Test
  public void nullifWithNullInputTest() {
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = new JSONObject(executeQuery(
            "SELECT NULLIF(1/0, firstname) as nullif1 ,"
                    + " NULLIF(firstname, 1/0) as nullif2 ,"
                    + " NULLIF(1/0, 1/0) as nullif3 "
                    + " FROM " + TEST_INDEX_BANK_WITH_NULL_VALUES
                    + " WHERE balance is null limit 2", "jdbc"));
    verifySchema(response,
            schema("NULLIF(1/0, firstname)", "nullif1", "unknown"),
            schema("NULLIF(firstname, 1/0)", "nullif2", "unknown"),
            schema("NULLIF(1/0, 1/0)", "nullif3", "integer"));
    verifyDataRows(response,
            rows(LITERAL_NULL.value(), "Hattie", LITERAL_NULL.value()),
            rows(LITERAL_NULL.value(), "Elinor", LITERAL_NULL.value())
    );
  }

  @Test
  public void nullifWithMissingInputTest(){
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = new JSONObject(executeQuery(
            "SELECT NULLIF(balance, firstname) as nullif1 ,"
                    + " NULLIF(firstname, balance) as nullif2 ,"
                    + " NULLIF(balance, balance) as nullif3 "
                    + " FROM " + TEST_INDEX_BANK_WITH_NULL_VALUES
                    + " WHERE balance is null limit 2", "jdbc"));
    verifySchema(response,
            schema("NULLIF(balance, firstname)", "nullif1", "unknown"),
            schema("NULLIF(firstname, balance)", "nullif2", "unknown"),
            schema("NULLIF(balance, balance)", "nullif3", "long"));
    verifyDataRows(response,
            rows(LITERAL_NULL.value(), "Hattie", LITERAL_NULL.value()),
            rows(LITERAL_NULL.value(), "Elinor", LITERAL_NULL.value())
    );

  }

  @Test
  public void isnullShouldPassJDBC() throws IOException {
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = executeJdbcRequest(
            "SELECT ISNULL(lastname) AS name FROM " + TEST_INDEX_ACCOUNT);
    assertEquals("ISNULL(lastname)", response.query("/schema/0/name"));
    assertEquals("name", response.query("/schema/0/alias"));
    assertEquals("boolean", response.query("/schema/0/type"));
  }

  @Test
  public void isnullWithNotNullInputTest() throws IOException {
    assertThat(
            executeQuery("SELECT ISNULL('elastic') AS isnull FROM " + TEST_INDEX_ACCOUNT),
            hitAny(kvInt("/fields/isnull/0", equalTo(0)))
    );
    assertThat(
            executeQuery("SELECT ISNULL('') AS isnull FROM " + TEST_INDEX_ACCOUNT),
            hitAny(kvInt("/fields/isnull/0", equalTo(0)))
    );
  }

  @Test
  public void isnullWithNullInputTest() {
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = new JSONObject(executeQuery(
            "SELECT ISNULL(1/0) as ISNULL1 ,"
                    + " ISNULL(firstname) as ISNULL2 "
                    + " FROM " + TEST_INDEX_BANK_WITH_NULL_VALUES
                    + " WHERE balance is null limit 2", "jdbc"));
    verifySchema(response,
            schema("ISNULL(1/0)", "ISNULL1", "boolean"),
            schema("ISNULL(firstname)", "ISNULL2", "boolean"));
    verifyDataRows(response,
            rows(LITERAL_TRUE.value(), LITERAL_FALSE.value()),
            rows(LITERAL_TRUE.value(), LITERAL_FALSE.value())
    );
  }

  @Test
  public void isnullWithMissingInputTest() {
    Assume.assumeTrue(isNewQueryEngineEabled());
    JSONObject response = new JSONObject(executeQuery(
            "SELECT ISNULL(balance) as ISNULL1 ,"
                    + " ISNULL(firstname) as ISNULL2 "
                    + " FROM " + TEST_INDEX_BANK_WITH_NULL_VALUES
                    + " WHERE balance is null limit 2", "jdbc"));
    verifySchema(response,
            schema("ISNULL(balance)", "ISNULL1", "boolean"),
            schema("ISNULL(firstname)", "ISNULL2", "boolean"));
    verifyDataRows(response,
            rows(LITERAL_TRUE.value(), LITERAL_FALSE.value()),
            rows(LITERAL_TRUE.value(), LITERAL_FALSE.value())
    );

  }

  @Test
  public void isnullWithMathExpr() throws IOException{
    assertThat(
            executeQuery("SELECT ISNULL(1+1) AS isnull FROM " + TEST_INDEX_ACCOUNT),
            hitAny(kvInt("/fields/isnull/0", equalTo(0)))
    );
    assertThat(
            executeQuery("SELECT ISNULL(1+1*1/0) AS isnull FROM " + TEST_INDEX_ACCOUNT),
            hitAny(kvInt("/fields/isnull/0", equalTo(1)))
    );
  }

  private SearchHits query(String query) throws IOException {
    final String rsp = executeQueryWithStringOutput(query);

    final XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
        NamedXContentRegistry.EMPTY,
        LoggingDeprecationHandler.INSTANCE,
        rsp);
    return SearchResponse.fromXContent(parser).getHits();
  }

  private JSONObject executeJdbcRequest(String query) {
    return new JSONObject(executeQuery(query, "jdbc"));
  }
}
