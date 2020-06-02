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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.builder;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.markup.Document;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import com.google.common.base.Strings;
import org.elasticsearch.client.RestClient;

import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest.UrlParam;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.IGNORE_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.KIBANA_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.PRETTY_JSON_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.TABLE_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction.EXPLAIN_API_ENDPOINT;
import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction.QUERY_API_ENDPOINT;

/**
 * Build document by custom DSL. To make it more readable, each doc test needs to implement this interface
 * and provide things required, such as client connection and document handle. As benefit, they can use the
 * DSL to build document in readable and fluent way.
 */
public interface DocBuilder {

    String DOCUMENT_FOLDER_ROOT = "/docs/user/";
    String IMAGE_FOLDER_PATH = DOCUMENT_FOLDER_ROOT + "img/";

    /**
     * Get client connection to cluster for sending request
     * @return  REST client
     */
    RestClient restClient();

    /**
     * Open document file to write
     * @return  document class
     */
    Document openDocument();

    default void section(String title, String description, Example... examples) {
        section(title, description, new String[0], examples);
    }

    /**
     * Entry point to start building document by DSL.
     * Each section consists of:
     *  1. Title
     *  2. Description
     *  3. Example(s)
     *     3.1 Description
     *     3.2 [Sample request]
     *     3.3 [Explain request]
     *     3.4 [Explain output]
     *     3.5 [Result set]
     *
     * @param title         title of the section
     * @param description   description paragraph
     * @param examples      examples for the section
     */
    default void section(String title, String description, String[] images, Example... examples) {
        try (Document document = openDocument()) {
            document.section(title);

            if (!description.isEmpty()) {
                document.subSection("Description").paragraph(description);
            }

            if (images.length > 0) {
                document.subSection("Syntax");
                for (String image : images) {
                    // Convert image name ex. "rdd/queryStatement.png" to "queryStatement" as description.
                    String imageDesc = image.substring(image.lastIndexOf('/') + 1, image.lastIndexOf('.'));
                    document.image("Rule ``" + imageDesc + "``", IMAGE_FOLDER_PATH + image);
                }
            }

            for (int i = 0; i < examples.length; i++) {
                String exampleTitle;
                if (examples.length > 1) {
                    exampleTitle = "Example " + (i + 1);
                } else {
                    exampleTitle = "Example";
                }
                if (!Strings.isNullOrEmpty(examples[i].getTitle())) {
                    exampleTitle += (": " + examples[i].getTitle());
                }
                document.subSection(exampleTitle);

                Example example = examples[i];
                if (!example.getDescription().isEmpty()) {
                    document.paragraph(example.getDescription());
                }

                document.codeBlock("SQL query", example.getQuery()).
                         codeBlock("Explain query", example.getExplainQuery()).
                         codeBlock("Explain", example.getExplainResult());

                if (example.isTable()) {
                    document.table("Result set", example.getResult());
                } else {
                    document.codeBlock("Result set", example.getResult());
                }
            }
        }
    }

    default Example example(String description, Requests requests) {
        return example("", description, requests);
    }

    /** Construct an example by default query and explain format */
    default Example example(String title, String description, Requests requests) {
        return example(title, description, requests,
            queryFormat(KIBANA_REQUEST, TABLE_RESPONSE),
            explainFormat(IGNORE_REQUEST, PRETTY_JSON_RESPONSE)
        );
    }

    default Example example(String description,
                            Requests requests,
                            Formats queryFormat,
                            Formats explainFormat) {
        return example("", description, requests, queryFormat, explainFormat);
    }

    default Example example(String title,
                            String description,
                            Requests requests,
                            Formats queryFormat,
                            Formats explainFormat) {
        Example example = new Example();
        example.setTitle(title);
        example.setDescription(description);
        example.setQuery(queryFormat.format(requests.query()));
        example.setTable(queryFormat.isTableFormat());
        example.setResult(queryFormat.format(requests.queryResponse()));
        example.setExplainQuery(explainFormat.format(requests.explain()));
        example.setExplainResult(explainFormat.format(requests.explainResponse()));
        return example;
    }

    /** Simple method just for readability */
    default String title(String title) {
        return title;
    }

    default String description(String... sentences) {
        return String.join(" ", sentences);
    }

    default String[] images(String... images) {
        return images;
    }

    default Formats queryFormat(SqlRequestFormat requestFormat, SqlResponseFormat responseFormat) {
        return new Formats(requestFormat, responseFormat);
    }

    default Formats explainFormat(SqlRequestFormat requestFormat, SqlResponseFormat responseFormat) {
        return new Formats(requestFormat, responseFormat);
    }

    default Requests get(String sql) {
        Body body = new Body("\"query\":\"" + sql + "\"");
        return new Requests(
            restClient(),
            new SqlRequest("GET", QUERY_API_ENDPOINT, "", new UrlParam("sql", sql)),
            new SqlRequest("POST", EXPLAIN_API_ENDPOINT, body.toString())
        );
    }

    default Requests put(String name, Object value) {
        String setting = value == null ?
                StringUtils.format("\"%s\": {\"%s\": null}", "transient", name) :
                StringUtils.format("\"%s\": {\"%s\": \"%s\"}", "transient", name, value);
        return new Requests(
            restClient(),
            new SqlRequest("PUT", "/_opendistro/_sql/settings", new Body(setting).toString()),
            null
        );
    }

    default String multiLine(String... lines) {
        return String.join("\\n", lines);
    }

    /** Query by a simple SQL is too common and deserve a dedicated overload method */
    default Requests post(String sql, UrlParam... params) {
        return post(new Body("\"query\":\"" + sql + "\""), params);
    }

    default Requests post(Body body, UrlParam... params) {
        String bodyStr = body.toString();
        return new Requests(
            restClient(),
            new SqlRequest("POST", QUERY_API_ENDPOINT, bodyStr, params),
            new SqlRequest("POST", EXPLAIN_API_ENDPOINT, bodyStr)
        );
    }

    default Body body(String... fieldValues) {
        return new Body(fieldValues);
    }

    default UrlParam[] params(String... keyValues) {
        return Arrays.stream(keyValues).map(UrlParam::new).toArray(UrlParam[]::new);
    }

}
