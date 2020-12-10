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

import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

public class AggregationExpressionIT extends SQLIntegTestCase {

  @Override
  protected void init() throws Exception {
    loadIndex(Index.ACCOUNT);
    loadIndex(Index.BANK);
  }

  @Test
  public void noGroupKeySingleFuncOverAggWithoutAliasShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT abs(MAX(age)) " +
            "FROM %s",
        Index.ACCOUNT.getName()));

    verifySchema(response, schema("abs(MAX(age))", null, "long"));
    verifyDataRows(response, rows(40));
  }

  @Test
  public void noGroupKeyMaxAddMinShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT MAX(age) + MIN(age) as add " +
            "FROM %s",
        Index.ACCOUNT.getName()));

    verifySchema(response, schema("add", "add", "long"));
    verifyDataRows(response, rows(60));
  }

  // todo age field should has long type instead of integer type.
  @Ignore
  @Test
  public void noGroupKeyMaxAddLiteralShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT MAX(age) + 1 as add " +
            "FROM %s",
        Index.ACCOUNT.getName()));

    verifySchema(response, schema("add", "add", "long"));
    verifyDataRows(response, rows(41));
  }

  @Ignore("skip this test because the old engine returns an integer instead of a double type")
  @Test
  public void noGroupKeyAvgOnIntegerShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT AVG(age) as avg " +
            "FROM %s",
        Index.BANK.getName()));

    verifySchema(response, schema("avg", "avg", "double"));
    verifyDataRows(response, rows(34));
  }

  @Test
  public void hasGroupKeyAvgOnIntegerShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT gender, AVG(age) as avg " +
            "FROM %s " +
            "GROUP BY gender",
        Index.BANK.getName()));

    verifySchema(response,
        schema("gender", null, "text"),
        schema("avg", "avg", "double"));
    verifyDataRows(response,
        rows("m", 34.25),
        rows("f", 33.666666666666664d));
  }

  @Test
  public void hasGroupKeyMaxAddMinShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT gender, MAX(age) + MIN(age) as add " +
            "FROM %s " +
            "GROUP BY gender",
        Index.ACCOUNT.getName()));

    verifySchema(response,
        schema("gender", null, "text"),
        schema("add", "add", "long"));
    verifyDataRows(response,
        rows("m", 60),
        rows("f", 60));
  }

  // todo age field should has long type instead of integer type.
  @Ignore
  @Test
  public void hasGroupKeyMaxAddLiteralShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT gender, MAX(age) + 1 as add " +
            "FROM %s " +
            "GROUP BY gender",
        Index.ACCOUNT.getName()));

    verifySchema(response,
        schema("gender", null, "text"),
        schema("add", "add", "long"));
    verifyDataRows(response,
        rows("m", 1),
        rows("f", 1));
  }

  @Test
  public void noGroupKeyLogMaxAddMinShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT Log(MAX(age) + MIN(age)) as log " +
            "FROM %s",
        Index.ACCOUNT.getName()));

    verifySchema(response, schema("log", "log", "double"));
    verifyDataRows(response, rows(4.0943445622221d));
  }

  @Test
  public void hasGroupKeyLogMaxAddMinShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT gender, Log(MAX(age) + MIN(age)) as log " +
            "FROM %s " +
            "GROUP BY gender",
        Index.ACCOUNT.getName()));

    verifySchema(response,
        schema("gender", null, "text"),
        schema("log", "log", "double"));
    verifyDataRows(response,
        rows("m", 4.0943445622221d),
        rows("f", 4.0943445622221d));
  }

  // todo age field should has long type instead of integer type.
  @Ignore
  @Test
  public void AddLiteralOnGroupKeyShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT gender, age+10, max(balance) as max " +
            "FROM %s " +
            "WHERE gender = 'm' and age < 22 " +
            "GROUP BY gender, age " +
            "ORDER BY age",
        Index.ACCOUNT.getName()));

    verifySchema(response,
        schema("gender", null, "text"),
        schema("age", "age", "long"),
        schema("max", "max", "long"));
    verifyDataRows(response,
        rows("m", 30, 49568),
        rows("m", 31, 49433));
  }

  @Test
  public void logWithAddLiteralOnGroupKeyShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT gender, Log(age+10) as logAge, max(balance) as max " +
            "FROM %s " +
            "WHERE gender = 'm' and age < 22 " +
            "GROUP BY gender, age " +
            "ORDER BY age",
        Index.ACCOUNT.getName()));

    verifySchema(response,
        schema("gender", null, "text"),
        schema("logAge", "logAge", "double"),
        schema("max", "max", "long"));
    verifyDataRows(response,
        rows("m", 3.4011973816621555d, 49568),
        rows("m", 3.4339872044851463d, 49433));
  }

  // todo max field should has long as type instead of integer type.
  @Ignore
  @Test
  public void logWithAddLiteralOnGroupKeyAndMaxSubtractLiteralShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT gender, Log(age+10) as logAge, max(balance) - 100 as max " +
            "FROM %s " +
            "WHERE gender = 'm' and age < 22 " +
            "GROUP BY gender, age " +
            "ORDER BY age",
        Index.ACCOUNT.getName()));

    verifySchema(response,
        schema("gender", null, "text"),
        schema("logAge", "logAge", "double"),
        schema("max", "max", "long"));
    verifyDataRows(response,
        rows("m", 3.4011973816621555d, 49468),
        rows("m", 3.4339872044851463d, 49333));
  }

  /**
   * The date is in JDBC format.
   */
  @Ignore("skip this test due to inconsistency in type in new engine")
  @Test
  public void groupByDateShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT birthdate, count(*) as count " +
            "FROM %s " +
            "WHERE age < 30 " +
            "GROUP BY birthdate ",
        Index.BANK.getName()));

    verifySchema(response,
        schema("birthdate", null, "date"),
        schema("count", "count", "integer"));
    verifyDataRows(response,
        rows("2018-06-23 00:00:00.000", 1));
  }

  @Ignore("skip this test due to inconsistency in type in new engine")
  @Test
  public void groupByDateWithAliasShouldPass() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT birthdate as birth, count(*) as count " +
            "FROM %s " +
            "WHERE age < 30 " +
            "GROUP BY birthdate ",
        Index.BANK.getName()));

    verifySchema(response,
        schema("birth", "birth", "date"),
        schema("count", "count", "integer"));
    verifyDataRows(response,
        rows("2018-06-23 00:00:00.000", 1));
  }

  @Test
  public void aggregateCastStatementShouldNotReturnZero() {
    JSONObject response = executeJdbcRequest(String.format(
        "SELECT SUM(CAST(male AS INT)) AS male_sum FROM %s",
        Index.BANK.getName()));

    verifySchema(response, schema("male_sum", "male_sum", "double"));
    verifyDataRows(response, rows(4));
  }

  private JSONObject executeJdbcRequest(String query) {
    return new JSONObject(executeQuery(query, "jdbc"));
  }
}
