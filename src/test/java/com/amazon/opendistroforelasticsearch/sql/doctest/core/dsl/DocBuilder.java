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
import org.elasticsearch.client.RestClient;

import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.markup.Document.Example;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.KIBANA;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.NO_REQUEST;
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
                         paragraph(syntax);
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

    default Example example(String description, SqlRequest[] request) {
        return example(
            description, request,
            new RequestFormat[] { KIBANA, NO_REQUEST },
            new ResponseFormat[] { TABLE, PRETTY_JSON }
        );
    }

    default Example example(String description,
                            SqlRequest[] request,
                            RequestFormat[] requestFormats,
                            ResponseFormat[] responseFormats) {
        Example example = new Example();
        example.setDescription(description);
        if (requestFormats[0] != NO_REQUEST) {
            example.setQuery(requestFormats[0].format(request[0]));
        }
        if (responseFormats[0] != NO_RESPONSE) {
            example.setResult(responseFormats[0].format(request[0].send(restClient())));
        }
        if (requestFormats[1] != NO_REQUEST) {
            example.setExplainQuery(requestFormats[1].format(request[1]));
        }
        if (responseFormats[1] != NO_RESPONSE) {
            example.setExplainResult(responseFormats[1].format(request[1].send(restClient())));
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

    default RequestFormat[] format(RequestFormat... formats) {
        if (formats.length != 2) {
            throw new IllegalArgumentException("Please provide request formats for both query and explain");
        }
        return formats;
    }

    default ResponseFormat[] format(ResponseFormat... formats) {
        if (formats.length != 2) {
            throw new IllegalArgumentException("Please provide response formats for both query and explain");
        }
        return formats;
    }

    default SqlRequest[] query(String sql, String... keyValues) {
        SqlRequest.UrlParam[] params;
        if (keyValues.length == 0) {
            params = new SqlRequest.UrlParam[]{ new SqlRequest.UrlParam("format", "jdbc") };
        } else {
            params = Arrays.stream(keyValues).map(SqlRequest.UrlParam::new).toArray(SqlRequest.UrlParam[]::new);
        }

        String body = String.format("{\n" + "  \"query\": \"%s\"\n" + "}", sql);;
        return new SqlRequest[]{
            new SqlRequest("POST", QUERY_API_ENDPOINT, body, params),
            new SqlRequest("POST", EXPLAIN_API_ENDPOINT, body)
        };
    }

}
