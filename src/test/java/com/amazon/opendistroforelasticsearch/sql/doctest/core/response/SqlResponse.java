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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.response;

import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils;
import org.elasticsearch.client.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Response from SQL plugin
 */
public class SqlResponse {

    public static final SqlResponse NONE = null;

    /** Native Elasticsearch response */
    private final Response response;

    public SqlResponse(Response response) {
        this.response = response;
    }

    /**
     * Parse body in the response.
     * @return  response body
     */
    public String body() {
        try {
            return replaceChangingFields(TestUtils.getResponseBody(response, true));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read response body", e);
        }
    }

    /**
     * In Elasticsearch response, there is field changed between each query, such as "took".
     * We have to replace those variants with fake constant to avoid re-generate documents.
     * The order of fields in JSON is a little different from original because of internal
     * key set in org.json.
     */
    private String replaceChangingFields(String response) {
        try {
            JSONObject root = new JSONObject(response);
            if (root.has("took")) {
                root.put("took", 100);
            } else {
                return response; // return original response to minimize impact
            }

            if (root.has("_shards")) {
                JSONObject shards = root.getJSONObject("_shards");
                shards.put("total", 5);
                shards.put("successful", 5);
            }
            return root.toString();
        } catch (JSONException e) {
            // Response is not a valid JSON which is not our interest.
            return response;
        }
    }
}
