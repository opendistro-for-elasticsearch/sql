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
import com.amazon.opendistroforelasticsearch.jdbc.internal.JdbcWrapper;
import com.amazon.opendistroforelasticsearch.jdbc.internal.Version;
import com.amazon.opendistroforelasticsearch.jdbc.internal.util.JavaUtil;
import com.amazon.opendistroforelasticsearch.jdbc.logging.Logger;
import com.amazon.opendistroforelasticsearch.jdbc.logging.LoggingSource;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.ClusterMetadata;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.ConnectionResponse;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.Protocol;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.ProtocolFactory;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.ResponseException;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JsonHttpProtocolFactory;
import com.amazon.opendistroforelasticsearch.jdbc.transport.Transport;
import com.amazon.opendistroforelasticsearch.jdbc.transport.TransportException;
import com.amazon.opendistroforelasticsearch.jdbc.transport.TransportFactory;
import com.amazon.opendistroforelasticsearch.jdbc.transport.http.ApacheHttpTransportFactory;

import java.io.IOException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionImpl implements ElasticsearchConnection, JdbcWrapper, LoggingSource {

    private String url;
    private String user;
    private Logger log;
    private int fetchSize;
    private boolean open = false;
    private Transport transport;
    private Protocol protocol;
    private ClusterMetadata clusterMetadata;

    public ConnectionImpl(ConnectionConfig connectionConfig, Logger log) throws SQLException {
        this(connectionConfig, ApacheHttpTransportFactory.INSTANCE, JsonHttpProtocolFactory.INSTANCE, log);
    }

    public ConnectionImpl(ConnectionConfig connectionConfig, TransportFactory transportFactory,
                          ProtocolFactory protocolFactory, Logger log) throws SQLException {
        this.log = log;
        this.url = connectionConfig.getUrl();
        this.user = connectionConfig.getUser();
        this.fetchSize = connectionConfig.getFetchSize();

        try {
            this.transport = transportFactory.getTransport(connectionConfig, log, getUserAgent());
        } catch (TransportException te) {
            logAndThrowSQLException(
                    log, new SQLNonTransientException("Could not initialize transport for the connection: "+te.getMessage(), te)
            );
        }

        this.protocol = protocolFactory.getProtocol(connectionConfig, this.transport);

        log.debug(() -> logMessage("Initialized Transport: %s, Protocol: %s", transport, protocol));

        try {
            ConnectionResponse connectionResponse = this.protocol.connect(connectionConfig.getLoginTimeout() * 1000);
            this.clusterMetadata = connectionResponse.getClusterMetadata();
            this.open = true;
        } catch (ResponseException | IOException ex) {
            logAndThrowSQLException(log, new SQLException("Connection error "+ex.getMessage(), ex));
        }

    }

    public String getUser() {
        return user;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    @Override
    public Statement createStatement() throws SQLException {
        log.debug(() -> logEntry("createStatement()"));
        Statement st = createStatementX();
        log.debug(() -> logExit("createStatement", st));
        return st;
    }

    public Statement createStatementX() throws SQLException {
        return new StatementImpl(this, log);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        log.debug(() -> logEntry("prepareStatment (%s)", sql));
        checkOpen();
        PreparedStatement pst = prepareStatementX(sql);
        log.debug(() -> logExit("prepareStatement", pst));
        return pst;
    }

    private PreparedStatement prepareStatementX(String sql) throws SQLException {
        return new PreparedStatementImpl(this, sql, log);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("prepareCall is not supported.");
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        checkOpen();
        return sql;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkOpen();
        if (!autoCommit) {
            throw new SQLNonTransientException("autoCommit can not be disabled.");
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        checkOpen();
        return true;
    }

    @Override
    public void commit() throws SQLException {
        checkOpen();
        throw new SQLNonTransientException("autoCommit is enabled on the connection.");
    }

    @Override
    public void rollback() throws SQLException {
        checkOpen();
        throw new SQLNonTransientException("autoCommit is enabled on the connection.");
    }

    @Override
    public void close() throws SQLException {
        log.debug(() -> logEntry("close ()"));
        closeX();
    }

    private void closeX() throws SQLException {
        open = false;
        try {
            transport.close();
        } catch (TransportException te) {
            log.error(() -> logMessage("Exception closing transport: "+te), te);
        }
        log.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return isClosedX();
    }

    protected boolean isClosedX() throws SQLException {
        return !open;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        log.debug(() -> logEntry("getMetaData()"));
        DatabaseMetaData dbmd = new DatabaseMetaDataImpl(this, log);
        log.debug(() -> logExit("getMetaData", dbmd));
        return dbmd;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        if (!readOnly)
            throw new SQLNonTransientException("read-only mode can not be disabled.");
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        checkOpen();
        return true;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        checkOpen();
        // no-op
    }

    @Override
    public String getCatalog() throws SQLException {
        return getClusterName();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        checkOpen();
        if (level != TRANSACTION_NONE)
            throw new SQLNonTransientException("Only TRANSACTION_NONE is supported.");
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        checkOpen();
        return TRANSACTION_NONE;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkOpen();
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        checkOpen();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        log.debug(() -> logEntry("createStatement (%d, %d)", resultSetType, resultSetConcurrency));
        checkOpen();
        validateResultSetCharacteristics(resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement st = createStatementX();
        log.debug(() -> logExit("createStatement", st));
        return st;
    }

    private void validateResultSetCharacteristics(
            int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        validateResultSetType(resultSetType);
        validateResulSetConcurrency(resultSetConcurrency);
        validateResultSetHoldability(resultSetHoldability);
    }

    private void validateResultSetType(int resultSetType) throws SQLException {
        if (resultSetType != ResultSet.TYPE_FORWARD_ONLY)
            throw new SQLNonTransientException("Only ResultSets of TYPE_FORWARD_ONLY are supported.");
    }

    private void validateResulSetConcurrency(int resultSetConcurrency) throws SQLException {
        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY)
            throw new SQLNonTransientException("Only ResultSets with concurrency CONCUR_READ_ONLY are supported.");
    }

    private void validateResultSetHoldability(int holdability) throws SQLException {
        if (holdability != ResultSet.HOLD_CURSORS_OVER_COMMIT)
            throw new SQLNonTransientException("Only HOLD_CURSORS_OVER_COMMIT holdability is supported.");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        log.debug(() -> logEntry("prepareStatement (%s, %d, %d)", sql, resultSetType, resultSetConcurrency));
        checkOpen();
        validateResultSetCharacteristics(resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        PreparedStatement pst = prepareStatementX(sql);
        log.debug(() -> logExit("prepareStatement", pst));
        return pst;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException("prepareCall is not supported");
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return null;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("setTypeMap is not supported");
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        checkOpen();
        validateResultSetHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        checkOpen();
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLFeatureNotSupportedException("Transactions and savepoints are not supported.");
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException("Transactions and savepoints are not supported.");
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException("Transactions are not supported.");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException("Transactions and savepoints are not supported.");
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        log.debug(() -> logEntry("createStatement (%d, %d, %d)", resultSetType, resultSetConcurrency, resultSetHoldability));
        checkOpen();
        validateResultSetCharacteristics(resultSetType, resultSetConcurrency, resultSetHoldability);
        Statement st = createStatementX();
        log.debug(() -> logExit("createStatment", st));
        return st;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        log.debug(() -> logEntry("prepareStatement (%s, %d, %d, %d)", sql, resultSetType, resultSetConcurrency, resultSetHoldability));
        checkOpen();
        validateResultSetCharacteristics(resultSetType, resultSetConcurrency, resultSetHoldability);
        PreparedStatement pst = prepareStatementX(sql);
        log.debug(() -> logExit("prepareStatement", pst));
        return pst;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException("prepareCall is not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        log.debug(() -> logEntry("prepareStatement (%s, %d)", sql, autoGeneratedKeys));
        checkOpen();
        if (autoGeneratedKeys != Statement.NO_GENERATED_KEYS) {
            throw new SQLFeatureNotSupportedException("Auto generated keys are not supported.");
        }
        PreparedStatement pst = prepareStatementX(sql);
        log.debug(() -> logExit("prepareStatement", pst));
        return pst;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException("Auto generated keys are not supported.");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException("Auto generated keys are not supported.");
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("Clob is not supported.");
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLFeatureNotSupportedException("Blob is not supported.");
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("NClob is not supported.");
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLFeatureNotSupportedException("SQLXML is not supported.");
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        // TODO - implement through a HEAD or a GET to "/", or a dummy SQL?
        log.debug(() -> logEntry("isValid (%d)", timeout));

        boolean isValid = true;

        log.debug(() -> logExit("isValid", isValid));
        return isValid;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        throw new SQLClientInfoException("Client info is not supported.", null);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        throw new SQLClientInfoException("Client info is not supported.", null);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        checkOpen();
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        checkOpen();
        return null;
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLFeatureNotSupportedException("Array is not supported.");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLFeatureNotSupportedException("Struct is not supported.");
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        // no-op
    }

    @Override
    public String getSchema() throws SQLException {
        return "";
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        log.debug(() -> logEntry("abort (%s) ", executor));
        closeX();
        log.debug(() -> logExit("abort"));
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        checkOpen();
        // no-op, not supported yet
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    public String getUrl() {
        return url;
    }

    private void checkOpen() throws SQLException {
        if (isClosedX()) {
            logAndThrowSQLException(log, new SQLException("Connection is closed."));
        }
    }

    @Override
    public String getClusterName() throws SQLException {
        checkOpen();
        return clusterMetadata.getClusterName();
    }

    @Override
    public String getClusterUUID() throws SQLException {
        checkOpen();
        return clusterMetadata.getClusterUUID();
    }

    public ClusterMetadata getClusterMetadata() throws SQLException {
        checkOpen();
        return this.clusterMetadata;
    }

    public Transport getTransport() {
        return transport;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Logger getLog() {
        return log;
    }

    private String getUserAgent() {
        return String.format("openes-jdbc/%s (Java %s)",
                Version.Current.getFullVersion(), JavaUtil.getJavaVersion());
    }
}
