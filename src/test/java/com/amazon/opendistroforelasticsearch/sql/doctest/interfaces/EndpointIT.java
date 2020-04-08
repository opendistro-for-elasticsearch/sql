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

package com.amazon.opendistroforelasticsearch.sql.doctest.interfaces;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.DocTest;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.annotation.DocTestConfig;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.annotation.Section;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.CURL_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.IGNORE_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.IGNORE_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.PRETTY_JSON_RESPONSE;

/**
 * Doc test for endpoints to access the plugin.
 */
@DocTestConfig(template = "interfaces/endpoint.rst", testData = {"accounts.json"})
public class EndpointIT extends DocTest {

    @Section(1)
    public void queryByPost() {
        section(
            title("POST"),
            description("You can also send HTTP POST request with your query in request body."),
            example(
                description(),
                post("SELECT * FROM accounts"),
                queryFormat(CURL_REQUEST, IGNORE_RESPONSE),
                explainFormat(IGNORE_REQUEST, IGNORE_RESPONSE)
            )
        );
    }

    @Section(2)
    public void explainQuery() {
        section(
            title("Explain"),
            description(
                "To translate your query, send it to explain endpoint. The explain output is Elasticsearch",
                "domain specific language (DSL) in JSON format. You can just copy and paste it to your",
                "console to run it against Elasticsearch directly."
            ),
            example(
                description(),
                post("SELECT firstname, lastname FROM accounts WHERE age > 20"),
                queryFormat(IGNORE_REQUEST, IGNORE_RESPONSE),
                explainFormat(CURL_REQUEST, PRETTY_JSON_RESPONSE)
            )
        );
    }

}
