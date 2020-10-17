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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings;
import com.amazon.opendistroforelasticsearch.sql.legacy.request.PreparedStatementRequest;
import com.amazon.opendistroforelasticsearch.sql.legacy.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.legacy.request.SqlRequestFactory;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.rest.RestRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SqlRequestFactoryTest {

    @Mock
    private RestRequest restRequest;

    @Before
    public void setup() {
        SqlSettings settings = spy(new SqlSettings());
        // Force return empty list to avoid ClusterSettings be invoked which is a final class and hard to mock.
        // In this case, default value in Setting will be returned all the time.
        doReturn(emptyList()).when(settings).getSettings();
        LocalClusterState.state().setSqlSettings(settings);
    }

    @Ignore("RestRequest is a final method, and Mockito 1.x cannot mock it." +
            "Ignore this test case till we can upgrade to Mockito 2.x")
    @Test
    public void testGenerateSqlRequest_fromUrlParams() {
        String sql = "select * from table";
        Mockito.when(restRequest.method()).thenReturn(RestRequest.Method.GET);
        Mockito.when(restRequest.param("sql")).thenReturn(sql);

        SqlRequest sqlRequest = SqlRequestFactory.getSqlRequest(restRequest);

        Assert.assertFalse(sqlRequest instanceof PreparedStatementRequest);
        Assert.assertEquals(sql, sqlRequest.getSql());
    }

    @Test
    public void testGenerateSqlRequest_sqlRequestFromPayload() {
        String payload = "{ \"query\": \"select * from my_table\" }";

        Mockito.when(this.restRequest.content()).thenReturn(new BytesArray(payload));
        Mockito.when(this.restRequest.method()).thenReturn(RestRequest.Method.POST);

        SqlRequest sqlRequest = SqlRequestFactory.getSqlRequest(this.restRequest);
        Assert.assertFalse(sqlRequest instanceof PreparedStatementRequest);
        Assert.assertEquals("select * from my_table", sqlRequest.getSql());
    }

    @Test
    public void testGenerateSqlRequest_preparedStatementFromPayload() {
        String payload = "{\n" +
                "  \"query\": \"select * from my_table where int_param = ? and double_param = ? and string_param = ? and date_param = ? and null_param = ?\",\n" +
                "  \"parameters\": [\n" +
                "    {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"value\": 1\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"double\",\n" +
                "      \"value\": \"2.0\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"string\",\n" +
                "      \"value\": \"string_value\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"date\",\n" +
                "      \"value\": \"2000-01-01\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"null\",\n" +
                "      \"value\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Mockito.when(this.restRequest.content()).thenReturn(new BytesArray(payload));
        Mockito.when(this.restRequest.method()).thenReturn(RestRequest.Method.POST);

        SqlRequest sqlRequest = SqlRequestFactory.getSqlRequest(this.restRequest);

        Assert.assertTrue(sqlRequest instanceof  PreparedStatementRequest);
        PreparedStatementRequest preparedStatementRequest = (PreparedStatementRequest) sqlRequest;
        Assert.assertEquals("select * from my_table where int_param = ? and double_param = ? and string_param = ? and date_param = ? and null_param = ?", preparedStatementRequest.getPreparedStatement());
        Assert.assertEquals("select * from my_table where int_param = 1 and double_param = 2.0 and string_param = 'string_value' and date_param = '2000-01-01' and null_param = null", preparedStatementRequest.getSql());
        Assert.assertEquals(5, preparedStatementRequest.getParameters().size());
        Assert.assertTrue(preparedStatementRequest.getParameters().get(0).getValue() instanceof Long);
        Assert.assertTrue(preparedStatementRequest.getParameters().get(1).getValue() instanceof Double);
        Assert.assertTrue(preparedStatementRequest.getParameters().get(2) instanceof PreparedStatementRequest.StringParameter);
        Assert.assertTrue(preparedStatementRequest.getParameters().get(3) instanceof PreparedStatementRequest.StringParameter);
        Assert.assertTrue(preparedStatementRequest.getParameters().get(4) instanceof PreparedStatementRequest.NullParameter);
    }

    @Test
    public void testGenerateSqlRequest_prearedStatementFromPayload2() {
        // type not covered in above test case
        String payload = "{\n" +
                "  \"query\": \"select * from my_table where long_param = ? and float_param = ? and keyword_param = ? and boolean_param = ? and byte_param = ?\",\n" +
                "  \"parameters\": [\n" +
                "    {\n" +
                "      \"type\": \"long\",\n" +
                "      \"value\": 1\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"float\",\n" +
                "      \"value\": \"2.0\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"keyword\",\n" +
                "      \"value\": \"string_value\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"boolean\",\n" +
                "      \"value\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"byte\",\n" +
                "      \"value\": 91\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Mockito.when(this.restRequest.content()).thenReturn(new BytesArray(payload));
        Mockito.when(this.restRequest.method()).thenReturn(RestRequest.Method.POST);
        SqlRequest sqlRequest = SqlRequestFactory.getSqlRequest(this.restRequest);

        Assert.assertTrue(sqlRequest instanceof  PreparedStatementRequest);
        PreparedStatementRequest preparedStatementRequest = (PreparedStatementRequest) sqlRequest;
        Assert.assertEquals(5, preparedStatementRequest.getParameters().size());
        Assert.assertTrue(preparedStatementRequest.getParameters().get(0).getValue() instanceof Long);
        Assert.assertTrue(preparedStatementRequest.getParameters().get(1).getValue() instanceof Double);
        Assert.assertTrue(preparedStatementRequest.getParameters().get(2) instanceof PreparedStatementRequest.StringParameter);
        System.out.println(preparedStatementRequest.getParameters().get(3));
        Assert.assertTrue(preparedStatementRequest.getParameters().get(3).getValue() instanceof Boolean);
        Assert.assertTrue(preparedStatementRequest.getParameters().get(4).getValue() instanceof Long);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateSqlRequest_unsupportedHttpMethod() {
        Mockito.when(this.restRequest.method()).thenReturn(RestRequest.Method.PUT);
        SqlRequest sqlRequest = SqlRequestFactory.getSqlRequest(this.restRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateSqlRequest_invalidJson() {
        String payload = "{\n" +
                "  \"query\": \"select * from my_table where param1 = ?\",\n";
        Mockito.when(this.restRequest.content()).thenReturn(new BytesArray(payload));
        Mockito.when(this.restRequest.method()).thenReturn(RestRequest.Method.POST);

        SqlRequest sqlRequest = SqlRequestFactory.getSqlRequest(this.restRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateSqlRequest_unsupportedType() {
        String payload = "{\n" +
                "  \"query\": \"select * from my_table where param1 = ?\",\n" +
                "  \"parameters\": [\n" +
                "    {\n" +
                "      \"type\": \"unsupported_type\",\n" +
                "      \"value\": 1\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"string\",\n" +
                "      \"value\": \"string_value\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Mockito.when(this.restRequest.content()).thenReturn(new BytesArray(payload));
        Mockito.when(this.restRequest.method()).thenReturn(RestRequest.Method.POST);

        SqlRequest sqlRequest = SqlRequestFactory.getSqlRequest(this.restRequest);
    }
}
