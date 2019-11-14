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

@DocTestConfig(
    template = "interfaces/protocol.rst",
    testData = {"accounts.json"}
)
public class ProtocolIT extends DocTest {

    @Section(order = 1)
    public void getOriginalDSLResponse() {
        section(
            title("Elasticsearch DSL"),
            description(
                "By default the plugin returns original response from Elasticsearch in JSON. Because this is the native response",
                "from Elasticsearch, extra efforts are needed to parse and interpret it. Meanwhile mutation like field alias will",
                "not be present in it."
            ),
            example(
                description(),
                query("SELECT firstname, lastname, age, city FROM accounts LIMIT 2", ""),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(PRETTY_JSON, NO_RESPONSE)
            )
        );
    }

    @Section(order = 2)
    public void getResponseInJDBCFormat() {
        section(
            title("JDBC Format"),
            description("JDBC format is provided for JDBC driver and client side that needs both schema and result set well formatted."),
            example(
                description(),
                query("SELECT firstname, lastname, age, city FROM accounts LIMIT 2", "format=jdbc"),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(PRETTY_JSON, NO_RESPONSE)
            )
        );
    }

    @Section(order = 3)
    public void getResponseInCsvFormat() {
        section(
            title("CSV Format"),
            description("You can also use CSV format to download result set in csv format."),
            example(
                description(),
                query("SELECT firstname, lastname, age, city FROM accounts", "format=csv"),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(ORIGINAL, NO_RESPONSE)
            )
        );
    }

    @Section(order = 4)
    public void getResponseInRawFormat() {
        section(
            title("Raw Format"),
            description("Additionally you can also use RAW format to pipe the result with other command line tool for post processing."),
            example(
                description(),
                query("SELECT firstname, lastname, age, city FROM accounts", "format=raw"),
                requestFormat(CURL, NO_REQUEST),
                responseFormat(ORIGINAL, NO_RESPONSE)
            )
        );
    }

}
