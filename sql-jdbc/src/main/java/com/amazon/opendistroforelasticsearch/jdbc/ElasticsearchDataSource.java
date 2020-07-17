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

import com.amazon.opendistroforelasticsearch.jdbc.config.AwsCredentialsProviderProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.ConnectionConfig;
import com.amazon.opendistroforelasticsearch.jdbc.config.LoginTimeoutConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.PasswordConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.UserConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.internal.JdbcWrapper;
import com.amazon.opendistroforelasticsearch.jdbc.internal.util.UrlParser;
import com.amazon.opendistroforelasticsearch.jdbc.logging.LoggingSource;
import com.amazonaws.auth.AWSCredentialsProvider;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Elasticsearch {@link DataSource} implementation.
 * <p>
 * Connection properties are expected to be included in a
 * JDBC connection URL and supplied to the DataSource via
 * {@link #setUrl(String)}.
 * <p>
 * Where properties like login timeout
 * have explicit setters in the DataSource API as well, the property
 * value specified directly on the DataSource overrides the
 * corresponding property value in the JDBC URL. Also, getter methods
 * for such properties return the value that was explicitly set on
 * the DataSource, and not the value that might be present on the JDBC URL.
 */
public class ElasticsearchDataSource implements DataSource, JdbcWrapper, LoggingSource {

    private String url;

    // properties that may come from URL as also be directly set on
    // DataSource are recorded in this map to help maintain the
    // precedence order of applying properties:
    // - values directly set on DataSource override values in URL and,
    // - if no value is explicitly set on the DataSource, then the
    //  value in the URL is applied.
    private Map<String, Object> connectionProperties = new HashMap<>();
    private PrintWriter logWriter;

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(null);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Map<String, Object> overrideProperties = new HashMap<>();
        overrideProperties.put(UserConnectionProperty.KEY, username);
        overrideProperties.put(PasswordConnectionProperty.KEY, password);

        return getConnection(overrideProperties);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        // property parsed here to ensure fail-fast behavior with property validation
        LoginTimeoutConnectionProperty property = new LoginTimeoutConnectionProperty();
        property.setRawValue(seconds);
        connectionProperties.put(LoginTimeoutConnectionProperty.KEY, property.getValue());
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return (Integer) getConnectionProperty(LoginTimeoutConnectionProperty.KEY, -1);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("DataSource does not use java.util.logging");
    }

    /**
     * Sets the JDBC connection URL for the DataSource.
     *
     * @param url the jdbc connection url to use for establishing and
     *         configuring a connection
     *
     * @throws SQLException if there is a problem in setting the url
     */
    public void setUrl(String url) throws SQLException {
        this.url = url;
        try {
            // fail-fast on invalid url
            UrlParser.parseProperties(url);
        } catch (URISyntaxException e) {
            throw new SQLException("Invalid connection URL", e);
        }
    }
    
    public void setAwsCredentialProvider(AWSCredentialsProvider awsCredentialProvider) {
        connectionProperties.put(AwsCredentialsProviderProperty.KEY, awsCredentialProvider);
    }

    /**
     * Updates DataSource configuration properties from the specified
     * {@link Properties} object.
     * <p>
     * All properties already set on the DataSource - either from a
     * prior call to this method or any other setters on this DataSource
     * are discarded, and the properties passed in the specified
     * {@link Properties} object are applied. All properties in the
     * properties object, including any default values get applied on the
     * DataSource.
     *
     * @param properties The property object containing properties to
     *         apply.
     *
     * @throws SQLException if there is a problem in applying the
     *         specified properties
     */
    public void setProperties(Properties properties) throws SQLException {
        this.connectionProperties.clear();

        if (properties != null) {
            Enumeration<?> propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                this.connectionProperties.put(propertyName, properties.getProperty(propertyName));
            }
        }
    }

    public String getUrl() throws SQLException {
        return url;
    }


    private Object getConnectionProperty(String key, Object defaultValue) {
        return connectionProperties.getOrDefault(key, defaultValue);
    }

    private Connection getConnection(Map<String, Object> overrideProperties)
            throws SQLException {
        ConnectionConfig connectionConfig = getConnectionConfig(overrideProperties);
        com.amazon.opendistroforelasticsearch.jdbc.logging.Logger log = Driver.initLog(connectionConfig);
        log.debug(() -> logMessage("Opening connection using config: %s", connectionConfig));
        return new ConnectionImpl(connectionConfig, log);
    }

    ConnectionConfig getConnectionConfig(Map<String, Object> overrideProperties)
            throws SQLException {
        return ConnectionConfig.builder()
                .setUrl(url)
                .setPropertyMap(connectionProperties)
                .setLogWriter(logWriter)
                .overrideProperties(overrideProperties)
                .build();
    }
}
