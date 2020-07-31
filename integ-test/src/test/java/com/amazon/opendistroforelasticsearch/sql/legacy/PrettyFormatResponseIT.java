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

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.elasticsearch.client.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

/**
 * PrettyFormatResponseIT will likely be excluding some of the tests written in PrettyFormatResponseTest since
 * those tests were asserting on class objects directly. These updated tests will only be making assertions based
 * on the REST response.
 * <p>
 * Any integ tests from PrettyFormatResponseTest that were excluded can perhaps later be changed and moved over
 * to be unit tests.
 * <p>
 * Tests from original integ tests excluded:
 * - noIndexType()
 * - withIndexType()
 */
public class PrettyFormatResponseIT extends SQLIntegTestCase {

  private static final Set<String> allAccountFields = Sets.newHashSet(
      "account_number", "balance", "firstname", "lastname", "age", "gender", "address", "employer",
      "email", "city", "state"
  );

  private static final Set<String> regularFields = Sets.newHashSet("someField", "myNum");

  private static final Set<String> messageFields = Sets.newHashSet(
      "message.dayOfWeek", "message.info", "message.author");

  private static final Set<String> commentFields = Sets.newHashSet("comment.data", "comment.likes");

  private static final List<String> nameFields = Arrays.asList("firstname", "lastname");

  private final int RESPONSE_DEFAULT_MAX_SIZE = 200;

  @Override
  protected void init() throws Exception {
    loadIndex(Index.ACCOUNT);
    loadIndex(Index.PHRASE);
    loadIndex(Index.GAME_OF_THRONES);
    loadIndex(Index.NESTED);
  }

  @Override
  protected Request getSqlRequest(String request, boolean explain) {
    Request sqlRequest = super.getSqlRequest(request, explain);
    sqlRequest.addParameter("format", "jdbc");

    return sqlRequest;
  }

  @Ignore("Index type is removed in ES 7+")
  @Test
  public void wrongIndexType() throws IOException {
    String type = "wrongType";
    try {
      executeQuery(String.format(Locale.ROOT, "SELECT * FROM %s/%s",
          TestsConstants.TEST_INDEX_ACCOUNT, type));
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(),
          is(String.format(Locale.ROOT, "Index type %s does not exist", type)));
    }
  }

  @Test
  public void selectAll() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s",
            TestsConstants.TEST_INDEX_ACCOUNT));

    // This also tests that .keyword fields are ignored when SELECT * is called
    assertContainsColumnsInAnyOrder(getSchema(response), allAccountFields);
    assertContainsData(getDataRows(response), allAccountFields);
  }

  @Test
  public void selectNames() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT firstname, lastname FROM %s",
            TestsConstants.TEST_INDEX_ACCOUNT));

    assertContainsColumns(getSchema(response), nameFields);
    assertContainsData(getDataRows(response), nameFields);
  }

  @Ignore("Semantic analysis takes care of this")
  @Test
  public void selectWrongField() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT wrongField FROM %s",
            TestsConstants.TEST_INDEX_ACCOUNT));

    assertThat(getSchema(response).length(), equalTo(0));

    // DataRows object will still get populated with SearchHits but since wrongField is not available in the Map
    // each row in the response will be empty
    // TODO Perhaps a code change should be made to format logic to ensure a
    //  'datarows' length of 0 in response for this case
    assertThat(getDataRows(response).length(), equalTo(RESPONSE_DEFAULT_MAX_SIZE));
  }

  @Test
  public void selectKeyword() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT firstname.keyword FROM %s",
            TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Collections.singletonList("firstname.keyword");
    assertContainsColumns(getSchema(response), fields);

    /*
     * firstname.keyword will appear in Schema but because there is no 'firstname.keyword' in SearchHits source
     * the DataRows will output null.
     *
     * Looks like x-pack adds this keyword field to "docvalue_fields", this is likely how it ends up in SearchHits
     */
    // assertContainsData(getDataRows(protocol), fields);
  }

  @Test
  public void selectScore() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT _score FROM %s WHERE balance > 30000",
            TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Collections.singletonList("_score");
    assertContainsColumns(getSchema(response), fields);
    assertContainsData(getDataRows(response), fields);
  }

  @Test
  public void selectAllFromNestedWithoutFieldInFrom() throws IOException {
    assertNestedFieldQueryResultContainsColumnsAndData("SELECT * FROM %s",
        regularFields, fields("message", "comment"));
  }

  @Test
  public void selectAllFromNestedWithFieldInFrom() throws IOException {
    assertNestedFieldQueryResultContainsColumnsAndData("SELECT * FROM %s e, e.message m",
        regularFields, messageFields);
  }

  @Test
  public void selectAllFromNestedWithMultipleFieldsInFrom() throws IOException {
    assertNestedFieldQueryResultContainsColumnsAndData(
        "SELECT * FROM %s e, e.message m, e.comment c",
        regularFields, messageFields, commentFields);
  }

  @Test
  public void selectAllNestedFromNestedWithFieldInFrom() throws IOException {
    assertNestedFieldQueryResultContainsColumnsAndData("SELECT m.* FROM %s e, e.message m",
        messageFields);
  }

  @Test
  public void selectSpecificRegularFieldAndAllFromNestedWithFieldInFrom() throws IOException {
    assertNestedFieldQueryResultContainsColumnsAndData(
        "SELECT e.someField, m.* FROM %s e, e.message m",
        fields("someField"), messageFields);
  }

  /**
   * Execute the query against index with nested fields and assert result contains columns and data as expected.
   */
  @SafeVarargs
  private final void assertNestedFieldQueryResultContainsColumnsAndData(String query,
                                                                        Set<String>... expectedFieldNames)
      throws IOException {
    JSONObject response =
        executeQuery(String.format(Locale.ROOT, query, TestsConstants.TEST_INDEX_NESTED_TYPE));
    Set<String> allExpectedFieldNames = Stream.of(expectedFieldNames).
        flatMap(Set::stream).
        collect(toSet());

    assertContainsColumnsInAnyOrder(getSchema(response), allExpectedFieldNames);
    assertContainsData(getDataRows(response), allExpectedFieldNames);
  }

  private Set<String> fields(String... fieldNames) {
    return Sets.newHashSet(fieldNames);
  }

  @Test
  public void selectNestedFields() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT nested(message.info), someField FROM %s",
            TestsConstants.TEST_INDEX_NESTED_TYPE));

    List<String> fields = Arrays.asList("message.info", "someField");
    assertContainsColumns(getSchema(response), fields);
    assertContainsData(getDataRows(response), fields);

    // The nested test index being used contains 5 entries but one of them has an array of 2 message objects, so
    // we check to see if the amount of data rows is 6 since that is the result after flattening
    assertThat(getDataRows(response).length(), equalTo(6));
  }

  @Test
  public void selectNestedFieldWithWildcard() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT nested(message.*) FROM %s",
            TestsConstants.TEST_INDEX_NESTED_TYPE));

    assertContainsColumnsInAnyOrder(getSchema(response), messageFields);
    assertContainsData(getDataRows(response), messageFields);
  }

  @Test
  public void selectWithWhere() throws IOException {
    int balanceToCompare = 30000;
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT balance " +
                "FROM %s " +
                "WHERE balance > %d",
            TestsConstants.TEST_INDEX_ACCOUNT, balanceToCompare));

    /*
     * Previously the DataRows map was used to check specific fields but the JDBC response for "datarows" is a
     * JSONArray so keys cannot be used to identify the field. Instead, the expected position will be used to
     * retrieve the data from the JSONArray representing each "row".
     */
    JSONArray dataRows = getDataRows(response);
    for (int i = 0; i < dataRows.length(); i++) {
      JSONArray row = dataRows.getJSONArray(i);
      int balance = row.getInt(0);

      assertThat(balance, greaterThan(balanceToCompare));
    }
  }

  @Test
  public void groupBySingleField() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s GROUP BY age",
            TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Collections.singletonList("age");
    assertContainsColumns(getSchema(response), fields);
    assertContainsData(getDataRows(response), fields);
  }

  @Test
  public void groupByMultipleFields() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s GROUP BY age, balance",
            TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Arrays.asList("age", "balance");
    assertContainsColumns(getSchema(response), fields);
    assertContainsData(getDataRows(response), fields);
  }

  @Test
  public void testSizeAndTotal() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * " +
                "FROM %s " +
                "WHERE balance > 30000 " +
                "LIMIT 5",
            TestsConstants.TEST_INDEX_ACCOUNT));

    JSONArray dataRows = getDataRows(response);
    assertThat(dataRows.length(), equalTo(5));

    // The value to compare to here was obtained by running the query in the plugin and looking at the SearchHits
    int totalHits = response.getInt("total");
    assertThat(totalHits, equalTo(402));
  }

  @Test
  public void testSizeWithGroupBy() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT * FROM %s GROUP BY age LIMIT 5",
            TestsConstants.TEST_INDEX_ACCOUNT));

    assertThat(getDataRows(response).length(), equalTo(5));
  }

  @Test
  public void aggregationFunctionInSelect() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT COUNT(*) FROM %s GROUP BY age",
            TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Arrays.asList("COUNT(*)");
    assertContainsColumns(getSchema(response), fields);

    JSONArray dataRows = getDataRows(response);
    for (int i = 0; i < dataRows.length(); i++) {
      JSONArray row = dataRows.getJSONArray(i);
      long countVal = row.getLong(0);

      assertThat(countVal, greaterThan((long) 0));
    }
  }

  @Test
  public void aggregationFunctionInSelectCaseCheck() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT count(*) FROM %s GROUP BY age",
            TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Arrays.asList("COUNT(*)");
    assertContainsColumns(getSchema(response), fields);

    JSONArray dataRows = getDataRows(response);
    for (int i = 0; i < dataRows.length(); i++) {
      JSONArray row = dataRows.getJSONArray(i);
      long countVal = row.getLong(0);

      assertThat(countVal, greaterThan((long) 0));
    }
  }

  @Test
  public void aggregationFunctionInSelectWithAlias() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT COUNT(*) AS total FROM %s GROUP BY age",
            TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Arrays.asList("total");
    assertContainsColumns(getSchema(response), fields);

    JSONArray dataRows = getDataRows(response);
    for (int i = 0; i < dataRows.length(); i++) {
      JSONArray row = dataRows.getJSONArray(i);
      long countVal = row.getLong(0);

      assertThat(countVal, greaterThan((long) 0));
    }
  }

  @Test
  public void aggregationFunctionInSelectGroupByMultipleFields() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT SUM(age) FROM %s GROUP BY age, state.keyword",
            TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Arrays.asList("SUM(age)");
    assertContainsColumns(getSchema(response), fields);
    assertContainsData(getDataRows(response), fields);
  }

  @Test
  public void aggregationFunctionInSelectNoGroupBy() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT, "SELECT SUM(age) FROM %s",
        TestsConstants.TEST_INDEX_ACCOUNT));

    String ageSum = "SUM(age)";
    assertContainsColumns(getSchema(response), Collections.singletonList(ageSum));

    JSONArray dataRows = getDataRows(response);
    for (int i = 0; i < dataRows.length(); i++) {
      JSONArray row = dataRows.getJSONArray(i);
      double sumVal = row.getDouble(0);

      assertThat(sumVal, greaterThan((double) 0));
    }
  }

  @Test
  public void multipleAggregationFunctionsInSelect() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT COUNT(*), AVG(age) FROM %s GROUP BY age",
            TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Arrays.asList("COUNT(*)", "AVG(age)");
    assertContainsColumns(getSchema(response), fields);
    assertContainsData(getDataRows(response), fields);
  }

  @Test
  public void aggregationFunctionInHaving() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT,
        "SELECT gender " +
            "FROM %s " +
            "GROUP BY gender " +
            "HAVING count(*) > 500",
        TestsConstants.TEST_INDEX_ACCOUNT));

    String ageSum = "gender";
    assertContainsColumns(getSchema(response), Collections.singletonList(ageSum));

    JSONArray dataRows = getDataRows(response);
    assertEquals(1, dataRows.length());
    assertEquals("m", dataRows.getJSONArray(0).getString(0));
  }

  /**
   * This case doesn't seem to be supported by the plugin at the moment.
   * Looks like the painless script of the inner function is put inside the aggregation function but
   * this syntax may not be correct since it returns 0 which is the default value (since 0 is returned in
   * cases like COUNT(wrongField) as well).
   */
//    @Test
//    public void nestedAggregationFunctionInSelect() {
//        String query = String.format(Locale.ROOT, "SELECT SUM(SQRT(age)) FROM age GROUP BY age", TEST_INDEX_ACCOUNT);
//    }
  @Ignore("New engine returns string type")
  @Test
  public void fieldsWithAlias() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT firstname AS first, age AS a FROM %s",
            TestsConstants.TEST_INDEX_ACCOUNT));

    Map<String, String> aliases = new HashMap<>();
    aliases.put("firstname", "first");
    aliases.put("age", "a");

    assertContainsAliases(getSchema(response), aliases);
  }

  @Test
  public void indexWithMissingFields() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT phrase, insert_time2 " +
                "FROM %s " +
                "WHERE match_phrase(phrase, 'brown fox')",
            TestsConstants.TEST_INDEX_PHRASE));

    JSONArray dataRowEntry = getDataRows(response).getJSONArray(0);
    assertThat(dataRowEntry.length(), equalTo(2));
    assertThat(dataRowEntry.get(0), equalTo("brown fox"));
    assertThat(dataRowEntry.get(1),
        equalTo(JSONObject.NULL)); // TODO See if this null check is failing
  }

  @Test
  public void joinQuery() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT b1.balance, b1.age, b2.firstname " +
                "FROM %s b1 JOIN %s b2 ON b1.age = b2.age",
            TestsConstants.TEST_INDEX_ACCOUNT, TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Arrays.asList("b1.balance", "b1.age", "b2.firstname");
    assertContainsColumns(getSchema(response), fields);
    assertContainsData(getDataRows(response), fields);
  }

  @Test
  public void joinQueryWithAlias() throws IOException {
    JSONObject response = executeQuery(String.format(Locale.ROOT, "SELECT b1.balance AS bal, " +
            " b1.age AS age, b2.firstname AS name FROM %s b1 JOIN %s b2 ON b1.age = b2.age",
        TestsConstants.TEST_INDEX_ACCOUNT, TestsConstants.TEST_INDEX_ACCOUNT));

    Map<String, String> aliases = new HashMap<>();
    aliases.put("b1.balance", "bal");
    aliases.put("b1.age", "age");
    aliases.put("b2.firstname", "name");

    assertContainsAliases(getSchema(response), aliases);
    assertContainsData(getDataRows(response), Arrays.asList("bal", "age", "name"));
  }

  @Test
  public void joinQueryWithObjectFieldInSelect() throws IOException {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT c.name.firstname, d.name.lastname " +
                "FROM %s c JOIN %s d ON d.hname = c.house",
            TestsConstants.TEST_INDEX_GAME_OF_THRONES,
            TestsConstants.TEST_INDEX_GAME_OF_THRONES));

    List<String> fields = Arrays.asList("c.name.firstname", "d.name.lastname");
    assertContainsColumns(getSchema(response), fields);

    // d.name.lastname is null here since entries with hname don't have a name.lastname entry, so only length is
    // checked
    JSONArray dataRows = getDataRows(response);
    assertThat(dataRows.length(), greaterThan(0));

    JSONArray row = dataRows.getJSONArray(0);
    assertThat(row.length(), equalTo(fields.size()));
  }

  @Test
  public void joinQuerySelectOnlyOnOneTable() throws Exception {
    JSONObject response = executeQuery(
        String.format(Locale.ROOT, "SELECT b1.age " +
                "FROM %s b1 JOIN %s b2 ON b1.firstname = b2.firstname",
            TestsConstants.TEST_INDEX_ACCOUNT, TestsConstants.TEST_INDEX_ACCOUNT));

    List<String> fields = Collections.singletonList("b1.age");
    assertContainsColumns(getSchema(response), fields);
    assertContainsData(getDataRows(response), fields);
  }

  @Test
  public void fieldOrder() throws IOException {

    final String[] expectedFields = {"age", "firstname", "address", "gender", "email"};
    final Object[] expectedValues = {32, "Amber", "880 Holmes Lane", "M", "amberduke@pyrami.com"};

    testFieldOrder(expectedFields, expectedValues);
  }

  @Test
  public void fieldOrderOther() throws IOException {

    final String[] expectedFields = {"email", "firstname", "age", "gender", "address"};
    final Object[] expectedValues = {"amberduke@pyrami.com", "Amber", 32, "M", "880 Holmes Lane"};

    testFieldOrder(expectedFields, expectedValues);
  }

  private void testFieldOrder(final String[] expectedFields, final Object[] expectedValues)
      throws IOException {

    final String fields = String.join(", ", expectedFields);
    final String query = String.format(Locale.ROOT, "SELECT %s FROM %s " +
        "WHERE email='amberduke@pyrami.com'", fields, TestsConstants.TEST_INDEX_ACCOUNT);
    final JSONObject result = executeQuery(query);

    for (int i = 0; i < expectedFields.length; ++i) {

      final String fieldName =
          (String) result.query(String.format(Locale.ROOT, "/schema/%d/name", i));
      assertThat(fieldName, equalTo(expectedFields[i]));
      final Object fieldValue = result.query(String.format(Locale.ROOT, "/datarows/0/%d", i));
      assertThat(fieldValue, equalTo(expectedValues[i]));
    }
  }

  private JSONArray getSchema(JSONObject jdbcResponse) {
    return jdbcResponse.getJSONArray("schema");
  }

  private JSONArray getDataRows(JSONObject jdbcResponse) {
    return jdbcResponse.getJSONArray("datarows");
  }

  private void assertContainsColumnsInAnyOrder(JSONArray schema, Set<String> fields) {

    assertThat(schema.length(), equalTo(fields.size()));

    for (int i = 0; i < schema.length(); i++) {
      JSONObject column = schema.getJSONObject(i);
      String name = column.getString("name");

      assertTrue(fields.contains(name));
    }
  }

  private void assertContainsColumns(JSONArray schema, List<String> fields) {

    assertThat(schema.length(), equalTo(fields.size()));

    for (int i = 0; i < schema.length(); i++) {
      JSONObject column = schema.getJSONObject(i);
      String name = column.getString("name");

      assertThat(name, equalTo(fields.get(i)));
    }
  }

  private void assertContainsAliases(JSONArray schema, Map<String, String> aliases) {
    for (int i = 0; i < schema.length(); i++) {
      JSONObject column = schema.getJSONObject(i);
      assertTrue(column.has("alias"));

      String name = column.getString("name");
      String alias = column.getString("alias");

      assertThat(alias, equalTo(aliases.get(name)));
    }
  }

  private void assertContainsData(JSONArray dataRows, Collection<String> fields) {
    assertThat(dataRows.length(), greaterThan(0));
    JSONArray row = dataRows.getJSONArray(0);

    assertThat(row.length(), equalTo(fields.size()));
    for (int i = 0; i < row.length(); i++) {
      assertThat(row.get(i), not(equalTo(JSONObject.NULL)));
    }
  }
}
