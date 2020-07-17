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

import com.amazon.opendistroforelasticsearch.jdbc.protocol.JdbcQueryRequest;
import com.amazon.opendistroforelasticsearch.jdbc.types.ElasticsearchType;
import com.amazon.opendistroforelasticsearch.jdbc.types.TypeConverters;
import com.amazon.opendistroforelasticsearch.jdbc.internal.util.SqlParser;
import com.amazon.opendistroforelasticsearch.jdbc.logging.Logger;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.JdbcDateTimeFormatter;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.JdbcQueryParam;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PreparedStatementImpl extends StatementImpl implements PreparedStatement {
    // TODO - support String representations containing Timestamp With Timezone
    protected String sql;
    private JdbcQueryParam[] parameters;

    public PreparedStatementImpl(ConnectionImpl connection, String sql, Logger log) throws SQLException {
        super(connection, log);
        this.sql = sql;

        try {
            parameters = new JdbcQueryParam[SqlParser.countParameterMarkers(sql)];
        } catch (IllegalArgumentException iae) {
            logAndThrowSQLException(
                    log,
                    new SQLNonTransientException( "Error preparing SQL statement: "+iae.getMessage(), iae));
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        log.debug(() -> logEntry("executeQuery()"));
        checkOpen();
        ResultSet rs = executeQueryX(getFetchSize());
        log.debug(() -> logExit("executeQuery", rs));
        return rs;
    }

    protected ResultSet executeQueryX(int fetchSize) throws SQLException {
        checkParamsFilled();
        JdbcQueryRequest jdbcQueryRequest = new JdbcQueryRequest(sql, fetchSize);
        jdbcQueryRequest.setParameters(Arrays.asList(parameters));
        return executeQueryRequest(jdbcQueryRequest);
    }

    @Override
    public int executeUpdate() throws SQLException {
        throw new SQLFeatureNotSupportedException("Updates are not supported");
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        log.debug(() -> logEntry("setNull(%d, %d)", parameterIndex, sqlType));
        checkOpen();
        setParameter(parameterIndex, ElasticsearchType.fromJdbcType(JDBCType.valueOf(sqlType)).getTypeName(), null);
        log.debug(() -> logExit("setNull"));
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        log.debug(() -> logEntry("setBoolean(%d, %s)", parameterIndex, x));
        checkOpen();
        setObjectX(parameterIndex, x, Types.BOOLEAN);
        log.debug(() -> logExit("setBoolean"));
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        log.debug(() -> logEntry("setByte(%d, %d)", parameterIndex, x));
        checkOpen();
        setObjectX(parameterIndex, x, Types.TINYINT);
        log.debug(() -> logExit("setByte"));
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        log.debug(() -> logEntry("setShort(%d, %d)", parameterIndex, x));
        checkOpen();
        setObjectX(parameterIndex, x, Types.SMALLINT);
        log.debug(() -> logExit("setShort"));
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        log.debug(() -> logEntry("setInt(%d, %d)", parameterIndex, x));
        checkOpen();
        setObjectX(parameterIndex, x, Types.INTEGER);
        log.debug(() -> logExit("setInt"));
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        log.debug(() -> logEntry("setLong(%d, %d)", parameterIndex, x));
        checkOpen();
        setObjectX(parameterIndex, x, Types.BIGINT);
        log.debug(() -> logExit("setLong"));
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        log.debug(() -> logEntry("setFloat(%d, %f)", parameterIndex, x));
        checkOpen();
        setObjectX(parameterIndex, x, Types.REAL);
        log.debug(() -> logExit("setFloat"));
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        log.debug(() -> logEntry("setDouble(%d, %f)", parameterIndex, x));
        checkOpen();
        setObjectX(parameterIndex, x, Types.DOUBLE);
        log.debug(() -> logExit("setDouble"));
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException("BigDecimal is not supported");
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        log.debug(() -> logEntry("setString(%d, %s)", parameterIndex, x));
        checkOpen();
        setParameter(parameterIndex, ElasticsearchType.fromJdbcType(JDBCType.VARCHAR).getTypeName(), x);
        log.debug(() -> logExit("setString"));
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Setting byte arrays is not supported");
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        log.debug(() -> logEntry("setDate(%d, %s)", parameterIndex, x));
        checkOpen();
        setObjectX(parameterIndex, x, Types.DATE);
        log.debug(() -> logExit("setDate"));
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Setting Time is not supported");
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        log.debug(() -> logEntry("setTimestamp(%d, %s)", parameterIndex, x));
        checkOpen();
        setObjectX(parameterIndex, x, Types.TIMESTAMP);
        log.debug(() -> logExit("setTimestamp"));
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Setting ASCII Stream is not supported");
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Setting Unicode Stream is not supported");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Setting Binary Stream is not supported");
    }

    @Override
    public void clearParameters() throws SQLException {
        log.debug(() -> logEntry("clearParameters()"));
        for (int i = 0; i < parameters.length; i++)
            parameters[i] = null;
        log.debug(() -> logExit("clearParameters"));
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        log.debug(() -> logEntry("setObject(%d, %s, %d)", parameterIndex, x, targetSqlType));
        checkOpen();
        setObjectX(parameterIndex, x, targetSqlType, null);
        log.debug(() -> logExit("setObject"));
    }

    private void setObjectX(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setObjectX(parameterIndex, x, targetSqlType, null);
    }

    private void setObjectX(int parameterIndex, Object x, int targetSqlType, Map<String, Object> conversionParams)
            throws SQLException {
        JDBCType jdbcType = JDBCType.valueOf(targetSqlType);
        ElasticsearchType esType = ElasticsearchType.fromJdbcType(jdbcType);

        Object value = TypeConverters.getInstance(jdbcType).convert(x, null, conversionParams);

        // flow date/times in JDBC escape format
        if (jdbcType == JDBCType.TIMESTAMP) {
            value = JdbcDateTimeFormatter.JDBC_FORMAT.format((Timestamp) value);
        } else if (jdbcType == JDBCType.DATE) {
            value = JdbcDateTimeFormatter.JDBC_FORMAT.format((Date) value);
        }

        setParameter(parameterIndex, esType.getTypeName(), value);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        log.debug(() -> logEntry("setObject(%d, %s)", parameterIndex, x));
        checkOpen();
        setObjectX(parameterIndex, x, javaToSqlType(x));
        log.debug(() -> logExit("setObject"));
    }

    private JDBCType javaToJDBCType(Object x) throws SQLException {
        // Based on JDBC spec, Table B-4
        // TODO support java.time.* and JDBCType.TIME_WITH_TIMEZONE/JDBCType.TIMESTAMP_WITH_TIMEZONE

        if (x instanceof String)
            return JDBCType.VARCHAR;
        else if (x instanceof Boolean)
            return JDBCType.BOOLEAN;
        else if (x instanceof Byte)
            return JDBCType.TINYINT;
        else if (x instanceof Short)
            return JDBCType.SMALLINT;
        else if (x instanceof Integer)
            return JDBCType.INTEGER;
        else if (x instanceof Long)
            return JDBCType.BIGINT;
        else if (x instanceof Float)
            return JDBCType.REAL;
        else if (x instanceof Double)
            return JDBCType.DOUBLE;
        else if (x instanceof byte[])
            return JDBCType.VARBINARY;
        else if (x instanceof java.sql.Date)
            return JDBCType.DATE;
        else if (x instanceof java.sql.Timestamp)
            return JDBCType.TIMESTAMP;
        else
            throw new SQLDataException("Objects of type " + x.getClass().getName() + " not supported.");
    }

    private int javaToSqlType(Object x) throws SQLException {
        return javaToJDBCType(x).getVendorTypeNumber();
    }

    @Override
    public boolean execute() throws SQLException {
        log.debug(() -> logEntry("execute()"));
        checkOpen();
        executeQueryX(getFetchSize());
        log.debug(() -> logExit("execute", true));
        return true;
    }

    @Override
    public void addBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException("addBatch is not supported");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setCharacterStream is not supported");
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setRef is not supported");
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBlob is not supported");
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setClob is not supported");
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setArray is not supported");
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        // can only return metadata after query execution
        log.debug(() -> logEntry("getMetaData ()"));
        ResultSetMetaData rsmd = resultSet != null ? resultSet.getMetaData() : null;
        log.debug(() -> logExit("getMetaData", rsmd));
        return rsmd;
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        log.debug(() -> logEntry("setDate (%d, %s, %s)", parameterIndex, x,
                cal == null ? "null" : "Calendar TZ= " + cal.getTimeZone()));
        checkOpen();
        Map<String, Object> conversionParams = new HashMap<>();
        conversionParams.put("calendar", cal);

        setObjectX(parameterIndex, x, Types.DATE, conversionParams);
        log.debug(() -> logExit("setDate"));
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        // TODO - implement
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        log.debug(() -> logEntry("setTimestamp (%d, %s, %s)", parameterIndex, x,
                cal == null ? "null" : "Calendar TZ= " + cal.getTimeZone()));
        checkOpen();
        Map<String, Object> conversionParams = new HashMap<>();
        conversionParams.put("calendar", cal);
        setObjectX(parameterIndex, x, Types.TIMESTAMP, conversionParams);
        log.debug(() -> logExit("setTimestamp"));
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        // TODO - implement?
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setURL not supported");
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        // can't determine parameterMetadata as we don't have a query
        // "prepare" phase that could return us this info from the server
        // where the query gets parsed
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setRowId not supported");
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNString not supported");
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNCharacterStream not supported");
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob not supported");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setClob not supported");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBlob not supported");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob not supported");
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException("setSQLXML not supported");
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        // currently ignore scaleOrLength
        setObjectX(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setAsciiStream not supported");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBinaryStream not supported");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setCharacterStream not supported");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setAsciiStream not supported");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBinaryStream not supported");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("setCharacterStream not supported");
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNCharacterStream not supported");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("setClob not supported");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBlob not supported");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob not supported");
    }

    private void checkParamsFilled() throws SQLException {
        int filled = 0;

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] != null) {
                filled++;
            }
        }

        if (filled < parameters.length)
            logAndThrowSQLException(log, new SQLDataException(
                    String.format("Missing parameter values. The PreparedStatement " +
                                    "requires %d parameter values but only %d were found.",
                            parameters.length, filled)));
    }

    protected void setParameter(int index, String type, Object value) throws SQLException {
        checkParamIndex(index);
        parameters[index - 1] = new JdbcQueryParam(type, value);
    }

    private void checkParamIndex(int index) throws SQLException {
        if (parameters == null || index < 1 || index > parameters.length)
            logAndThrowSQLException(log, new SQLDataException("Invalid parameter index " + index));
    }
}
