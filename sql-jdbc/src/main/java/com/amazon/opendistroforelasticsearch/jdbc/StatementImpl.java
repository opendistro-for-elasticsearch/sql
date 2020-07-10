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

import com.amazon.opendistroforelasticsearch.jdbc.internal.exceptions.ObjectClosedException;
import com.amazon.opendistroforelasticsearch.jdbc.logging.LoggingSource;
import com.amazon.opendistroforelasticsearch.jdbc.logging.Logger;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.JdbcQueryRequest;
import com.amazon.opendistroforelasticsearch.jdbc.internal.JdbcWrapper;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.InternalServerErrorException;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.QueryResponse;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.ResponseException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class StatementImpl implements Statement, JdbcWrapper, LoggingSource {

    protected ConnectionImpl connection;
    protected boolean open = false;
    protected int fetchSize;
    protected ResultSetImpl resultSet;
    protected Logger log;
    private boolean closeOnCompletion;

    public StatementImpl(ConnectionImpl connection, Logger log) {
        this.connection = connection;
        this.open = true;
        this.fetchSize = connection.getFetchSize();
        this.log = log;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        log.debug(()-> logEntry("executeQuery (%s)", sql));
        ResultSet rs = executeQueryX(sql, fetchSize);
        log.debug(()-> logExit("executeQuery", rs));
        return rs;
    }

    protected ResultSet executeQueryX(String sql, int fetchSize) throws SQLException {
        JdbcQueryRequest jdbcQueryRequest = new JdbcQueryRequest(sql, fetchSize);
        return executeQueryRequest(jdbcQueryRequest);
    }

    protected ResultSet executeQueryRequest(JdbcQueryRequest jdbcQueryRequest) throws SQLException {

        // JDBC Spec: A ResultSet object is automatically closed when the Statement
        // object that generated it is closed, re-executed, or used to retrieve the
        // next result from a sequence of multiple results.
        closeResultSet(false);

        try {
            QueryResponse queryResponse = connection.getProtocol().execute(jdbcQueryRequest);

            if (queryResponse.getError() != null) {
                throw new InternalServerErrorException(
                        queryResponse.getError().getReason(),
                        queryResponse.getError().getType(),
                        queryResponse.getError().getDetails());
            }

            resultSet = buildResultSet(queryResponse);

        } catch (ResponseException | IOException ex) {
            logAndThrowSQLException(log, new SQLException("Error executing query", ex));
        }
        return resultSet;
    }

    protected ResultSetImpl buildResultSet(QueryResponse queryResponse) throws SQLException {
        return new ResultSetImpl(this, queryResponse, log);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        checkOpen();
        throw new SQLFeatureNotSupportedException("Updates are not supported.");
    }

    @Override
    public void close() throws SQLException {
        log.debug(()->logEntry("close ()"));
        open = false;
        log.debug(()->logExit("close"));
    }

    private void closeX() throws SQLException {
        open = false;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {

    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {

    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        checkOpen();
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        // no-op
    }

    @Override
    public void cancel() throws SQLException {
        throw new SQLFeatureNotSupportedException("cancel not supported");
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {
        checkOpen();
        // no-op
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        log.debug(()->logEntry("execute (%s)", sql));
        checkOpen();
        executeQueryX(sql, fetchSize);
        log.debug(() -> logExit("execute", true));
        return true;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        log.debug(() -> logEntry("getResultSet ()"));
        checkOpen();
        log.debug(() -> logExit("getResultSet", resultSet));
        return resultSet;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        checkOpen();
        return -1;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        checkOpen();
        closeResultSet(true);
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        fetchSize = rows;
    }

    @Override
    public int getFetchSize() throws SQLException {
        return fetchSize;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("Batch execution is not supported");
    }

    @Override
    public void clearBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException("Batch execution is not supported");
    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException("Batch execution is not supported");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        log.debug(()->logEntry("execute (%s, %d)", sql, autoGeneratedKeys));
        checkOpen();
        if (autoGeneratedKeys != Statement.NO_GENERATED_KEYS) {
            throw new SQLNonTransientException("Auto generated keys are not supported");
        }
        executeQueryX(sql, fetchSize);
        log.debug(() -> logExit("execute", true));
        return true;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLNonTransientException("Auto generated keys are not supported");
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLNonTransientException("Auto generated keys are not supported");
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return isClosedX();
    }

    protected boolean isClosedX() throws SQLException {
        return !open;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        checkOpen();
        // no-op
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        closeOnCompletion = true;
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return closeOnCompletion;
    }

    protected void checkOpen() throws SQLException {
        if (isClosedX()) {
            throw new ObjectClosedException("Statement closed.");
        }
    }

    protected void closeResultSet(boolean closeStatement) throws SQLException {
        if (resultSet != null) {
            resultSet.closeX(closeStatement);
        }
    }

    void resultSetClosed(ResultSet rs, boolean closeStatement) throws SQLException {
        if (closeOnCompletion && closeStatement) {
            log.debug(() -> logMessage("Child ResultSet closed and closeOnCompletion is enabled. Closing statement."));
            closeX();
        }
    }

    private SQLException updatesNotSupportedException() {
        return new SQLFeatureNotSupportedException("Updates are not supported");
    }
}
