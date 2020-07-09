/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.protocol;

import com.amazon.opendistroforelasticsearch.jdbc.config.ConnectionConfig;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.MalformedResponseException;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.ResponseException;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.HttpException;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JsonHttpProtocol;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JsonHttpProtocolFactory;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JsonQueryRequest;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JsonQueryResponse;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.MockCloseableHttpResponseBuilder;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.MockES;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.MockHttpTransport;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.QueryMock;
import com.amazon.opendistroforelasticsearch.jdbc.transport.TransportException;
import com.amazon.opendistroforelasticsearch.jdbc.transport.http.HttpTransport;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;


public class JsonHttpProtocolTests {

    @Test
    void testConnect() throws IOException {
        CloseableHttpResponse mockResponse = new MockCloseableHttpResponseBuilder()
                .withHttpReturnCode(200)
                .withResponseBody(MockES.INSTANCE.getConnectionResponse())
                .build();

        HttpTransport mockTransport = mock(HttpTransport.class);

        ArgumentCaptor<Header[]> captor = ArgumentCaptor.forClass(Header[].class);
        when(mockTransport.doGet(eq("/"), captor.capture(), isNull(), anyInt()))
                .thenReturn(mockResponse);

        JsonHttpProtocol protocol = JsonHttpProtocolFactory.INSTANCE.getProtocol(
                mock(ConnectionConfig.class), mockTransport);
        ConnectionResponse response = assertDoesNotThrow(() -> protocol.connect(0));

        verify(mockTransport, times(1)).doGet(eq("/"), captor.capture(), isNull(), anyInt());

        assertNotNull(captor.getAllValues(), "No headers captured in request");
        Header[] headers = captor.getAllValues().get(0);

        assertNotNull(headers, "No headers found in request");

        boolean expectedHeadersPresent = Arrays.stream(headers).anyMatch(
                (header) -> "Accept".equalsIgnoreCase(header.getName()) &&
                        "application/json".equals(header.getValue()));

        assertTrue(expectedHeadersPresent, "Expected headers not found in request. Headers received: "
                + Arrays.toString(headers));

        assertNotNull(response.getClusterMetadata());
        assertEquals("c1", response.getClusterMetadata().getClusterName());
        assertEquals("JpZSfOJiSLOntGp0zljpVQ", response.getClusterMetadata().getClusterUUID());
        assertNotNull(response.getClusterMetadata().getVersion());
        assertEquals("6.3.1", response.getClusterMetadata().getVersion().getFullVersion());
        assertEquals(6, response.getClusterMetadata().getVersion().getMajor());
        assertEquals(3, response.getClusterMetadata().getVersion().getMinor());
        assertEquals(1, response.getClusterMetadata().getVersion().getRevision());
    }


    @Test
    void testConnectError() throws IOException {
        HttpTransport mockTransport = mock(HttpTransport.class);

        CloseableHttpResponse mockResponse = new MockCloseableHttpResponseBuilder()
                .withHttpReturnCode(404)
                .build();

        MockHttpTransport.setupConnectionResponse(mockTransport, mockResponse);

        JsonHttpProtocol protocol = new JsonHttpProtocol(mockTransport);

        HttpException ex = assertThrows(HttpException.class, () -> protocol.connect(0));
        assertEquals(404, ex.getStatusCode());
    }


    @Test
    void testConnectForbiddenError() throws IOException, TransportException {
        HttpTransport mockTransport = mock(HttpTransport.class);
        String responseBody = " {\"Message\":\"User: arn:aws:iam::1010001001000:user/UserId " +
                "is not authorized to perform: es:ESHttpGet\"}";
        CloseableHttpResponse mockResponse = new MockCloseableHttpResponseBuilder()
                .withHttpReturnCode(403)
                .withContentType("application/json")
                .withResponseBody(responseBody)
                .build();

        MockHttpTransport.setupConnectionResponse(mockTransport, mockResponse);

        JsonHttpProtocol protocol = new JsonHttpProtocol(mockTransport);

        HttpException httpException = assertThrows(HttpException.class, () -> protocol.connect(0));
        assertEquals(403, httpException.getStatusCode());
        assertNotNull(httpException.getLocalizedMessage(), "HttpException message is null");
        assertTrue(httpException.getLocalizedMessage().contains(responseBody),
                "HttpException message does not contain response received");
    }

    @Test
    void testConnectMalformedResponse() throws IOException {
        HttpTransport mockTransport = mock(HttpTransport.class);

        CloseableHttpResponse mockResponse = new MockCloseableHttpResponseBuilder()
                .withHttpReturnCode(200)
                .withResponseBody("")
                .build();

        MockHttpTransport.setupConnectionResponse(mockTransport, mockResponse);

        JsonHttpProtocol protocol = JsonHttpProtocolFactory.INSTANCE.getProtocol(
                mock(ConnectionConfig.class), mockTransport);

        assertThrows(MalformedResponseException.class, () -> protocol.connect(0));
    }

    @Test
    void testQueryResponseNycTaxis() throws IOException {
        QueryMock queryMock = new QueryMock.NycTaxisQueryMock();

        HttpTransport mockTransport = mock(HttpTransport.class);

        CloseableHttpResponse mockResponse = new MockCloseableHttpResponseBuilder()
                .withHttpReturnCode(200)
                .withResponseBody(queryMock.getResponseBody())
                .build();

        JsonHttpProtocol protocol = JsonHttpProtocolFactory.INSTANCE.getProtocol(
                mock(ConnectionConfig.class), mockTransport);

        MockHttpTransport.setupQueryResponse(protocol, mockTransport, mockResponse);

        QueryResponse response = assertDoesNotThrow(() -> protocol.execute(buildJsonQueryRequest(queryMock)));

        Assertions.assertEquals(
                buildJsonQueryResponse(
                        toSchema(
                                schemaEntry("pickup_datetime", "date"),
                                schemaEntry("trip_type", "keyword"),
                                schemaEntry("passenger_count", "integer"),
                                schemaEntry("fare_amount", "scaled_float"),
                                schemaEntry("extra", "scaled_float"),
                                schemaEntry("vendor_id", "keyword")
                        ),
                        toDatarows(
                                toDatarow("2015-01-01 00:34:42", "1", 1, 5, 0.5, "2"),
                                toDatarow("2015-01-01 00:34:46", "1", 1, 12, 0.5, "2"),
                                toDatarow("2015-01-01 00:34:44", "1", 1, 5, 0.5, "1"),
                                toDatarow("2015-01-01 00:34:48", "1", 1, 5, 0.5, "2"),
                                toDatarow("2015-01-01 00:34:53", "1", 1, 24.5, 0.5, "2")
                        ),
                        5, 1000, 200),
                response);
    }

    @Test
    void testQueryResponseWithAliasesNycTaxis() throws IOException {
        QueryMock queryMock = new QueryMock.NycTaxisQueryWithAliasMock();

        HttpTransport mockTransport = mock(HttpTransport.class);

        CloseableHttpResponse mockResponse = new MockCloseableHttpResponseBuilder()
                .withHttpReturnCode(200)
                .withResponseBody(queryMock.getResponseBody())
                .build();

        JsonHttpProtocol protocol = JsonHttpProtocolFactory.INSTANCE.getProtocol(
                mock(ConnectionConfig.class), mockTransport);

        MockHttpTransport.setupQueryResponse(protocol, mockTransport, mockResponse);

        QueryResponse response = assertDoesNotThrow(() -> protocol.execute(buildJsonQueryRequest(queryMock)));

        Assertions.assertEquals(
                buildJsonQueryResponse(
                        toSchema(
                                schemaEntry("pickup_datetime", "date", "pdt"),
                                schemaEntry("trip_type", "keyword"),
                                schemaEntry("passenger_count", "integer", "pc"),
                                schemaEntry("fare_amount", "scaled_float"),
                                schemaEntry("extra", "scaled_float"),
                                schemaEntry("vendor_id", "keyword")
                        ),
                        toDatarows(
                                toDatarow("2015-01-01 00:34:42", "1", 1, 5, 0.5, "2"),
                                toDatarow("2015-01-01 00:34:46", "1", 1, 12, 0.5, "2"),
                                toDatarow("2015-01-01 00:34:44", "1", 1, 5, 0.5, "1"),
                                toDatarow("2015-01-01 00:34:48", "1", 1, 5, 0.5, "2"),
                                toDatarow("2015-01-01 00:34:53", "1", 1, 24.5, 0.5, "2")
                        ),
                        5, 1000, 200),
                response);
    }

    @Test
    void testQueryResponseSoNested() throws IOException {
        QueryMock queryMock = new QueryMock.SoNestedQueryMock();

        HttpTransport mockTransport = mock(HttpTransport.class);

        CloseableHttpResponse mockResponse = new MockCloseableHttpResponseBuilder()
                .withHttpReturnCode(200)
                .withResponseBody(queryMock.getResponseBody())
                .build();

        JsonHttpProtocol protocol = JsonHttpProtocolFactory.INSTANCE.getProtocol(
                mock(ConnectionConfig.class), mockTransport);

        MockHttpTransport.setupQueryResponse(protocol, mockTransport, mockResponse);

        QueryResponse response = assertDoesNotThrow(() -> protocol.execute(buildJsonQueryRequest(queryMock)));

        Assertions.assertEquals(
                buildJsonQueryResponse(
                        toSchema(
                                schemaEntry("user", "keyword"),
                                schemaEntry("title", "text"),
                                schemaEntry("qid", "keyword"),
                                schemaEntry("creationDate", "date")
                        ),
                        toDatarows(
                                toDatarow("Jash",
                                        "Display Progress Bar at the Time of Processing",
                                        "1000000",
                                        "2009-06-16T07:28:42.770"),
                                toDatarow("Michael Ecklund (804104)",
                                        "PHP Sort array by field?",
                                        "10000005",
                                        "2012-04-03T19:25:46.213"),
                                toDatarow("farley (1311218)",
                                        "Arrays in PHP seems to drop elements",
                                        "10000007",
                                        "2012-04-03T19:26:05.400"),
                                toDatarow("John Strickler (292614)",
                                        "RESTful servlet URLs - servlet-mapping in web.xml",
                                        "10000008",
                                        "2012-04-03T19:26:09.137"),
                                toDatarow("rahulm (123536)",
                                        "Descriptor conversion problem",
                                        "1000001",
                                        "2009-06-16T07:28:52.333")
                        ),
                        5, 20000, 200),
                response);
    }

    @Test
    void testQueryResponseInternalServerError() throws IOException {
        QueryMock queryMock = new QueryMock.NycTaxisQueryInternalErrorMock();

        HttpTransport mockTransport = mock(HttpTransport.class);

        CloseableHttpResponse mockResponse = new MockCloseableHttpResponseBuilder()
                .withHttpReturnCode(200)
                .withResponseBody(queryMock.getResponseBody())
                .build();

        JsonHttpProtocol protocol = JsonHttpProtocolFactory.INSTANCE.getProtocol(
                mock(ConnectionConfig.class), mockTransport);

        MockHttpTransport.setupQueryResponse(protocol, mockTransport, mockResponse);

        QueryResponse response = assertDoesNotThrow(() -> protocol.execute(buildJsonQueryRequest(queryMock)));

        JsonQueryResponse.JsonRequestError error = new JsonQueryResponse.JsonRequestError();
        error.setReason("error reason");
        error.setType("java.lang.NullPointerException");
        error.setDetails(
                "java.lang.NullPointerException\n\t" +
                        "at org.elasticsearch.plugin.nlpcn.Schema.getTypeFromMetaData(Schema.java:156)\n\t" +
                        "at org.elasticsearch.plugin.nlpcn.Schema.populateColumns(Schema.java:146)\n\t" +
                        "at java.base/java.lang.Thread.run(Thread.java:844)\n"
        );

        assertEquals(buildJsonQueryResponse(null, null, 0, 0, 500, error), response);
    }

    @Test
    void testQueryResponseSqlPluginPossiblyMissing() throws IOException {
        QueryMock queryMock = new QueryMock.NycTaxisQueryInternalErrorMock();

        HttpTransport mockTransport = mock(HttpTransport.class);

        String responseBody = "{\"error\":\"Incorrect HTTP method for uri [/_sql?format=jdbc] " +
                "and method [POST], allowed: [PUT, DELETE, GET, HEAD]\",\"status\":405}";

        CloseableHttpResponse mockResponse = new MockCloseableHttpResponseBuilder()
                .withHttpReturnCode(405)
                .withContentType("application/json; charset=UTF-8")
                .withResponseBody(responseBody)
                .build();

        JsonHttpProtocol protocol = JsonHttpProtocolFactory.INSTANCE.getProtocol(
                mock(ConnectionConfig.class), mockTransport);

        MockHttpTransport.setupQueryResponse(protocol, mockTransport, mockResponse);

        ResponseException responseException = assertThrows(ResponseException.class,
                () -> protocol.execute(buildJsonQueryRequest(queryMock)));

        assertNotNull(responseException.getMessage());
        assertTrue(responseException.getMessage().contains("Make sure the SQL plugin is installed"));

        Throwable cause = responseException.getCause();
        assertNotNull(cause, "Expected ResponseException cause to be non-null ");
        assertTrue(responseException.getCause() instanceof HttpException, () -> "ResponseException cause expected to " +
                "be of type " + HttpException.class + " but was: " + responseException.getCause().getClass());

        HttpException httpException = (HttpException) cause;
        assertEquals(405, httpException.getStatusCode());
        assertNotNull(httpException.getLocalizedMessage(), "HttpException message is null");
        assertTrue(httpException.getLocalizedMessage().contains(responseBody),
                "HttpException message does not contain response received");

    }


    private JsonQueryRequest buildJsonQueryRequest(QueryMock queryMock) {
        return buildJsonQueryRequest(queryMock.getSql());
    }

    private JsonQueryRequest buildJsonQueryRequest(String sql) {
        return new JsonQueryRequest(new JdbcQueryRequest(sql));
    }

    private JsonQueryResponse buildJsonQueryResponse(
            List<JsonQueryResponse.SchemaEntry> schema, List<List<Object>> datarows,
            int size, int total, int status) {
        return buildJsonQueryResponse(schema, datarows, size, total, status, null);
    }

    private JsonQueryResponse buildJsonQueryResponse(
            List<JsonQueryResponse.SchemaEntry> schema, List<List<Object>> datarows,
            int size, int total, int status, JsonQueryResponse.JsonRequestError error) {
        JsonQueryResponse response = new JsonQueryResponse();

        response.setSchema(schema);
        response.setDatarows(datarows);
        response.setSize(size);
        response.setTotal(total);
        response.setStatus(status);
        response.setError(error);

        return response;
    }

    private static List<JsonQueryResponse.SchemaEntry> toSchema(JsonQueryResponse.SchemaEntry... schemaEntries) {
        return Arrays.asList(schemaEntries);
    }

    private static JsonQueryResponse.SchemaEntry schemaEntry(String name, String type) {
        return schemaEntry(name, type, null);
    }

    private static JsonQueryResponse.SchemaEntry schemaEntry(String name, String type, String label) {
        return new JsonQueryResponse.SchemaEntry(name, type, label);
    }

    private static List<List<Object>> toDatarows(List<Object>... values) {
        return Arrays.asList(values);
    }

    private static List<Object> toDatarow(Object... values) {
        return Arrays.asList(values);
    }
}
