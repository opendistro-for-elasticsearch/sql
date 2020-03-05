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

import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import com.google.common.io.Files;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.hamcrest.Matchers.equalTo;

public class PrettyFormatterIT extends SQLIntegTestCase{

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void assertExplainPrettyFormatted() throws IOException {
        String query = StringUtils.format("SELECT firstname FROM %s", TEST_INDEX_ACCOUNT);

        String notPrettyExplainOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/explainIT_format_not_pretty.json");
        String notPrettyExplainOutput = Files.toString(new File(notPrettyExplainOutputFilePath), StandardCharsets.UTF_8);

        assertThat(executeExplainRequest(query, ""), equalTo(notPrettyExplainOutput));
        assertThat(executeExplainRequest(query, "pretty=false"), equalTo(notPrettyExplainOutput));

        String prettyExplainOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/explainIT_format_pretty.json");
        String prettyExplainOutput = Files.toString(new File(prettyExplainOutputFilePath), StandardCharsets.UTF_8);

        assertThat(executeExplainRequest(query, "pretty"), equalTo(prettyExplainOutput));
        assertThat(executeExplainRequest(query, "pretty=true"), equalTo(prettyExplainOutput));
    }

    private String executeExplainRequest(String query, String explainParam) throws IOException {
        String endpoint = "/_opendistro/_sql/_explain?" + explainParam;
        String request = makeRequest(query);

        Request sqlRequest = new Request("POST", endpoint);
        sqlRequest.setJsonEntity(request);

        Response response = client().performRequest(sqlRequest);
        String responseString = TestUtils.getResponseBody(response, true);

        return responseString;
    }
}
