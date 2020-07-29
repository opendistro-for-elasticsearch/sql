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
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_GAME_OF_THRONES;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_NESTED_TYPE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_ONLINE;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.rest.RestStatus;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class QueryIT extends SQLIntegTestCase {

  /**
   * Currently commenting out tests related to JoinType index since there is an issue with mapping.
   * <p>
   * Also ignoring the following tests as they are failing, will require investigation:
   * - idsQuerySubQueryIds
   * - escapedCharactersCheck
   * - fieldCollapsingTest
   * - idsQueryOneId
   * - idsQueryMultipleId
   * - multipleIndicesOneNotExistWithoutHint
   * <p>
   * The following tests are being ignored because subquery is still running in ES transport thread:
   * - twoSubQueriesTest()
   * - inTermsSubQueryTest()
   */

  final static int BANK_INDEX_MALE_TRUE = 4;
  final static int BANK_INDEX_MALE_FALSE = 3;

  @Override
  protected void init() throws Exception {
    loadIndex(Index.ONLINE);
    loadIndex(Index.ACCOUNT);
    loadIndex(Index.PHRASE);
    loadIndex(Index.DOG);
    loadIndex(Index.PEOPLE);
    loadIndex(Index.GAME_OF_THRONES);
    loadIndex(Index.ODBC);
    loadIndex(Index.LOCATION);
    loadIndex(Index.NESTED);
    // TODO Remove comment after issue with loading join type is resolved
    // loadIndex(Index.JOIN);
    loadIndex(Index.BANK);
    loadIndex(Index.BANK_TWO);
    loadIndex(Index.BANK_WITH_NULL_VALUES);
  }

  @Test
  public void queryEndWithSemiColonTest() {
    executeQuery(StringUtils.format("SELECT * FROM %s;", TEST_INDEX_BANK), "jdbc");
  }

  @Test
  public void searchTypeTest() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT, "SELECT * FROM %s LIMIT 1000",
        TestsConstants.TEST_INDEX_PHRASE));
    Assert.assertTrue(response.has("hits"));
    Assert.assertEquals(6, getTotalHits(response));
  }

  @Test
  public void multipleFromTest() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT,
        "SELECT * FROM %s, %s LIMIT 2000",
        TestsConstants.TEST_INDEX_BANK, TestsConstants.TEST_INDEX_BANK_TWO));
    Assert.assertTrue(response.has("hits"));
    Assert.assertEquals(14, getTotalHits(response));
  }

  @Test
  public void selectAllWithFieldReturnsAll() throws IOException {
    JSONObject response = executeQuery(StringUtils.format(
        "SELECT *, age " +
            "FROM %s " +
            "LIMIT 5",
        TestsConstants.TEST_INDEX_BANK
    ));

    checkSelectAllAndFieldResponseSize(response);
  }

  @Test
  public void selectAllWithFieldReverseOrder() throws IOException {
    JSONObject response = executeQuery(StringUtils.format(
        "SELECT *, age " +
            "FROM %s " +
            "LIMIT 5",
        TestsConstants.TEST_INDEX_BANK
    ));

    checkSelectAllAndFieldResponseSize(response);
  }

  @Test
  public void selectAllWithMultipleFields() throws IOException {
    JSONObject response = executeQuery(StringUtils.format(
        "SELECT *, age, address " +
            "FROM %s " +
            "LIMIT 5",
        TestsConstants.TEST_INDEX_BANK
    ));

    checkSelectAllAndFieldResponseSize(response);
  }

  @Test
  public void selectAllWithFieldAndOrderBy() throws IOException {
    JSONObject response = executeQuery(StringUtils.format(
        "SELECT *, age " +
            "FROM %s " +
            "ORDER BY age " +
            "LIMIT 5",
        TestsConstants.TEST_INDEX_BANK
    ));

    checkSelectAllAndFieldResponseSize(response);
  }

  @Test
  public void selectAllWithFieldAndGroupBy() throws IOException {
    JSONObject response = executeQuery(StringUtils.format(
        "SELECT *, age " +
            "FROM %s " +
            "GROUP BY age " +
            "LIMIT 10",
        TestsConstants.TEST_INDEX_BANK
    ));

    checkSelectAllAndFieldAggregationResponseSize(response, "age");
  }

  @Test
  public void selectAllWithFieldAndGroupByReverseOrder() throws IOException {
    JSONObject response = executeQuery(StringUtils.format(
        "SELECT *, age " +
            "FROM %s " +
            "GROUP BY age " +
            "LIMIT 10",
        TestsConstants.TEST_INDEX_BANK
    ));

    checkSelectAllAndFieldAggregationResponseSize(response, "age");
  }

  @Test
  public void selectFieldWithAliasAndGroupBy() {
    String response =
        executeQuery("SELECT lastname AS name FROM " + TEST_INDEX_ACCOUNT + " GROUP BY name",
            "jdbc");
    assertThat(response, containsString("\"alias\": \"name\""));
  }

  public void indexWithWildcardTest() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT, "SELECT * FROM %s* LIMIT 1000",
        TestsConstants.TEST_INDEX_BANK));
    Assert.assertTrue(response.has("hits"));
    assertThat(getTotalHits(response), greaterThan(0));
  }

  @Test
  public void selectSpecificFields() throws IOException {
    String[] arr = new String[] {"age", "account_number"};
    Set<String> expectedSource = new HashSet<>(Arrays.asList(arr));

    JSONObject response =
        executeQuery(String.format(Locale.ROOT, "SELECT age, account_number FROM %s",
            TEST_INDEX_ACCOUNT));
    assertResponseForSelectSpecificFields(response, expectedSource);
  }

  @Test
  public void selectSpecificFieldsUsingTableAlias() throws IOException {
    String[] arr = new String[] {"age", "account_number"};
    Set<String> expectedSource = new HashSet<>(Arrays.asList(arr));

    JSONObject response =
        executeQuery(String.format(Locale.ROOT, "SELECT a.age, a.account_number FROM %s a",
            TEST_INDEX_ACCOUNT));
    assertResponseForSelectSpecificFields(response, expectedSource);
  }

  @Test
  public void selectSpecificFieldsUsingTableNamePrefix() throws IOException {
    String[] arr = new String[] {"age", "account_number"};
    Set<String> expectedSource = new HashSet<>(Arrays.asList(arr));

    JSONObject response = executeQuery(String.format(Locale.ROOT,
        "SELECT elasticsearch-sql_test_index_account.age, elasticsearch-sql_test_index_account.account_number" +
            " FROM %s",
        TEST_INDEX_ACCOUNT));
    assertResponseForSelectSpecificFields(response, expectedSource);
  }

  private void assertResponseForSelectSpecificFields(JSONObject response,
                                                     Set<String> expectedSource) {
    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      Assert.assertEquals(expectedSource, getSource(hit).keySet());
    }
  }

  @Ignore("Will fix this in issue https://github.com/opendistro-for-elasticsearch/sql/issues/121")
  @Test
  public void selectFieldWithSpace() throws IOException {
    String[] arr = new String[] {"test field"};
    Set<String> expectedSource = new HashSet<>(Arrays.asList(arr));

    JSONObject response = executeQuery(String.format(Locale.ROOT, "SELECT ['test field'] FROM %s " +
            "WHERE ['test field'] IS NOT null",
        TestsConstants.TEST_INDEX_PHRASE));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      Assert.assertEquals(expectedSource, getSource(hit).keySet());
    }
  }

  @Ignore("field aliases are not supported currently")
  // it might be possible to change field names after the query already executed.
  @Test
  public void selectAliases() throws IOException {

    String[] arr = new String[] {"myage", "myaccount_number"};
    Set<String> expectedSource = new HashSet<>(Arrays.asList(arr));

    JSONObject result = executeQuery(String.format(Locale.ROOT,
        "SELECT age AS myage, account_number AS myaccount_number FROM %s", TEST_INDEX_ACCOUNT));
    JSONArray hits = getHits(result);
    hits.forEach(hitObj -> {
      JSONObject hit = (JSONObject) hitObj;
      Assert.assertEquals(expectedSource, hit.getJSONObject("_source").keySet());
    });
  }

  @Test
  public void useTableAliasInWhereClauseTest() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT,
        "SELECT * FROM %s a WHERE a.city = 'Nogal' LIMIT 1000", TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));
    Assert.assertEquals("Nogal", getSource(hits.getJSONObject(0)).get("city"));
  }

  @Test
  public void notUseTableAliasInWhereClauseTest() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT,
        "SELECT * FROM %s a WHERE city = 'Nogal' LIMIT 1000", TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));
    Assert.assertEquals("Nogal", getSource(hits.getJSONObject(0)).get("city"));
  }

  @Test
  public void useTableNamePrefixInWhereClauseTest() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT,
        "SELECT * FROM %s WHERE elasticsearch-sql_test_index_account.city = 'Nogal' LIMIT 1000",
        TEST_INDEX_ACCOUNT
    ));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));
    Assert.assertEquals("Nogal", getSource(hits.getJSONObject(0)).get("city"));
  }

  @Test
  public void equalityTest() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT,
        "SELECT * FROM %s WHERE city = 'Nogal' LIMIT 1000", TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));
    Assert.assertEquals("Nogal", getSource(hits.getJSONObject(0)).get("city"));
  }

  @Test
  public void equalityTestPhrase() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT, "SELECT * FROM %s WHERE " +
            "match_phrase(phrase, 'quick fox here') LIMIT 1000",
        TestsConstants.TEST_INDEX_PHRASE));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));
    Assert.assertEquals("quick fox here", getSource(hits.getJSONObject(0)).getString("phrase"));
  }

  @Test
  public void greaterThanTest() throws IOException {
    int someAge = 25;
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE age > %s LIMIT 1000",
            TestsConstants.TEST_INDEX_PEOPLE,
            someAge));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      int age = getSource(hit).getInt("age");
      assertThat(age, greaterThan(someAge));
    }
  }

  @Test
  public void greaterThanOrEqualTest() throws IOException {
    int someAge = 25;
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE age >= %s LIMIT 1000",
            TEST_INDEX_ACCOUNT,
            someAge));

    boolean isEqualFound = false;
    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      int age = getSource(hit).getInt("age");
      assertThat(age, greaterThanOrEqualTo(someAge));

        if (age == someAge) {
            isEqualFound = true;
        }
    }

    Assert.assertTrue(
        String.format(Locale.ROOT, "At least one of the documents need to contains age equal to %s",
            someAge),
        isEqualFound);
  }

  @Test
  public void lessThanTest() throws IOException {
    int someAge = 25;
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE age < %s LIMIT 1000",
            TestsConstants.TEST_INDEX_PEOPLE,
            someAge));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      int age = getSource(hit).getInt("age");
      assertThat(age, lessThan(someAge));
    }
  }

  @Test
  public void lessThanOrEqualTest() throws IOException {
    int someAge = 25;
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE age <= %s LIMIT 1000",
            TEST_INDEX_ACCOUNT,
            someAge));

    boolean isEqualFound = false;
    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      int age = getSource(hit).getInt("age");
      assertThat(age, lessThanOrEqualTo(someAge));

        if (age == someAge) {
            isEqualFound = true;
        }
    }

    Assert.assertTrue(
        String.format(Locale.ROOT, "At least one of the documents need to contains age equal to %s",
            someAge),
        isEqualFound);
  }

  @Test
  public void orTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
            "FROM %s " +
            "WHERE match_phrase(gender, 'F') OR match_phrase(gender, 'M') " +
            "LIMIT 1000", TEST_INDEX_ACCOUNT));
    Assert.assertEquals(1000, getTotalHits(response));
  }

  @Test
  public void andTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE age=32 AND gender='M' LIMIT 1000",
            TestsConstants.TEST_INDEX_PEOPLE));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      Assert.assertEquals(32, getSource(hit).getInt("age"));
      Assert.assertEquals("M", getSource(hit).getString("gender"));
    }
  }

  @Test
  public void likeTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE firstname LIKE 'amb%%' LIMIT 1000",
            TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));
    Assert.assertEquals("Amber", getSource(hits.getJSONObject(0)).getString("firstname"));
  }

  @Test
  public void notLikeTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE firstname NOT LIKE 'amb%%'",
            TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    Assert.assertNotEquals(0, getTotalHits(response));
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      Assert.assertFalse(getSource(hit).getString("firstname").toLowerCase().startsWith("amb"));
    }
  }

  @Test
  public void regexQueryTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE dog_name = REGEXP_QUERY('sn.*', 'INTERSECTION|COMPLEMENT|EMPTY', 10000)",
            TestsConstants.TEST_INDEX_DOG));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, hits.length());

    JSONObject hitSource = getSource(hits.getJSONObject(0));
    Assert.assertEquals("snoopy", hitSource.getString("dog_name"));
    Assert.assertEquals("Hattie", hitSource.getString("holdersName"));
    Assert.assertEquals(4, hitSource.getInt("age"));
  }

  @Test
  public void doubleNotTest() throws IOException {
    JSONObject response1 = executeQuery(
        String.format(Locale.ROOT,
            "SELECT * FROM %s WHERE NOT gender LIKE 'm' AND NOT gender LIKE 'f'",
            TEST_INDEX_ACCOUNT));
    Assert.assertEquals(0, getTotalHits(response1));

    JSONObject response2 = executeQuery(
        String.format(Locale.ROOT,
            "SELECT * FROM %s WHERE NOT gender LIKE 'm' AND gender NOT LIKE 'f'",
            TEST_INDEX_ACCOUNT));
    Assert.assertEquals(0, getTotalHits(response2));

    JSONObject response3 = executeQuery(
        String.format(Locale.ROOT,
            "SELECT * FROM %s WHERE gender NOT LIKE 'm' AND gender NOT LIKE 'f'",
            TEST_INDEX_ACCOUNT));
    Assert.assertEquals(0, getTotalHits(response3));

    JSONObject response4 = executeQuery(
        String.format(Locale.ROOT,
            "SELECT * FROM %s WHERE gender LIKE 'm' AND NOT gender LIKE 'f'",
            TEST_INDEX_ACCOUNT));
    // Assert there are results and they all have gender 'm'
    Assert.assertNotEquals(0, getTotalHits(response4));
    JSONArray hits = getHits(response4);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      Assert.assertEquals("m", getSource(hit).getString("gender").toLowerCase());
    }

    JSONObject response5 = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE NOT (gender = 'm' OR gender = 'f')",
            TEST_INDEX_ACCOUNT));
    Assert.assertEquals(0, getTotalHits(response5));
  }

  @Test
  public void limitTest() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT, "SELECT * FROM %s LIMIT 30",
        TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    Assert.assertEquals(30, hits.length());
  }

  @Test
  public void betweenTest() throws IOException {
    int min = 27;
    int max = 30;
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE age BETWEEN %s AND %s LIMIT 1000",
            TestsConstants.TEST_INDEX_PEOPLE, min, max));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      int age = getSource(hit).getInt("age");
      assertThat(age, allOf(greaterThanOrEqualTo(min), lessThanOrEqualTo(max)));
    }
  }

  // TODO When using NOT BETWEEN on fields, documents not containing the field
  //  are returned as well. This may be incorrect behavior.
  @Test
  public void notBetweenTest() throws IOException {
    int min = 20;
    int max = 37;
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE age NOT BETWEEN %s AND %s LIMIT 1000",
            TestsConstants.TEST_INDEX_PEOPLE, min, max));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);

      // Ignore documents which do not contain the age field
      if (source.has("age")) {
        int age = source.getInt("age");
        assertThat(age, not(allOf(greaterThanOrEqualTo(min), lessThanOrEqualTo(max))));
      }
    }
  }

  @Ignore("Semantic analysis failed because 'age' doesn't exist.")
  @Test
  public void inTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT age FROM %s WHERE age IN (20, 22) LIMIT 1000",
            TestsConstants.TEST_INDEX_PHRASE));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      int age = getSource(hit).getInt("age");
      assertThat(age, isOneOf(20, 22));
    }
  }

  @Test
  public void inTestWithStrings() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT,
            "SELECT phrase FROM %s WHERE phrase IN ('quick', 'fox') LIMIT 1000",
            TestsConstants.TEST_INDEX_PHRASE));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      String phrase = getSource(hit).getString("phrase");
      assertThat(phrase, isOneOf("quick fox here", "fox brown", "quick fox", "brown fox"));
    }
  }

  @Test
  public void inTermsTestWithIdentifiersTreatedLikeStrings() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT name " +
                "FROM %s " +
                "WHERE name.firstname = IN_TERMS('daenerys','eddard') " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(2, getTotalHits(response));
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      String firstname = ((JSONObject) getSource(hit).get("name")).getString("firstname");
      assertThat(firstname, isOneOf("Daenerys", "Eddard"));
    }
  }

  @Test
  public void inTermsTestWithStrings() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT name " +
                "FROM %s " +
                "WHERE name.firstname = IN_TERMS('daenerys','eddard') " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(2, getTotalHits(response));
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      String firstname = ((JSONObject) getSource(hit).get("name")).getString("firstname");
      assertThat(firstname, isOneOf("Daenerys", "Eddard"));
    }
  }

  @Test
  public void inTermsWithNumbers() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT name " +
                "FROM %s " +
                "WHERE name.ofHisName = IN_TERMS(4,2) " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));

    JSONObject hit = hits.getJSONObject(0);
    String firstname = ((JSONObject) getSource(hit).get("name")).getString("firstname");
    Assert.assertEquals("Brandon", firstname);
  }

  @Test
  public void termQueryWithNumber() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT,
            "SELECT name FROM %s WHERE name.ofHisName = term(4) LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));

    JSONObject hit = hits.getJSONObject(0);
    String firstname = ((JSONObject) getSource(hit).get("name")).getString("firstname");
    Assert.assertEquals("Brandon", firstname);
  }

  @Test
  public void termQueryWithStringIdentifier() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT name " +
                "FROM %s " +
                "WHERE name.firstname = term('brandon') " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));

    JSONObject hit = hits.getJSONObject(0);
    String firstname = ((JSONObject) getSource(hit).get("name")).getString("firstname");
    Assert.assertEquals("Brandon", firstname);
  }

  @Test
  public void termQueryWithStringLiteral() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT name " +
                "FROM %s " +
                "WHERE name.firstname = term('brandon') " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));

    JSONObject hit = hits.getJSONObject(0);
    String firstname = ((JSONObject) getSource(hit).get("name")).getString("firstname");
    Assert.assertEquals("Brandon", firstname);
  }

  // TODO When using NOT IN on fields, documents not containing the field
  //  are returned as well. This may be incorrect behavior.
  @Test
  public void notInTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT age FROM %s WHERE age NOT IN (20, 22) LIMIT 1000",
            TestsConstants.TEST_INDEX_PEOPLE));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);

      // Ignore documents which do not contain the age field
      if (source.has("age")) {
        int age = source.getInt("age");
        assertThat(age, not(isOneOf(20, 22)));
      }
    }
  }

  @Test
  public void dateSearch() throws IOException {
    DateTimeFormatter formatter = DateTimeFormat.forPattern(TestsConstants.DATE_FORMAT);
    DateTime dateToCompare = new DateTime(2014, 8, 18, 0, 0, 0);

    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT insert_time FROM %s WHERE insert_time < '2014-08-18'",
            TestsConstants.TEST_INDEX_ONLINE));
    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);
      DateTime insertTime = formatter.parseDateTime(source.getString("insert_time"));

      String errorMessage =
          String.format(Locale.ROOT, "insert_time must be before 2014-08-18. Found: %s",
              insertTime);
      Assert.assertTrue(errorMessage, insertTime.isBefore(dateToCompare));
    }
  }

  @Test
  public void dateSearchBraces() throws IOException {
    DateTimeFormatter formatter = DateTimeFormat.forPattern(TestsConstants.TS_DATE_FORMAT);
    DateTime dateToCompare = new DateTime(2015, 3, 15, 0, 0, 0);

    JSONObject response = executeQuery(
        String.format(Locale.ROOT,
            "SELECT odbc_time FROM %s/odbc WHERE odbc_time < {ts '2015-03-15 00:00:00.000'}",
            TestsConstants.TEST_INDEX_ODBC));
    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);
      String insertTimeStr = source.getString("odbc_time");
      insertTimeStr = insertTimeStr.replace("{ts '", "").replace("'}", "");

      DateTime insertTime = formatter.parseDateTime(insertTimeStr);
      String errorMessage =
          String.format(Locale.ROOT, "insert_time must be before 2015-03-15. Found: %s",
              insertTime);
      Assert.assertTrue(errorMessage, insertTime.isBefore(dateToCompare));
    }
  }

  @Test
  public void dateBetweenSearch() throws IOException {
    DateTimeFormatter formatter = DateTimeFormat.forPattern(TestsConstants.DATE_FORMAT);

    DateTime dateLimit1 = new DateTime(2014, 8, 18, 0, 0, 0);
    DateTime dateLimit2 = new DateTime(2014, 8, 21, 0, 0, 0);

    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT insert_time " +
                "FROM %s " +
                "WHERE insert_time BETWEEN '2014-08-18' AND '2014-08-21' " +
                "LIMIT 3",
            TestsConstants.TEST_INDEX_ONLINE));
    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);
      DateTime insertTime = formatter.parseDateTime(source.getString("insert_time"));

      boolean isBetween = (insertTime.isAfter(dateLimit1) || insertTime.isEqual(dateLimit1)) &&
          (insertTime.isBefore(dateLimit2) || insertTime.isEqual(dateLimit2));

      Assert.assertTrue("insert_time must be between 2014-08-18 and 2014-08-21", isBetween);
    }
  }

  @Test
  public void missFilterSearch() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE insert_time2 IS missing",
            TestsConstants.TEST_INDEX_PHRASE));

    JSONArray hits = getHits(response);
    Assert.assertEquals(4, getTotalHits(response));
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);

      Assert.assertFalse(source.has("insert_time2"));
    }
  }

  @Test
  public void notMissFilterSearch() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE insert_time2 IS NOT missing",
            TestsConstants.TEST_INDEX_PHRASE));

    JSONArray hits = getHits(response);
    Assert.assertEquals(2, getTotalHits(response));
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);

      Assert.assertTrue(source.has("insert_time2"));
    }
  }

  @Test
  public void complexConditionQuery() throws IOException {
    String errorMessage = "Result does not exist to the condition " +
        "(gender='m' AND (age> 25 OR account_number>5)) OR (gender='f' AND (age>30 OR account_number < 8)";

    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE (gender='m' AND (age> 25 OR account_number>5)) " +
                "OR (gender='f' AND (age>30 OR account_number < 8))",
            TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);

      String gender = source.getString("gender").toLowerCase();
      int age = source.getInt("age");
      int accountNumber = source.getInt("account_number");

      Assert.assertTrue(errorMessage,
          (gender.equals("m") && (age > 25 || accountNumber > 5))
              || (gender.equals("f") && (age > 30 || accountNumber < 8)));
    }
  }

  @Test
  public void complexNotConditionQuery() throws IOException {
    String errorMessage = "Result does not exist to the condition " +
        "NOT (gender='m' AND NOT (age > 25 OR account_number > 5)) " +
        "OR (NOT gender='f' AND NOT (age > 30 OR account_number < 8))";

    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE NOT (gender='m' AND NOT (age > 25 OR account_number > 5)) " +
                "OR (NOT gender='f' AND NOT (age > 30 OR account_number < 8))",
            TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    Assert.assertNotEquals(0, hits.length());
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);

      String gender = source.getString("gender").toLowerCase();
      int age = source.getInt("age");
      int accountNumber = source.getInt("account_number");

      Assert.assertTrue(errorMessage,
          !(gender.equals("m") && !(age > 25 || accountNumber > 5))
              || (!gender.equals("f") && !(age > 30 || accountNumber < 8)));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void orderByAscTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT age FROM %s ORDER BY age ASC LIMIT 1000",
            TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    ArrayList<Integer> ages = new ArrayList<>();
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);

      ages.add(source.getInt("age"));
    }

    ArrayList<Integer> sortedAges = (ArrayList<Integer>) ages.clone();
    Collections.sort(sortedAges);
    Assert.assertEquals("The list is not in ascending order", sortedAges, ages);
  }

  @Test
  public void orderByDescTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT age FROM %s ORDER BY age DESC LIMIT 1000",
            TEST_INDEX_ACCOUNT));
    assertResponseForOrderByTest(response);
  }

  @Test
  public void orderByDescUsingTableAliasTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT a.age FROM %s a ORDER BY a.age DESC LIMIT 1000",
            TEST_INDEX_ACCOUNT));
    assertResponseForOrderByTest(response);
  }

  @SuppressWarnings("unchecked")
  private void assertResponseForOrderByTest(JSONObject response) {
    JSONArray hits = getHits(response);
    ArrayList<Integer> ages = new ArrayList<>();
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);

      ages.add(source.getInt("age"));
    }

    ArrayList<Integer> sortedAges = (ArrayList<Integer>) ages.clone();
    Collections.sort(sortedAges, Collections.reverseOrder());
    Assert.assertEquals("The list is not in ascending order", sortedAges, ages);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void orderByAscFieldWithSpaceTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE `test field` IS NOT null " +
                "ORDER BY `test field` ASC " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_PHRASE));

    JSONArray hits = getHits(response);
    ArrayList<Integer> testFields = new ArrayList<>();
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);

      testFields.add(source.getInt("test field"));
    }

    ArrayList<Integer> sortedTestFields = (ArrayList<Integer>) testFields.clone();
    Collections.sort(sortedTestFields);
    Assert.assertEquals("The list is not in ascending order", sortedTestFields, testFields);
  }

  @Test
  public void testWhereWithBoolEqualsTrue() throws IOException {
    JSONObject response = executeQuery(
        StringUtils.format(
            "SELECT * " +
                "FROM %s " +
                "WHERE male = true " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_BANK)
    );

    checkResponseSize(response, BANK_INDEX_MALE_TRUE);
  }

  @Test
  public void testWhereWithBoolEqualsTrueAndGroupBy() throws IOException {
    JSONObject response = executeQuery(
        StringUtils.format(
            "SELECT * " +
                "FROM %s " +
                "WHERE male = true " +
                "GROUP BY balance " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_BANK)
    );

    checkAggregationResponseSize(response, BANK_INDEX_MALE_TRUE);
  }

  @Test
  public void testWhereWithBoolEqualsTrueAndOrderBy() throws IOException {
    JSONObject response = executeQuery(
        StringUtils.format(
            "SELECT * " +
                "FROM %s " +
                "WHERE male = true " +
                "ORDER BY age " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_BANK)
    );

    checkResponseSize(response, BANK_INDEX_MALE_TRUE);
  }

  @Test
  public void testWhereWithBoolIsTrue() throws IOException {
    JSONObject response = executeQuery(
        StringUtils.format(
            "SELECT * " +
                "FROM %s " +
                "WHERE male IS true " +
                "GROUP BY balance " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_BANK)
    );

    checkAggregationResponseSize(response, BANK_INDEX_MALE_TRUE);
  }

  @Test
  public void testWhereWithBoolIsNotTrue() throws IOException {
    JSONObject response = executeQuery(
        StringUtils.format(
            "SELECT * " +
                "FROM %s " +
                "WHERE male IS NOT true " +
                "GROUP BY balance " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_BANK)
    );

    checkAggregationResponseSize(response, BANK_INDEX_MALE_FALSE);
  }

  @Test
  public void testWhereWithBoolEqualsFalse() throws IOException {
    JSONObject response = executeQuery(
        StringUtils.format(
            "SELECT * " +
                "FROM %s " +
                "WHERE male = false " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_BANK)
    );

    checkResponseSize(response, BANK_INDEX_MALE_FALSE);
  }

  @Test
  public void testWhereWithBoolEqualsFalseAndGroupBy() throws IOException {
    JSONObject response = executeQuery(
        StringUtils.format(
            "SELECT * " +
                "FROM %s " +
                "WHERE male = false " +
                "GROUP BY balance " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_BANK)
    );

    checkAggregationResponseSize(response, BANK_INDEX_MALE_FALSE);
  }

  @Test
  public void testWhereWithBoolEqualsFalseAndOrderBy() throws IOException {
    JSONObject response = executeQuery(
        StringUtils.format(
            "SELECT * " +
                "FROM %s " +
                "WHERE male = false " +
                "ORDER BY age " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_BANK)
    );

    checkResponseSize(response, BANK_INDEX_MALE_FALSE);
  }

  @Test
  public void testWhereWithBoolIsFalse() throws IOException {
    JSONObject response = executeQuery(
        StringUtils.format(
            "SELECT * " +
                "FROM %s " +
                "WHERE male IS false " +
                "GROUP BY balance " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_BANK)
    );

    checkAggregationResponseSize(response, BANK_INDEX_MALE_FALSE);
  }

  @Test
  public void testWhereWithBoolIsNotFalse() throws IOException {
    JSONObject response = executeQuery(
        StringUtils.format(
            "SELECT * " +
                "FROM %s " +
                "WHERE male IS NOT false " +
                "GROUP BY balance " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_BANK)
    );

    checkAggregationResponseSize(response, BANK_INDEX_MALE_TRUE);
  }

  @Test
  public void testMultiPartWhere() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE (firstname LIKE 'opal' OR firstname LIKE 'rodriquez') " +
                "AND (state like 'oh' OR state like 'hi')",
            TEST_INDEX_ACCOUNT));

    Assert.assertEquals(2, getTotalHits(response));
  }

  @Test
  public void testMultiPartWhere2() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE ((account_number > 200 AND account_number < 300) OR gender LIKE 'm') " +
                "AND (state LIKE 'hi' OR address LIKE 'avenue')",
            TEST_INDEX_ACCOUNT));

    Assert.assertEquals(127, getTotalHits(response));
  }

  @Test
  public void testMultiPartWhere3() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE ((account_number > 25 AND account_number < 75) AND age >35 ) " +
                "AND (state LIKE 'md' OR (address LIKE 'avenue' OR address LIKE 'street'))",
            TEST_INDEX_ACCOUNT));

    Assert.assertEquals(7, getTotalHits(response));
  }

  @Test
  public void filterPolygonTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE GEO_INTERSECTS(place,'POLYGON ((102 2, 103 2, 103 3, 102 3, 102 2))')",
            TestsConstants.TEST_INDEX_LOCATION));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));

    JSONObject hit = hits.getJSONObject(0);
    Assert.assertEquals("bigSquare", getSource(hit).getString("description"));
  }

  @Test
  public void boundingBox() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT,
            "SELECT * FROM %s WHERE GEO_BOUNDING_BOX(center, 100.0, 1.0, 101, 0.0)",
            TestsConstants.TEST_INDEX_LOCATION));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));

    JSONObject hit = hits.getJSONObject(0);
    Assert.assertEquals("square", getSource(hit).getString("description"));
  }

  @Test
  public void geoDistance() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT,
            "SELECT * FROM %s WHERE GEO_DISTANCE(center, '1km', 100.5, 0.500001)",
            TestsConstants.TEST_INDEX_LOCATION));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));

    JSONObject hit = hits.getJSONObject(0);
    Assert.assertEquals("square", getSource(hit).getString("description"));
  }

  @Test
  public void geoPolygon() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT,
            "SELECT * FROM %s WHERE GEO_POLYGON(center, 100,0, 100.5, 2, 101.0,0)",
            TestsConstants.TEST_INDEX_LOCATION));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));

    JSONObject hit = hits.getJSONObject(0);
    Assert.assertEquals("square", getSource(hit).getString("description"));
  }

  @Ignore
  @Test
  public void escapedCharactersCheck() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE MATCH_PHRASE(nickname, 'Daenerys \"Stormborn\"') " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    Assert.assertEquals(1, getTotalHits(response));
  }

  @Test
  public void complexObjectSearch() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE MATCH_PHRASE(name.firstname, 'Jaime') " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    Assert.assertEquals(1, getTotalHits(response));
  }

  @Test
  public void complexObjectReturnField() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT parents.father " +
                "FROM %s " +
                "WHERE MATCH_PHRASE(name.firstname, 'Brandon') " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, getTotalHits(response));

    JSONObject hit = hits.getJSONObject(0);
    Assert.assertEquals("Eddard", getSource(hit).getJSONObject("parents").getString("father"));
  }

  /**
   * TODO: Fields prefixed with @ gets converted to SQLVariantRefExpr instead of SQLIdentifierExpr
   * Either change SQLVariantRefExpr to SQLIdentifierExpr
   * Or handle the special case for SQLVariantRefExpr
   */
  @Ignore
  @Test
  public void queryWithAtFieldOnWhere() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT,
        "SELECT * FROM %s where @wolf = 'Summer' LIMIT 1000", TEST_INDEX_GAME_OF_THRONES));
    Assert.assertEquals(1, getTotalHits(response));
    JSONObject hit = getHits(response).getJSONObject(0);
    Assert.assertEquals("Summer", hit.get("@wolf"));
    Assert.assertEquals("Brandon", hit.query("name/firstname"));
  }

  @Test
  public void queryWithDotAtStartOfIndexName() throws Exception {
    TestUtils.createHiddenIndexByRestClient(client(), ".bank", null);
    TestUtils.loadDataByRestClient(client(), ".bank", "/src/test/resources/.bank.json");

    String response = executeQuery("SELECT education FROM .bank WHERE account_number = 12345",
        "jdbc");
    Assert.assertTrue(response.contains("PhD"));
  }

  @Test
  public void notLikeTests() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT name " +
                "FROM %s " +
                "WHERE name.firstname NOT LIKE 'd%%' AND name IS NOT NULL " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(3, getTotalHits(response));
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = getSource(hit);

      String name = source.getJSONObject("name").getString("firstname");
      Assert
          .assertFalse(String.format(Locale.ROOT, "Name [%s] should not match pattern [d%%]", name),
              name.startsWith("d"));
    }
  }

  @Test
  public void isNullTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT name " +
                "FROM %s " +
                "WHERE nickname IS NULL " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    Assert.assertEquals(6, getTotalHits(response));
  }

  @Test
  public void isNotNullTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT name " +
                "FROM %s " +
                "WHERE nickname IS NOT NULL " +
                "LIMIT 1000",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    Assert.assertEquals(1, getTotalHits(response));
  }

  @Test
  public void innerQueryTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s D " +
                "WHERE holdersName IN (SELECT firstname " +
                "FROM %s " +
                "WHERE firstname = 'Hattie')",
            TestsConstants.TEST_INDEX_DOG, TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, hits.length());

    JSONObject hit = hits.getJSONObject(0);
    JSONObject source = getSource(hit);
    Assert.assertEquals("snoopy", source.getString("D.dog_name"));
    Assert.assertEquals("Hattie", source.getString("D.holdersName"));
    Assert.assertEquals(4, source.getInt("D.age"));
  }

  @Ignore
  @Test
  public void twoSubQueriesTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE holdersName IN (SELECT firstname " +
                "FROM %s " +
                "WHERE firstname = 'Hattie') " +
                "AND age IN (SELECT name.ofHisName " +
                "FROM %s " +
                "WHERE name.firstname <> 'Daenerys' " +
                "AND name.ofHisName IS NOT NULL) ",
            TestsConstants.TEST_INDEX_DOG,
            TEST_INDEX_ACCOUNT,
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, hits.length());

    JSONObject hit = hits.getJSONObject(0);
    JSONObject source = getSource(hit);
    Assert.assertEquals("snoopy", source.getString("dog_name"));
    Assert.assertEquals("Hattie", source.getString("holdersName"));
    Assert.assertEquals(4, source.getInt("age"));
  }

  @Ignore
  @Test
  public void inTermsSubQueryTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE age = IN_TERMS (SELECT name.ofHisName " +
                "FROM %s " +
                "WHERE name.firstname <> 'Daenerys' " +
                "AND name.ofHisName IS NOT NULL)",
            TestsConstants.TEST_INDEX_DOG, TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, hits.length());

    JSONObject hit = hits.getJSONObject(0);
    JSONObject source = getSource(hit);
    Assert.assertEquals("snoopy", source.getString("dog_name"));
    Assert.assertEquals("Hattie", source.getString("holdersName"));
    Assert.assertEquals(4, source.getInt("age"));
  }

  @Ignore
  @Test
  public void idsQueryOneId() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE _id = IDS_QUERY(dog, 1)",
            TestsConstants.TEST_INDEX_DOG));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, hits.length());

    JSONObject hit = hits.getJSONObject(0);
    JSONObject source = getSource(hit);
    Assert.assertEquals("rex", source.getString("dog_name"));
    Assert.assertEquals("Daenerys", source.getString("holdersName"));
    Assert.assertEquals(2, hit.getInt("age"));
  }

  @Ignore
  @Test
  public void idsQueryMultipleId() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE _id = IDS_QUERY(dog, 1, 2, 3)",
            TestsConstants.TEST_INDEX_DOG));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, hits.length());

    JSONObject hit = hits.getJSONObject(0);
    JSONObject source = getSource(hit);
    Assert.assertEquals("rex", source.getString("dog_name"));
    Assert.assertEquals("Daenerys", source.getString("holdersName"));
    Assert.assertEquals(2, hit.getInt("age"));
  }

  @Ignore
  @Test
  public void idsQuerySubQueryIds() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE _id = IDS_QUERY(dog, (SELECT name.ofHisName " +
                "FROM %s " +
                "WHERE name.firstname <> 'Daenerys' " +
                "AND name.ofHisName IS NOT NULL))",
            TestsConstants.TEST_INDEX_DOG, TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    JSONArray hits = getHits(response);
    Assert.assertEquals(1, hits.length());

    JSONObject hit = hits.getJSONObject(0);
    JSONObject source = getSource(hit);
    Assert.assertEquals("rex", source.getString("dog_name"));
    Assert.assertEquals("Daenerys", source.getString("holdersName"));
    Assert.assertEquals(2, hit.getInt("age"));
  }

  @Test
  public void nestedEqualsTestFieldNormalField() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE nested(message.info)='b'",
            TestsConstants.TEST_INDEX_NESTED_TYPE));

    Assert.assertEquals(1, getTotalHits(response));
  }

  @Test
  public void nestedEqualsTestFieldInsideArrays() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s WHERE nested(message.info) = 'a'",
            TestsConstants.TEST_INDEX_NESTED_TYPE));

    Assert.assertEquals(2, getTotalHits(response));
  }

  @Ignore // Seems like we don't support nested with IN, throwing IllegalArgumentException
  @Test
  public void nestedOnInQuery() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT,
        "SELECT * FROM %s where nested(message.info) IN ('a','b')", TEST_INDEX_NESTED_TYPE));
    Assert.assertEquals(3, getTotalHits(response));
  }

  @Test
  public void complexNestedQueryBothOnSameObject() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE nested('message', message.info = 'a' AND message.author ='i')",
            TestsConstants.TEST_INDEX_NESTED_TYPE));

    Assert.assertEquals(1, getTotalHits(response));
  }

  @Test
  public void complexNestedQueryNotBothOnSameObject() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE nested('message', message.info = 'a' AND message.author ='h')",
            TestsConstants.TEST_INDEX_NESTED_TYPE));

    Assert.assertEquals(0, getTotalHits(response));
  }

  @Test
  public void nestedOnInTermsQuery() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE nested(message.info) = IN_TERMS('a', 'b')",
            TestsConstants.TEST_INDEX_NESTED_TYPE));

    Assert.assertEquals(3, getTotalHits(response));
  }

  // TODO Uncomment these after problem with loading join index is resolved
//    @Test
//    public void childrenEqualsTestFieldNormalField() throws IOException {
//        JSONObject response = executeQuery(
//                        String.format(Locale.ROOT, "SELECT * " +
//                                      "FROM %s/joinType " +
//                                      "WHERE children(childrenType, info) = 'b'", TestsConstants.TEST_INDEX_JOIN_TYPE));
//
//        Assert.assertEquals(1, getTotalHits(response));
//    }
//
//    @Test
//    public void childrenOnInQuery() throws IOException {
//        JSONObject response = executeQuery(
//                        String.format(Locale.ROOT, "SELECT * " +
//                                      "FROM %s/joinType " +
//                                      "WHERE children(childrenType, info) IN ('a', 'b')",
//                                TestsConstants.TEST_INDEX_JOIN_TYPE));
//
//        Assert.assertEquals(2, getTotalHits(response));
//    }
//
//    @Test
//    public void complexChildrenQueryBothOnSameObject() throws IOException {
//        JSONObject response = executeQuery(
//                        String.format(Locale.ROOT, "SELECT * " +
//                                      "FROM %s/joinType " +
//                                      "WHERE children(childrenType, info = 'a' AND author ='e')",
//                                TestsConstants.TEST_INDEX_JOIN_TYPE));
//
//        Assert.assertEquals(1, getTotalHits(response));
//    }
//
//    @Test
//    public void complexChildrenQueryNotOnSameObject() throws IOException {
//        JSONObject response = executeQuery(
//                        String.format(Locale.ROOT, "SELECT * " +
//                                      "FROM %s/joinType " +
//                                      "WHERE children(childrenType, info = 'a' AND author ='j')",
//                                TestsConstants.TEST_INDEX_JOIN_TYPE));
//
//        Assert.assertEquals(0, getTotalHits(response));
//    }
//
//    @Test
//    public void childrenOnInTermsQuery() throws IOException {
//        JSONObject response = executeQuery(
//                        String.format(Locale.ROOT, "SELECT * " +
//                                      "FROM %s/joinType " +
//                                      "WHERE children(childrenType, info) = IN_TERMS(a, b)",
//                                TestsConstants.TEST_INDEX_JOIN_TYPE));
//
//        Assert.assertEquals(2, getTotalHits(response));
//    }

  @Ignore // the hint does not really work, NoSuchIndexException is thrown
  @Test
  public void multipleIndicesOneNotExistWithHint() throws IOException {

    JSONObject response = executeQuery(String
        .format(Locale.ROOT, "SELECT /*! IGNORE_UNAVAILABLE */ * FROM %s,%s ", TEST_INDEX_ACCOUNT,
            "badindex"));

    Assert.assertTrue(getTotalHits(response) > 0);
  }

  @Test
  public void multipleIndicesOneNotExistWithoutHint() throws IOException {
    try {
      executeQuery(
          String.format(Locale.ROOT, "SELECT * FROM %s, %s", TEST_INDEX_ACCOUNT, "badindex"));
      Assert.fail("Expected exception, but call succeeded");
    } catch (ResponseException e) {
      Assert.assertEquals(RestStatus.BAD_REQUEST.getStatus(),
          e.getResponse().getStatusLine().getStatusCode());
      final String entity = TestUtils.getResponseBody(e.getResponse());
      Assert.assertThat(entity, containsString("\"type\": \"IndexNotFoundException\""));
    }
  }

  // TODO Find way to check routing() without SearchRequestBuilder
  //  to properly update these tests to ESIntegTestCase format
//    @Test
//    public void routingRequestOneRounting() throws IOException {
//        SqlElasticSearchRequestBuilder request = getRequestBuilder(String.format(Locale.ROOT,
//                                  "SELECT /*! ROUTINGS(hey) */ * FROM %s ", TEST_INDEX_ACCOUNT));
//        SearchRequestBuilder searchRequestBuilder = (SearchRequestBuilder) request.getBuilder();
//        Assert.assertEquals("hey",searchRequestBuilder.request().routing());
//    }
//
//    @Test
//    public void routingRequestMultipleRountings() throws IOException {
//        SqlElasticSearchRequestBuilder request = getRequestBuilder(String.format(Locale.ROOT,
//                                  "SELECT /*! ROUTINGS(hey,bye) */ * FROM %s ", TEST_INDEX_ACCOUNT));
//        SearchRequestBuilder searchRequestBuilder = (SearchRequestBuilder) request.getBuilder();
//        Assert.assertEquals("hey,bye",searchRequestBuilder.request().routing());
//    }

  @Ignore // Getting parser error: syntax error, expect RPAREN, actual IDENTIFIER insert_time
  @Test
  public void scriptFilterNoParams() throws IOException {

    JSONObject result = executeQuery(String.format(Locale.ROOT,
        "SELECT insert_time FROM %s where script('doc[\\'insert_time\''].date.hourOfDay==16') " +
            "and insert_time <'2014-08-21T00:00:00.000Z'", TEST_INDEX_ONLINE));
    Assert.assertEquals(237, getTotalHits(result));
  }

  @Ignore // Getting parser error: syntax error, expect RPAREN, actual IDENTIFIER insert_time
  @Test
  public void scriptFilterWithParams() throws IOException {

    JSONObject result = executeQuery(String.format(Locale.ROOT,
        "SELECT insert_time FROM %s where script('doc[\\'insert_time\''].date.hourOfDay==x','x'=16) " +
            "and insert_time <'2014-08-21T00:00:00.000Z'", TEST_INDEX_ONLINE));
    Assert.assertEquals(237, getTotalHits(result));
  }

  @Test
  public void highlightPreTagsAndPostTags() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT,
            "SELECT /*! HIGHLIGHT(phrase, pre_tags : ['<b>'], post_tags : ['</b>']) */ " +
                "* FROM %s " +
                "WHERE phrase LIKE 'fox' " +
                "ORDER BY _score", TestsConstants.TEST_INDEX_PHRASE));

    JSONArray hits = getHits(response);
    for (int i = 0; i < hits.length(); i++) {
      JSONObject hit = hits.getJSONObject(i);
      JSONObject highlightFields = hit.getJSONObject("highlight");

      String phrase = highlightFields.getJSONArray("phrase").getString(0);
      Assert.assertTrue(phrase.contains("<b>fox</b>"));
    }
  }

  @Ignore
  @Test
  public void fieldCollapsingTest() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT /*! COLLAPSE({\"field\":\"age\"," +
            "\"inner_hits\":{\"name\": \"account\"," +
            "\"size\":1," +
            "\"sort\":[{\"age\":\"asc\"}]}," +
            "\"max_concurrent_group_searches\": 4}) */ " +
            "* FROM %s", TEST_INDEX_ACCOUNT));

    JSONArray hits = getHits(response);
    Assert.assertEquals(21, hits.length());
  }

  @Test
  public void backticksQuotedIndexNameTest() throws Exception {
    TestUtils.createIndexByRestClient(client(), "bank_unquote", null);
    TestUtils
        .loadDataByRestClient(client(), "bank", "/src/test/resources/bank_for_unquote_test.json");

    JSONArray hits = getHits(executeQuery("SELECT lastname FROM `bank`"));
    Object responseIndex = ((JSONObject) hits.get(0)).query("/_index");
    assertEquals("bank", responseIndex);

    assertEquals(
        executeQuery("SELECT lastname FROM bank", "jdbc"),
        executeQuery("SELECT `bank`.`lastname` FROM `bank`", "jdbc")
    );

    assertEquals(
        executeQuery(
            "SELECT `b`.`age` AS `AGE`, AVG(`b`.`balance`) FROM `bank` AS `b` " +
                "WHERE ABS(`b`.`age`) > 20 GROUP BY `b`.`age` ORDER BY `b`.`age`",
            "jdbc"),
        executeQuery("SELECT b.age AS AGE, AVG(balance) FROM bank AS b " +
                "WHERE ABS(age) > 20 GROUP BY b.age ORDER BY b.age",
            "jdbc")
    );
  }

  @Test
  public void backticksQuotedFieldNamesTest() {
    String expected = executeQuery(StringUtils.format("SELECT b.lastname FROM %s " +
        "AS b ORDER BY age LIMIT 3", TestsConstants.TEST_INDEX_BANK), "jdbc");
    String quotedFieldResult = executeQuery(StringUtils.format("SELECT b.`lastname` FROM %s " +
        "AS b ORDER BY age LIMIT 3", TestsConstants.TEST_INDEX_BANK), "jdbc");

    assertEquals(expected, quotedFieldResult);
  }

  @Test
  public void backticksQuotedAliasTest() {
    String expected = executeQuery(StringUtils.format("SELECT b.lastname FROM %s " +
        "AS b ORDER BY age LIMIT 3", TestsConstants.TEST_INDEX_BANK), "jdbc");
    String quotedAliasResult = executeQuery(StringUtils.format("SELECT `b`.lastname FROM %s" +
        " AS `b` ORDER BY age LIMIT 3", TestsConstants.TEST_INDEX_BANK), "jdbc");
    String quotedAliasAndFieldResult =
        executeQuery(StringUtils.format("SELECT `b`.`lastname` FROM %s " +
            "AS `b` ORDER BY age LIMIT 3", TestsConstants.TEST_INDEX_BANK), "jdbc");

    assertEquals(expected, quotedAliasResult);
    assertEquals(expected, quotedAliasAndFieldResult);
  }

  @Test
  public void backticksQuotedAliasWithSpecialCharactersTest() {
    String expected = executeQuery(StringUtils.format("SELECT b.lastname FROM %s " +
        "AS b ORDER BY age LIMIT 3", TestsConstants.TEST_INDEX_BANK), "jdbc");
    String specialCharAliasResult =
        executeQuery(StringUtils.format("SELECT `b k`.lastname FROM %s " +
            "AS `b k` ORDER BY age LIMIT 3", TestsConstants.TEST_INDEX_BANK), "jdbc");

    assertEquals(expected, specialCharAliasResult);
  }

  @Test
  public void backticksQuotedAliasInJDBCResponseTest() {
    String query = StringUtils.format("SELECT `b`.`lastname` AS `name` FROM %s AS `b` " +
        "ORDER BY age LIMIT 3", TestsConstants.TEST_INDEX_BANK);
    String response = executeQuery(query, "jdbc");

    assertTrue(response.contains("\"alias\": \"name\""));
  }

  @Test
  public void caseWhenSwitchTest() throws IOException {
    JSONObject response = executeQuery("SELECT CASE age " +
        "WHEN '30' THEN '1' " +
        "WHEN '40' THEN '2' " +
        "ELSE '0' END AS cases FROM " + TEST_INDEX_ACCOUNT + " WHERE age IS NOT NULL");
    JSONObject hit = getHits(response).getJSONObject(0);
    String age = hit.query("/_source/age").toString();
    String cases = age.equals("30") ? "1" : age.equals("40") ? "2" : "0";

    assertThat(cases, equalTo(hit.query("/fields/cases/0")));
  }

  @Test
  public void caseWhenJdbcResponseTest() {
    String response = executeQuery("SELECT CASE age " +
        "WHEN '30' THEN 'age is 30' " +
        "WHEN '40' THEN 'age is 40' " +
        "ELSE 'NA' END AS cases FROM " + TEST_INDEX_ACCOUNT + " WHERE age is not null", "jdbc");
    assertTrue(
        response.contains("age is 30") ||
            response.contains("age is 40") ||
            response.contains("NA")
    );
  }

  @Test
  public void functionInCaseFieldShouldThrowESExceptionDueToIllegalScriptInJdbc() {
    String response = executeQuery(
        "select case lower(firstname) when 'amber' then '1' else '2' end as cases from " +
            TEST_INDEX_ACCOUNT,
        "jdbc");
    queryInJdbcResponseShouldIndicateESException(response, "SearchPhaseExecutionException",
        "For more details, please send request for Json format");
  }

  @Test
  public void functionCallWithIllegalScriptShouldThrowESExceptionInJdbc() {
    String response = executeQuery("select log(balance + 2) from " + TEST_INDEX_BANK,
        "jdbc");
    queryInJdbcResponseShouldIndicateESException(response, "SearchPhaseExecutionException",
        "please send request for Json format to see the raw response from elasticsearch engine.");
  }

  @Ignore("Goes in different route, does not call PrettyFormatRestExecutor.execute methods." +
      "The performRequest method in RestClient doesn't throw any exceptions for null value fields in script")
  @Test
  public void functionArgWithNullValueFieldShouldThrowESExceptionInJdbc() {
    String response = executeQuery(
        "select log(balance) from " + TEST_INDEX_BANK_WITH_NULL_VALUES, "jdbc");
    queryInJdbcResponseShouldIndicateESException(response, "SearchPhaseExecutionException",
        "For more details, please send request for Json format");
  }

  private void queryInJdbcResponseShouldIndicateESException(String response, String exceptionType,
                                                            String... errMsgs) {
    Assert.assertThat(response, containsString(exceptionType));
    for (String errMsg : errMsgs) {
      Assert.assertThat(response, containsString(errMsg));
    }
  }

  private String getScrollId(JSONObject response) {
    return response.getString("_scroll_id");
  }

  private void checkResponseSize(JSONObject response, int sizeCheck) {
    JSONArray queryResponse = getHits(response);
    Assert.assertThat(queryResponse.length(), equalTo(sizeCheck));
  }

  private void checkAggregationResponseSize(JSONObject response, int sizeCheck) {
    JSONArray queryResponse = (JSONArray) response.query("/aggregations/balance/buckets");
    Assert.assertThat(queryResponse.length(), equalTo(sizeCheck));
  }

  private void checkSelectAllAndFieldResponseSize(JSONObject response) {
    String[] arr =
        new String[] {"account_number", "firstname", "address", "birthdate", "gender", "city",
            "lastname",
            "balance", "employer", "state", "age", "email", "male"};
    Set<String> expectedSource = new HashSet<>(Arrays.asList(arr));

    JSONArray hits = getHits(response);
    Assert.assertTrue(hits.length() > 0);
    for (int i = 0; i < hits.length(); ++i) {
      JSONObject hit = hits.getJSONObject(i);
      Assert.assertEquals(expectedSource, getSource(hit).keySet());
    }
  }

  private void checkSelectAllAndFieldAggregationResponseSize(JSONObject response, String field) {
    JSONObject fieldAgg = (response.getJSONObject("aggregations")).getJSONObject(field);
    JSONArray buckets = fieldAgg.getJSONArray("buckets");
    Assert.assertTrue(buckets.length() == 6);
  }
}
