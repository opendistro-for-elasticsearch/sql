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

import com.amazon.opendistroforelasticsearch.jdbc.config.ConnectionConfig;
import com.amazon.opendistroforelasticsearch.jdbc.internal.util.UrlParser;
import com.amazon.opendistroforelasticsearch.jdbc.logging.LoggingSource;
import com.amazon.opendistroforelasticsearch.jdbc.internal.Version;
import com.amazon.opendistroforelasticsearch.jdbc.logging.LoggerFactory;
import com.amazon.opendistroforelasticsearch.jdbc.logging.NoOpLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class Driver implements java.sql.Driver, LoggingSource {

    //
    // Register with the DriverManager
    //
    static {
        try {
            java.sql.DriverManager.registerDriver(new Driver());
        } catch (SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        ConnectionConfig connectionConfig = ConnectionConfig.builder()
                .setUrl(url)
                .setProperties(info)
                .build();
        com.amazon.opendistroforelasticsearch.jdbc.logging.Logger log = initLog(connectionConfig);
        log.debug(() -> logMessage("connect (%s, %s)", url, info == null ? "null" : info.toString()));
        log.debug(() -> logMessage("Opening connection using config: %s", connectionConfig));
        return new ConnectionImpl(connectionConfig, log);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return UrlParser.isAcceptable(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        // TODO - implement this?
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return Version.Current.getMajor();
    }

    @Override
    public int getMinorVersion() {
        return Version.Current.getMinor();
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    static com.amazon.opendistroforelasticsearch.jdbc.logging.Logger initLog(ConnectionConfig connectionConfig) {
        // precedence:
        // 1. explicitly supplied logWriter
        // 2. logOutput property
        // 3. DriverManager logWriter
        if (connectionConfig.getLogWriter() != null) {

            return LoggerFactory.getLogger(connectionConfig.getLogWriter(), connectionConfig.getLogLevel());

        } else if (connectionConfig.getLogOutput() != null) {

            return LoggerFactory.getLogger(connectionConfig.getLogOutput(), connectionConfig.getLogLevel());

        } else if (DriverManager.getLogWriter() != null) {

            return LoggerFactory.getLogger(DriverManager.getLogWriter(), connectionConfig.getLogLevel());

        } else {

            return NoOpLogger.INSTANCE;
        }
    }

}
