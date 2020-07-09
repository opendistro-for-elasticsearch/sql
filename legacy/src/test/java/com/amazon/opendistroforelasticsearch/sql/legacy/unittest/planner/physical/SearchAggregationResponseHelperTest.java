/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.scroll.BindingTupleRow;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.scroll.SearchAggregationResponseHelper;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.AggregationUtils;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils.featureValueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SearchAggregationResponseHelperTest {
    /**
     * SELECT MAX(age) as max
     * FROM accounts
     */
    @Test
    public void noBucketOneMetricShouldPass() {
        String json = "{\n"
                      + "  \"max#max\": {\n"
                      + "    \"value\": 40\n"
                      + "  }\n"
                      + "}";
        List<Map<String, Object>> result = SearchAggregationResponseHelper.flatten(AggregationUtils.fromJson(json));
        assertThat(result, contains(allOf(hasEntry("max", 40d))));
    }

    /**
     * SELECT MAX(age) as max, MIN(age) as min
     * FROM accounts
     */
    @Test
    public void noBucketMultipleMetricShouldPass() {
        String json = "{\n"
                      + "  \"max#max\": {\n"
                      + "    \"value\": 40\n"
                      + "  },\n"
                      + "  \"min#min\": {\n"
                      + "    \"value\": 20\n"
                      + "  }\n"
                      + "}";
        List<Map<String, Object>> result = SearchAggregationResponseHelper.flatten(AggregationUtils.fromJson(json));
        assertThat(result, contains(allOf(hasEntry("max", 40d), hasEntry("min", 20d))));
    }

    /**
     * SELECT gender, MAX(age) as max, MIN(age) as min
     * FROM accounts
     * GROUP BY gender
     */
    @Test
    public void oneBucketMultipleMetricShouldPass() {
        String json = "{\n"
                      + "  \"sterms#gender\": {\n"
                      + "    \"buckets\": [\n"
                      + "      {\n"
                      + "        \"key\": \"m\",\n"
                      + "        \"doc_count\": 507,\n"
                      + "        \"min#min\": {\n"
                      + "          \"value\": 10\n"
                      + "        },\n"
                      + "        \"max#max\": {\n"
                      + "          \"value\": 20\n"
                      + "        }\n"
                      + "      },\n"
                      + "      {\n"
                      + "        \"key\": \"f\",\n"
                      + "        \"doc_count\": 493,\n"
                      + "        \"min#min\": {\n"
                      + "          \"value\": 20\n"
                      + "        },\n"
                      + "        \"max#max\": {\n"
                      + "          \"value\": 40\n"
                      + "        }\n"
                      + "      }\n"
                      + "    ]\n"
                      + "  }\n"
                      + "}";
        List<Map<String, Object>> result = SearchAggregationResponseHelper.flatten(AggregationUtils.fromJson(json));
        assertThat(result, contains(allOf(hasEntry("gender", (Object) "m"), hasEntry("min", 10d), hasEntry("max", 20d)),
                                    allOf(hasEntry("gender", (Object) "f"), hasEntry("min", 20d),
                                          hasEntry("max", 40d))));
    }

    /**
     * SELECT gender, state, MAX(age) as max, MIN(age) as min
     * FROM accounts
     * GROUP BY gender, state
     */
    @Test
    public void multipleBucketMultipleMetricShouldPass() {
        String json = "{\n"
                      + "  \"sterms#gender\": {\n"
                      + "    \"buckets\": [\n"
                      + "      {\n"
                      + "        \"key\": \"m\",\n"
                      + "        \"sterms#state\": {\n"
                      + "          \"buckets\": [\n"
                      + "            {\n"
                      + "              \"key\": \"MD\",\n"
                      + "              \"min#min\": {\n"
                      + "                \"value\": 22\n"
                      + "              },\n"
                      + "              \"max#max\": {\n"
                      + "                \"value\": 39\n"
                      + "              }\n"
                      + "            },\n"
                      + "            {\n"
                      + "              \"key\": \"ID\",\n"
                      + "              \"min#min\": {\n"
                      + "                \"value\": 23\n"
                      + "              },\n"
                      + "              \"max#max\": {\n"
                      + "                \"value\": 40\n"
                      + "              }\n"
                      + "            }\n"
                      + "          ]\n"
                      + "        }\n"
                      + "      },\n"
                      + "      {\n"
                      + "        \"key\": \"f\",\n"
                      + "        \"sterms#state\": {\n"
                      + "          \"buckets\": [\n"
                      + "            {\n"
                      + "              \"key\": \"TX\",\n"
                      + "              \"min#min\": {\n"
                      + "                \"value\": 20\n"
                      + "              },\n"
                      + "              \"max#max\": {\n"
                      + "                \"value\": 38\n"
                      + "              }\n"
                      + "            },\n"
                      + "            {\n"
                      + "              \"key\": \"MI\",\n"
                      + "              \"min#min\": {\n"
                      + "                \"value\": 22\n"
                      + "              },\n"
                      + "              \"max#max\": {\n"
                      + "                \"value\": 40\n"
                      + "              }\n"
                      + "            }\n"
                      + "          ]\n"
                      + "        }\n"
                      + "      }\n"
                      + "    ]\n"
                      + "  }\n"
                      + "}";
        List<Map<String, Object>> result = SearchAggregationResponseHelper.flatten(AggregationUtils.fromJson(json));
        assertThat(result, contains(
                allOf(hasEntry("gender", (Object) "m"), hasEntry("state", (Object) "MD"), hasEntry("min", 22d),
                      hasEntry("max", 39d)),
                allOf(hasEntry("gender", (Object) "m"), hasEntry("state", (Object) "ID"), hasEntry("min", 23d),
                      hasEntry("max", 40d)),
                allOf(hasEntry("gender", (Object) "f"), hasEntry("state", (Object) "TX"), hasEntry("min", 20d),
                      hasEntry("max", 38d)),
                allOf(hasEntry("gender", (Object) "f"), hasEntry("state", (Object) "MI"), hasEntry("min", 22d),
                      hasEntry("max", 40d))));
    }

    /**
     * SELECT age, gender FROM accounts GROUP BY age, gender
     */
    @Test
    public void multipleBucketWithoutMetricShouldPass() {
        String json = "{\n"
                      + "  \"lterms#age\": {\n"
                      + "    \"buckets\": [\n"
                      + "      {\n"
                      + "        \"key\": 31,\n"
                      + "        \"doc_count\": 61,\n"
                      + "        \"sterms#gender\": {\n"
                      + "          \"buckets\": [\n"
                      + "            {\n"
                      + "              \"key\": \"m\",\n"
                      + "              \"doc_count\": 35\n"
                      + "            },\n"
                      + "            {\n"
                      + "              \"key\": \"f\",\n"
                      + "              \"doc_count\": 26\n"
                      + "            }\n"
                      + "          ]\n"
                      + "        }\n"
                      + "      },\n"
                      + "      {\n"
                      + "        \"key\": 39,\n"
                      + "        \"doc_count\": 60,\n"
                      + "        \"sterms#gender\": {\n"
                      + "          \"buckets\": [\n"
                      + "            {\n"
                      + "              \"key\": \"f\",\n"
                      + "              \"doc_count\": 38\n"
                      + "            },\n"
                      + "            {\n"
                      + "              \"key\": \"m\",\n"
                      + "              \"doc_count\": 22\n"
                      + "            }\n"
                      + "          ]\n"
                      + "        }\n"
                      + "      }\n"
                      + "    ]\n"
                      + "  }\n"
                      + "}";
        List<Map<String, Object>> result = SearchAggregationResponseHelper.flatten(AggregationUtils.fromJson(json));
        assertThat(result, containsInAnyOrder(
                allOf(hasEntry("age", (Object) 31L), hasEntry("gender","m")),
                allOf(hasEntry("age", (Object) 31L), hasEntry("gender","f")),
                allOf(hasEntry("age", (Object) 39L), hasEntry("gender","m")),
                allOf(hasEntry("age", (Object) 39L), hasEntry("gender","f"))));
    }

    /**
     * SELECT PERCENTILES(age) FROM accounts
     */
    @Test
    public void noBucketPercentilesShouldPass() {
        String json = "{\n"
                      + "  \"percentiles_bucket#age\": {\n"
                      + "    \"values\": {\n"
                      + "      \"1.0\": 20,\n"
                      + "      \"5.0\": 21,\n"
                      + "      \"25.0\": 25,\n"
                      + "      \"50.0\": 30.90909090909091,\n"
                      + "      \"75.0\": 35,\n"
                      + "      \"95.0\": 39,\n"
                      + "      \"99.0\": 40\n"
                      + "    }\n"
                      + "  }\n"
                      + "}";
        List<Map<String, Object>> result = SearchAggregationResponseHelper.flatten(AggregationUtils.fromJson(json));
        assertThat(result, contains(allOf(hasEntry("age_1.0", 20d))));
    }

    /**
     * SELECT count(*) from online
     * GROUP BY date_histogram('field'='insert_time','interval'='4d','alias'='days')
     */
    @Test
    public void populateShouldPass() {
        String json = "{\n"
                      + "  \"date_histogram#days\": {\n"
                      + "    \"buckets\": [\n"
                      + "      {\n"
                      + "        \"key_as_string\": \"2014-08-14 00:00:00\",\n"
                      + "        \"key\": 1407974400000,\n"
                      + "        \"doc_count\": 477,\n"
                      + "        \"value_count#COUNT_0\": {\n"
                      + "          \"value\": 477\n"
                      + "        }\n"
                      + "      }\n"
                      + "    ]\n"
                      + "  }\n"
                      + "}";
        List<Map<String, Object>> result = SearchAggregationResponseHelper.flatten(AggregationUtils.fromJson(json));
        assertThat(result, containsInAnyOrder(
                allOf(hasEntry("days", (Object) "2014-08-14 00:00:00"), hasEntry("COUNT_0",477d))));
    }

    /**
     * SELECT s
     */
    @Test
    public void populateSearchAggregationResponeShouldPass() {
        String json = "{\n"
                      + "  \"lterms#age\": {\n"
                      + "    \"buckets\": [\n"
                      + "      {\n"
                      + "        \"key\": 31,\n"
                      + "        \"doc_count\": 61,\n"
                      + "        \"sterms#gender\": {\n"
                      + "          \"buckets\": [\n"
                      + "            {\n"
                      + "              \"key\": \"m\",\n"
                      + "              \"doc_count\": 35\n"
                      + "            },\n"
                      + "            {\n"
                      + "              \"key\": \"f\",\n"
                      + "              \"doc_count\": 26\n"
                      + "            }\n"
                      + "          ]\n"
                      + "        }\n"
                      + "      },\n"
                      + "      {\n"
                      + "        \"key\": 39,\n"
                      + "        \"doc_count\": 60,\n"
                      + "        \"sterms#gender\": {\n"
                      + "          \"buckets\": [\n"
                      + "            {\n"
                      + "              \"key\": \"f\",\n"
                      + "              \"doc_count\": 38\n"
                      + "            },\n"
                      + "            {\n"
                      + "              \"key\": \"m\",\n"
                      + "              \"doc_count\": 22\n"
                      + "            }\n"
                      + "          ]\n"
                      + "        }\n"
                      + "      }\n"
                      + "    ]\n"
                      + "  }\n"
                      + "}";
        List<BindingTupleRow> bindingTupleRows =
                SearchAggregationResponseHelper.populateSearchAggregationResponse(AggregationUtils.fromJson(json));
        assertEquals(4, bindingTupleRows.size());
        assertThat(bindingTupleRows, containsInAnyOrder(
                bindingTupleRow(BindingTuple.from(ImmutableMap.of("age", 31L, "gender", "m"))),
                bindingTupleRow(BindingTuple.from(ImmutableMap.of("age", 31L, "gender", "f"))),
                bindingTupleRow(BindingTuple.from(ImmutableMap.of("age", 39L, "gender", "m"))),
                bindingTupleRow(BindingTuple.from(ImmutableMap.of("age", 39L, "gender", "f")))));
    }

    private static Matcher<BindingTupleRow> bindingTupleRow(BindingTuple bindingTuple) {
        return featureValueOf("BindingTuple", equalTo(bindingTuple), BindingTupleRow::data);
    }
}