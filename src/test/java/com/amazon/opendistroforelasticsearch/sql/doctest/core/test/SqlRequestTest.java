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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.test;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest.UrlParam;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test cases for {@link SqlRequest}
 */
public class SqlRequestTest {

    @Test
    public void requestShouldIncludeAllFields() throws IOException {
        String method = "POST";
        String endpoint = "/_opendistro/_sql";
        String body = "{\"query\":\"SELECT * FROM accounts\"}";
        String key = "format";
        String value = "jdbc";
        UrlParam param = new UrlParam(key, value);

        RestClient client = mock(RestClient.class);
        SqlRequest sqlRequest = new SqlRequest(method, endpoint, body, param);
        sqlRequest.send(client);

        ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);
        verify(client).performRequest(argument.capture());
        Request actual = argument.getValue();
        assertThat(actual.getMethod(), is(method));
        assertThat(actual.getEndpoint(), is(endpoint));
        assertThat(actual.getParameters(), hasEntry(key, value));
        assertThat(body(actual), is(body));
    }

    @Test(expected = IllegalArgumentException.class)
    public void badUrlParamShouldThrowException() {
        new UrlParam("test");
    }

    private String body(Request request) throws IOException {
        InputStream content = request.getEntity().getContent();
        return CharStreams.toString(new InputStreamReader(content, Charsets.UTF_8));
    }

}
