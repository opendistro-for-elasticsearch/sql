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

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_DOGSUBQUERY;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_EMPLOYEE_NESTED;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.hitAll;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvInt;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvString;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import com.google.common.collect.Ordering;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.elasticsearch.client.ResponseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SubqueryIT extends SQLIntegTestCase {

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();


  @Override
  protected void init() throws Exception {
    loadIndex(Index.ACCOUNT);
    loadIndex(Index.DOGSSUBQUERY);
    loadIndex(Index.EMPLOYEE_NESTED);
  }

  @Test
  public void testIN() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT dog_name " +
            "FROM %s A " +
            "WHERE holdersName IN (SELECT firstname FROM %s B) " +
            "AND dog_name <> 'babala'",
        TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            kvString("/_source/A.dog_name", is("snoopy")),
            kvString("/_source/A.dog_name", is("gogo"))
        )
    );
  }

  @Test
  public void testINWithAlias() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT A.dog_name " +
            "FROM %s A " +
            "WHERE A.holdersName IN (SELECT B.firstname FROM %s B) " +
            "AND A.dog_name <> 'babala'",
        TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            kvString("/_source/A.dog_name", is("snoopy")),
            kvString("/_source/A.dog_name", is("gogo"))
        )
    );
  }

  @Test
  public void testINSelectAll() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT * " +
            "FROM %s A " +
            "WHERE holdersName IN (SELECT firstname FROM %s B) " +
            "AND dog_name <> 'babala'",
        TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            both(kvString("/_source/A.dog_name", is("snoopy")))
                .and(kvString("/_source/A.holdersName", is("Hattie")))
                .and(kvInt("/_source/A.age", is(4))),
            both(kvString("/_source/A.dog_name", is("gogo")))
                .and(kvString("/_source/A.holdersName", is("Gabrielle")))
                .and(kvInt("/_source/A.age", is(6)))
        )
    );
  }

  @Test
  public void testINWithInnerWhere() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT dog_name " +
            "FROM %s A " +
            "WHERE holdersName IN (SELECT firstname FROM %s B WHERE age <> 36) " +
            "AND dog_name <> 'babala'",
        TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            kvString("/_source/A.dog_name", is("gogo"))
        )
    );
  }

  @Test
  public void testNotSupportedQuery() throws IOException {
    exceptionRule.expect(ResponseException.class);
    exceptionRule.expectMessage("Unsupported subquery");
    String query = String.format(Locale.ROOT,
        "SELECT dog_name " +
            "FROM %s A " +
            "WHERE holdersName NOT IN (SELECT firstname FROM %s B WHERE age <> 36) " +
            "AND dog_name <> 'babala'",
        TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);
    executeQuery(query);
  }

  // todo Pending on DISTINCT support in JOIN
  @Ignore
  @Test
  public void testINWithDuplicate() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT dog_name " +
            "FROM %s A " +
            "WHERE holdersName IN (SELECT firstname FROM %s B)",
        TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            kvString("/_source/A.dog_name", is("snoopy")),
            kvString("/_source/A.dog_name", is("babala"))
        )
    );
  }

  @Test
  public void nonCorrelatedExists() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT e.name " +
            "FROM %s as e " +
            "WHERE EXISTS (SELECT * FROM e.projects as p)",
        TEST_INDEX_EMPLOYEE_NESTED);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            kvString("/_source/name", is("Bob Smith")),
            kvString("/_source/name", is("Jane Smith"))
        )
    );
  }

  @Test
  public void nonCorrelatedExistsWhere() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT e.name " +
            "FROM %s as e " +
            "WHERE EXISTS (SELECT * FROM e.projects as p WHERE p.name LIKE 'aurora')",
        TEST_INDEX_EMPLOYEE_NESTED);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            kvString("/_source/name", is("Bob Smith"))
        )
    );
  }

  @Test
  public void nonCorrelatedExistsParentWhere() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT e.name " +
            "FROM %s as e " +
            "WHERE EXISTS (SELECT * FROM e.projects as p WHERE p.name LIKE 'security') " +
            "AND e.name LIKE 'jane'",
        TEST_INDEX_EMPLOYEE_NESTED);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            kvString("/_source/name", is("Jane Smith"))
        )
    );
  }

  @Test
  public void nonCorrelatedNotExists() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT e.name " +
            "FROM %s as e " +
            "WHERE NOT EXISTS (SELECT * FROM e.projects as p)",
        TEST_INDEX_EMPLOYEE_NESTED);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            kvString("/_source/name", is("Susan Smith")),
            kvString("/_source/name", is("John Doe"))
        )
    );
  }

  @Test
  public void nonCorrelatedNotExistsWhere() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT e.name " +
            "FROM %s as e " +
            "WHERE NOT EXISTS (SELECT * FROM e.projects as p WHERE p.name LIKE 'aurora')",
        TEST_INDEX_EMPLOYEE_NESTED);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            kvString("/_source/name", is("Susan Smith")),
            kvString("/_source/name", is("Jane Smith")),
            kvString("/_source/name", is("John Doe"))
        )
    );
  }

  @Test
  public void nonCorrelatedNotExistsParentWhere() throws IOException {
    String query = String.format(Locale.ROOT,
        "SELECT e.name " +
            "FROM %s as e " +
            "WHERE NOT EXISTS (SELECT * FROM e.projects as p WHERE p.name LIKE 'security') " +
            "AND e.name LIKE 'smith'",
        TEST_INDEX_EMPLOYEE_NESTED);

    JSONObject response = executeQuery(query);
    assertThat(
        response,
        hitAll(
            kvString("/_source/name", is("Susan Smith"))
        )
    );
  }

  @Test
  public void selectFromSubqueryWithCountShouldPass() throws IOException {
    JSONObject result = executeQuery(
        StringUtils.format("SELECT t.TEMP as count " +
            "FROM (SELECT COUNT(*) as TEMP FROM %s) t", TEST_INDEX_ACCOUNT));

    assertThat(result.query("/aggregations/count/value"), equalTo(1000));
  }

  @Test
  public void selectFromSubqueryWithWhereAndCountShouldPass() throws IOException {
    JSONObject result = executeQuery(
        StringUtils.format("SELECT t.TEMP as count " +
            "FROM (SELECT COUNT(*) as TEMP FROM %s WHERE age > 30) t", TEST_INDEX_ACCOUNT));

    assertThat(result.query("/aggregations/count/value"), equalTo(502));
  }

  @Test
  public void selectFromSubqueryWithCountAndGroupByShouldPass() throws Exception {
    JSONObject result = executeQuery(
        StringUtils.format("SELECT t.TEMP as count " +
            "FROM (SELECT COUNT(*) as TEMP FROM %s GROUP BY gender) t", TEST_INDEX_ACCOUNT));

    assertThat(getTotalHits(result), equalTo(1000));
    JSONObject gender = (JSONObject) result.query("/aggregations/gender");
    assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    boolean isMaleFirst = gender.optQuery("/buckets/0/key").equals("m");
    int maleBucketId = isMaleFirst ? 0 : 1;
    int femaleBucketId = isMaleFirst ? 1 : 0;

    String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
    String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

    assertThat(gender.query(maleBucketPrefix + "/key"), equalTo("m"));
    assertThat(gender.query(maleBucketPrefix + "/count/value"), equalTo(507));
    assertThat(gender.query(femaleBucketPrefix + "/key"), equalTo("f"));
    assertThat(gender.query(femaleBucketPrefix + "/count/value"), equalTo(493));
  }

  @Test
  public void selectFromSubqueryWithCountAndGroupByAndOrderByShouldPass() throws IOException {
    JSONObject result = executeQuery(
        StringUtils.format(
            "SELECT t.TEMP as count " +
                "FROM (SELECT COUNT(*) as TEMP FROM %s GROUP BY age ORDER BY TEMP) t",
            TEST_INDEX_ACCOUNT));
    JSONArray buckets = (JSONArray) result.query("/aggregations/age/buckets");
    List<Integer> countList = new ArrayList<>();
    for (int i = 0; i < buckets.length(); ++i) {
      countList.add((int) buckets.query(String.format(Locale.ROOT, "/%d/count/value", i)));
    }

    assertTrue(Ordering.natural().isOrdered(countList));
  }

  @Test
  public void selectFromSubqueryWithCountAndGroupByAndHavingShouldPass() throws Exception {
    JSONObject result = executeQuery(
        StringUtils.format("SELECT t.T1 as g, t.T2 as c " +
            "FROM (SELECT gender as T1, COUNT(*) as T2 " +
            "      FROM %s " +
            "      GROUP BY gender " +
            "      HAVING T2 > 500) t", TEST_INDEX_ACCOUNT));
    assertThat(result.query("/aggregations/g/buckets/0/c/value"), equalTo(507));
  }

  @Test
  public void selectFromSubqueryCountAndSum() throws IOException {
    JSONObject result = executeQuery(
        StringUtils.format(
            "SELECT t.TEMP1 as count, t.TEMP2 as balance " +
                "FROM (SELECT COUNT(*) as TEMP1, SUM(balance) as TEMP2 " +
                "      FROM %s) t",
            TEST_INDEX_ACCOUNT));

    assertThat(result.query("/aggregations/count/value"), equalTo(1000));
    assertThat(result.query("/aggregations/balance/value"), equalTo(25714837.0));
  }

  @Ignore("Skip to avoid breaking test due to inconsistency in JDBC schema")
  @Test
  public void selectFromSubqueryWithoutAliasShouldPass() throws IOException {
    JSONObject response = executeJdbcRequest(
        StringUtils.format(
            "SELECT a.firstname AS my_first, a.lastname AS my_last, a.age AS my_age " +
                "FROM (SELECT firstname, lastname, age " +
                "FROM %s " +
                "WHERE age = 40 and account_number = 291) AS a",
            TEST_INDEX_ACCOUNT));

    verifySchema(response,
        schema("firstname", "my_first", "text"),
        schema("lastname", "my_last", "text"),
        schema("age", "my_age", "long"));
    verifyDataRows(response,
        rows("Lynn", "Pollard", 40));
  }

  private JSONObject executeJdbcRequest(String query) {
    return new JSONObject(executeQuery(query, "jdbc"));
  }
}
