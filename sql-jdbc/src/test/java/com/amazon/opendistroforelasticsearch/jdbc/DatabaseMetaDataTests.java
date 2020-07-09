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

import com.amazon.opendistroforelasticsearch.jdbc.DatabaseMetaDataImpl.ColumnMetadataStatement;
import com.amazon.opendistroforelasticsearch.jdbc.config.ConnectionConfig;
import com.amazon.opendistroforelasticsearch.jdbc.logging.NoOpLogger;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.ClusterMetadata;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.ConnectionResponse;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.Protocol;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.ProtocolFactory;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.ResponseException;
import com.amazon.opendistroforelasticsearch.jdbc.transport.Transport;
import com.amazon.opendistroforelasticsearch.jdbc.transport.TransportFactory;
import com.amazon.opendistroforelasticsearch.jdbc.types.ElasticsearchType;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.MockResultSet;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.MockResultSetMetaData;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.MockResultSetRows;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DatabaseMetaDataTests {

    @Test
    void testClusterMetaData() throws ResponseException, IOException, SQLException {
        Connection con = getMockConnection();

        DatabaseMetaData dbmd = con.getMetaData();

        assertNotNull(dbmd, "DatabaseMetaData is null");

        assertEquals("6.3.2", dbmd.getDatabaseProductVersion());
        assertEquals(6, dbmd.getDatabaseMajorVersion());
        assertEquals(3, dbmd.getDatabaseMinorVersion());
        assertEquals("Elasticsearch", dbmd.getDatabaseProductName());

        assertFalse(con.isClosed());
    }

    @Test
    void testGetAttributes() throws ResponseException, IOException, SQLException {
        Connection con = getMockConnection();

        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("TYPE_CAT")
                .column("TYPE_SCHEM")
                .column("TYPE_NAME")
                .column("ATTR_NAME")
                .column("DATA_TYPE", ElasticsearchType.INTEGER)
                .column("ATTR_TYPE_NAME")
                .column("ATTR_SIZE", ElasticsearchType.INTEGER)
                .column("DECIMAL_DIGITS", ElasticsearchType.INTEGER)
                .column("NUM_PREC_RADIX", ElasticsearchType.INTEGER)
                .column("NULLABLE", ElasticsearchType.INTEGER)
                .column("REMARKS")
                .column("ATTR_DEF")
                .column("SQL_DATA_TYPE", ElasticsearchType.INTEGER)
                .column("SQL_DATETIME_SUB", ElasticsearchType.INTEGER)
                .column("CHAR_OCTET_LENGTH", ElasticsearchType.INTEGER)
                .column("ORDINAL_POSITION", ElasticsearchType.INTEGER)
                .column("IS_NULLABLE")
                .column("SCOPE_CATALOG")
                .column("SCOPE_SCHEMA")
                .column("SCOPE_TABLE")
                .column("SOURCE_DATA_TYPE", ElasticsearchType.SHORT)
                .build();

        DatabaseMetaData dbmd = con.getMetaData();

        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getAttributes("", null, null, null);

        new MockResultSet(mockResultSetMetaData, MockResultSetRows.emptyResultSetRows()).assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }

    @Test
    void testGetSuperTables() throws ResponseException, IOException, SQLException {
        Connection con = getMockConnection();

        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("TABLE_CAT")
                .column("TABLE_SCHEM")
                .column("TABLE_NAME")
                .column("SUPERTABLE_NAME")
                .build();

        DatabaseMetaData dbmd = con.getMetaData();

        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getSuperTables("", null, null);

        new MockResultSet(mockResultSetMetaData, MockResultSetRows.emptyResultSetRows()).assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }

    @Test
    void testGetSuperTypes() throws ResponseException, IOException, SQLException {
        Connection con = getMockConnection();

        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("TYPE_CAT")
                .column("TYPE_SCHEM")
                .column("TYPE_NAME")
                .column("SUPERTYPE_CAT")
                .column("SUPERTYPE_SCHEM")
                .column("SUPERTYPE_NAME")
                .build();

        DatabaseMetaData dbmd = con.getMetaData();

        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getSuperTypes("", null, null);

        new MockResultSet(mockResultSetMetaData, MockResultSetRows.emptyResultSetRows()).assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }

    @Test
    void testGetUDTs() throws ResponseException, IOException, SQLException {
        Connection con = getMockConnection();

        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("TYPE_CAT")
                .column("TYPE_SCHEM")
                .column("TYPE_NAME")
                .column("CLASS_NAME")
                .column("DATA_TYPE", ElasticsearchType.INTEGER)
                .column("REMARKS")
                .column("BASE_TYPE", ElasticsearchType.SHORT)
                .build();

        DatabaseMetaData dbmd = con.getMetaData();

        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getUDTs("", null, null, null);

        new MockResultSet(mockResultSetMetaData, MockResultSetRows.emptyResultSetRows()).assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }

    @Test
    void testGetProcedures() throws ResponseException, IOException, SQLException {
        Connection con = getMockConnection();

        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("PROCEDURE_CAT")
                .column("PROCEDURE_SCHEM")
                .column("PROCEDURE_NAME")
                .column("RESERVED4")
                .column("RESERVED5")
                .column("RESERVED6")
                .column("REMARKS")
                .column("PROCEDURE_TYPE", ElasticsearchType.SHORT)
                .column("SPECIFIC_NAME")
                .build();

        DatabaseMetaData dbmd = con.getMetaData();

        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getProcedures(null, null, null);

        new MockResultSet(mockResultSetMetaData, MockResultSetRows.emptyResultSetRows()).assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }

    @Test
    void testGetProcedureColumns() throws ResponseException, IOException, SQLException {
        Connection con = getMockConnection();

        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("PROCEDURE_CAT")
                .column("PROCEDURE_SCHEM")
                .column("PROCEDURE_NAME")
                .column("COLUMN_NAME")
                .column("COLUMN_TYPE", ElasticsearchType.SHORT)
                .column("DATA_TYPE", ElasticsearchType.INTEGER)
                .column("TYPE_NAME")
                .column("PRECISION", ElasticsearchType.INTEGER)
                .column("LENGTH", ElasticsearchType.INTEGER)
                .column("SCALE", ElasticsearchType.SHORT)
                .column("RADIX", ElasticsearchType.SHORT)
                .column("NULLABLE", ElasticsearchType.SHORT)
                .column("REMARKS")
                .column("COLUMN_DEF")
                .column("SQL_DATA_TYPE", ElasticsearchType.INTEGER)
                .column("SQL_DATETIME_SUB", ElasticsearchType.INTEGER)
                .column("CHAR_OCTET_LENGTH", ElasticsearchType.INTEGER)
                .column("ORDINAL_POSITION", ElasticsearchType.INTEGER)
                .column("IS_NULLABLE")
                .column("SPECIFIC_NAME")
                .build();

        DatabaseMetaData dbmd = con.getMetaData();

        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getProcedureColumns("", null, null, null);

        new MockResultSet(mockResultSetMetaData, MockResultSetRows.emptyResultSetRows()).assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }

    @Test
    void testGetFunctions() throws ResponseException, IOException, SQLException {
        Connection con = getMockConnection();

        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("FUNCTION_CAT")
                .column("FUNCTION_SCHEM")
                .column("FUNCTION_NAME")
                .column("REMARKS")
                .column("FUNCTION_TYPE", ElasticsearchType.SHORT)
                .column("SPECIFIC_NAME")
                .build();

        DatabaseMetaData dbmd = con.getMetaData();

        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getFunctions("", null, null);

        new MockResultSet(mockResultSetMetaData, MockResultSetRows.emptyResultSetRows()).assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }


    @Test
    void testGetFunctionColumns() throws ResponseException, IOException, SQLException {
        Connection con = getMockConnection();

        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("FUNCTION_CAT")
                .column("FUNCTION_SCHEM")
                .column("FUNCTION_NAME")
                .column("COLUMN_NAME")
                .column("COLUMN_TYPE", ElasticsearchType.SHORT)
                .column("DATA_TYPE", ElasticsearchType.INTEGER)
                .column("TYPE_NAME")
                .column("PRECISION", ElasticsearchType.INTEGER)
                .column("LENGTH", ElasticsearchType.INTEGER)
                .column("SCALE", ElasticsearchType.SHORT)
                .column("RADIX", ElasticsearchType.SHORT)
                .column("NULLABLE", ElasticsearchType.SHORT)
                .column("REMARKS")
                .column("CHAR_OCTET_LENGTH", ElasticsearchType.INTEGER)
                .column("ORDINAL_POSITION", ElasticsearchType.INTEGER)
                .column("IS_NULLABLE")
                .column("SPECIFIC_NAME")
                .build();

        DatabaseMetaData dbmd = con.getMetaData();

        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getFunctionColumns("", null, null, null);

        new MockResultSet(mockResultSetMetaData, MockResultSetRows.emptyResultSetRows()).assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }

    @Test
    void testPseudoColumns() throws ResponseException, IOException, SQLException {
        Connection con = getMockConnection();

        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("TABLE_CAT")
                .column("TABLE_SCHEM")
                .column("TABLE_NAME")
                .column("COLUMN_NAME")
                .column("DATA_TYPE", ElasticsearchType.INTEGER)
                .column("COLUMN_SIZE", ElasticsearchType.INTEGER)
                .column("DECIMAL_DIGITS", ElasticsearchType.INTEGER)
                .column("NUM_PREC_RADIX", ElasticsearchType.INTEGER)
                .column("COLUMN_USAGE")
                .column("REMARKS")
                .column("CHAR_OCTET_LENGTH", ElasticsearchType.INTEGER)
                .column("IS_NULLABLE")
                .build();

        DatabaseMetaData dbmd = con.getMetaData();

        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getPseudoColumns("", null, null, null);

        new MockResultSet(mockResultSetMetaData, MockResultSetRows.emptyResultSetRows()).assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }

    @Test
    void testGetCatalogs() throws Exception {
        Connection con = getMockConnection();

        DatabaseMetaData dbmd = con.getMetaData();
        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getCatalogs();

        getExpectedCatalogsResultSet().assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }

    @Test
    void testGetSchemas() throws Exception {
        Connection con = getMockConnection();

        DatabaseMetaData dbmd = con.getMetaData();
        assertNotNull(dbmd, "DatabaseMetaData is null");

        ResultSet rs = dbmd.getSchemas();

        getExpectedSchemaResultSet().assertMatches(rs);
        assertDoesNotThrow(rs::close);
    }

    @Test
    void testGetSchemasWithValidPatterns() throws Exception {
        Connection con = getMockConnection();

        DatabaseMetaData dbmd = con.getMetaData();
        assertNotNull(dbmd, "DatabaseMetaData is null");

        assertValidSchemaResultSet(dbmd.getSchemas(null, null));
        assertValidSchemaResultSet(dbmd.getSchemas(null, "%"));
        assertValidSchemaResultSet(dbmd.getSchemas(null, ""));
        assertValidSchemaResultSet(dbmd.getSchemas("mock-cluster", null));
        assertValidSchemaResultSet(dbmd.getSchemas("mock-cluster", "%"));
        assertValidSchemaResultSet(dbmd.getSchemas("mock-cluster", ""));
    }

    @Test
    void testGetSchemasWithInvalidPatterns() throws Exception {
        Connection con = getMockConnection();

        DatabaseMetaData dbmd = con.getMetaData();
        assertNotNull(dbmd, "DatabaseMetaData is null");

        assertEmptySchemaResultSet(dbmd.getSchemas("", null));
        assertEmptySchemaResultSet(dbmd.getSchemas("some-cat", "%"));
        assertEmptySchemaResultSet(dbmd.getSchemas("mock-cluster", "some-schema"));
        assertEmptySchemaResultSet(dbmd.getSchemas(null, "some-schema"));
    }
    
    @Test
    void testGetColumnsWithoutColumnNamePattern() throws Exception {
        Connection con = getMockConnection();
        
        ColumnMetadataStatement stmt = new ColumnMetadataStatement((ConnectionImpl)con, "TABLE_%", null, NoOpLogger.INSTANCE);
        assertEquals("DESCRIBE TABLES LIKE TABLE_%", stmt.sql);
        assertDoesNotThrow(stmt::close);
    }
    
    @Test
    void testGetColumnsWithColumnNamePattern() throws Exception {
        Connection con = getMockConnection();
        
        ColumnMetadataStatement stmt = new ColumnMetadataStatement((ConnectionImpl)con, "TABLE_%", "COLUMN_%", NoOpLogger.INSTANCE);
        assertEquals("DESCRIBE TABLES LIKE TABLE_% COLUMNS LIKE COLUMN_%", stmt.sql);
        assertDoesNotThrow(stmt::close);
    }

    private void assertValidSchemaResultSet(ResultSet rs) throws SQLException {
        getExpectedSchemaResultSet().assertMatches(rs);
    }

    private void assertEmptySchemaResultSet(ResultSet rs) throws SQLException {
        getEmptySchemaResultSet().assertMatches(rs);
    }

    private MockResultSet getExpectedCatalogsResultSet() {
        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("TABLE_CAT", ElasticsearchType.TEXT)
                .build();

        MockResultSetRows mockResultSetRows = MockResultSetRows.builder()
                .row()
                .column("mock-cluster")
                .build();

        return new MockResultSet(mockResultSetMetaData, mockResultSetRows);
    }


    private MockResultSet getExpectedSchemaResultSet() {
        MockResultSetRows mockResultSetRows = MockResultSetRows.builder()
                .row()
                .column("")
                .column("mock-cluster")
                .build();

        return new MockResultSet(getMockSchemaResultSetMetaData(), mockResultSetRows);
    }

    private MockResultSet getEmptySchemaResultSet() {
        return new MockResultSet(getMockSchemaResultSetMetaData(), MockResultSetRows.emptyResultSetRows());
    }


    private MockResultSetMetaData getMockSchemaResultSetMetaData() {
        return MockResultSetMetaData.builder()
                .column("TABLE_SCHEM", ElasticsearchType.TEXT)
                .column("TABLE_CATALOG", ElasticsearchType.TEXT)
                .build();
    }

    private Connection getMockConnection() throws ResponseException, IOException, SQLException {
        TransportFactory mockTransportFactory = mock(TransportFactory.class);
        when(mockTransportFactory.getTransport(any(), any(), any()))
                .thenReturn(mock(Transport.class));

        ProtocolFactory mockProtocolFactory = mock(ProtocolFactory.class);
        Protocol mockProtocol = mock(Protocol.class);

        when(mockProtocolFactory.getProtocol(any(ConnectionConfig.class), any(Transport.class)))
                .thenReturn(mockProtocol);

        ClusterMetadata mockClusterMetaData = mock(ClusterMetadata.class);
        ElasticsearchVersion mockEV = mock(ElasticsearchVersion.class);

        when(mockEV.getFullVersion()).thenReturn("6.3.2");
        when(mockEV.getMajor()).thenReturn(6);
        when(mockEV.getMinor()).thenReturn(3);
        when(mockEV.getRevision()).thenReturn(2);

        when(mockClusterMetaData.getVersion()).thenReturn(mockEV);
        when(mockClusterMetaData.getClusterName()).thenReturn("mock-cluster");
        when(mockClusterMetaData.getClusterUUID()).thenReturn("mock-cluster-uuid");

        ConnectionResponse mockConnectionResponse = mock(ConnectionResponse.class);
        when(mockConnectionResponse.getClusterMetadata()).thenReturn(mockClusterMetaData);

        when(mockProtocol.connect(anyInt())).thenReturn(mockConnectionResponse);

        Connection con = new ConnectionImpl(mock(ConnectionConfig.class),
                mockTransportFactory, mockProtocolFactory, NoOpLogger.INSTANCE);
        return con;
    }
}
