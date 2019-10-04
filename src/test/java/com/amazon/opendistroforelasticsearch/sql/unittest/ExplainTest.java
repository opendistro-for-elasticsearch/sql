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

package com.amazon.opendistroforelasticsearch.sql.unittest;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils;
import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.utils.JsonPrettyFormatter;
import com.amazon.opendistroforelasticsearch.sql.query.ESActionFactory;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents;
import com.google.common.io.Files;
import org.elasticsearch.client.Client;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ExplainTest {

    @Test
    public void assertExplainRequestShouldBeJson() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/explain_format_pretty.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8);

        String explainResult = explain(String.format("{\"query\":\"" +
                "SELECT firstname " +
                "FROM %s\"}", TestsConstants.TEST_INDEX_ACCOUNT));
        String result = new JsonPrettyFormatter().format(explainResult);

        assertThat(result.replaceAll("\\s", ""),
                equalTo(expectedOutput.replaceAll("\\s", "")));
    }

    @Test
    public void assertExplainRequestPrettyFormatted() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/explain_format_pretty.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8);

        String explainResult = explain(String.format("{\"query\":\"" +
                "SELECT firstname " +
                "FROM %s\"}", TestsConstants.TEST_INDEX_ACCOUNT));
        String result = new JsonPrettyFormatter().format(explainResult);

        assertThat(result, equalTo(expectedOutput));
    }

    @Test
    public void assertExplainJoinRequestPrettyFormatted() throws IOException {

        String expectedOutPutFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/explain_join_format_pretty.json");
        String expectedOutput = Files.toString(new File(expectedOutPutFilePath), StandardCharsets.UTF_8);

        String bank = TestsConstants.TEST_INDEX_BANK;
        String explainResult = explain(String.format("{\"query\":\"" +
                "SELECT b1.firstname, b1.lastname, b2.age  " +
                "FROM %s b1 " +
                "LEFT JOIN %s b2 " +
                "ON b1.age = b2.age AND b1.state = b2.state\"}", bank, bank));
        String result = new JsonPrettyFormatter().format(explainResult);

        assertThat(result, equalTo(expectedOutput));
    }

    private String explain(String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            String sql = jsonRequest.getString("query");

            return translate(sql, jsonRequest);
        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in request: " + request);
        }
    }

    private String translate(String sql, JSONObject jsonRequest) throws SQLFeatureNotSupportedException, SqlParseException {
        Client mockClient = Mockito.mock(Client.class);
        CheckScriptContents.stubMockClient(mockClient);
        QueryAction queryAction = ESActionFactory.create(mockClient, sql);

        SqlRequest sqlRequest = new SqlRequest(sql, jsonRequest);
        queryAction.setSqlRequest(sqlRequest);

        SqlElasticRequestBuilder requestBuilder = queryAction.explain();
        return requestBuilder.explain();
    }
}