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

import com.amazon.opendistroforelasticsearch.jdbc.internal.results.ColumnMetaData;
import com.amazon.opendistroforelasticsearch.jdbc.internal.results.Cursor;
import com.amazon.opendistroforelasticsearch.jdbc.internal.exceptions.ObjectClosedException;
import com.amazon.opendistroforelasticsearch.jdbc.internal.results.Row;
import com.amazon.opendistroforelasticsearch.jdbc.internal.results.Schema;
import com.amazon.opendistroforelasticsearch.jdbc.logging.LoggingSource;
import com.amazon.opendistroforelasticsearch.jdbc.logging.Logger;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.ColumnDescriptor;
import com.amazon.opendistroforelasticsearch.jdbc.internal.JdbcWrapper;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.QueryResponse;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.InternalServerErrorException;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.ResponseException;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JdbcCursorQueryRequest;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JsonCursorHttpProtocol;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JsonCursorHttpProtocolFactory;
import com.amazon.opendistroforelasticsearch.jdbc.transport.http.HttpTransport;
import com.amazon.opendistroforelasticsearch.jdbc.types.TypeConverter;
import com.amazon.opendistroforelasticsearch.jdbc.types.TypeConverters;
import com.amazon.opendistroforelasticsearch.jdbc.types.UnrecognizedElasticsearchTypeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Elasticsearch implementaion for a {@link ResultSet}
 * <p>
 * Column names or labels received in APIs are treated in a
 * case-sensitive manner since Elasticsearch field names are
 * case-sensitive.
 * </p>
 * The lookup
 */
public class ResultSetImpl implements ResultSet, JdbcWrapper, LoggingSource {

    private StatementImpl statement;
    protected Cursor cursor;
    private String cursorId;
    private boolean open = false;
    private boolean wasNull = false;
    private boolean afterLast = false;
    private boolean beforeFirst = true;
    private Logger log;

    public ResultSetImpl(StatementImpl statement, QueryResponse queryResponse, Logger log) throws SQLException {
        this(statement, queryResponse.getColumnDescriptors(), queryResponse.getDatarows(), queryResponse.getCursor(), log);
    }

    public ResultSetImpl(StatementImpl statement, List<? extends ColumnDescriptor> columnDescriptors,
                         List<List<Object>> dataRows, Logger log) throws SQLException {
        this(statement, columnDescriptors, dataRows, null, log);
    }

    public ResultSetImpl(StatementImpl statement, List<? extends ColumnDescriptor> columnDescriptors,
                         List<List<Object>> dataRows, String cursorId, Logger log) throws SQLException {
        this.statement = statement;
        this.log = log;

        final Schema schema;
        try {
            schema = new Schema(columnDescriptors
                    .stream()
                    .map(ColumnMetaData::new)
                    .collect(Collectors.toList()));

            List<Row> rows = getRowsFromDataRows(dataRows);

            this.cursor = new Cursor(schema, rows);
            this.cursorId = cursorId;
            this.open = true;

        } catch (UnrecognizedElasticsearchTypeException ex) {
            logAndThrowSQLException(log, new SQLException("Exception creating a ResultSet.", ex));
        }

    }

    @Override
    public boolean next() throws SQLException {
        log.debug(() -> logEntry("next()"));
        checkOpen();
        boolean next = cursor.next();

        if (!next && this.cursorId != null) {
            log.debug(() -> logEntry("buildNextPageFromCursorId()"));
            buildNextPageFromCursorId();
            log.debug(() -> logExit("buildNextPageFromCursorId()"));
            next = cursor.next();
        }

        if (next) {
            beforeFirst = false;
        } else {
            afterLast = true;
        }
        boolean finalNext = next;
        log.debug(() -> logExit("next", finalNext));
        return next;
    }

    /**
     * TODO: Refactor as suggested https://github.com/opendistro-for-elasticsearch/sql-jdbc/pull/76#discussion_r421571383
     *
     * This method has side effects. It creates a new Cursor to hold rows from new pages.
     * Ideally fetching next set of rows using cursorId should be delegated to Cursor.
     * In addition, the cursor should be final.
     *
     **/
    protected void buildNextPageFromCursorId() throws SQLException {
        try {
            JdbcCursorQueryRequest jdbcCursorQueryRequest = new JdbcCursorQueryRequest(this.cursorId);
            JsonCursorHttpProtocolFactory protocolFactory = JsonCursorHttpProtocolFactory.INSTANCE;
            ConnectionImpl connection = (ConnectionImpl) statement.getConnection();

            JsonCursorHttpProtocol protocol = protocolFactory.getProtocol(null, (HttpTransport) connection.getTransport());
            QueryResponse queryResponse = protocol.execute(jdbcCursorQueryRequest);

            if (queryResponse.getError() != null) {
                throw new InternalServerErrorException(
                        queryResponse.getError().getReason(),
                        queryResponse.getError().getType(),
                        queryResponse.getError().getDetails());
            }

            cursor = new Cursor(cursor.getSchema(), getRowsFromDataRows(queryResponse.getDatarows()));
            cursorId = queryResponse.getCursor();

        } catch (ResponseException | IOException ex) {
            logAndThrowSQLException(log, new SQLException("Error executing cursor query", ex));
        }
    }

    private List<Row> getRowsFromDataRows(List<List<Object>> dataRows) {
        return dataRows
                .parallelStream()
                .map(Row::new)
                .collect(Collectors.toList());
    }

    @Override
    public void close() throws SQLException {
        log.debug(() -> logEntry("close()"));
        closeX(true);
        log.debug(() -> logExit("close"));
    }

    protected void closeX(boolean closeStatement) throws SQLException {
        cursor = null;
        open = false;
        if (statement != null) {
            statement.resultSetClosed(this, closeStatement);
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        return wasNull;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getString (%d)", columnIndex));
        checkCursorOperationPossible();
        String value = getStringX(columnIndex);
        log.debug(() -> logExit("getString", value));
        return value;
    }

    private String getStringX(int columnIndex) throws SQLException {
        return getObjectX(columnIndex, String.class);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getBoolean (%d)", columnIndex));
        checkCursorOperationPossible();
        boolean value = getBooleanX(columnIndex);
        log.debug(() -> logExit("getBoolean", value));
        return value;
    }

    private boolean getBooleanX(int columnIndex) throws SQLException {
        return getObjectX(columnIndex, Boolean.class);
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getByte (%d)", columnIndex));
        checkCursorOperationPossible();
        byte value = getByteX(columnIndex);
        log.debug(() -> logExit("getByte", value));
        return value;
    }

    private byte getByteX(int columnIndex) throws SQLException {
        return getObjectX(columnIndex, Byte.class);
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getShort (%d)", columnIndex));
        checkCursorOperationPossible();
        short value = getShortX(columnIndex);
        log.debug(() -> logExit("getShort", value));
        return value;
    }

    private short getShortX(int columnIndex) throws SQLException {
        return getObjectX(columnIndex, Short.class);
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getInt (%d)", columnIndex));
        checkCursorOperationPossible();
        int value = getIntX(columnIndex);
        log.debug(() -> logExit("getInt", value));
        return value;
    }

    private int getIntX(int columnIndex) throws SQLException {
        return getObjectX(columnIndex, Integer.class);
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getLong (%d)", columnIndex));
        checkCursorOperationPossible();
        long value = getLongX(columnIndex);
        log.debug(() -> logExit("getLong", value));
        return value;
    }

    private long getLongX(int columnIndex) throws SQLException {
        checkCursorOperationPossible();
        return getObjectX(columnIndex, Long.class);
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getFloat (%d)", columnIndex));
        checkCursorOperationPossible();
        float value = getFloatX(columnIndex);
        log.debug(() -> logExit("getFloat", value));
        return value;
    }

    private float getFloatX(int columnIndex) throws SQLException {
        return getObjectX(columnIndex, Float.class);
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getDouble (%d)", columnIndex));
        checkCursorOperationPossible();
        double value = getDoubleX(columnIndex);
        log.debug(() -> logExit("getDouble", value));
        return value;
    }

    private double getDoubleX(int columnIndex) throws SQLException {
        return getObjectX(columnIndex, Double.class);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        log.debug(() -> logEntry("getBigDecimal (%d, %d)", columnIndex, scale));
        checkCursorOperationPossible();
        BigDecimal value = getBigDecimalX(columnIndex, scale);
        log.debug(() -> logExit("getBigDecimal", value));
        return value;
    }

    private BigDecimal getBigDecimalX(int columnIndex, int scale) throws SQLException {
        checkOpen();
        // TODO - add support?
        throw new SQLFeatureNotSupportedException("BigDecimal is not supported");
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getBytes (%d)", columnIndex));
        checkCursorOperationPossible();
        byte[] value = getBytesX(columnIndex);
        log.debug(() -> logExit("getBytes",
                String.format("%s, length(%s)", value, value != null ? value.length : 0)));
        return value;
    }

    private byte[] getBytesX(int columnIndex) throws SQLException {
        // TODO - add ByteArrayType support
        return getObjectX(columnIndex, byte[].class);
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getDate (%d)", columnIndex));
        checkCursorOperationPossible();
        Date value = getDateX(columnIndex, null);
        log.debug(() -> logExit("getDate", value));
        return value;
    }

    private Date getDateX(int columnIndex, Calendar calendar) throws SQLException {
        Map<String, Object> conversionParams = null;
        if (calendar != null) {
            conversionParams = new HashMap<>();
            conversionParams.put("calendar", calendar);
        }
        return getObjectX(columnIndex, Date.class, conversionParams);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getTime (%d)", columnIndex));
        checkCursorOperationPossible();
        Time value = getTimeX(columnIndex);
        log.debug(() -> logExit("getTime", value));
        return value;
    }

    private Time getTimeX(int columnIndex) throws SQLException {
        // TODO - add/check support
        return getObjectX(columnIndex, Time.class);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getTimestamp (%d)", columnIndex));
        checkCursorOperationPossible();
        Timestamp value = getTimestampX(columnIndex, null);
        log.debug(() -> logExit("getTimestamp", value));
        return value;
    }

    private Timestamp getTimestampX(int columnIndex, Calendar calendar) throws SQLException {
        Map<String, Object> conversionParams = null;
        if (calendar != null) {
            conversionParams = new HashMap<>();
            conversionParams.put("calendar", calendar);
        }
        return getObjectX(columnIndex, Timestamp.class, conversionParams);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Streams are not supported");
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Streams are not supported");
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Streams are not supported");
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getString (%s)", columnLabel));
        checkCursorOperationPossible();
        String value = getStringX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getString", value));
        return value;
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getBoolean (%s)", columnLabel));
        checkCursorOperationPossible();
        boolean value = getBooleanX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getBoolean", value));
        return value;
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getByte (%s)", columnLabel));
        checkCursorOperationPossible();
        byte value = getByteX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getByte", value));
        return value;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getShort (%s)", columnLabel));
        checkCursorOperationPossible();
        short value = getShortX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getShort", value));
        return value;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getInt (%s)", columnLabel));
        checkCursorOperationPossible();
        int value = getIntX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getInt", value));
        return value;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getLong (%s)", columnLabel));
        checkCursorOperationPossible();
        long value = getLongX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getLong", value));
        return value;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getFloat (%s)", columnLabel));
        checkCursorOperationPossible();
        float value = getFloatX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getFloat", value));
        return value;
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getDouble (%s)", columnLabel));
        checkCursorOperationPossible();
        double value = getDoubleX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getDouble", value));
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        log.debug(() -> logEntry("getBigDecimal (%s, %d)", columnLabel, scale));
        checkCursorOperationPossible();
        BigDecimal value = getBigDecimalX(getColumnIndex(columnLabel), scale);
        log.debug(() -> logExit("getBigDecimal", value));
        return value;
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getBytes (%s)", columnLabel));
        checkCursorOperationPossible();
        byte[] value = getBytesX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getBytes",
                String.format("%s, length(%s)", value, value != null ? value.length : 0)));
        return value;
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getDate (%s)", columnLabel));
        checkCursorOperationPossible();
        Date value = getDateX(getColumnIndex(columnLabel), null);
        log.debug(() -> logExit("getDate", value));
        return value;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getTime (%s)", columnLabel));
        checkCursorOperationPossible();
        Time value = getTimeX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getTime", value));
        return value;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getTimestamp (%s)", columnLabel));
        checkCursorOperationPossible();
        Timestamp value = getTimestampX(getColumnIndex(columnLabel), null);
        log.debug(() -> logExit("getTimestamp", value));
        return value;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Streams are not supported");
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Streams are not supported");
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Streams are not supported");
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
    public String getCursorName() throws SQLException {
        throw new SQLFeatureNotSupportedException("Cursor name is not supported");
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        checkOpen();
        return new ResultSetMetaDataImpl(this, cursor.getSchema());
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getObject (%d)", columnIndex));
        checkCursorOperationPossible();
        Object value = getObjectX(columnIndex);
        log.debug(() -> logExit("getObject",
                value != null ? "(" + value.getClass().getName() + ") " + value : "null"));
        return value;
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getObject (%s)", columnLabel));
        checkCursorOperationPossible();
        Object value = getObjectX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getObject",
                value != null ? "(" + value.getClass().getName() + ") " + value : "null"));
        return value;
    }

    private Object getObjectX(int columnIndex) throws SQLException {
        return getObjectX(columnIndex, (Class<Object>) null);
    }

    protected <T> T getObjectX(int columnIndex, Class<T> javaClass) throws SQLException {
        return getObjectX(columnIndex, javaClass, null);
    }

    protected <T> T getObjectX(int columnIndex, Class<T> javaClass, Map<String, Object> conversionParams) throws SQLException {
        Object value = getColumn(columnIndex);
        TypeConverter tc = TypeConverters.getInstance(getColumnMetaData(columnIndex).getEsType().getJdbcType());
        return tc.convert(value, javaClass, conversionParams);
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        checkOpen();
        return getColumnIndex(columnLabel);
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Streams are not supported");
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Streams are not supported");
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        // TODO - add support?
        checkOpen();
        throw new SQLFeatureNotSupportedException("BigDecimal is not supported");
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        // TODO - add support?
        checkOpen();
        throw new SQLFeatureNotSupportedException("BigDecimal is not supported");
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        checkOpen();
        return beforeFirst;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        checkOpen();
        return afterLast;
    }

    private boolean isBeforeFirstX() throws SQLException {
        return beforeFirst;
    }

    private boolean isAfterLastX() throws SQLException {
        return afterLast;
    }

    @Override
    public boolean isFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isLast() throws SQLException {
        return false;
    }

    @Override
    public void beforeFirst() throws SQLException {
        checkOpen();
        throw new SQLDataException("Illegal operation on ResultSet of type TYPE_FORWARD_ONLY");
    }

    @Override
    public void afterLast() throws SQLException {
        checkOpen();
        throw new SQLDataException("Illegal operation on ResultSet of type TYPE_FORWARD_ONLY");
    }

    @Override
    public boolean first() throws SQLException {
        checkOpen();
        throw new SQLDataException("Illegal operation on ResultSet of type TYPE_FORWARD_ONLY");
    }

    @Override
    public boolean last() throws SQLException {
        checkOpen();
        throw new SQLDataException("Illegal operation on ResultSet of type TYPE_FORWARD_ONLY");
    }

    @Override
    public int getRow() throws SQLException {
        // not supported yet
        return 0;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        checkOpen();
        throw new SQLDataException("Illegal operation on ResultSet of type TYPE_FORWARD_ONLY");
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        checkOpen();
        throw new SQLDataException("Illegal operation on ResultSet of type TYPE_FORWARD_ONLY");
    }

    @Override
    public boolean previous() throws SQLException {
        checkOpen();
        throw new SQLDataException("Illegal operation on ResultSet of type TYPE_FORWARD_ONLY");
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        checkOpen();
        if (direction != ResultSet.FETCH_FORWARD) {
            throw new SQLDataException("The ResultSet only supports FETCH_FORWARD direction");
        }
    }

    @Override
    public int getFetchDirection() throws SQLException {
        checkOpen();
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        checkOpen();
        // no-op
    }

    @Override
    public int getFetchSize() throws SQLException {
        checkOpen();
        return 0;
    }

    @Override
    public int getType() throws SQLException {
        checkOpen();
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public int getConcurrency() throws SQLException {
        checkOpen();
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        checkOpen();
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        checkOpen();
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        checkOpen();
        return false;
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void insertRow() throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateRow() throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void deleteRow() throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void refreshRow() throws SQLException {

    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {

    }

    @Override
    public Statement getStatement() throws SQLException {
        return statement;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        log.debug(() -> logEntry("getObject (%d, %s)", columnIndex, map));

        Object value = getObjectX(columnIndex, map);

        log.debug(() -> logExit("getObject", value));
        return value;
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Ref is not supported");
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Blob is not supported");
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Clob is not supported");
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Array is not supported");
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        log.debug(() -> logEntry("getObject (%s, %s)", columnLabel, map));
        Object value = getObjectX(getColumnIndex(columnLabel), map);
        log.debug(() -> logExit("getObject", value));
        return value;
    }

    private Object getObjectX(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        String columnSQLTypeName = null;
        Class targetClass = null;
        if (map != null) {
            columnSQLTypeName = getColumnMetaData(columnIndex).getEsType().getJdbcType().getName();
            targetClass = map.get(columnSQLTypeName);
        }

        if (log.isDebugEnabled()) {
            log.debug(logMessage("Column SQL Type is: %s. Target class retrieved from custom mapping: %s",
                    columnSQLTypeName, targetClass));
        }
        return getObjectX(columnIndex, targetClass);
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Ref is not supported");
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Blob is not supported");
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Clob is not supported");
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Array is not supported");
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        log.debug(() -> logEntry("getDate (%d, %s)", columnIndex,
                cal == null ? "null" : "Calendar TZ= " + cal.getTimeZone()));
        checkCursorOperationPossible();
        Date value = getDateX(columnIndex, cal);
        log.debug(() -> logExit("getDate", value));
        return value;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        log.debug(() -> logEntry("getDate (%s, %s)", columnLabel,
                cal == null ? "null" : "Calendar TZ= " + cal.getTimeZone()));
        checkCursorOperationPossible();
        Date value = getDateX(getColumnIndex(columnLabel), cal);
        log.debug(() -> logExit("getDate", value));
        return value;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        // TODO - implement?
        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        // TODO - implement?
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        log.debug(() -> logEntry("getTimestamp (%d, %s)", columnIndex,
                cal == null ? "null" : "Calendar TZ= " + cal.getTimeZone()));
        checkCursorOperationPossible();
        Timestamp value = getTimestampX(columnIndex, cal);
        log.debug(() -> logExit("getTimestamp", value));
        return value;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        log.debug(() -> logEntry("getTimestamp (%s, %s)", columnLabel,
                cal == null ? "null" : "Calendar TZ= " + cal.getTimeZone()));
        checkCursorOperationPossible();
        Timestamp value = getTimestampX(getColumnIndex(columnLabel), cal);
        log.debug(() -> logExit("getTimestamp", value));
        return value;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        // TODO - implement
        return null;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        // TODO - implement
        return null;
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("RowId is not supported");
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("RowId is not supported");
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public int getHoldability() throws SQLException {
        checkOpen();
        return HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return !open;
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("NClob is not supported");
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("NClob is not supported");
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("SQLXML is not supported");
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("SQLXML is not supported");
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        log.debug(() -> logEntry("getNString (%d)", columnIndex));
        String value = getStringX(columnIndex);
        log.debug(() -> logExit("getNString", value));
        return value;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        log.debug(() -> logEntry("getNString (%s)", columnLabel));
        String value = getStringX(getColumnIndex(columnLabel));
        log.debug(() -> logExit("getNString", value));
        return value;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Streams are not supported");
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Streams are not supported");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw updatesNotSupportedException();
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        log.debug(() -> logEntry("getObject(%d, %s)", columnIndex, type));
        T value = getObjectX(columnIndex, type);
        log.debug(() -> logExit("getObject", value));
        return value;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        log.debug(() -> logEntry("getObject(%d, %s)", columnLabel, type));
        T value = getObjectX(getColumnIndex(columnLabel), type);
        log.debug(() -> logExit("getObject", value));
        return value;
    }

    private int getColumnIndex(String columnLabel) throws SQLException {
        Integer index = cursor.findColumn(columnLabel);

        if (index == null)
            logAndThrowSQLException(log, new SQLDataException("Column '" + columnLabel + "' not found."));

        // +1 to adjust for JDBC indices that start from 1
        return index + 1;
    }

    protected Object getColumn(int columnIndex) throws SQLException {
        checkColumnIndex(columnIndex);
        Object columnData = getColumnFromCursor(columnIndex);

        wasNull = (columnData == null);
        return columnData;
    }

    protected Object getColumnFromCursor(int columnIndex) {
        return cursor.getColumn(columnIndex - 1);
    }

    private ColumnMetaData getColumnMetaData(int columnIndex) throws SQLException {
        checkColumnIndex(columnIndex);
        return cursor.getSchema().getColumnMetaData(columnIndex - 1);
    }

    protected void checkColumnIndex(int columnIndex) throws SQLException {
        if (columnIndex < 1 || columnIndex > cursor.getColumnCount())
            logAndThrowSQLException(log, new SQLDataException("Column index out of range."));
    }

    protected void checkCursorOperationPossible() throws SQLException {
        checkOpen();
        checkValidCursorPosition();
    }

    protected void checkOpen() throws SQLException {
        if (isClosed()) {
            logAndThrowSQLException(log, new ObjectClosedException("ResultSet closed."));
        }
    }

    private void checkValidCursorPosition() throws SQLException {
        if (isBeforeFirstX())
            logAndThrowSQLException(log, new SQLNonTransientException("Illegal operation before start of ResultSet."));
        else if (isAfterLastX())
            logAndThrowSQLException(log, new SQLNonTransientException("Illegal operation after end of ResultSet."));
    }

    private SQLException updatesNotSupportedException() {
        return new SQLFeatureNotSupportedException("Updates are not supported");
    }

}
