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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

// Refer to https://www.elastic.co/guide/en/elasticsearch/reference/6.5/integration-tests.html
// for detailed ESIntegTestCase usages doc.
public class PreparedStatementIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void testPreparedStatement() throws IOException {
        int ageToCompare = 35;

        JSONObject response = executeRequest(String.format("{\n" +
                "  \"query\": \"SELECT * FROM %s WHERE age > ? AND state in (?, ?) LIMIT ?\",\n" +
                "  \"parameters\": [\n" +
                "    {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"value\": \"" + ageToCompare + "\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"string\",\n" +
                "      \"value\": \"TN\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"string\",\n" +
                "      \"value\": \"UT\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"value\": \"20\"\n" +
                "    }\n" +
                "  ]\n" +
                "}", TestsConstants.TEST_INDEX_ACCOUNT));

        Assert.assertTrue(response.has("hits"));
        Assert.assertTrue(response.getJSONObject("hits").has("hits"));

        JSONArray hits = response.getJSONObject("hits").getJSONArray("hits");
        Assert.assertTrue(hits.length() > 0);
        for(int i = 0; i<hits.length(); i++) {
            JSONObject accountJson = hits.getJSONObject(i);
            Assert.assertTrue(accountJson.getJSONObject("_source").getInt("age") > ageToCompare);
        }
    }

    /* currently the integ test case will fail if run using Intellj, have to run using gradle command
     * because the integ test cluster created by IntellJ has http diabled, need to spend some time later to
     * figure out how to configure the integ test cluster properly. Related online resources:
     *     https://discuss.elastic.co/t/http-enabled-with-esintegtestcase/102032
     *     https://discuss.elastic.co/t/help-with-esintegtestcase/105245
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Arrays.asList(MockTcpTransportPlugin.class);
    }

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return Settings.builder().put(super.nodeSettings(nodeOrdinal))
                // .put("node.mode", "network")
                .put("http.enabled", true)
                //.put("http.type", "netty4")
                .build();
    }
    */
}
