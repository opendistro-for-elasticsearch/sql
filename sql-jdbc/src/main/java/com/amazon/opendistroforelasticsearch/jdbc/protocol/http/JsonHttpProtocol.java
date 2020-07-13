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

package com.amazon.opendistroforelasticsearch.jdbc.protocol.http;

import com.amazon.opendistroforelasticsearch.jdbc.protocol.ClusterMetadata;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.ConnectionResponse;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.Protocol;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.QueryRequest;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.QueryResponse;
import com.amazon.opendistroforelasticsearch.jdbc.transport.http.HttpParam;
import com.amazon.opendistroforelasticsearch.jdbc.transport.http.HttpTransport;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.ResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class JsonHttpProtocol implements Protocol {

    // the value is based on the API endpoint the sql plugin sets up,
    // but this could be made configurable if required
    public static final String DEFAULT_SQL_CONTEXT_PATH = "/_opendistro/_sql";

    private static final Header acceptJson = new BasicHeader(HttpHeaders.ACCEPT, "application/json");
    private static final Header contentTypeJson = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    private static final HttpParam requestJdbcFormatParam = new HttpParam("format", "jdbc");
    protected static final Header[] defaultJsonHeaders = new Header[]{acceptJson, contentTypeJson};
    private static final Header[] defaultEmptyRequestBodyJsonHeaders = new Header[]{acceptJson};
    protected static final HttpParam[] defaultJdbcParams = new HttpParam[]{requestJdbcFormatParam};

    protected static final ObjectMapper mapper = new ObjectMapper();
    private String sqlContextPath;
    private HttpTransport transport;
    private JsonHttpResponseHandler jsonHttpResponseHandler;

    public JsonHttpProtocol(HttpTransport transport) {
        this(transport, DEFAULT_SQL_CONTEXT_PATH);
    }

    public JsonHttpProtocol(HttpTransport transport, String sqlContextPath) {
        this.transport = transport;
        this.sqlContextPath = sqlContextPath;
        this.jsonHttpResponseHandler = new JsonHttpResponseHandler(this);
    }

    public String getSqlContextPath() {
        return sqlContextPath;
    }

    public HttpTransport getTransport() {
        return this.transport;
    }

    public JsonHttpResponseHandler getJsonHttpResponseHandler() {
        return this.jsonHttpResponseHandler;
    }

    @Override
    public ConnectionResponse connect(int timeout) throws ResponseException, IOException {
        try (CloseableHttpResponse response = transport.doGet(
                "/",
                defaultEmptyRequestBodyJsonHeaders,
                null, timeout)) {

            return jsonHttpResponseHandler.handleResponse(response, this::processConnectionResponse);

        }
    }

    @Override
    public QueryResponse execute(QueryRequest request) throws ResponseException, IOException {
        try (CloseableHttpResponse response = transport.doPost(
                sqlContextPath,
                defaultJsonHeaders,
                defaultJdbcParams,
                buildQueryRequestBody(request), 0)) {

            return jsonHttpResponseHandler.handleResponse(response, this::processQueryResponse);

        }
    }

    private String buildQueryRequestBody(QueryRequest queryRequest) throws IOException {
        JsonQueryRequest jsonQueryRequest = new JsonQueryRequest(queryRequest);
        String requestBody = mapper.writeValueAsString(jsonQueryRequest);
        return requestBody;
    }

    @Override
    public void close() throws IOException {
        this.transport.close();
    }

    private JsonConnectionResponse processConnectionResponse(InputStream contentStream) throws IOException {
        ClusterMetadata clusterMetadata = mapper.readValue(contentStream, JsonClusterMetadata.class);
        return new JsonConnectionResponse(clusterMetadata);
    }

    private JsonQueryResponse processQueryResponse(InputStream contentStream) throws IOException {
        return mapper.readValue(contentStream, JsonQueryResponse.class);
    }
}
