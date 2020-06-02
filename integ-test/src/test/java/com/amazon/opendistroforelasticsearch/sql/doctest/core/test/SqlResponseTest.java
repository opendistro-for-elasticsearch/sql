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
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link SqlResponse}
 */
public class SqlResponseTest {

    @Test
    public void responseBodyShouldRetainNewLine() throws IOException {
        Response response = mock(Response.class);
        HttpEntity entity = mock(HttpEntity.class);
        String expected = "123\nabc\n";
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(expected.getBytes()));

        SqlResponse sqlResponse = new SqlResponse(response);
        String actual = sqlResponse.body();
        assertThat(actual, is(expected));
    }

}
