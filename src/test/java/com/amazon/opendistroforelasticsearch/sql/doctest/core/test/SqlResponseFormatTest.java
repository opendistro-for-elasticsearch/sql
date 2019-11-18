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

import com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponse;
import org.apache.http.HttpEntity;
import org.elasticsearch.client.Response;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.NO_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.ORIGINAL_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.PRETTY_JSON_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.TABLE_RESPONSE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link SqlResponse}
 */
public class SqlResponseFormatTest {

    private final String expected =
        "{" +
        "\"schema\":[{\"name\":\"firstname\",\"type\":\"text\"}]," +
        "\"datarows\":[[\"John\"]]," +
        "\"total\":10," +
        "\"size\":1," +
        "\"status\":200" +
        "}";

    private SqlResponse sqlResponse;

    @Before
    public void setUp() throws IOException {
        Response response = mock(Response.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(expected.getBytes()));
        sqlResponse = new SqlResponse(response);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noResponseFormatShouldNotBeUsedForFormat() {
        NO_RESPONSE.format(sqlResponse);
    }

    @Test
    public void originalFormatShouldReturnResponseAsItIs() {
        assertThat(ORIGINAL_RESPONSE.format(sqlResponse), is(expected + "\n"));
    }

    @Test
    public void prettyJsonFormatShouldReturnResponseInJson() {
        assertThat(
            PRETTY_JSON_RESPONSE.format(sqlResponse),
            is(
                "{\n" +
                "  \"schema\" : [\n" +
                "    {\n" +
                "      \"name\" : \"firstname\",\n" +
                "      \"type\" : \"text\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"datarows\" : [\n" +
                "    [\n" +
                "      \"John\"\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"total\" : 10,\n" +
                "  \"size\" : 1,\n" +
                "  \"status\" : 200\n" +
                "}"
            )
        );
    }

    @Test
    public void tableFormatShouldReturnResponseInDataTable() {
        assertThat(
            TABLE_RESPONSE.format(sqlResponse),
            is(
                "+----------------+\n" +
                "|firstname (text)|\n" +
                "+================+\n" +
                "|            John|\n" +
                "+----------------+\n"
            )
        );
    }

}
