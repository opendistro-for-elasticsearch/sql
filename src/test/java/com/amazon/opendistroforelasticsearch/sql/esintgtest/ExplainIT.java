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

import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_LOCATION;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_NESTED_TYPE;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_PEOPLE;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_PHRASE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class ExplainIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.DOG);
        loadIndex(Index.PEOPLE);
        loadIndex(Index.PHRASE);
        loadIndex(Index.LOCATION);
        loadIndex(Index.NESTED);
    }

    @Test
    public void searchSanity() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/search_explain.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");

        String result = explainQuery(String.format("SELECT * FROM %s WHERE firstname LIKE 'A%%' AND age > 20 " +
                "GROUP BY gender order by _score", TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void aggregationQuery() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/aggregation_query_explain.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");

        String result = explainQuery(String.format("SELECT a, CASE WHEN gender='0' then 'aaa' else 'bbb'end a2345," +
                        "count(c) FROM %s GROUP BY terms('field'='a','execution_hint'='global_ordinals'),a2345",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void explainScriptValue() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/script_value.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");

        String result = explainQuery(String.format("SELECT case when gender is null then 'aaa' " +
                "else gender  end  test , cust_code FROM %s", TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void betweenScriptValue() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/between_query.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");

        String result = explainQuery(String.format("SELECT  case when value between 100 and 200 then 'aaa' " +
                "else value  end  test, cust_code FROM %s", TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void searchSanityFilter() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/search_explain_filter.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");

        String result = explainQuery(String.format("SELECT * FROM %s WHERE firstname LIKE 'A%%' " +
                "AND age > 20 GROUP BY gender", TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void deleteSanity() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/delete_explain.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");;

        String result = explainQuery(String.format("DELETE FROM %s WHERE firstname LIKE 'A%%' AND age > 20",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void spatialFilterExplainTest() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/search_spatial_explain.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");;

        String result = explainQuery(String.format("SELECT * FROM %s WHERE GEO_INTERSECTS" +
                "(place,'POLYGON ((102 2, 103 2, 103 3, 102 3, 102 2))')", TEST_INDEX_LOCATION));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void orderByOnNestedFieldTest() throws Exception {

        String result = explainQuery(String.format("SELECT * FROM %s ORDER BY NESTED('message.info','message')",
                TEST_INDEX_NESTED_TYPE));
        Assert.assertThat(result.replaceAll("\\s+", ""),
                equalTo("{\"from\":0,\"size\":200,\"sort\":[{\"message.info\":" +
                        "{\"order\":\"asc\",\"nested\":{\"path\":\"message\"}}}]}"));
    }

    @Test
    public void multiMatchQuery() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/multi_match_query.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        String result = explainQuery(String.format("SELECT * FROM %s WHERE q=multimatch(query='this is a test'," +
                "fields='subject^3,message',analyzer='standard',type='best_fields',boost=1.0," +
                "slop=0,tie_breaker=0.3,operator='and')", TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+", ""), equalTo(expectedOutput.replaceAll("\\s+", "")));
    }

    @Test
    public void termsIncludeExcludeExplainTest() throws IOException {

        final String queryPrefix = "SELECT * FROM " + TEST_INDEX_PHRASE + " GROUP BY ";
        final String expected1 = "\"include\":\".*sport.*\",\"exclude\":\"water_.*\"";
        final String expected2 = "\"include\":[\"honda\",\"mazda\"],\"exclude\":[\"jensen\",\"rover\"]";
        final String expected3 = "\"include\":{\"partition\":0,\"num_partitions\":20}";

        String result = explainQuery(queryPrefix + " terms(field='correspond_brand_name',size='10'," +
                "alias='correspond_brand_name',include='\\\".*sport.*\\\"',exclude='\\\"water_.*\\\"')");
        Assert.assertThat(result, containsString(expected1));

        result = explainQuery(queryPrefix + "terms(field='correspond_brand_name',size='10'," +
                "alias='correspond_brand_name',include='[\\\"mazda\\\", \\\"honda\\\"]'," +
                "exclude='[\\\"rover\\\", \\\"jensen\\\"]')");
        Assert.assertThat(result, containsString(expected2));

        result = explainQuery(queryPrefix + "terms(field='correspond_brand_name',size='10'," +
                "alias='correspond_brand_name',include='{\\\"partition\\\":0,\\\"num_partitions\\\":20}')");
        Assert.assertThat(result, containsString(expected3));
    }

    @Test
    public void explainNLJoin() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/nested_loop_join_explain.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        String query = "SELECT /*! USE_NL*/ a.firstname ,a.lastname , a.gender ,d.dog_name  FROM " +
                TEST_INDEX_PEOPLE + "/people a JOIN " + TEST_INDEX_DOG + "/dog d on d.holdersName = a.firstname" +
                " WHERE (a.age > 10 OR a.balance > 2000) AND d.age > 1";
        String result = explainQuery(query);

        Assert.assertThat(result.replaceAll("\\s+", ""), equalTo(expectedOutput.replaceAll("\\s+", "")));
    }
}
