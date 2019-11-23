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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.request;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponse;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.KIBANA_REQUEST;

/**
 * Request to SQL plugin to isolate Elasticsearch native request
 */
public class SqlRequest {

    public static final SqlRequest NONE = null;

    /** Native Elasticsearch request object */
    private final Request request;

    public SqlRequest(String method, String endpoint, String body, UrlParam... params) {
        this.request = makeRequest(method, endpoint, body, params);
    }

    /**
     * Send request to Elasticsearch via client and create response for it.
     * @param client    restful client connection
     * @return          sql response
     */
    public SqlResponse send(RestClient client) {
        try {
            return new SqlResponse(client.performRequest(request));
        } catch (IOException e) {
            // Some test may expect failure
            if (e instanceof ResponseException) {
                return new SqlResponse(((ResponseException) e).getResponse());
            }

            throw new IllegalStateException(StringUtils.format(
                "Exception occurred during sending request %s", KIBANA_REQUEST.format(this)), e);
        }
    }

    /**
     * Expose request for request formatter.
     * @return  native Elasticsearch format
     */
    public Request request() {
        return request;
    }

    private Request makeRequest(String method, String endpoint, String body, UrlParam[] params) {
        Request request = new Request(method, endpoint);
        request.setJsonEntity(body);
        for (UrlParam param : params) {
            request.addParameter(param.key, param.value);
        }

        RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        restOptionsBuilder.addHeader("Content-Type", "application/json");
        request.setOptions(restOptionsBuilder);
        return request;
    }

    public static class UrlParam {
        private String key;
        private String value;

        public UrlParam(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public UrlParam(String keyValue) {
            int equality = keyValue.indexOf('=');
            if (equality == -1) {
                throw new IllegalArgumentException(String.format(
                    "Key value pair is in bad format [%s]", keyValue));
            }

            this.key = keyValue.substring(0, equality);
            this.value = keyValue.substring(equality + 1);
        }
    }

}
