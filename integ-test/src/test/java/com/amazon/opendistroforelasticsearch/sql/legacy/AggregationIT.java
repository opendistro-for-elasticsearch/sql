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
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_GAME_OF_THRONES;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_NESTED_TYPE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_ONLINE;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRowsInOrder;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class AggregationIT extends SQLIntegTestCase {

  @Override
  protected void init() throws Exception {
    loadIndex(Index.ACCOUNT);
    loadIndex(Index.GAME_OF_THRONES);
    loadIndex(Index.DOG);
    loadIndex(Index.ONLINE);
    loadIndex(Index.NESTED);
  }

  @Test
  public void countTest() throws IOException {

    JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s", TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    Assert.assertThat(getIntAggregationValue(result, "COUNT(*)", "value"), equalTo(1000));
  }

  @Test
  public void countDistinctTest() {
    JSONObject response = executeJdbcRequest(
        String.format("SELECT COUNT(distinct gender) FROM %s", TEST_INDEX_ACCOUNT));

    verifySchema(response, schema("COUNT(DISTINCT gender)", null, "integer"));
    verifyDataRows(response, rows(2));
  }

  @Test
  public void countWithDocsHintTest() throws Exception {

    JSONObject result =
        executeQuery(String.format("SELECT /*! DOCS_WITH_AGGREGATION(10) */ count(*) from %s",
            TEST_INDEX_ACCOUNT));
    JSONArray hits = (JSONArray) result.query("/hits/hits");
    Assert.assertThat(hits.length(), equalTo(10));
  }

  @Test
  public void sumTest() throws IOException {

    JSONObject result =
        executeQuery(String.format("SELECT SUM(balance) FROM %s", TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    Assert.assertThat(getDoubleAggregationValue(result, "SUM(balance)", "value"),
        equalTo(25714837.0));
  }

  @Test
  public void minTest() throws IOException {

    JSONObject result = executeQuery(String.format("SELECT MIN(age) FROM %s", TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    Assert.assertThat(getDoubleAggregationValue(result, "MIN(age)", "value"), equalTo(20.0));
  }

  @Test
  public void maxTest() throws IOException {

    JSONObject result = executeQuery(String.format("SELECT MAX(age) FROM %s", TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    Assert.assertThat(getDoubleAggregationValue(result, "MAX(age)", "value"), equalTo(40.0));
  }

  @Test
  public void avgTest() throws IOException {

    JSONObject result = executeQuery(String.format("SELECT AVG(age) FROM %s", TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    Assert.assertThat(getDoubleAggregationValue(result, "AVG(age)", "value"), equalTo(30.171));
  }

  @Test
  public void statsTest() throws IOException {

    JSONObject result =
        executeQuery(String.format("SELECT STATS(age) FROM %s", TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    Assert.assertThat(getIntAggregationValue(result, "STATS(age)", "count"), equalTo(1000));
    Assert.assertThat(getDoubleAggregationValue(result, "STATS(age)", "min"), equalTo(20.0));
    Assert.assertThat(getDoubleAggregationValue(result, "STATS(age)", "max"), equalTo(40.0));
    Assert.assertThat(getDoubleAggregationValue(result, "STATS(age)", "avg"), equalTo(30.171));
    Assert.assertThat(getDoubleAggregationValue(result, "STATS(age)", "sum"), equalTo(30171.0));
  }

  @Test
  public void extendedStatsTest() throws IOException {

    JSONObject result = executeQuery(String.format("SELECT EXTENDED_STATS(age) FROM %s",
        TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    Assert
        .assertThat(getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "min"), equalTo(20.0));
    Assert
        .assertThat(getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "max"), equalTo(40.0));
    Assert.assertThat(getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "avg"),
        equalTo(30.171));
    Assert.assertThat(getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "sum"),
        equalTo(30171.0));
    Assert.assertThat(getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "sum_of_squares"),
        equalTo(946393.0));
    Assert.assertEquals(6.008640362012022,
        getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "std_deviation"), 0.0001);
    Assert.assertEquals(36.10375899999996,
        getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "variance"), 0.0001);
  }

  @Test
  public void percentileTest() throws IOException {

    JSONObject result =
        executeQuery(String.format("SELECT PERCENTILES(age) FROM %s", TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    Assert
        .assertEquals(20.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "1.0"),
            0.001);
    Assert
        .assertEquals(21.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "5.0"),
            0.001);
    Assert
        .assertEquals(25.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "25.0"),
            0.001);
    // All percentiles are approximations calculated by t-digest, however, P50 has the widest distribution (not sure why)
    Assert
        .assertEquals(30.5, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "50.0"),
            0.6);
    Assert
        .assertEquals(35.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "75.0"),
            0.001);
    Assert
        .assertEquals(39.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "95.0"),
            0.001);
    Assert
        .assertEquals(40.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "99.0"),
            0.001);
  }

  @Test
  public void percentileTestSpecific() throws IOException {

    JSONObject result = executeQuery(String.format("SELECT PERCENTILES(age,25.0,75.0) FROM %s",
        TEST_INDEX_ACCOUNT));

    Assert.assertThat(getTotalHits(result), equalTo(1000));
    Assert.assertEquals(25.0,
        getDoubleAggregationValue(result, "PERCENTILES(age,25.0,75.0)", "values", "25.0"), 0.001);
    Assert.assertEquals(35.0,
        getDoubleAggregationValue(result, "PERCENTILES(age,25.0,75.0)", "values", "75.0"), 0.001);
  }

  @Test
  public void aliasTest() throws IOException {

    JSONObject result = executeQuery(String.format("SELECT COUNT(*) AS mycount FROM %s",
        TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    Assert.assertThat(getIntAggregationValue(result, "mycount", "value"), equalTo(1000));
  }

  @Test
  public void groupByTest() throws Exception {
    JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s GROUP BY gender",
        TEST_INDEX_ACCOUNT));
    assertResultForGroupByTest(result);
  }

  @Test
  public void groupByUsingTableAliasTest() throws Exception {
    JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s a GROUP BY a.gender",
        TEST_INDEX_ACCOUNT));
    assertResultForGroupByTest(result);
  }

  @Test
  public void groupByUsingTableNamePrefixTest() throws Exception {
    JSONObject result = executeQuery(String.format(
        "SELECT COUNT(*) FROM %s GROUP BY elasticsearch-sql_test_index_account.gender",
        TEST_INDEX_ACCOUNT
    ));
    assertResultForGroupByTest(result);
  }

  private void assertResultForGroupByTest(JSONObject result) {
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    JSONObject gender = getAggregation(result, "gender");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    final boolean isMaleFirst = gender.optQuery("/buckets/0/key").equals("m");
    final int maleBucketId = isMaleFirst ? 0 : 1;
    final int femaleBucketId = isMaleFirst ? 1 : 0;

    final String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
    final String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

    Assert.assertThat(gender.query(maleBucketPrefix + "/key"), equalTo("m"));
    Assert.assertThat(gender.query(maleBucketPrefix + "/COUNT(*)/value"), equalTo(507));
    Assert.assertThat(gender.query(femaleBucketPrefix + "/key"), equalTo("f"));
    Assert.assertThat(gender.query(femaleBucketPrefix + "/COUNT(*)/value"), equalTo(493));
  }

  @Test
  public void groupByHavingTest() throws Exception {
    JSONObject result = executeQuery(String.format(
        "SELECT gender " +
            "FROM %s " +
            "GROUP BY gender " +
            "HAVING COUNT(*) > 0", TEST_INDEX_ACCOUNT));
    assertResultForGroupByHavingTest(result);
  }

  @Test
  public void groupByHavingUsingTableAliasTest() throws Exception {
    JSONObject result = executeQuery(String.format(
        "SELECT a.gender " +
            "FROM %s a " +
            "GROUP BY a.gender " +
            "HAVING COUNT(*) > 0", TEST_INDEX_ACCOUNT));
    assertResultForGroupByHavingTest(result);
  }

  @Test
  public void groupByHavingUsingTableNamePrefixTest() throws Exception {
    JSONObject result = executeQuery(String.format(
        "SELECT elasticsearch-sql_test_index_account.gender " +
            "FROM %s " +
            "GROUP BY elasticsearch-sql_test_index_account.gender " +
            "HAVING COUNT(*) > 0", TEST_INDEX_ACCOUNT));
    assertResultForGroupByHavingTest(result);
  }

  private void assertResultForGroupByHavingTest(JSONObject result) {
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    JSONObject gender = getAggregation(result, "gender");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    final boolean isMaleFirst = gender.optQuery("/buckets/0/key").equals("m");
    final int maleBucketId = isMaleFirst ? 0 : 1;
    final int femaleBucketId = isMaleFirst ? 1 : 0;

    final String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
    final String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

    Assert.assertThat(gender.query(maleBucketPrefix + "/key"), equalTo("m"));
    Assert.assertThat(gender.query(maleBucketPrefix + "/count_0/value"), equalTo(507));
    Assert.assertThat(gender.query(femaleBucketPrefix + "/key"), equalTo("f"));
    Assert.assertThat(gender.query(femaleBucketPrefix + "/count_0/value"), equalTo(493));
  }

  @Ignore //todo VerificationException: table alias or field name missing
  @Test
  public void groupBySubqueryTest() throws Exception {

    JSONObject result = executeQuery(String.format(
        "SELECT COUNT(*) FROM %s " +
            "WHERE firstname IN (SELECT firstname FROM %s) " +
            "GROUP BY gender",
        TEST_INDEX_ACCOUNT, TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    JSONObject gender = getAggregation(result, "gender");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    final boolean isMaleFirst = gender.optQuery("/buckets/0/key").equals("m");
    final int maleBucketId = isMaleFirst ? 0 : 1;
    final int femaleBucketId = isMaleFirst ? 1 : 0;

    final String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
    final String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

    Assert.assertThat(gender.query(maleBucketPrefix + "/key"), equalTo("m"));
    Assert.assertThat(gender.query(maleBucketPrefix + "/COUNT(*)/value"), equalTo(507));
    Assert.assertThat(gender.query(femaleBucketPrefix + "/key"), equalTo("f"));
    Assert.assertThat(gender.query(femaleBucketPrefix + "/COUNT(*)/value"), equalTo(493));
  }

  @Test
  public void postFilterTest() throws Exception {

    JSONObject result = executeQuery(String.format("SELECT /*! POST_FILTER({\\\"term\\\":" +
            "{\\\"gender\\\":\\\"m\\\"}}) */ COUNT(*) FROM %s GROUP BY gender",
        TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(507));
    JSONObject gender = getAggregation(result, "gender");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    final boolean isMaleFirst = gender.optQuery("/buckets/0/key").equals("m");
    final int maleBucketId = isMaleFirst ? 0 : 1;
    final int femaleBucketId = isMaleFirst ? 1 : 0;

    final String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
    final String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

    Assert.assertThat(gender.query(maleBucketPrefix + "/key"), equalTo("m"));
    Assert.assertThat(gender.query(maleBucketPrefix + "/COUNT(*)/value"), equalTo(507));
    Assert.assertThat(gender.query(femaleBucketPrefix + "/key"), equalTo("f"));
    Assert.assertThat(gender.query(femaleBucketPrefix + "/COUNT(*)/value"), equalTo(493));
  }

  @Test
  public void multipleGroupByTest() throws Exception {

    JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s GROUP BY gender," +
            " terms('field'='age','size'=200,'alias'='age')",
        TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    JSONObject gender = getAggregation(result, "gender");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    final boolean isMaleFirst = gender.optQuery("/buckets/0/key").equals("m");
    final int maleBucketId = isMaleFirst ? 0 : 1;
    final int femaleBucketId = isMaleFirst ? 1 : 0;

    final String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
    final String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

    final JSONArray mAgeBuckets = (JSONArray) (gender.optQuery(maleBucketPrefix + "/age/buckets"));
    final JSONArray fAgeBuckets =
        (JSONArray) (gender.optQuery(femaleBucketPrefix + "/age/buckets"));

    final Set<Integer> expectedAges =
        IntStream.range(20, 41).boxed().collect(Collectors.toCollection(HashSet::new));
    Assert.assertThat(mAgeBuckets.length(), equalTo(expectedAges.size()));
    Assert.assertThat(fAgeBuckets.length(), equalTo(expectedAges.size()));

    final Set<Integer> actualAgesM = new HashSet<>(expectedAges.size());
    final Set<Integer> actualAgesF = new HashSet<>(expectedAges.size());
    mAgeBuckets.iterator()
        .forEachRemaining(json -> actualAgesM.add(((JSONObject) json).getInt("key")));
    fAgeBuckets.iterator()
        .forEachRemaining(json -> actualAgesF.add(((JSONObject) json).getInt("key")));

    Assert.assertThat(actualAgesM, equalTo(expectedAges));
    Assert.assertThat(actualAgesF, equalTo(expectedAges));
  }

  @Test
  public void multipleGroupBysWithSize() throws Exception {

    JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s GROUP BY gender," +
            " terms('alias'='ageAgg','field'='age','size'=3)",
        TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    JSONObject gender = getAggregation(result, "gender");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    final JSONArray mAgeBuckets = (JSONArray) (gender.optQuery("/buckets/0/ageAgg/buckets"));
    final JSONArray fAgeBuckets = (JSONArray) (gender.optQuery("/buckets/0/ageAgg/buckets"));

    Assert.assertThat(mAgeBuckets.length(), equalTo(3));
    Assert.assertThat(fAgeBuckets.length(), equalTo(3));
  }

  @Test
  public void termsWithSize() throws Exception {

    JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s GROUP BY terms" +
            "('alias'='ageAgg','field'='age','size'=3)",
        TEST_INDEX_ACCOUNT));
    Assert.assertThat(getTotalHits(result), equalTo(1000));
    JSONObject gender = getAggregation(result, "ageAgg");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(3));
  }

  @Test
  public void termsWithMissing() throws Exception {

    JSONObject result = executeQuery(String.format("SELECT count(*) FROM %s GROUP BY terms" +
            "('alias'='nick','field'='nickname','missing'='no_nickname')",
        TEST_INDEX_GAME_OF_THRONES));
    JSONObject nick = getAggregation(result, "nick");

    Optional<JSONObject> noNicknameBucket = Optional.empty();
    Iterator<Object> iter = nick.getJSONArray("buckets").iterator();
    while (iter.hasNext()) {
      JSONObject bucket = (JSONObject) iter.next();
      if (bucket.getString("key").equals("no_nickname")) {
        noNicknameBucket = Optional.of(bucket);
        Assert.assertThat(bucket.getInt("doc_count"), equalTo(6));
      }
    }
    Assert.assertTrue(noNicknameBucket.isPresent());
  }

  @Test
  public void termsWithOrder() throws Exception {

    final String dog1 = "snoopy";
    final String dog2 = "rex";

    JSONObject result = executeQuery(String.format("SELECT count(*) FROM %s GROUP BY terms" +
            "('field'='dog_name', 'alias'='dog_name', 'order'='desc')",
        TEST_INDEX_DOG));
    JSONObject dogName = getAggregation(result, "dog_name");

    String firstDog = (String) (dogName.optQuery("/buckets/0/key"));
    String secondDog = (String) (dogName.optQuery("/buckets/1/key"));
    Assert.assertThat(firstDog, equalTo(dog1));
    Assert.assertThat(secondDog, equalTo(dog2));

    result = executeQuery(String.format("SELECT count(*) FROM %s GROUP BY terms" +
        "('field'='dog_name', 'alias'='dog_name', 'order'='asc')", TEST_INDEX_DOG));

    dogName = getAggregation(result, "dog_name");

    firstDog = (String) (dogName.optQuery("/buckets/0/key"));
    secondDog = (String) (dogName.optQuery("/buckets/1/key"));
    Assert.assertThat(firstDog, equalTo(dog2));
    Assert.assertThat(secondDog, equalTo(dog1));
  }

  @Test
  public void orderByAscTest() {
    JSONObject response = executeJdbcRequest(String.format("SELECT COUNT(*) FROM %s " +
        "GROUP BY gender ORDER BY COUNT(*)", TEST_INDEX_ACCOUNT));

    verifySchema(response, schema("COUNT(*)", null, "integer"));
    verifyDataRows(response,
        rows(493),
        rows(507));
  }

  @Test
  public void orderByAliasAscTest() {
    JSONObject response = executeJdbcRequest(String.format("SELECT COUNT(*) as count FROM %s " +
        "GROUP BY gender ORDER BY count", TEST_INDEX_ACCOUNT));

    verifySchema(response, schema("count", "count", "integer"));
    verifyDataRowsInOrder(response,
        rows(493),
        rows(507));
  }

  @Test
  public void orderByDescTest() throws IOException {
    JSONObject response = executeJdbcRequest(String.format("SELECT COUNT(*) FROM %s " +
        "GROUP BY gender ORDER BY COUNT(*) DESC", TEST_INDEX_ACCOUNT));

    verifySchema(response, schema("COUNT(*)", null, "integer"));
    verifyDataRowsInOrder(response,
        rows(507),
        rows(493));
  }

  @Test
  public void orderByAliasDescTest() throws IOException {
    JSONObject response = executeJdbcRequest(String.format("SELECT COUNT(*) as count FROM %s " +
        "GROUP BY gender ORDER BY count DESC", TEST_INDEX_ACCOUNT));

    verifySchema(response, schema("count", "count", "integer"));
    verifyDataRowsInOrder(response,
        rows(507),
        rows(493));
  }

  @Test
  public void orderByGroupFieldWithAlias() throws IOException {
    // ORDER BY field name
    JSONObject response = executeJdbcRequest(String.format("SELECT gender as g, COUNT(*) as count "
        + "FROM %s GROUP BY gender ORDER BY gender", TEST_INDEX_ACCOUNT));

    verifySchema(response,
        schema("g", "g", "text"),
        schema("count", "count", "integer"));
    verifyDataRowsInOrder(response,
        rows("f", 493),
        rows("m", 507));

    // ORDER BY field alias
    response = executeJdbcRequest(String.format("SELECT gender as g, COUNT(*) as count "
        + "FROM %s GROUP BY gender ORDER BY g", TEST_INDEX_ACCOUNT));

    verifySchema(response,
        schema("g", "g", "text"),
        schema("count", "count", "integer"));
    verifyDataRowsInOrder(response,
        rows("f", 493),
        rows("m", 507));
  }

  @Test
  public void limitTest() throws IOException {
    JSONObject response = executeJdbcRequest(String.format("SELECT COUNT(*) FROM %s " +
        "GROUP BY age ORDER BY COUNT(*) LIMIT 5", TEST_INDEX_ACCOUNT));

    verifySchema(response, schema("COUNT(*)", null, "integer"));
    verifyDataRowsInOrder(response,
        rows(35),
        rows(39),
        rows(39),
        rows(42),
        rows(42));
  }

  @Test
  public void countGroupByRange() throws IOException {

    JSONObject result = executeQuery(String.format("SELECT COUNT(age) FROM %s" +
        " GROUP BY range(age, 20,25,30,35,40)", TEST_INDEX_ACCOUNT));
    JSONObject ageAgg = getAggregation(result, "range(age,20,25,30,35,40)");
    JSONArray buckets = ageAgg.getJSONArray("buckets");
    Assert.assertThat(buckets.length(), equalTo(4));

    final int[] expectedResults = new int[] {225, 226, 259, 245};

    for (int i = 0; i < expectedResults.length; ++i) {

      Assert.assertThat(buckets.query(String.format(Locale.ROOT, "/%d/COUNT(age)/value", i)),
          equalTo(expectedResults[i]));
    }
  }

  /**
   * <a>http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-datehistogram-aggregation.html</a>
   */
  @Test
  public void countGroupByDateTest() throws IOException {

    String result =
        explainQuery(String.format("select insert_time from %s group by date_histogram" +
                "('field'='insert_time','fixed_interval'='1h','format'='yyyy-MM','min_doc_count'=5) ",
            TEST_INDEX_ONLINE));
    Assert.assertThat(result.replaceAll("\\s+", ""),
        containsString("{\"date_histogram\":{\"field\":\"insert_time\",\"format\":\"yyyy-MM\"," +
            "\"fixed_interval\":\"1h\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false," +
            "\"min_doc_count\":5}"));
  }

  @Test
  public void countGroupByDateTestWithAlias() throws IOException {
    String result =
        explainQuery(String.format("select insert_time from %s group by date_histogram" +
                "('field'='insert_time','fixed_interval'='1h','format'='yyyy-MM','alias'='myAlias')",
            TEST_INDEX_ONLINE));
    Assert.assertThat(result.replaceAll("\\s+", ""),
        containsString("myAlias\":{\"date_histogram\":{\"field\":\"insert_time\"," +
            "\"format\":\"yyyy-MM\",\"fixed_interval\":\"1h\""));
  }

//    /**
//     * <a>http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-daterange-aggregation.html</a>
//     */
//    @Test
//    public void countDateRangeTest() throws IOException, SqlParseException, SQLFeatureNotSupportedException {
//        String result = explainQuery(String.format("select online from %s group by date_range(field='insert_time'," +
//                "'format'='yyyy-MM-dd' ,'2014-08-18','2014-08-17','now-8d','now-7d','now-6d','now')",
//                TEST_INDEX_ONLINE));
//        // TODO: fix the query or fix the code for the query to work
//    }

  @Test
  public void topHitTest() throws IOException {

    String query = String
        .format("select topHits('size'=3,age='desc') from %s group by gender", TEST_INDEX_ACCOUNT);
    JSONObject result = executeQuery(query);
    JSONObject gender = getAggregation(result, "gender");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    final boolean isMaleFirst = gender.optQuery("/buckets/0/key").equals("m");
    final int maleBucketId = isMaleFirst ? 0 : 1;
    final int femaleBucketId = isMaleFirst ? 1 : 0;

    final String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
    final String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

    Assert.assertThat(gender.query(maleBucketPrefix + "/key"), equalTo("m"));
    Assert.assertThat(gender.query(maleBucketPrefix + "/topHits(size=3,age=desc)/hits/total/value"),
        equalTo(507));
    Assert.assertThat(
        gender.query(maleBucketPrefix + "/topHits(size=3,age=desc)/hits/total/relation"),
        equalTo("eq"));
    Assert.assertThat(
        ((JSONArray) gender.query(maleBucketPrefix + "/topHits(size=3,age=desc)/hits/hits"))
            .length(),
        equalTo(3));
    Assert.assertThat(gender.query(femaleBucketPrefix + "/key"), equalTo("f"));
    Assert
        .assertThat(gender.query(femaleBucketPrefix + "/topHits(size=3,age=desc)/hits/total/value"),
            equalTo(493));
    Assert.assertThat(
        gender.query(femaleBucketPrefix + "/topHits(size=3,age=desc)/hits/total/relation"),
        equalTo("eq"));
    Assert.assertThat(
        ((JSONArray) gender.query(femaleBucketPrefix + "/topHits(size=3,age=desc)/hits/hits"))
            .length(),
        equalTo(3));
  }

  @Test
  public void topHitTest_WithInclude() throws IOException {

    String query =
        String.format("select topHits('size'=3,age='desc','include'=age) from %s group by gender",
            TEST_INDEX_ACCOUNT);
    JSONObject result = executeQuery(query);
    JSONObject gender = getAggregation(result, "gender");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    final boolean isMaleFirst = gender.optQuery("/buckets/0/key").equals("m");
    final int maleBucketId = isMaleFirst ? 0 : 1;
    final int femaleBucketId = isMaleFirst ? 1 : 0;

    final String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
    final String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

    Assert.assertThat(gender.query(maleBucketPrefix + "/key"), equalTo("m"));
    Assert.assertThat(
        gender.query(maleBucketPrefix + "/topHits(size=3,age=desc,include=age)/hits/total/value"),
        equalTo(507));
    Assert.assertThat(gender
            .query(maleBucketPrefix + "/topHits(size=3,age=desc,include=age)/hits/total/relation"),
        equalTo("eq"));
    Assert.assertThat(((JSONArray) gender.query(
        maleBucketPrefix + "/topHits(size=3,age=desc,include=age)/hits/hits")).length(),
        equalTo(3));

    Assert.assertThat(gender.query(femaleBucketPrefix + "/key"), equalTo("f"));
    Assert.assertThat(
        gender.query(femaleBucketPrefix + "/topHits(size=3,age=desc,include=age)/hits/total/value"),
        equalTo(493));
    Assert.assertThat(gender
            .query(femaleBucketPrefix + "/topHits(size=3,age=desc,include=age)/hits/total/relation"),
        equalTo("eq"));
    Assert.assertThat(((JSONArray) gender.query(
        femaleBucketPrefix + "/topHits(size=3,age=desc,include=age)/hits/hits")).length(),
        equalTo(3));

    for (int i = 0; i < 2; ++i) {
      for (int j = 0; j < 3; ++j) {
        JSONObject source = (JSONObject) gender.query(String.format(Locale.ROOT,
            "/buckets/%d/topHits(size=3,age=desc,include=age)/hits/hits/%d/_source", i, j));
        Assert.assertThat(source.length(), equalTo(1));
        Assert.assertTrue(source.has("age"));
        Assert.assertThat(source.getInt("age"), equalTo(40));
      }
    }
  }

  @Test
  public void topHitTest_WithIncludeTwoFields() throws IOException {

    String query =
        String.format("select topHits('size'=3,'include'='age,firstname',age='desc') from %s " +
            "group by gender", TEST_INDEX_ACCOUNT);
    JSONObject result = executeQuery(query);
    JSONObject gender = getAggregation(result, "gender");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    for (int i = 0; i < 2; ++i) {
      for (int j = 0; j < 3; ++j) {
        JSONObject source = (JSONObject) gender.query(String.format(Locale.ROOT,
            "/buckets/%d/topHits(size=3,include=age,firstname,age=desc)/hits/hits/%d/_source", i,
            j));
        Assert.assertThat(source.length(), equalTo(2));
        Assert.assertTrue(source.has("age"));
        Assert.assertThat(source.getInt("age"), equalTo(40));
        Assert.assertTrue(source.has("firstname"));
        final String name = source.getString("firstname");
        Assert.assertThat(name, not(isEmptyString()));
      }
    }
  }

  @Test
  public void topHitTest_WithExclude() throws IOException {

    String query = String.format("select topHits('size'=3,'exclude'='lastname',age='desc') from " +
        "%s group by gender", TEST_INDEX_ACCOUNT);
    JSONObject result = executeQuery(query);
    JSONObject gender = getAggregation(result, "gender");
    Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

    final boolean isMaleFirst = gender.optQuery("/buckets/0/key").equals("m");
    final int maleBucketId = isMaleFirst ? 0 : 1;
    final int femaleBucketId = isMaleFirst ? 1 : 0;

    final String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
    final String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

    Assert.assertThat(gender.query(maleBucketPrefix + "/key"), equalTo("m"));
    Assert.assertThat(gender
            .query(maleBucketPrefix + "/topHits(size=3,exclude=lastname,age=desc)/hits/total/value"),
        equalTo(507));
    Assert.assertThat(gender
            .query(maleBucketPrefix + "/topHits(size=3,exclude=lastname,age=desc)/hits/total/relation"),
        equalTo("eq"));
    Assert.assertThat(((JSONArray) gender.query(
        maleBucketPrefix + "/topHits(size=3,exclude=lastname,age=desc)/hits/hits")).length(),
        equalTo(3));

    Assert.assertThat(gender.query(femaleBucketPrefix + "/key"), equalTo("f"));
    Assert.assertThat(gender
            .query(femaleBucketPrefix + "/topHits(size=3,exclude=lastname,age=desc)/hits/total/value"),
        equalTo(493));
    Assert.assertThat(gender.query(
        femaleBucketPrefix + "/topHits(size=3,exclude=lastname,age=desc)/hits/total/relation"),
        equalTo("eq"));
    Assert.assertThat(((JSONArray) gender.query(
        femaleBucketPrefix + "/topHits(size=3,exclude=lastname,age=desc)/hits/hits")).length(),
        equalTo(3));

    final Set<String> expectedFields = new HashSet<>(Arrays.asList(
        "account_number",
        "firstname",
        "address",
        "balance",
        "gender",
        "city",
        "employer",
        "state",
        "age",
        "email"
    ));

    for (int i = 0; i < 2; ++i) {
      for (int j = 0; j < 3; ++j) {
        JSONObject source = (JSONObject) gender.query(String.format(Locale.ROOT,
            "/buckets/%d/topHits(size=3,exclude=lastname,age=desc)/hits/hits/%d/_source", i, j));
        Assert.assertThat(source.length(), equalTo(expectedFields.size()));
        Assert.assertFalse(source.has("lastname"));
        Assert.assertThat(source.keySet().containsAll(expectedFields), equalTo(true));
      }
    }
  }

  //region not migrated

  // script on metric aggregation tests. uncomment if your elastic has scripts enable (disabled by default)
//    @Test
//    public void sumWithScriptTest() throws IOException, SqlParseException, SQLFeatureNotSupportedException {
//        Aggregations result = query(String.format("SELECT SUM(script('','doc[\\'balance\\'].value + doc[\\'balance\\'].value')) as doubleSum FROM %s", TEST_INDEX));
//        Sum sum = result.get("doubleSum");
//        assertThat(sum.getValue(), equalTo(25714837.0*2));
//    }
//
//    @Test
//    public void sumWithImplicitScriptTest() throws IOException, SqlParseException, SQLFeatureNotSupportedException {
//        Aggregations result = query(String.format("SELECT SUM(balance + balance) as doubleSum FROM %s", TEST_INDEX));
//        Sum sum = result.get("doubleSum");
//        assertThat(sum.getValue(), equalTo(25714837.0*2));
//    }
//
//    @Test
//    public void sumWithScriptTestNoAlias() throws IOException, SqlParseException, SQLFeatureNotSupportedException {
//        Aggregations result = query(String.format("SELECT SUM(balance + balance) FROM %s", TEST_INDEX));
//        Sum sum = result.get("SUM(script=script(balance + balance,doc('balance').value + doc('balance').value))");
//        assertThat(sum.getValue(), equalTo(25714837.0*2));
//    }
//
//    @Test
//    public void scriptedMetricAggregation() throws SQLFeatureNotSupportedException, SqlParseException {
//        Aggregations result = query ("select scripted_metric('map_script'='if(doc[\\'balance\\'].value > 49670){ if(!_agg.containsKey(\\'ages\\')) { _agg.put(\\'ages\\',doc[\\'age\\'].value); } " +
//                "else { _agg.put(\\'ages\\',_agg.get(\\'ages\\')+doc[\\'age\\'].value); }}'," +
//                "'reduce_script'='sumThem = 0; for (a in _aggs) { if(a.containsKey(\\'ages\\')){ sumThem += a.get(\\'ages\\');} }; return sumThem;') as wierdSum from " + TEST_INDEX + "");
//        ScriptedMetric metric = result.get("wierdSum");
//        Assert.assertEquals(136L,metric.aggregation());
//    }
//
//    @Test
//    public void scriptedMetricConcatWithStringParamAndReduceParamAggregation() throws SQLFeatureNotSupportedException, SqlParseException {
//        String query = "select scripted_metric(\n" +
//                "  'init_script' = '_agg[\"concat\"]=[] ',\n" +
//                "  'map_script'='_agg.concat.add(doc[field].value)' ,\n" +
//                "  'combine_script'='return _agg.concat.join(delim);',\t\t\t\t\n" +
//                "  'reduce_script'='_aggs.removeAll(\"\"); return _aggs.join(delim)'," +
//                "'@field' = 'name.firstname' , '@delim'=';',@reduce_delim =';' ) as all_characters \n" +
//                "from "+TEST_INDEX+"";
//        Aggregations result = query (query);
//        ScriptedMetric metric = result.get("all_characters");
//        List<String> names = Arrays.asList(metric.aggregation().toString().split(";"));
//
//
//        Assert.assertEquals(4,names.size());
//        String[] expectedNames = new String[]{"brandon","daenerys","eddard","jaime"};
//        for(String name : expectedNames){
//            Assert.assertTrue("not contains:" + name,names.contains(name));
//        }
//    }
//
//    @Test
//    public void scriptedMetricAggregationWithNumberParams() throws SQLFeatureNotSupportedException, SqlParseException {
//        Aggregations result = query ("select scripted_metric('map_script'='if(doc[\\'balance\\'].value > 49670){ if(!_agg.containsKey(\\'ages\\')) { _agg.put(\\'ages\\',doc[\\'age\\'].value+x); } " +
//                "else { _agg.put(\\'ages\\',_agg.get(\\'ages\\')+doc[\\'age\\'].value+x); }}'," +
//                "'reduce_script'='sumThem = 0; for (a in _aggs) { if(a.containsKey(\\'ages\\')){ sumThem += a.get(\\'ages\\');} }; return sumThem;'" +
//                ",'@x'=3) as wierdSum from " + TEST_INDEX + "");
//        ScriptedMetric metric = result.get("wierdSum");
//        Assert.assertEquals(148L,metric.aggregation());
//    }
//

//    @Test
//    public void topHitTest_WithIncludeAndExclude() throws IOException, SqlParseException, SQLFeatureNotSupportedException {
//        Aggregations result = query(String.format("select topHits('size'=3,'exclude'='lastname','include'='firstname,lastname',age='desc') from %s group by gender ", TEST_INDEX_ACCOUNT));
//        List<? extends Terms.Bucket> buckets = ((Terms) (result.asList().get(0))).getBuckets();
//        for (Terms.Bucket bucket : buckets) {
//            SearchHits hits = ((InternalTopHits) bucket.getAggregations().asList().get(0)).getHits();
//            for (SearchHit hit : hits) {
//                Set<String> fields = hit.getSourceAsMap().keySet();
//                Assert.assertEquals(1, fields.size());
//                Assert.assertTrue(fields.contains("firstname"));
//            }
//        }
//    }
//
//    private Aggregations query(String query) throws SqlParseException, SQLFeatureNotSupportedException {
//        SqlElasticSearchRequestBuilder select = getSearchRequestBuilder(query);
//        return ((SearchResponse)select.get()).getAggregations();
//    }
//
//    private SqlElasticSearchRequestBuilder getSearchRequestBuilder(String query) throws SqlParseException, SQLFeatureNotSupportedException {
//        SearchDao searchDao = MainTestSuite.getSearchDao();
//        return (SqlElasticSearchRequestBuilder) searchDao.explain(query).explain();
//    }
//
//    @Test
//    public void testFromSizeWithAggregations() throws Exception {
//        final String query1 = String.format("SELECT /*! DOCS_WITH_AGGREGATION(0,1) */" +
//                " account_number FROM %s GROUP BY gender", TEST_INDEX_ACCOUNT);
//        SearchResponse response1 = (SearchResponse) getSearchRequestBuilder(query1).get();
//
//        Assert.assertEquals(1, response1.getHits().getHits().length);
//        Terms gender1 = response1.getAggregations().get("gender");
//        Assert.assertEquals(2, gender1.getBuckets().size());
//        Object account1 = response1.getHits().getHits()[0].getSourceAsMap().get("account_number");
//
//        final String query2 = String.format("SELECT /*! DOCS_WITH_AGGREGATION(1,1) */" +
//                " account_number FROM %s GROUP BY gender", TEST_INDEX_ACCOUNT);
//        SearchResponse response2 = (SearchResponse) getSearchRequestBuilder(query2).get();
//
//        Assert.assertEquals(1, response2.getHits().getHits().length);
//        Terms gender2 = response2.getAggregations().get("gender");
//        Assert.assertEquals(2, gender2.getBuckets().size());
//        Object account2 = response2.getHits().getHits()[0].getSourceAsMap().get("account_number");
//
//        Assert.assertEquals(response1.getHits().getTotalHits(), response2.getHits().getTotalHits());
//        Assert.assertNotEquals(account1, account2);
//    }
//
//    @Test
//    public void testSubAggregations() throws  Exception {
//        Set expectedAges = new HashSet<>(ContiguousSet.create(Range.closed(20, 40), DiscreteDomain.integers()));
//        final String query = String.format("SELECT /*! DOCS_WITH_AGGREGATION(10) */" +
//                " * FROM %s GROUP BY (gender, terms('field'='age','size'=200,'alias'='age')), (state) LIMIT 200,200", TEST_INDEX_ACCOUNT);
//
//        Map<String, Set<Integer>> buckets = new HashMap<>();
//
//        SqlElasticSearchRequestBuilder select = getSearchRequestBuilder(query);
//        SearchResponse response = (SearchResponse) select.get();
//        Aggregations result = response.getAggregations();
//
//        Terms gender = result.get("gender");
//        for(Terms.Bucket genderBucket : gender.getBuckets()) {
//            String genderKey = genderBucket.getKey().toString();
//            buckets.put(genderKey, new HashSet<Integer>());
//            Terms ageBuckets = (Terms) genderBucket.getAggregations().get("age");
//            for(Terms.Bucket ageBucket : ageBuckets.getBuckets()) {
//                buckets.get(genderKey).add(Integer.parseInt(ageBucket.getKey().toString()));
//            }
//        }
//
//        Assert.assertEquals(2, buckets.keySet().size());
//        Assert.assertEquals(expectedAges, buckets.get("m"));
//        Assert.assertEquals(expectedAges, buckets.get("f"));
//
//        Terms state = result.get("state.keyword");
//        for(Terms.Bucket stateBucket : state.getBuckets()) {
//            if(stateBucket.getKey().toString().equalsIgnoreCase("ak")) {
//                Assert.assertTrue("There are 22 entries for state ak", stateBucket.getDocCount() == 22);
//            }
//        }
//
//        Assert.assertEquals(response.getHits().getTotalHits(), 1000);
//        Assert.assertEquals(response.getHits().getHits().length, 10);
//    }
//
//    @Test
//    public void testSimpleSubAggregations() throws  Exception {
//        final String query = String.format("SELECT /*! DOCS_WITH_AGGREGATION(10) */ * FROM %s GROUP BY (gender), (state) ", TEST_INDEX_ACCOUNT);
//
//        SqlElasticSearchRequestBuilder select = getSearchRequestBuilder(query);
//        SearchResponse response = (SearchResponse) select.get();
//        Aggregations result = response.getAggregations();
//
//        Terms gender = result.get("gender");
//        for(Terms.Bucket genderBucket : gender.getBuckets()) {
//            String genderKey = genderBucket.getKey().toString();
//            Assert.assertTrue("Gender should be m or f", genderKey.equals("m") || genderKey.equals("f"));
//        }
//
//        Assert.assertEquals(2, gender.getBuckets().size());
//
//        Terms state = result.get("state.keyword");
//        for(Terms.Bucket stateBucket : state.getBuckets()) {
//            if(stateBucket.getKey().toString().equalsIgnoreCase("ak")) {
//                Assert.assertTrue("There are 22 entries for state ak", stateBucket.getDocCount() == 22);
//            }
//        }
//
//        Assert.assertEquals(response.getHits().getTotalHits(), 1000);
//        Assert.assertEquals(response.getHits().getHits().length, 10);
//    }
//
//    @Test
//    public void geoHashGrid() throws SQLFeatureNotSupportedException, SqlParseException {
//        Aggregations result = query(String.format("SELECT COUNT(*) FROM %s/location GROUP BY geohash_grid(field='center',precision=5) ", TEST_INDEX_LOCATION));
//        InternalGeoHashGrid grid = result.get("geohash_grid(field=center,precision=5)");
//        Collection<? extends InternalMultiBucketAggregation.InternalBucket> buckets = grid.getBuckets();
//        for (InternalMultiBucketAggregation.InternalBucket bucket : buckets) {
//            Assert.assertTrue(bucket.getKeyAsString().equals("w2fsm") || bucket.getKeyAsString().equals("w0p6y") );
//            Assert.assertEquals(1,bucket.getDocCount());
//        }
//    }
//
//    @Test
//    public void geoBounds() throws SQLFeatureNotSupportedException, SqlParseException {
//        Aggregations result = query(String.format("SELECT * FROM %s/location GROUP BY geo_bounds(field='center',alias='bounds') ", TEST_INDEX_LOCATION));
//        InternalGeoBounds bounds = result.get("bounds");
//        Assert.assertEquals(0.5,bounds.bottomRight().getLat(),0.001);
//        Assert.assertEquals(105.0,bounds.bottomRight().getLon(),0.001);
//        Assert.assertEquals(5.0,bounds.topLeft().getLat(),0.001);
//        Assert.assertEquals(100.5,bounds.topLeft().getLon(),0.001);
//    }
//
//    @Test
//    public void groupByOnNestedFieldTest() throws Exception {
//        Aggregations result = query(String.format("SELECT COUNT(*) FROM %s GROUP BY nested(message.info)", TEST_INDEX_NESTED_TYPE));
//        InternalNested nested = result.get("message.info@NESTED");
//        Terms infos = nested.getAggregations().get("message.info");
//        Assert.assertEquals(3,infos.getBuckets().size());
//        for(Terms.Bucket bucket : infos.getBuckets()) {
//            String key = bucket.getKey().toString();
//            long count = ((ValueCount) bucket.getAggregations().get("COUNT(*)")).getValue();
//            if(key.equalsIgnoreCase("a")) {
//                Assert.assertEquals(2, count);
//            }
//            else if(key.equalsIgnoreCase("c")) {
//                Assert.assertEquals(2, count);
//            }
//            else if(key.equalsIgnoreCase("b")) {
//                Assert.assertEquals(1, count);
//            }
//            else {
//                throw new Exception(String.format("Unexpected key. expected: a OR b OR c . found: %s", key));
//            }
//        }
//    }
//
//    @Test
//    public void groupByTestWithFilter() throws Exception {
//        Aggregations result = query(String.format("SELECT COUNT(*) FROM %s GROUP BY filter(gender='m'),gender", TEST_INDEX_ACCOUNT));
//        InternalFilter filter = result.get("filter(gender = 'm')@FILTER");
//        Terms gender = filter.getAggregations().get("gender");
//
//        for(Terms.Bucket bucket : gender.getBuckets()) {
//            String key = bucket.getKey().toString();
//            long count = ((ValueCount) bucket.getAggregations().get("COUNT(*)")).getValue();
//            if(key.equalsIgnoreCase("m")) {
//                Assert.assertEquals(507, count);
//            }
//            else {
//                throw new Exception(String.format("Unexpected key. expected: only m. found: %s", key));
//            }
//        }
//    }
//
//
  //endregion not migrated

  @Test
  public void groupByOnNestedFieldWithFilterTest() throws Exception {

    String query = String.format("SELECT COUNT(*) FROM %s GROUP BY  nested(message.info)," +
        "filter('myFilter',message.info = 'a')", TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);

    JSONObject aggregation = getAggregation(result, "message.info@NESTED");
    JSONArray buckets = (JSONArray) aggregation.optQuery("/myFilter@FILTER/message.info/buckets");
    Assert.assertNotNull(buckets);
    Assert.assertThat(buckets.length(), equalTo(1));

    JSONObject bucket = buckets.getJSONObject(0);
    Assert.assertThat(bucket.getString("key"), equalTo("a"));
    Assert.assertThat(bucket.query("/COUNT(*)/value"), equalTo(2));
  }

  @Test
  public void minOnNestedField() throws Exception {

    String query = String.format("SELECT min(nested(message.dayOfWeek)) as minDays FROM %s",
        TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);
    JSONObject aggregation = getAggregation(result, "message.dayOfWeek@NESTED");
    Assert.assertEquals(1.0, (double) aggregation.query("/minDays/value"), 0.0001);
  }

  @Test
  public void sumOnNestedField() throws Exception {

    String query = String.format("SELECT sum(nested(message.dayOfWeek)) as sumDays FROM %s",
        TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);
    JSONObject aggregation = getAggregation(result, "message.dayOfWeek@NESTED");
    Assert.assertEquals(19.0, (double) aggregation.query("/sumDays/value"), 0.0001);
  }

  @Test
  public void histogramOnNestedField() throws Exception {

    String query = String.format("select count(*) from %s group by histogram" +
            "('field'='message.dayOfWeek','nested'='message','interval'='2' , 'alias' = 'someAlias' )",
        TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);
    JSONObject aggregation = getAggregation(result, "message@NESTED");

    final Map<Double, Integer> expectedCountsByKey = new HashMap<>();
    expectedCountsByKey.put(0.0, 2);
    expectedCountsByKey.put(2.0, 1);
    expectedCountsByKey.put(4.0, 2);
    expectedCountsByKey.put(6.0, 1);

    JSONArray buckets = (JSONArray) aggregation.query("/someAlias/buckets");
    Assert.assertThat(buckets.length(), equalTo(4));

    buckets.forEach(obj -> {
      JSONObject bucket = (JSONObject) obj;
      final double key = bucket.getDouble("key");
      Assert.assertTrue(expectedCountsByKey.containsKey(key));
      Assert.assertThat(bucket.getJSONObject("COUNT(*)").getInt("value"),
          equalTo(expectedCountsByKey.get(key)));
    });
  }

  @Test
  public void reverseToRootGroupByOnNestedFieldWithFilterTestWithReverseNestedAndEmptyPath()
      throws Exception {

    String query = String.format("SELECT COUNT(*) FROM %s GROUP BY  nested(message.info)," +
            "filter('myFilter',message.info = 'a'),reverse_nested(someField,'')",
        TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);
    JSONObject aggregation = getAggregation(result, "message.info@NESTED");

    JSONArray msgInfoBuckets =
        (JSONArray) aggregation.optQuery("/myFilter@FILTER/message.info/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(msgInfoBuckets.length(), equalTo(1));

    JSONArray someFieldBuckets =
        (JSONArray) msgInfoBuckets.optQuery("/0/someField@NESTED/someField/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(msgInfoBuckets.length(), equalTo(1));
    Assert.assertThat(someFieldBuckets.query("/0/key"), equalTo("b"));
    Assert.assertThat(someFieldBuckets.query("/0/COUNT(*)/value"), equalTo(2));
  }

  @Test
  public void reverseToRootGroupByOnNestedFieldWithFilterTestWithReverseNestedNoPath()
      throws Exception {

    String query = String.format("SELECT COUNT(*) FROM %s GROUP BY  nested(message.info),filter" +
        "('myFilter',message.info = 'a'),reverse_nested(someField)", TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);
    JSONObject aggregation = getAggregation(result, "message.info@NESTED");

    JSONArray msgInfoBuckets =
        (JSONArray) aggregation.optQuery("/myFilter@FILTER/message.info/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(msgInfoBuckets.length(), equalTo(1));

    JSONArray someFieldBuckets =
        (JSONArray) msgInfoBuckets.optQuery("/0/someField@NESTED/someField/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(someFieldBuckets.length(), equalTo(1));
    Assert.assertThat(someFieldBuckets.query("/0/key"), equalTo("b"));
    Assert.assertThat(someFieldBuckets.query("/0/COUNT(*)/value"), equalTo(2));
  }

  @Test
  public void reverseToRootGroupByOnNestedFieldWithFilterTestWithReverseNestedOnHistogram()
      throws Exception {

    String query = String.format("SELECT COUNT(*) FROM %s GROUP BY  nested(message.info)," +
        "filter('myFilter',message.info = 'a'),histogram('field'='myNum','reverse_nested'='','interval'='2', " +
        "'alias' = 'someAlias' )", TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);
    JSONObject aggregation = getAggregation(result, "message.info@NESTED");

    JSONArray msgInfoBuckets =
        (JSONArray) aggregation.optQuery("/myFilter@FILTER/message.info/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(msgInfoBuckets.length(), equalTo(1));

    JSONArray someAliasBuckets =
        (JSONArray) msgInfoBuckets.optQuery("/0/someAlias@NESTED/someAlias/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(someAliasBuckets.length(), equalTo(3));

    final Map<Double, Integer> expectedCountsByKey = new HashMap<>();
    expectedCountsByKey.put(0.0, 1);
    expectedCountsByKey.put(2.0, 0);
    expectedCountsByKey.put(4.0, 1);

    someAliasBuckets.forEach(obj -> {
      JSONObject bucket = (JSONObject) obj;
      final double key = bucket.getDouble("key");
      Assert.assertTrue(expectedCountsByKey.containsKey(key));
      Assert.assertThat(bucket.getJSONObject("COUNT(*)").getInt("value"),
          equalTo(expectedCountsByKey.get(key)));
    });
  }

  @Test
  public void reverseToRootGroupByOnNestedFieldWithFilterAndSumOnReverseNestedField()
      throws Exception {

    String query = String.format("SELECT sum(reverse_nested(myNum)) bla FROM %s GROUP BY " +
        "nested(message.info),filter('myFilter',message.info = 'a')", TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);
    JSONObject aggregation = getAggregation(result, "message.info@NESTED");

    JSONArray msgInfoBuckets =
        (JSONArray) aggregation.optQuery("/myFilter@FILTER/message.info/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(msgInfoBuckets.length(), equalTo(1));

    Assert.assertNotNull(msgInfoBuckets.optQuery("/0/myNum@NESTED/bla/value"));
    JSONObject bla = (JSONObject) msgInfoBuckets.query("/0/myNum@NESTED/bla");
    Assert.assertEquals(5.0, bla.getDouble("value"), 0.000001);
  }

  @Test
  public void reverseAnotherNestedGroupByOnNestedFieldWithFilterTestWithReverseNestedNoPath()
      throws Exception {

    String query = String.format("SELECT COUNT(*) FROM %s GROUP BY  nested(message.info)," +
            "filter('myFilter',message.info = 'a'),reverse_nested(comment.data,'~comment')",
        TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);
    JSONObject aggregation = getAggregation(result, "message.info@NESTED");

    JSONArray msgInfoBuckets =
        (JSONArray) aggregation.optQuery("/myFilter@FILTER/message.info/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(msgInfoBuckets.length(), equalTo(1));

    JSONArray commentDataBuckets =
        (JSONArray) msgInfoBuckets.optQuery("/0/comment.data@NESTED_REVERSED" +
            "/comment.data@NESTED/comment.data/buckets");
    Assert.assertNotNull(commentDataBuckets);
    Assert.assertThat(commentDataBuckets.length(), equalTo(1));
    Assert.assertThat(commentDataBuckets.query("/0/key"), equalTo("ab"));
    Assert.assertThat(commentDataBuckets.query("/0/COUNT(*)/value"), equalTo(2));
  }

  @Test
  public void reverseAnotherNestedGroupByOnNestedFieldWithFilterTestWithReverseNestedOnHistogram()
      throws Exception {

    String query = String.format("SELECT COUNT(*) FROM %s GROUP BY  nested(message.info),filter" +
        "('myFilter',message.info = 'a'),histogram('field'='comment.likes','reverse_nested'='~comment'," +
        "'interval'='2' , 'alias' = 'someAlias' )", TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);
    JSONObject aggregation = getAggregation(result, "message.info@NESTED");

    JSONArray msgInfoBuckets =
        (JSONArray) aggregation.optQuery("/myFilter@FILTER/message.info/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(msgInfoBuckets.length(), equalTo(1));

    JSONArray someAliasBuckets = (JSONArray) msgInfoBuckets.optQuery(
        "/0/~comment@NESTED_REVERSED/~comment@NESTED/someAlias/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(someAliasBuckets.length(), equalTo(2));

    final Map<Double, Integer> expectedCountsByKey = new HashMap<>();
    expectedCountsByKey.put(0.0, 1);
    expectedCountsByKey.put(2.0, 1);

    someAliasBuckets.forEach(obj -> {
      JSONObject bucket = (JSONObject) obj;
      final double key = bucket.getDouble("key");
      Assert.assertTrue(expectedCountsByKey.containsKey(key));
      Assert.assertThat(bucket.getJSONObject("COUNT(*)").getInt("value"),
          equalTo(expectedCountsByKey.get(key)));
    });
  }

  @Test
  public void reverseAnotherNestedGroupByOnNestedFieldWithFilterAndSumOnReverseNestedField()
      throws Exception {

    String query =
        String.format("SELECT sum(reverse_nested(comment.likes,'~comment')) bla FROM %s " +
                "GROUP BY  nested(message.info),filter('myFilter',message.info = 'a')",
            TEST_INDEX_NESTED_TYPE);
    JSONObject result = executeQuery(query);
    JSONObject aggregation = getAggregation(result, "message.info@NESTED");

    JSONArray msgInfoBuckets =
        (JSONArray) aggregation.optQuery("/myFilter@FILTER/message.info/buckets");
    Assert.assertNotNull(msgInfoBuckets);
    Assert.assertThat(msgInfoBuckets.length(), equalTo(1));

    Assert.assertNotNull(msgInfoBuckets.optQuery(
        "/0/comment.likes@NESTED_REVERSED/comment.likes@NESTED/bla/value"));
    JSONObject bla = (JSONObject) msgInfoBuckets
        .query("/0/comment.likes@NESTED_REVERSED/comment.likes@NESTED/bla");
    Assert.assertEquals(4.0, bla.getDouble("value"), 0.000001);
  }

  @Test
  public void docsReturnedTestWithoutDocsHint() throws Exception {
    String query = String.format("SELECT count(*) from %s", TEST_INDEX_ACCOUNT);
    JSONObject result = executeQuery(query);
    Assert.assertThat(getHits(result).length(), equalTo(0));
  }

  @Test
  public void docsReturnedTestWithDocsHint() throws Exception {
    String query = String.format("SELECT /*! DOCS_WITH_AGGREGATION(10) */ count(*) from %s",
        TEST_INDEX_ACCOUNT);
    JSONObject result = executeQuery(query);
    Assert.assertThat(getHits(result).length(), equalTo(10));
  }

  @Ignore("There is not any text field in the index. Need fix later")
  @Test
  public void termsWithScript() throws Exception {
    String query =
        String.format("select count(*), avg(all_client) from %s group by terms('alias'='asdf'," +
            " substring(field, 0, 1)), date_histogram('alias'='time', 'field'='timestamp', " +
            "'interval'='20d ', 'format'='yyyy-MM-dd') limit 1000", TEST_INDEX_ONLINE);
    String result = explainQuery(query);

    Assert.assertThat(result, containsString("\"script\":{\"source\""));
    Assert.assertThat(result, containsString("substring(0, 1)"));
  }

  @Test
  public void groupByScriptedDateHistogram() throws Exception {
    String query = String
        .format("select count(*), avg(all_client) from %s group by date_histogram('alias'='time'," +
                " ceil(all_client), 'fixed_interval'='20d ', 'format'='yyyy-MM-dd') limit 1000",
            TEST_INDEX_ONLINE);
    String result = explainQuery(query);

    Assert.assertThat(result, containsString("Math.ceil(doc['all_client'].value);"));
    Assert.assertThat(result, containsString("\"script\":{\"source\""));
  }

  @Test
  public void groupByScriptedHistogram() throws Exception {
    String query = String.format(
        "select count(*) from %s group by histogram('alias'='all_field', pow(all_client,1))",
        TEST_INDEX_ONLINE);
    String result = explainQuery(query);

    Assert.assertThat(result, containsString("Math.pow(doc['all_client'].value, 1)"));
    Assert.assertThat(result, containsString("\"script\":{\"source\""));
  }

  @Test
  public void distinctWithOneField() {
    Assert.assertEquals(
        executeQuery("SELECT DISTINCT name.lastname FROM " + TEST_INDEX_GAME_OF_THRONES, "jdbc"),
        executeQuery("SELECT name.lastname FROM " + TEST_INDEX_GAME_OF_THRONES
            + " GROUP BY name.lastname", "jdbc")
    );
  }

  @Ignore("Skip this because it compares result of GROUP BY and DISTINCT and find difference in\n"
      + "schema type (string and text). Remove this when DISTINCT supported in new engine.")
  @Test
  public void distinctWithMultipleFields() {
    Assert.assertEquals(
        executeQuery("SELECT DISTINCT age, gender FROM " + TEST_INDEX_ACCOUNT, "jdbc"),
        executeQuery("SELECT age, gender FROM " + TEST_INDEX_ACCOUNT
            + " GROUP BY age, gender", "jdbc")
    );
  }

  private JSONObject getAggregation(final JSONObject queryResult, final String aggregationName) {
    final String aggregationsObjName = "aggregations";
    Assert.assertTrue(queryResult.has(aggregationsObjName));

    final JSONObject aggregations = queryResult.getJSONObject(aggregationsObjName);
    Assert.assertTrue(aggregations.has(aggregationName));
    return aggregations.getJSONObject(aggregationName);
  }

  private int getIntAggregationValue(final JSONObject queryResult, final String aggregationName,
                                     final String fieldName) {

    final JSONObject targetAggregation = getAggregation(queryResult, aggregationName);
    Assert.assertTrue(targetAggregation.has(fieldName));
    return targetAggregation.getInt(fieldName);
  }

  private double getDoubleAggregationValue(final JSONObject queryResult,
                                           final String aggregationName,
                                           final String fieldName) {

    final JSONObject targetAggregation = getAggregation(queryResult, aggregationName);
    Assert.assertTrue(targetAggregation.has(fieldName));
    return targetAggregation.getDouble(fieldName);
  }

  private double getDoubleAggregationValue(final JSONObject queryResult,
                                           final String aggregationName,
                                           final String fieldName, final String subFieldName) {

    final JSONObject targetAggregation = getAggregation(queryResult, aggregationName);
    Assert.assertTrue(targetAggregation.has(fieldName));
    final JSONObject targetField = targetAggregation.getJSONObject(fieldName);
    Assert.assertTrue(targetField.has(subFieldName));

    return targetField.getDouble(subFieldName);
  }

  private JSONObject executeJdbcRequest(String query) {
    return new JSONObject(executeQuery(query, "jdbc"));
  }
}
