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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.dsl;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.markup.Document;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.markup.RstDocument;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.response.ResponseFormat;
import com.amazon.opendistroforelasticsearch.sql.utils.JsonPrettyFormatter;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.markup.Document.Example;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.KIBANA;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.NO_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest.UrlParam;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.ResponseFormat.NO_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.ResponseFormat.PRETTY_JSON;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.ResponseFormat.TABLE;
import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction.EXPLAIN_API_ENDPOINT;
import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction.QUERY_API_ENDPOINT;

/**
 * Build document in DSL for readability
 */
public interface DocBuilder {

    RestClient restClient();

    default void section(String title, Example... examples) {
        section(title, "", "", examples);
    }

    default void section(String title, String description, Example... examples) {
        section(title, description, "", examples);
    }

    default void section(String title, String description, String syntax, Example... examples) {
        DocTestConfig config = getClass().getAnnotation(DocTestConfig.class);
        try (RstDocument document = new RstDocument(Document.path(config.template()))) {
            document.section(title);

            if (!description.isEmpty()) {
                document.subSection("Description"). // TODO required?
                         paragraph(description);
            }

            if (!syntax.isEmpty()) {
                document.subSection("Syntax").
                         codeBlock("", syntax);
            }

            if (examples.length > 0) {
                document.subSection("Examples");

                for (Example example : examples) {
                    if (!example.getDescription().isEmpty()) {
                        document.paragraph(example.getDescription());
                    }

                    document.codeBlock("SQL query", example.getQuery()).
                             codeBlock("Explain", example.getExplainResult()).
                             codeBlock("Result set", example.getResultSet());
                }
            }
        }
    }

    default Example example(String description, SqlRequest[] requests) {
        return example(
            description, requests,
            new RequestFormat[] { KIBANA, NO_REQUEST },
            new ResponseFormat[] { TABLE, PRETTY_JSON }
        );
    }

    default Example example(String description,
                            SqlRequest[] requests,
                            RequestFormat[] requestFormats,
                            ResponseFormat[] responseFormats) {
        Example example = new Example();
        example.setDescription(description);
        if (requestFormats[0] != NO_REQUEST) {
            example.setQuery(requestFormats[0].format(requests[0]));
        }
        if (responseFormats[0] != NO_RESPONSE) {
            example.setResult(responseFormats[0].format(requests[0].send(restClient())));
        }
        if (requestFormats[1] != NO_REQUEST) {
            example.setExplainQuery(requestFormats[1].format(requests[1]));
        }
        if (responseFormats[1] != NO_RESPONSE) {
            example.setExplainResult(responseFormats[1].format(requests[1].send(restClient())));
        }
        return example;
    }

    default String title(String title) {
        return title;
    }

    default String description(String... sentences) {
        return String.join(" ", sentences);
    }

    default String syntax(String... sentences) {
        return String.join(" ", sentences);
    }

    default RequestFormat[] requestFormat(RequestFormat... formats) {
        if (formats.length != 2) {
            throw new IllegalArgumentException("Please provide request formats for both query and explain");
        }
        return formats;
    }

    default ResponseFormat[] responseFormat(ResponseFormat... formats) {
        if (formats.length != 2) {
            throw new IllegalArgumentException("Please provide response formats for both query and explain");
        }
        return formats;
    }

    default SqlRequest[] query(String sql, UrlParam... params) {
        return query(new Body("\"query\":\"" + sql + "\""), params);
    }

    default SqlRequest[] query(Body body, UrlParam... params) {
        //String body = String.format("{\n  \"query\": \"%s\"\n}", sql);
        String bodyStr = body.toString();
        return new SqlRequest[]{
            new SqlRequest("POST", QUERY_API_ENDPOINT, bodyStr, params),
            new SqlRequest("POST", EXPLAIN_API_ENDPOINT, bodyStr)
        };
    }

    default Body body(String... fieldValues) {
        return new Body(fieldValues);
    }

    default UrlParam[] params(String... keyValues) {
        UrlParam[] params;
        if (keyValues.length == 0 ) {
            params = new UrlParam[]{ new UrlParam("format", "jdbc") };
        } else {
            if (keyValues[0].isEmpty()) { //TODO: hack to get DSL although jdbc is default
                params = new UrlParam[0];
            } else {
                params = Arrays.stream(keyValues).map(UrlParam::new).toArray(UrlParam[]::new);
            }
        }
        return params;
    }

    class Body {
        private final String[] fieldValues;

        public Body(String... fieldValues) {
            this.fieldValues = fieldValues;
        }

        @Override
        public String toString() {
            try {
                return JsonPrettyFormatter.format("{" + String.join(",", fieldValues) + "}");
            } catch (IOException e) {
                throw new IllegalStateException(StringUtils.format(
                    "Failed to jsonify body for fields %s", Arrays.toString(fieldValues), e));
            }
        }
    }

}
