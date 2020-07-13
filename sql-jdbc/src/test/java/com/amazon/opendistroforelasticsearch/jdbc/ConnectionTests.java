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

package com.amazon.opendistroforelasticsearch.jdbc;

import com.amazon.opendistroforelasticsearch.jdbc.config.AuthConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.ConnectionPropertyException;
import com.amazon.opendistroforelasticsearch.jdbc.config.PasswordConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.RegionConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.RequestCompressionConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.UserConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JsonHttpProtocol;
import com.amazon.opendistroforelasticsearch.jdbc.test.PerTestWireMockServerExtension;
import com.amazon.opendistroforelasticsearch.jdbc.test.WireMockServerHelpers;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.MockES;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.QueryMock;
import com.amazonaws.auth.SdkClock;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PerTestWireMockServerExtension.class)
class ConnectionTests implements WireMockServerHelpers {

    @Test
    void testGetConnection(final WireMockServer mockServer) throws SQLException {
        mockServer.stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockES.INSTANCE.getConnectionResponse())));

        Driver driver = new Driver();
        Connection con = Assertions.assertDoesNotThrow(
                () -> driver.connect(getBaseURLForMockServer(mockServer), (Properties) null));

        assertConnectionOpen(con);
        MockES.INSTANCE.assertMockESConnectionResponse((ElasticsearchConnection) con);
        con.close();
    }

    @Test
    void testConnectWithBasicAuth(final WireMockServer mockServer) throws ConnectionPropertyException, SQLException {
        // HTTP Client Basic Auth is not pre-emptive, set up an Auth Challenge
        mockServer.stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Basic realm=\"Auth Realm\"")
                ));

        // Response if request's basic auth matches expectation
        mockServer.stubFor(get(urlEqualTo("/"))
                .withBasicAuth("user-name", "password-$#@!*%^123")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockES.INSTANCE.getConnectionResponse())));

        Properties props = new Properties();
        props.put(AuthConnectionProperty.KEY, "basic");
        props.put(UserConnectionProperty.KEY, "user-name");
        props.put(PasswordConnectionProperty.KEY, "password-$#@!*%^123");

        Connection con = Assertions.assertDoesNotThrow(() -> new Driver().connect(getBaseURLForMockServer(mockServer), props));

        mockServer.verify(2, getRequestedFor(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/json")));

        MockES.INSTANCE.assertMockESConnectionResponse((ElasticsearchConnection) con);
        con.close();
    }

    @Test
    void testConnectDefaultAuthWithUsername(final WireMockServer mockServer) throws SQLException {
        // In the absence of explicit auth type, Basic is used if a username/password
        // is specified

        // HTTP Client Basic Auth is not pre-emptive, set up an Auth Challenge
        mockServer.stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("WWW-Authenticate", "Basic realm=\"Auth Realm\"")
                ));

        // Response if request's basic auth matches expectation
        mockServer.stubFor(get(urlEqualTo("/"))
                .withBasicAuth("user-name", "password-$#@!*%^123")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockES.INSTANCE.getConnectionResponse())));

        Properties props = new Properties();
        props.put(UserConnectionProperty.KEY, "user-name");
        props.put(PasswordConnectionProperty.KEY, "password-$#@!*%^123");

        Connection con = Assertions.assertDoesNotThrow(() -> new Driver().connect(getBaseURLForMockServer(mockServer), props));

        mockServer.verify(2, getRequestedFor(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/json")));

        MockES.INSTANCE.assertMockESConnectionResponse((ElasticsearchConnection) con);
        con.close();
    }

    @Test
    void testConnectWithRequestCompression(final WireMockServer mockServer) throws SQLException {
        // Respond only if request mentions it accepts gzip
        // i.e. expected behavior when requestCompression is set
        mockServer.stubFor(
                get(urlEqualTo("/"))
                        .withHeader("Accept-Encoding", equalTo("gzip,deflate"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(MockES.INSTANCE.getConnectionResponse())));

        Properties props = new Properties();
        props.setProperty(RequestCompressionConnectionProperty.KEY, "true");

        // WireMockServer returns a gzip response by default
        // if Accept-Enconding: gzip,deflate is present in the request
        Connection con = Assertions.assertDoesNotThrow(() -> new Driver().connect(getBaseURLForMockServer(mockServer), props));
        MockES.INSTANCE.assertMockESConnectionResponse((ElasticsearchConnection) con);
        con.close();
    }

    @Test
    void testConnectWithoutRequestCompression(final WireMockServer mockServer) throws ConnectionPropertyException, SQLException {
        // Respond successfully only if request does not mention it accepts gzip
        // i.e. expected behavior when requestCompression is not set
        mockServer.stubFor(
                get(urlEqualTo("/"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(MockES.INSTANCE.getConnectionResponse())));

        mockServer.stubFor(
                get(urlEqualTo("/"))
                        .withHeader("Accept-Encoding", equalTo("gzip,deflate"))
                        .willReturn(aResponse()
                                .withStatus(400)
                                .withStatusMessage("Request seeks gzip response")));

        // explicitly disable requestCompression
        Properties props = new Properties();
        props.setProperty(RequestCompressionConnectionProperty.KEY, "false");

        // WireMockServer returns a gzip response by default
        // if Accept-Enconding: gzip,deflate is present in the request
        Connection con = Assertions.assertDoesNotThrow(() -> new Driver().connect(getBaseURLForMockServer(mockServer), props));
        MockES.INSTANCE.assertMockESConnectionResponse((ElasticsearchConnection) con);
        con.close();
    }

    @Test
    void testConnectWithDefaultRequestCompression(final WireMockServer mockServer) throws ConnectionPropertyException, SQLException {
        // Respond successfully only if request does not mention it accepts gzip
        // i.e. expected behavior when requestCompression is not set
        mockServer.stubFor(
                get(urlEqualTo("/"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(MockES.INSTANCE.getConnectionResponse())));

        // return HTTP 400 if request contains Accept-Encoding: gzip
        mockServer.stubFor(
                get(urlEqualTo("/"))
                        .withHeader("Accept-Encoding", equalTo("gzip,deflate"))
                        .willReturn(aResponse()
                                .withStatus(400)
                                .withStatusMessage("Request seeks gzip response by default")));

        // empty Properties - expect default behavior is to not set requestCompression
        Properties props = new Properties();

        Connection con = Assertions.assertDoesNotThrow(() -> new Driver().connect(getBaseURLForMockServer(mockServer), props));
        MockES.INSTANCE.assertMockESConnectionResponse((ElasticsearchConnection) con);
        con.close();
    }

    // TODO - find a way to test this differently?
    @Disabled("currently this does not work because Host header value " +
            "is included in signature which is of 'localhost:port' form " +
            "and since the port value differs every run of the test, the " +
            "signature generated is different from the canned response " +
            "we're testing against")
    @Test
    void testConnectWithAwsSigV4Auth(final WireMockServer mockServer) throws SQLException {
        mockServer.stubFor(get(urlEqualTo("/"))
                .withHeader("Authorization",
                        equalTo("AWS4-HMAC-SHA256 " +
                                "Credential=AKIAJUXF4LQLB55YQ73A/20181119/us-east-1/es/aws4_request, " +
                                "SignedHeaders=host;user-agent;x-amz-date, " +
                                "Signature=80088eaaa2e7766ccee12014a5ab80d323635347157ea29935e990d34bcbff12"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockES.INSTANCE.getConnectionResponse())));


        Properties props = new Properties();
        props.setProperty(AuthConnectionProperty.KEY, "aws_sigv4");
        props.setProperty(RegionConnectionProperty.KEY, "us-east-1");

        // Ensure AWS Signing uses same date/time as was used to generate
        // the signatures in this test case
        SdkClock.Instance.set(new SdkClock.MockClock(1542653839129L));

        Connection con = Assertions.assertDoesNotThrow(() ->
                new Driver().connect(getBaseURLForMockServer(mockServer), props));

        MockES.INSTANCE.assertMockESConnectionResponse((ElasticsearchConnection) con);
        con.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/context/path",
            "/context/path/",
            "//context/path",
            "//context/path/",
    })
    void testConnectionWithContextPath(final String userContextPath, final WireMockServer mockServer)
            throws SQLException, IOException {
        QueryMock.NycTaxisQueryMock queryMock = new QueryMock.NycTaxisQueryMock();
        queryMock.setupMockServerStub(mockServer, "/context/path/",
                "/context/path"+ JsonHttpProtocol.DEFAULT_SQL_CONTEXT_PATH+"?format=jdbc");

        Driver driver = new Driver();
        Connection con = Assertions.assertDoesNotThrow(
                () -> driver.connect(getURLForMockServerWithContext(mockServer, userContextPath), (Properties) null));

        assertConnectionOpen(con);
        queryMock.assertConnectionResponse((ElasticsearchConnection) con);

        Statement st = con.createStatement();

        Assertions.assertDoesNotThrow(() -> st.executeQuery(queryMock.getSql()));

        con.close();
    }

    private void assertConnectionOpen(final Connection con) {
        boolean closed = assertDoesNotThrow(con::isClosed);
        assertTrue(!closed, "Connection is closed");
    }

}
