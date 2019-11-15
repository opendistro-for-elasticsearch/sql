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
import com.amazon.opendistroforelasticsearch.sql.doctest.core.dsl.DocTestConfig;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.dsl.Section;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.CURL;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.NO_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.ResponseFormat.NO_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.ResponseFormat.ORIGINAL;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.ResponseFormat.PRETTY_JSON;

/**
 * Doc test for plugin supported protocols.
 */
@DocTestConfig(
    template = "interfaces/protocol.rst",
    testData = {"accounts.json"}
)
public class ProtocolIT extends DocTest {

    @Section(order = 1)
    public void requestFormat() {
        section(
            title("Request Format"),
            description("Request body by HTTP POST accepts a few more fields than SQL query."),
            //syntax(""),
            example(
                description(
                    "Use `filter` to work with Elasticsearch DSL directly. Note that the content is present in",
                    "final Elasticsearch request DSL as it is."
                ),
                query(
                    body(
                        "\"query\": \"SELECT firstname, lastname, balance FROM accounts\"",
                        "\"filter\":{\"range\":{\"balance\":{\"lt\":10000}}}"
                    )
                ),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(NO_RESPONSE, PRETTY_JSON)
            ),
            example(
                description("Use `parameters` for actual value for placeholder in prepared SQL query to be replaced."),
                query(
                    body(
                        "\"query\": \"SELECT * FROM accounts WHERE age = ?\"",
                        "\"parameters\": [{\"type\": \"integer\", \"value\": 30}]"
                    )
                ),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(NO_RESPONSE, PRETTY_JSON)
            )
        );
    }

    @Section(order = 2)
    public void originalDSLResponse() {
        section(
            title("Elasticsearch DSL"),
            description(
                "By default the plugin returns original response from Elasticsearch in JSON. Because this is",
                "the native response from Elasticsearch, extra efforts are needed to parse and interpret it.",
                "Meanwhile mutation like field alias will not be present in it."
            ),
            example(
                description(),
                query("SELECT firstname, lastname, age FROM accounts ORDER BY age LIMIT 2", params("")),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(PRETTY_JSON, NO_RESPONSE)
            )
        );
    }

    @Section(order = 3)
    public void responseInJDBCFormat() {
        section(
            title("JDBC Format"),
            description(
                "JDBC format is provided for JDBC driver and client side that needs both schema and",
                "result set well formatted."
            ),
            example(
                description(
                    "Here is an example for normal response. The `schema` includes field name and its type",
                    "and `datarows` includes the result set."
                ),
                query("SELECT firstname, lastname, age FROM accounts ORDER BY age LIMIT 2", params("format=jdbc")),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(PRETTY_JSON, NO_RESPONSE)
            ),
            example(
                description("If any error occurred, error message and the cause will be returned instead."),
                query("SELECT unknown FROM accounts", params("format=jdbc")),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(PRETTY_JSON, NO_RESPONSE)
            )
        );
    }

    @Section(order = 4)
    public void responseInCSVFormat() {
        section(
            title("CSV Format"),
            description("You can also use CSV format to download result set as CSV."),
            example(
                description(),
                query("SELECT firstname, lastname, age FROM accounts ORDER BY age", params("format=csv")),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(ORIGINAL, NO_RESPONSE)
            )
        );
    }

    @Section(order = 5)
    public void responseInRawFormat() {
        section(
            title("Raw Format"),
            description(
                "Additionally you can also use raw format to pipe the result with other command line tool.",
                "for post processing."
            ),
            example(
                description(),
                query("SELECT firstname, lastname, age FROM accounts ORDER BY age", params("format=raw")),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(ORIGINAL, NO_RESPONSE)
            )
        );
    }

}
