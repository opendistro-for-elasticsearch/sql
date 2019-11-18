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

package com.amazon.opendistroforelasticsearch.sql.doctest.test;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest.UrlParam;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.CURL_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.KIBANA_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.RequestFormat.NO_REQUEST;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test cases for {@link RequestFormat}
 */
public class RequestFormatTest {

    private final SqlRequest request = new SqlRequest(
        "POST",
        "/_opendistro/_sql",
        "{\"query\":\"SELECT * FROM accounts\"}",
        new UrlParam("format", "jdbc")
    );

    @Test(expected = UnsupportedOperationException.class)
    public void noRequestFormatShouldNotBeUsedForFormat() {
        NO_REQUEST.format(null);
    }

    @Test
    public void curlRequestFormatShouldBeCurlInCLI() {
        String expected =
            ">> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=jdbc -d '{\n" +
            "  \"query\" : \"SELECT * FROM accounts\"\n" +
            "}'";
        assertThat(CURL_REQUEST.format(request), is(expected));
    }

    @Test
    public void kibanaRequestFormatShouldBeFormatInKibana() {
        String expected =
            "POST /_opendistro/_sql?format=jdbc\n" +
            "{\n" +
            "  \"query\" : \"SELECT * FROM accounts\"\n" +
            "}";
        assertThat(KIBANA_REQUEST.format(request), is(expected));
    }

}
