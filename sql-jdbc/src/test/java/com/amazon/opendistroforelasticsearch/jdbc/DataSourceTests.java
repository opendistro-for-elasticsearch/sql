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

import com.amazon.opendistroforelasticsearch.jdbc.auth.AuthenticationType;
import com.amazon.opendistroforelasticsearch.jdbc.config.AuthConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.ConnectionConfig;
import com.amazon.opendistroforelasticsearch.jdbc.config.HostConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.LoginTimeoutConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.PasswordConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.PortConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.UserConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.test.PerTestWireMockServerExtension;
import com.amazon.opendistroforelasticsearch.jdbc.test.WireMockServerHelpers;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.QueryMock;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(PerTestWireMockServerExtension.class)
public class DataSourceTests implements WireMockServerHelpers {

    @Test
    void testDataSourceConfig() throws SQLException {
        ElasticsearchDataSource eds = new ElasticsearchDataSource();

        Properties props = new Properties();
        props.setProperty(HostConnectionProperty.KEY, "some-host");
        props.setProperty(PortConnectionProperty.KEY, "1100");
        props.setProperty(LoginTimeoutConnectionProperty.KEY, "100");
        eds.setProperties(props);

        ConnectionConfig config = eds.getConnectionConfig(null);

        assertEquals("some-host", config.getHost());
        assertEquals(1100, config.getPort());
        assertEquals(100, config.getLoginTimeout());
        assertNull(config.getUser());
        assertNull(config.getPassword());
        Assertions.assertEquals(AuthenticationType.NONE, config.getAuthenticationType());
        assertNull(config.getAwsCredentialsProvider());
    }

    @Test
    void testDataSourceConfigWithDefaults() throws SQLException {
        ElasticsearchDataSource eds = new ElasticsearchDataSource();

        Properties defaults = new Properties();
        defaults.setProperty(UserConnectionProperty.KEY, "default-user");
        defaults.setProperty(PasswordConnectionProperty.KEY, "default-pass");
        defaults.setProperty(AuthConnectionProperty.KEY, "basic");

        Properties props = new Properties(defaults);
        props.setProperty(HostConnectionProperty.KEY, "some-host");
        props.setProperty(PortConnectionProperty.KEY, "1100");
        props.setProperty(LoginTimeoutConnectionProperty.KEY, "100");

        eds.setProperties(props);

        ConnectionConfig config = eds.getConnectionConfig(null);

        assertEquals("some-host", config.getHost());
        assertEquals(1100, config.getPort());
        assertEquals(100, config.getLoginTimeout());
        assertEquals("default-user", config.getUser());
        assertEquals("default-pass", config.getPassword());
        assertEquals(AuthenticationType.BASIC, config.getAuthenticationType());
    }

    @Test
    void testDataSourceConfigUpdate() throws SQLException {
        ElasticsearchDataSource eds = new ElasticsearchDataSource();
        Properties props = new Properties();
        props.setProperty(HostConnectionProperty.KEY, "some-host");
        props.setProperty(PortConnectionProperty.KEY, "1100");
        props.setProperty(LoginTimeoutConnectionProperty.KEY, "100");
        eds.setProperties(props);

        props = new Properties();
        props.setProperty(HostConnectionProperty.KEY, "some-host-updated");
        props.setProperty(PortConnectionProperty.KEY, "2100");
        eds.setProperties(props);

        ConnectionConfig config = eds.getConnectionConfig(null);

        assertEquals("some-host-updated", config.getHost());
        assertEquals(2100, config.getPort());
        assertEquals(0, config.getLoginTimeout());
        assertNull(config.getUser());
        assertNull(config.getPassword());
    }

    @Test
    void testDataSourceConfigUpdateWithOverrides() throws SQLException {
        ElasticsearchDataSource eds = new ElasticsearchDataSource();
        Properties props = new Properties();
        props.setProperty(HostConnectionProperty.KEY, "some-host");
        props.setProperty(PortConnectionProperty.KEY, "2100");
        eds.setProperties(props);

        Map<String, Object> overrides = new HashMap<>();
        overrides.put(UserConnectionProperty.KEY, "override-user");
        overrides.put(PasswordConnectionProperty.KEY, "override-pass");
        ConnectionConfig config = eds.getConnectionConfig(overrides);

        assertEquals("some-host", config.getHost());
        assertEquals(2100, config.getPort());
        assertEquals(0, config.getLoginTimeout());
        assertEquals("override-user", config.getUser());
        assertEquals("override-pass", config.getPassword());
    }

    @Test
    void testDataSourceConfigUpdateWithOverridesPrecedence() throws SQLException {
        ElasticsearchDataSource eds = new ElasticsearchDataSource();
        Properties props = new Properties();
        props.setProperty(HostConnectionProperty.KEY, "some-host");
        props.setProperty(PortConnectionProperty.KEY, "1100");
        props.setProperty(LoginTimeoutConnectionProperty.KEY, "100");
        eds.setProperties(props);

        props = new Properties();
        props.setProperty(HostConnectionProperty.KEY, "some-host-updated");
        props.setProperty(PortConnectionProperty.KEY, "2100");
        props.setProperty(UserConnectionProperty.KEY, "user");
        props.setProperty(PasswordConnectionProperty.KEY, "pass");
        eds.setProperties(props);

        ConnectionConfig config = eds.getConnectionConfig(null);

        assertEquals("some-host-updated", config.getHost());
        assertEquals(2100, config.getPort());
        assertEquals(0, config.getLoginTimeout());
        assertEquals("user", config.getUser());
        assertEquals("pass", config.getPassword());

        Map<String, Object> overrides = new HashMap<>();
        overrides.put(UserConnectionProperty.KEY, "override-user");
        overrides.put(PasswordConnectionProperty.KEY, "override-pass");
        config = eds.getConnectionConfig(overrides);

        assertEquals("some-host-updated", config.getHost());
        assertEquals(2100, config.getPort());
        assertEquals(0, config.getLoginTimeout());
        assertEquals("override-user", config.getUser());
        assertEquals("override-pass", config.getPassword());
    }

    @Test
    void testDataSourceFromUrlNycTaxisQuery(WireMockServer mockServer) throws SQLException, IOException {
        QueryMock queryMock = new QueryMock.NycTaxisQueryMock();
        queryMock.setupMockServerStub(mockServer);

        DataSource ds = new ElasticsearchDataSource();
        ((ElasticsearchDataSource) ds).setUrl(getBaseURLForMockServer(mockServer));

        Connection con = ds.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = assertDoesNotThrow(() -> st.executeQuery(queryMock.getSql()));

        assertNotNull(rs);
        queryMock.getMockResultSet().assertMatches(rs);
    }

    @Test
    void testDataSourceFromPropsNycTaxisQuery(WireMockServer mockServer) throws SQLException, IOException {
        QueryMock queryMock = new QueryMock.NycTaxisQueryMock();
        queryMock.setupMockServerStub(mockServer);

        DataSource ds = new ElasticsearchDataSource();
        ((ElasticsearchDataSource) ds).setProperties(getConnectionPropertiesForMockServer(mockServer));

        Connection con = ds.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = assertDoesNotThrow(() -> st.executeQuery(queryMock.getSql()));

        assertNotNull(rs);
        queryMock.getMockResultSet().assertMatches(rs);
    }
}
