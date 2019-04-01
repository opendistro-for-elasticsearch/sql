/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.test.ESIntegTestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ONLINE;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_GAME_OF_THRONES;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public class AggregationIT extends SQLIntegTestCase {

    @Override
    protected void setupSuiteScopeCluster() throws Exception {
        AdminClient adminClient = this.admin();
        Client esClient = ESIntegTestCase.client();

        loadAccountIndex(adminClient, esClient);
        loadGameOfThronesIndex(adminClient, esClient);
        loadDogIndex(adminClient, esClient);
        loadOnlineIndex(adminClient, esClient);
    }

    @Test
    public void countTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s/account", TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        Assert.assertThat(getIntAggregationValue(result, "COUNT(*)", "value"), equalTo(1000));
    }

    @Test
    public void countWithDocsHintTest() throws Exception {

        JSONObject result = executeQuery(String.format("SELECT /*! DOCS_WITH_AGGREGATION(10) */ count(*) from %s/account",
                TEST_INDEX_ACCOUNT));
        JSONArray hits = (JSONArray)result.query("/hits/hits");
        Assert.assertThat(hits.length(), equalTo(10));
    }

    @Test
    public void sumTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT SUM(balance) FROM %s/account", TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        Assert.assertThat(getDoubleAggregationValue(result, "SUM(balance)", "value"), equalTo(25714837.0));
    }

    @Test
    public void minTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT MIN(age) FROM %s/account", TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        Assert.assertThat(getDoubleAggregationValue(result, "MIN(age)", "value"), equalTo(20.0));
    }

    @Test
    public void maxTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT MAX(age) FROM %s/account", TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        Assert.assertThat(getDoubleAggregationValue(result, "MAX(age)", "value"), equalTo(40.0));
    }

    @Test
    public void avgTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT AVG(age) FROM %s/account", TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        Assert.assertThat(getDoubleAggregationValue(result, "AVG(age)", "value"), equalTo(30.171));
    }

    @Test
    public void statsTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT STATS(age) FROM %s/account", TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        Assert.assertThat(getIntAggregationValue(result, "STATS(age)", "count"), equalTo(1000));
        Assert.assertThat(getDoubleAggregationValue(result, "STATS(age)", "min"), equalTo(20.0));
        Assert.assertThat(getDoubleAggregationValue(result, "STATS(age)", "max"), equalTo(40.0));
        Assert.assertThat(getDoubleAggregationValue(result, "STATS(age)", "avg"), equalTo(30.171));
        Assert.assertThat(getDoubleAggregationValue(result, "STATS(age)", "sum"), equalTo(30171.0));
    }

    @Test
    public void extendedStatsTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT EXTENDED_STATS(age) FROM %s/account",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        Assert.assertThat(getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "min"), equalTo(20.0));
        Assert.assertThat(getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "max"), equalTo(40.0));
        Assert.assertThat(getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "avg"), equalTo(30.171));
        Assert.assertThat(getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "sum"), equalTo(30171.0));
        Assert.assertThat(getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "sum_of_squares"), equalTo(946393.0));
        Assert.assertEquals(6.008640362012022, getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "std_deviation"), 0.0001);
        Assert.assertEquals(36.10375899999996, getDoubleAggregationValue(result, "EXTENDED_STATS(age)", "variance"), 0.0001);
    }

    @Test
    public void percentileTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT PERCENTILES(age) FROM %s/account", TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        Assert.assertEquals(20.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "1.0"), 0.001);
        Assert.assertEquals(21.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "5.0"), 0.001);
        Assert.assertEquals(25.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "25.0"), 0.001);
        // All percentiles are approximations calculated by t-digest, however, P50 has the widest distribution (not sure why)
        Assert.assertEquals(30.5, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "50.0"), 0.6);
        Assert.assertEquals(35.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "75.0"), 0.001);
        Assert.assertEquals(39.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "95.0"), 0.001);
        Assert.assertEquals(40.0, getDoubleAggregationValue(result, "PERCENTILES(age)", "values", "99.0"), 0.001);
    }

    @Test
    public void percentileTestSpecific() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT PERCENTILES(age,25.0,75.0) FROM %s/account",
                TEST_INDEX_ACCOUNT));

        Assert.assertThat(getTotalHits(result), equalTo(1000));
        Assert.assertEquals(25.0, getDoubleAggregationValue(result, "PERCENTILES(age,25.0,75.0)", "values", "25.0"), 0.001);
        Assert.assertEquals(35.0, getDoubleAggregationValue(result, "PERCENTILES(age,25.0,75.0)", "values", "75.0"), 0.001);
    }

    @Test
    public void aliasTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT COUNT(*) AS mycount FROM %s/account",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        Assert.assertThat(getIntAggregationValue(result, "mycount", "value"), equalTo(1000));
    }

    @Test
    public void groupByTest() throws Exception {

        JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s/account GROUP BY gender",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        JSONObject gender = getAggregation(result, "gender");
        Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

        final int maleBucketId;
        final int femaleBucketId;

        if ((gender.optQuery("/buckets/0/key")).equals("m")) {
            maleBucketId = 0;
            femaleBucketId = 1;
        } else {
            maleBucketId = 1;
            femaleBucketId = 0;
        }

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
                        "{\\\"gender\\\":\\\"m\\\"}}) */ COUNT(*) FROM %s/account GROUP BY gender",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(507));
        JSONObject gender = getAggregation(result, "gender");
        Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

        final int maleBucketId;
        final int femaleBucketId;

        if ((gender.optQuery("/buckets/0/key")).equals("m")) {
            maleBucketId = 0;
            femaleBucketId = 1;
        } else {
            maleBucketId = 1;
            femaleBucketId = 0;
        }

        final String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
        final String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

        Assert.assertThat(gender.query(maleBucketPrefix + "/key"), equalTo("m"));
        Assert.assertThat(gender.query(maleBucketPrefix + "/COUNT(*)/value"), equalTo(507));
        Assert.assertThat(gender.query(femaleBucketPrefix + "/key"), equalTo("f"));
        Assert.assertThat(gender.query(femaleBucketPrefix + "/COUNT(*)/value"), equalTo(493));
    }

    @Test
    public void multipleGroupByTest() throws Exception {

        JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s/account GROUP BY gender," +
                        " terms('field'='age','size'=200,'alias'='age')",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        JSONObject gender = getAggregation(result, "gender");
        Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

        final int maleBucketId;
        final int femaleBucketId;

        if ((gender.optQuery("/buckets/0/key")).equals("m")) {
            maleBucketId = 0;
            femaleBucketId = 1;
        } else {
            maleBucketId = 1;
            femaleBucketId = 0;
        }

        final String maleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", maleBucketId);
        final String femaleBucketPrefix = String.format(Locale.ROOT, "/buckets/%d", femaleBucketId);

        final JSONArray mAgeBuckets = (JSONArray)(gender.optQuery(maleBucketPrefix + "/age/buckets"));
        final JSONArray fAgeBuckets = (JSONArray)(gender.optQuery(femaleBucketPrefix + "/age/buckets"));

        final Set<Integer> expectedAges = IntStream.range(20, 41).boxed().collect(Collectors.toCollection(HashSet::new));
        Assert.assertThat(mAgeBuckets.length(), equalTo(expectedAges.size()));
        Assert.assertThat(fAgeBuckets.length(), equalTo(expectedAges.size()));

        final Set<Integer> actualAgesM = new HashSet<>(expectedAges.size());
        final Set<Integer> actualAgesF = new HashSet<>(expectedAges.size());
        mAgeBuckets.iterator().forEachRemaining(json -> actualAgesM.add(((JSONObject)json).getInt("key")));
        fAgeBuckets.iterator().forEachRemaining(json -> actualAgesF.add(((JSONObject)json).getInt("key")));

        Assert.assertThat(actualAgesM, equalTo(expectedAges));
        Assert.assertThat(actualAgesF, equalTo(expectedAges));
    }

    @Test
    public void multipleGroupBysWithSize() throws Exception {

        JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s/account GROUP BY gender," +
                        " terms('alias'='ageAgg','field'='age','size'=3)",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        JSONObject gender = getAggregation(result, "gender");
        Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(2));

        final JSONArray mAgeBuckets = (JSONArray)(gender.optQuery("/buckets/0/ageAgg/buckets"));
        final JSONArray fAgeBuckets = (JSONArray)(gender.optQuery("/buckets/0/ageAgg/buckets"));

        Assert.assertThat(mAgeBuckets.length(), equalTo(3));
        Assert.assertThat(fAgeBuckets.length(), equalTo(3));
    }

    @Test
    public void termsWithSize() throws Exception {

        JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s/account GROUP BY terms" +
                        "('alias'='ageAgg','field'='age','size'=3)",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(getTotalHits(result), equalTo(1000));
        JSONObject gender = getAggregation(result, "ageAgg");
        Assert.assertThat(gender.getJSONArray("buckets").length(), equalTo(3));
    }

    @Test
    public void termsWithMissing() throws Exception {

        JSONObject result = executeQuery(String.format("SELECT count(*) FROM %s/gotCharacters GROUP BY terms" +
                        "('alias'='nick','field'='nickname','missing'='no_nickname')",
                TEST_INDEX_GAME_OF_THRONES));
        JSONObject nick = getAggregation(result, "nick");

        Optional<JSONObject> noNicknameBucket = Optional.empty();

        Iterator<Object> iter = nick.getJSONArray("buckets").iterator();
        while (iter.hasNext()) {
            JSONObject bucket = (JSONObject)iter.next();
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

        JSONObject result = executeQuery(String.format("SELECT count(*) FROM %s/dog GROUP BY terms" +
                        "('field'='dog_name', 'alias'='dog_name', order='desc')",
                TEST_INDEX_DOG));
        JSONObject dogName = getAggregation(result, "dog_name");

        String firstDog = (String)(dogName.optQuery("/buckets/0/key"));
        String secondDog = (String)(dogName.optQuery("/buckets/1/key"));
        Assert.assertThat(firstDog, equalTo(dog1));
        Assert.assertThat(secondDog, equalTo(dog2));

        result = executeQuery(String.format("SELECT count(*) FROM %s/dog GROUP BY terms" +
                "('field'='dog_name', 'alias'='dog_name', order='asc')", TEST_INDEX_DOG));

        dogName = getAggregation(result, "dog_name");

        firstDog = (String)(dogName.optQuery("/buckets/0/key"));
        secondDog = (String)(dogName.optQuery("/buckets/1/key"));
        Assert.assertThat(firstDog, equalTo(dog2));
        Assert.assertThat(secondDog, equalTo(dog1));
    }

    @Test
    public void orderByAscTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s/account GROUP BY age ORDER BY COUNT(*)",
                TEST_INDEX_ACCOUNT));
        JSONObject ageAgg = getAggregation(result, "age");
        JSONArray buckets = ageAgg.getJSONArray("buckets");

        int previousBucketCount = 0;
        int currentBucketCount;
        for (int i = 0; i < buckets.length(); ++i) {
            currentBucketCount = (int) buckets.query(String.format(Locale.ROOT, "/%d/COUNT(*)/value", i));

            if (0 == i) {
                previousBucketCount = currentBucketCount;
                continue;
            }

            Assert.assertThat(currentBucketCount, greaterThanOrEqualTo(previousBucketCount));
            previousBucketCount = currentBucketCount;
        }
    }

    @Test
    public void orderByDescTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s/account GROUP BY age" +
                " ORDER BY COUNT(*) DESC", TEST_INDEX_ACCOUNT));
        JSONObject ageAgg = getAggregation(result, "age");
        JSONArray buckets = ageAgg.getJSONArray("buckets");

        int previousBucketCount = 0;
        int currentBucketCount;
        for (int i = 0; i < buckets.length(); ++i) {
            currentBucketCount = (int) buckets.query(String.format(Locale.ROOT, "/%d/COUNT(*)/value", i));

            if (0 == i) {
                previousBucketCount = currentBucketCount;
                continue;
            }

            Assert.assertThat(currentBucketCount, lessThanOrEqualTo(previousBucketCount));
            previousBucketCount = currentBucketCount;
        }
    }

    @Test
    public void limitTest() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT COUNT(*) FROM %s/account GROUP BY age" +
                " ORDER BY COUNT(*) LIMIT 5", TEST_INDEX_ACCOUNT));
        JSONObject ageAgg = getAggregation(result, "age");
        JSONArray buckets = ageAgg.getJSONArray("buckets");
        Assert.assertThat(buckets.length(), equalTo(5));
    }

    @Test
    public void countGroupByRange() throws IOException {

        JSONObject result = executeQuery(String.format("SELECT COUNT(age) FROM %s/account" +
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

        String result = explainQuery(String.format("select insert_time from %s group by date_histogram" +
                "(field='insert_time','interval'='1.5h','format'='yyyy-MM','min_doc_count'=5) ", TEST_INDEX_ONLINE));
        Assert.assertThat(result.replaceAll("\\s+", ""),
                containsString("{\"date_histogram\":{\"field\":\"insert_time\",\"format\":\"yyyy-MM\"," +
                        "\"interval\":\"1.5h\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false," +
                        "\"min_doc_count\":5}"));
    }

    @Test
    public void countGroupByDateTestWithAlias() throws IOException {
        String result = explainQuery(String.format("select insert_time from %s group by date_histogram" +
                "(field='insert_time','interval'='1.5h','format'='yyyy-MM','alias'='myAlias')", TEST_INDEX_ONLINE));
        Assert.assertThat(result.replaceAll("\\s+",""),
                containsString("myAlias\":{\"date_histogram\":{\"field\":\"insert_time\"," +
                        "\"format\":\"yyyy-MM\",\"interval\":\"1.5h\""));
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
    public void termsWithScript() throws Exception {
        String query = String.format("select count(*), avg(number) from %s group by terms('alias'='asdf'," +
                " substring(field, 0, 1)), date_histogram('alias'='time', 'field'='timestamp', " +
                "'interval'='20d ', 'format'='yyyy-MM-dd') limit 1000", TEST_INDEX_ONLINE);
        String result = explainQuery(query);

        Assert.assertThat(result, containsString("\"script\":{\"source\""));
        Assert.assertThat(result, containsString("substring(0, 1)"));
    }

    @Test
    public void groupByScriptedDateHistogram() throws Exception {
        String query = String.format("select count(*), avg(number) from %s group by date_histogram('alias'='time'," +
                " ceil(timestamp), 'interval'='20d ', 'format'='yyyy-MM-dd') limit 1000" , TEST_INDEX_ONLINE);
        String result = explainQuery(query);

        Assert.assertThat(result, containsString("Math.ceil(doc['timestamp'].value);"));
        Assert.assertThat(result, containsString("\"script\":{\"source\""));
    }

    @Test
    public void groupByScriptedHistogram() throws Exception {
        String query = String.format("select count(*) from %s group by histogram('alias'='field', pow(field,1))",
                TEST_INDEX_ONLINE);
        String result = explainQuery(query);

        Assert.assertThat(result, containsString("Math.pow(doc['field'].value, 1)"));
        Assert.assertThat(result, containsString("\"script\":{\"source\""));
    }


    private JSONObject getAggregation(final JSONObject queryResult, final String aggregationName)
    {
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

    private double getDoubleAggregationValue(final JSONObject queryResult, final String aggregationName,
                                             final String fieldName) {

        final JSONObject targetAggregation = getAggregation(queryResult, aggregationName);
        Assert.assertTrue(targetAggregation.has(fieldName));
        return targetAggregation.getDouble(fieldName);
    }

    private double getDoubleAggregationValue(final JSONObject queryResult, final String aggregationName,
                                             final String fieldName, final String subFieldName) {

        final JSONObject targetAggregation = getAggregation(queryResult, aggregationName);
        Assert.assertTrue(targetAggregation.has(fieldName));
        final JSONObject targetField = targetAggregation.getJSONObject(fieldName);
        Assert.assertTrue(targetField.has(subFieldName));

        return targetField.getDouble(subFieldName);
    }
}
