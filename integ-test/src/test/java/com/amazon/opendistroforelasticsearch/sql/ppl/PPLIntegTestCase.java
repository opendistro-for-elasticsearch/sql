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

package com.amazon.opendistroforelasticsearch.sql.ppl;

import com.amazon.opendistroforelasticsearch.sql.esintgtest.RestIntegTestCase;
import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.IOException;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getResponseBody;
import static com.amazon.opendistroforelasticsearch.sql.plugin.rest.RestPPLQueryAction.QUERY_API_ENDPOINT;

/**
 * ES Rest integration test base for PPL testing
 */
public abstract class PPLIntegTestCase extends RestIntegTestCase {

    protected String executeQuery(String query) throws IOException {
        Response response = client().performRequest(buildRequest(query));
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        return getResponseBody(response, true);
    }

    protected Request buildRequest(String query) {
        Request request = new Request("POST", QUERY_API_ENDPOINT);
        request.setJsonEntity(String.format(Locale.ROOT,
            "{\n" +
            "  \"query\": \"%s\"\n" +
            "}", query));

        RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        restOptionsBuilder.addHeader("Content-Type", "application/json");
        request.setOptions(restOptionsBuilder);
        return request;
    }

    protected JSONArray getJSONArrayResult(String result) {
        return jsonify(result).getJSONArray("datarows");
    }

    protected List<List<Object>> getResultSet(String result) {
        JSONArray array = getJSONArrayResult(result);
        List<List<Object>> datarows = new ArrayList<>();
        array.toList().forEach(object -> datarows.add(((JSONArray)object).toList()));
        return datarows;
    }

    protected JSONArray getSchema(String result) {
        return jsonify(result).getJSONArray("schema");
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, String>> getSchemaAsMapList(String result) {
        JSONArray schema = getSchema(result);
        List<Map<String, String>> mapList = new ArrayList<>();
        schema.forEach(item -> {
            mapList.add(((HashMap<String, String>)item));
        });
        return mapList;
    }

    protected List<String> getColumnNames(String result) {
        return getSchemaAsMapList(result).stream().map(map -> map.get("name")).collect(Collectors.toList());
    }

    private JSONObject jsonify(String result) {
        return new JSONObject(result);
    }

    /**
     * Util: get the file content as a string from a designated file in expectedOutput directory of integ-test module
     */
    protected static String getExpectedOutput(String filename) throws IOException {
        String projectPath = System.getProperty("project.root", null);
        String resourcesDir = "integ-test/src/test/resources/expectedOutput/";
        File file = new File(projectPath + "/" + resourcesDir + filename);
        return Files.asCharSource(file, StandardCharsets.UTF_8).read();
    }
}
