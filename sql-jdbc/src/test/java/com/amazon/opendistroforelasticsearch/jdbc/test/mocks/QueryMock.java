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

package com.amazon.opendistroforelasticsearch.jdbc.test.mocks;

import com.amazon.opendistroforelasticsearch.jdbc.ElasticsearchConnection;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JsonHttpProtocol;
import com.amazon.opendistroforelasticsearch.jdbc.test.TestResources;
import com.amazon.opendistroforelasticsearch.jdbc.types.ElasticsearchType;
import com.github.tomakehurst.wiremock.WireMockServer;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public abstract class QueryMock {

    public abstract String getSql();

    public abstract String getResponseResourcePath();

    public MockResultSet getMockResultSet() {
        // overridden in QueryMocks that intend to vend
        // a MockResultSet
        return null;
    }

    public void setupMockServerStub(final WireMockServer mockServer)
            throws java.io.IOException {
        setupMockServerStub(mockServer, "/", JsonHttpProtocol.DEFAULT_SQL_CONTEXT_PATH+"?format=jdbc");
    }

    public void setupMockServerStub(final WireMockServer mockServer, final String connectionUrl, final String queryUrl)
            throws java.io.IOException {

        setupStubForConnect(mockServer, connectionUrl);

        // query response stub
        mockServer.stubFor(post(urlEqualTo(queryUrl))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.query", equalTo(getSql())))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResponseBody())));
    }

    protected void setupStubForConnect(final WireMockServer mockServer, final String contextPath) {
        // get Connection stub
        mockServer.stubFor(get(urlEqualTo(contextPath))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockES.INSTANCE.getConnectionResponse())));
    }

    public String getResponseBody() throws IOException {
        return TestResources.readResourceAsString(getResponseResourcePath());
    }

    public void assertConnectionResponse(ElasticsearchConnection esConnection) throws SQLException {
        MockES.INSTANCE.assertMockESConnectionResponse(esConnection);
    }

    public static class NycTaxisQueryMock extends QueryMock {
        @Override
        public String getSql() {
            return "select pickup_datetime, trip_type, passenger_count, " +
                    "fare_amount, extra, vendor_id from nyc_taxis LIMIT 5";
        }

        @Override
        public String getResponseResourcePath() {
            return "mock/protocol/json/queryresponse_nyctaxis.json";
        }

        @Override
        public MockResultSet getMockResultSet() {
            MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                    .column("pickup_datetime", ElasticsearchType.DATE)
                    .column("trip_type", ElasticsearchType.KEYWORD)
                    .column("passenger_count", ElasticsearchType.INTEGER)
                    .column("fare_amount", ElasticsearchType.SCALED_FLOAT)
                    .column("extra", ElasticsearchType.SCALED_FLOAT)
                    .column("vendor_id", ElasticsearchType.KEYWORD)
                    .build();

            MockResultSetRows mockResultSetRows = MockResultSetRows.builder()
                    .row()
                    .column(Timestamp.valueOf("2015-01-01 00:34:42"))
                    .column("1")
                    .column(1)
                    .column(5D)
                    .column(0.5D)
                    .column("2")
                    .row()
                    .column(Timestamp.valueOf("2015-01-01 00:34:46"))
                    .column("1")
                    .column(1)
                    .column(12D)
                    .column(0.5D)
                    .column("2")
                    .row()
                    .column(Timestamp.valueOf("2015-01-01 00:34:44"))
                    .column("1")
                    .column(1)
                    .column(5D)
                    .column(0.5D)
                    .column("1")
                    .row()
                    .column(Timestamp.valueOf("2015-01-01 00:34:48"))
                    .column("1")
                    .column(1)
                    .column(5D)
                    .column(0.5D)
                    .column("2")
                    .row()
                    .column(Timestamp.valueOf("2015-01-01 00:34:53"))
                    .column("1")
                    .column(1)
                    .column(24.5D)
                    .column(0.5D)
                    .column("2")
                    .build();

            return new MockResultSet(mockResultSetMetaData, mockResultSetRows);
        }
    }

    public static class NycTaxisQueryWithAliasMock extends QueryMock {
        @Override
        public String getSql() {
            return "select pickup_datetime as pdt, trip_type, passenger_count as pc, " +
                    "fare_amount, extra, vendor_id from nyc_taxis LIMIT 5";
        }

        @Override
        public String getResponseResourcePath() {
            return "mock/protocol/json/queryresponse_with_alias_nyctaxis.json";
        }

        @Override
        public MockResultSet getMockResultSet() {
            MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                    .column("pickup_datetime", ElasticsearchType.DATE)
                    .setColumnLabel("pdt")
                    .column("trip_type", ElasticsearchType.KEYWORD)
                    .column("passenger_count", ElasticsearchType.INTEGER)
                    .setColumnLabel("pc")
                    .column("fare_amount", ElasticsearchType.SCALED_FLOAT)
                    .column("extra", ElasticsearchType.SCALED_FLOAT)
                    .column("vendor_id", ElasticsearchType.KEYWORD)
                    .build();

            MockResultSetRows mockResultSetRows = MockResultSetRows.builder()
                    .row()
                    .column(Timestamp.valueOf("2015-01-01 00:34:42"))
                    .column("1")
                    .column(1)
                    .column(5D)
                    .column(0.5D)
                    .column("2")
                    .row()
                    .column(Timestamp.valueOf("2015-01-01 00:34:46"))
                    .column("1")
                    .column(1)
                    .column(12D)
                    .column(0.5D)
                    .column("2")
                    .row()
                    .column(Timestamp.valueOf("2015-01-01 00:34:44"))
                    .column("1")
                    .column(1)
                    .column(5D)
                    .column(0.5D)
                    .column("1")
                    .row()
                    .column(Timestamp.valueOf("2015-01-01 00:34:48"))
                    .column("1")
                    .column(1)
                    .column(5D)
                    .column(0.5D)
                    .column("2")
                    .row()
                    .column(Timestamp.valueOf("2015-01-01 00:34:53"))
                    .column("1")
                    .column(1)
                    .column(24.5D)
                    .column(0.5D)
                    .column("2")
                    .build();

            return new MockResultSet(mockResultSetMetaData, mockResultSetRows);
        }
    }

    public static class SoNestedQueryMock extends QueryMock {
        @Override
        public String getSql() {
            return "select user, title, qid, creation_date from sonested LIMIT 5";
        }

        @Override
        public String getResponseResourcePath() {
            return "mock/protocol/json/queryresponse_sonested.json";
        }
    }

    public static class NycTaxisQueryInternalErrorMock extends NycTaxisQueryMock {

        @Override
        public String getResponseResourcePath() {
            return "mock/protocol/json/queryresponse_internal_server_error.json";
        }
    }

    public static class NullableFieldsQueryMock extends QueryMock {
        @Override
        public String getSql() {
            return "select * from nullablefields";
        }

        @Override
        public String getResponseResourcePath() {
            return "mock/protocol/json/queryresponse_nullablefields.json";
        }
    }
}
