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

import com.amazon.opendistroforelasticsearch.sql.utils.JsonPrettyFormatter;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.http.Header;
import org.elasticsearch.client.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Format Elasticsearch native request to specific format for documentation.
 */
public enum RequestFormat {
    NO_REQUEST {
        @Override
        public String format(SqlRequest request) {
            throw new UnsupportedOperationException();
        }
    },
    CURL_REQUEST {
        @Override
        public String format(SqlRequest sqlRequest) {
            Request request = sqlRequest.request();
            StringBuilder str = new StringBuilder();
            str.append(">> curl ");

            List<Header> headers = request.getOptions().getHeaders();
            if (!headers.isEmpty()) {
                str.append(headers.stream().
                           map(header -> StringUtils.format("-H '%s: %s'", header.getName(), header.getValue())).
                           collect(Collectors.joining(" ", "", " ")));
            }

            str.append(StringUtils.format("-X %s ", request.getMethod())).
                append(StringUtils.format("localhost:9200%s", request.getEndpoint()));

            if (!request.getParameters().isEmpty()) {
                str.append("?").
                    append(request.getParameters().entrySet().stream().
                        map(e -> e.getKey() + "=" + e.getValue()).
                        collect(Collectors.joining("&")));
            }

            String body = body(request);
            if (!body.isEmpty()) {
                str.append(" -d '").
                    append(body).
                    append('\'');
            }
            return str.toString();
        }
    },
    KIBANA_REQUEST {
        @Override
        public String format(SqlRequest sqlRequest) {
            Request request = sqlRequest.request();
            StringBuilder str = new StringBuilder();
            str.append(request.getMethod()).
                append(" ").
                append(request.getEndpoint());

            if (!request.getParameters().isEmpty()) {
                str.append("?").
                    append(request.getParameters().entrySet().stream().
                        map(e -> e.getKey() + "=" + e.getValue()).
                        collect(Collectors.joining("&")));
            }

            str.append('\n').
                append(body(request));
            return str.toString();
        }
    };

    public abstract String format(SqlRequest request);

    private static String body(Request request) {
        String body;
        try {
            InputStream content = request.getEntity().getContent();
            String rawBody = CharStreams.toString(new InputStreamReader(content, Charsets.UTF_8));
            body = JsonPrettyFormatter.format(rawBody);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse and format body from request", e);
        }
        return body;
    }
}
