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

import com.amazon.opendistroforelasticsearch.sql.doctest.core.builder.DocBuilder;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.markup.Document;
import org.apache.http.HttpEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link DocBuilder}
 */
@RunWith(MockitoJUnitRunner.class)
public class DocBuilderTest implements DocBuilder {

    private final String queryResponse =
        "{\"schema\":[{\"name\":\"firstname\",\"type\":\"text\"}]," +
        "\"datarows\":[[\"John\"]],\"total\":10,\"size\":1,\"status\":200}";

    private final String explainResponse =
        "{\"from\":0,\"size\":1,\"_source\":{\"includes\":[\"firstname\"],\"excludes\":[]}}";

    @Mock
    private Document document;

    private Verifier verifier;

    @Mock
    private RestClient client;

    @Before
    public void setUp() throws IOException {
        when(document.section(any())).thenReturn(document);
        when(document.subSection(any())).thenReturn(document);
        when(document.paragraph(any())).thenReturn(document);
        when(document.codeBlock(any(), any())).thenReturn(document);
        when(document.table(any(), any())).thenReturn(document);
        when(document.image(any(), any())).thenReturn(document);
        verifier = new Verifier(document);

        when(client.performRequest(any())).then(new Answer<Response>() {
            private int callCount = 0;

            @Override
            public Response answer(InvocationOnMock invocationOnMock) throws IOException {
                Response response = mock(Response.class);
                HttpEntity entity = mock(HttpEntity.class);
                when(response.getEntity()).thenReturn(entity);

                String body = (callCount++ == 0) ? queryResponse : explainResponse;
                when(entity.getContent()).thenReturn(new ByteArrayInputStream(body.getBytes()));
                return response;
            }
        });
    }

    @Test
    public void sectionShouldIncludeTitleAndDescription() {
        section(
            title("Test"),
            description("This is a test")
        );

        verifier.section("Test").
                 subSection("Description").
                 paragraph("This is a test");
    }

    @Test
    public void sectionShouldIncludeExample() {
        section(
            title("Test"),
            description("This is a test"),
            images("rdd/querySyntax.png"),
            example(
                description("This is an example for the test"),
                post("SELECT firstname FROM accounts")
            )
        );

        verifier.section("Test").
                 subSection("Description").
                 paragraph("This is a test").
                 image("Rule ``querySyntax``", "/docs/user/img/rdd/querySyntax.png").
                 subSection("Example").
                 paragraph("This is an example for the test").
                 codeBlock(
                     "SQL query",
                     "POST /_opendistro/_sql\n" +
                     "{\n" +
                     "  \"query\" : \"SELECT firstname FROM accounts\"\n" +
                     "}"
                 ).
                 codeBlock(
                     "Explain",
                     "{\n" +
                     "  \"from\" : 0,\n" +
                     "  \"size\" : 1,\n" +
                     "  \"_source\" : {\n" +
                     "    \"includes\" : [\n" +
                     "      \"firstname\"\n" +
                     "    ],\n" +
                     "    \"excludes\" : [ ]\n" +
                     "  }\n" +
                     "}"
                 ).table(
                     "Result set",
                     "+---------+\n" +
                     "|firstname|\n" +
                     "+=========+\n" +
                     "|     John|\n" +
                     "+---------+\n"
                 );
    }

    @Override
    public RestClient restClient() {
        return client;
    }

    @Override
    public Document openDocument() {
        return document;
    }

    private static class Verifier implements Document {
        private final Document mock;
        private final InOrder verifier;

        Verifier(Document mock) {
            this.mock = mock;
            this.verifier = inOrder(mock);
        }

        @Override
        public void close() {
            verifier.verify(mock).close();
        }

        @Override
        public Document section(String title) {
            verifier.verify(mock).section(title);
            return this;
        }

        @Override
        public Document subSection(String title) {
            verifier.verify(mock).subSection(title);
            return this;
        }

        @Override
        public Document paragraph(String text) {
            verifier.verify(mock).paragraph(text);
            return this;
        }

        @Override
        public Document codeBlock(String description, String code) {
            verifier.verify(mock).codeBlock(description, code);
            return this;
        }

        @Override
        public Document table(String description, String table) {
            verifier.verify(mock).table(description, table);
            return this;
        }

        @Override
        public Document image(String description, String filePath) {
            verifier.verify(mock).image(description, filePath);
            return this;
        }
    }
}
