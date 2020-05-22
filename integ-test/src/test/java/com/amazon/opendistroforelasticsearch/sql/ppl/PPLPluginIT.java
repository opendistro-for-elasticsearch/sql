/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.ppl;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.test.rest.ESRestTestCase;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;

public class PPLPluginIT extends PPLIntegTestCase {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Override
    protected void init() throws Exception {
        wipeAllClusterSettings();
    }

    @Test
    public void testQueryEndpointShouldOK() throws IOException {
        Request request = new Request("PUT", "/a/_doc/1");
        request.setJsonEntity("{\"name\": \"hello\"}");
        client().performRequest(request);

        Response response = client().performRequest(makeRequest("search source=a"));
        assertThat(
            response,
            allOf(
                statusCode(200),
                content(
                    "{\n" +
                    "  \"schema\": [{\n" +
                    "    \"name\": \"name\",\n" +
                    "    \"type\": \"string\"\n" +
                    "  }],\n" +
                    "  \"total\": 1,\n" +
                    "  \"datarows\": [{\"row\": [\"hello\"]}],\n" +
                    "  \"size\": 1\n" +
                    "}"
                )
            )
        );
    }

    @Test
    public void testQueryEndpointShouldFail() throws IOException {
        exceptionRule.expect(ResponseException.class);
        exceptionRule.expect(hasProperty("response", statusCode(500)));

        client().performRequest(makeRequest("search invalid"));
    }

    protected Request makeRequest(String query) {
        Request post = new Request("POST", "/_opendistro/_ppl");
        post.setJsonEntity(String.format(Locale.ROOT,
                "{\n" +
                        "  \"query\": \"%s\"\n" +
                        "}", query));
        return post;
    }

    private TypeSafeMatcher<Response> statusCode(int statusCode) {
        return new TypeSafeMatcher<Response>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(String.format(Locale.ROOT, "statusCode=%d", statusCode));
            }

            @Override
            protected boolean matchesSafely(Response resp) {
                return resp.getStatusLine().getStatusCode() == statusCode;
            }
        };
    }

    private TypeSafeMatcher<Response> content(String expected) {
        return new TypeSafeMatcher<Response>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(String.format(Locale.ROOT, "content=%s", expected));
            }

            @Override
            protected boolean matchesSafely(Response resp) {
                try {
                    String actual = toString(resp.getEntity().getContent());
                    return expected.equals(actual);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to get response body", e);
                }
            }

            private String toString(InputStream inputStream) {
                return new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)).
                        lines().collect(Collectors.joining("\n"));
            }
        };
    }

}
