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
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.KIBANA_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.NO_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest.UrlParam;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.ResponseFormat.NO_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.ResponseFormat.PRETTY_JSON_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.ResponseFormat.TABLE_RESPONSE;
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

            for (int i = 0; i < examples.length; i++) {
                if (examples.length > 1) {
                    document.subSection("Example " + (i + 1));
                } else {
                    document.subSection("Example");
                }

                Example example = examples[i];
                if (!example.getDescription().isEmpty()) {
                    document.paragraph(example.getDescription());
                }

                document.codeBlock("SQL query", example.getQuery()).
                         codeBlock("Explain query", example.getExplainQuery()).
                         codeBlock("Explain", example.getExplainResult()).
                         codeBlock("Result set", example.getResultSet());
            }
        }
    }

    default Example example(String description, SqlRequest[] requests) {
        return example(
            description, requests,
            queryFormat(KIBANA_REQUEST, TABLE_RESPONSE),
            explainFormat(NO_REQUEST, PRETTY_JSON_RESPONSE)
        );
    }

    default Example example(String description,
                            SqlRequest[] requests,
                            Format queryFormat,
                            Format explainFormat) {
        Example example = new Example();
        example.setDescription(description);
        if (queryFormat.request() != NO_REQUEST) {
            example.setQuery(queryFormat.request().format(requests[0]));
        }
        if (queryFormat.response() != NO_RESPONSE) {
            example.setResult(queryFormat.response().format(requests[0].send(restClient())));
        }
        if (explainFormat.request() != NO_REQUEST) {
            example.setExplainQuery(explainFormat.request().format(requests[1]));
        }
        if (explainFormat.response() != NO_RESPONSE) {
            example.setExplainResult(explainFormat.response().format(requests[1].send(restClient())));
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

    default Format queryFormat(RequestFormat requestFormat, ResponseFormat responseFormat) {
        return new Format(requestFormat, responseFormat);
    }

    default Format explainFormat(RequestFormat requestFormat, ResponseFormat responseFormat) {
        return new Format(requestFormat, responseFormat);
    }

    default SqlRequest[] get(String sql, UrlParam... params) {
        Body body = new Body("\"query\":\"" + sql + "\"");
        return new SqlRequest[] {
            new SqlRequest("GET", QUERY_API_ENDPOINT, "", new UrlParam("sql", sql)),
            new SqlRequest("POST", EXPLAIN_API_ENDPOINT, body.toString()),
        };
    }

    default SqlRequest[] query(String sql, UrlParam... params) {
        return query(new Body("\"query\":\"" + sql + "\""), params);
    }

    default SqlRequest[] put(String name, Object value) {
        String setting = StringUtils.format("\"%s\": {\"%s\": %s}", "transient", name, value);
        return new SqlRequest[] {
            new SqlRequest("PUT", "/_cluster/settings", new Body(setting).toString())
        };
    }

    default SqlRequest[] query(Body body, UrlParam... params) {
        String bodyStr = body.toString();
        return new SqlRequest[] {
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

        Body(String... fieldValues) {
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

    class Format {
        private final RequestFormat requestFormat;
        private final ResponseFormat responseFormat;

        Format(RequestFormat requestFormat, ResponseFormat responseFormat) {
            this.requestFormat = requestFormat;
            this.responseFormat = responseFormat;
        }

        RequestFormat request() {
            return requestFormat;
        }

        ResponseFormat response() {
            return responseFormat;
        }
    }

}
