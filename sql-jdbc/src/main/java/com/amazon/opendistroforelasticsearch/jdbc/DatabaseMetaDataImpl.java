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

import com.amazon.opendistroforelasticsearch.jdbc.internal.JdbcWrapper;
import com.amazon.opendistroforelasticsearch.jdbc.internal.Version;
import com.amazon.opendistroforelasticsearch.jdbc.logging.LoggingSource;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.ColumnDescriptor;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.QueryResponse;
import com.amazon.opendistroforelasticsearch.jdbc.types.ElasticsearchType;
import com.amazon.opendistroforelasticsearch.jdbc.logging.Logger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DatabaseMetaDataImpl implements DatabaseMetaData, JdbcWrapper, LoggingSource {

    private ConnectionImpl connection;
    private Logger log;

    public DatabaseMetaDataImpl(ConnectionImpl connection, Logger log) {
        this.connection = connection;
        this.log = log;
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return true;
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return true;
    }

    @Override
    public String getURL() throws SQLException {
        return connection.getUrl();
    }

    @Override
    public String getUserName() throws SQLException {
        return connection.getUser();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return true;
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return true;
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return "Elasticsearch";
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return connection.getClusterMetadata().getVersion().getFullVersion();
    }

    @Override
    public String getDriverName() throws SQLException {
        return "Elasticsearch JDBC Driver";
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return Version.Current.getFullVersion();
    }

    @Override
    public int getDriverMajorVersion() {
        return Version.Current.getMajor();
    }

    @Override
    public int getDriverMinorVersion() {
        return Version.Current.getMinor();
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return true;
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        // space to indicate quoting not supported currently
        return " ";
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        return "";
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return "";
    }

    @Override
    public String getStringFunctions() throws SQLException {
        return "";
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        return "";
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return "";
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        return "\\";
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return "";
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return "schema";
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return "procedure";
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        return "clusterName";
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return false;
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        return ".";
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        return 0;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return true;
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxStatements() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_NONE;
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        return level == Connection.TRANSACTION_NONE;
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        return emptyResultSet(log,
                rscd("PROCEDURE_CAT"),
                rscd("PROCEDURE_SCHEM"),
                rscd("PROCEDURE_NAME"),
                rscd("RESERVED4"),
                rscd("RESERVED5"),
                rscd("RESERVED6"),
                rscd("REMARKS"),
                rscd("PROCEDURE_TYPE", ElasticsearchType.SHORT.getTypeName()),
                rscd("SPECIFIC_NAME")
        );
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern)
            throws SQLException {
        return emptyResultSet(log,
                rscd("PROCEDURE_CAT"),
                rscd("PROCEDURE_SCHEM"),
                rscd("PROCEDURE_NAME"),
                rscd("COLUMN_NAME"),
                rscd("COLUMN_TYPE", ElasticsearchType.SHORT.getTypeName()),
                rscd("DATA_TYPE", ElasticsearchType.INTEGER.getTypeName()),
                rscd("TYPE_NAME"),
                rscd("PRECISION", ElasticsearchType.INTEGER.getTypeName()),
                rscd("LENGTH", ElasticsearchType.INTEGER.getTypeName()),
                rscd("SCALE", ElasticsearchType.SHORT.getTypeName()),
                rscd("RADIX", ElasticsearchType.SHORT.getTypeName()),
                rscd("NULLABLE", ElasticsearchType.SHORT.getTypeName()),
                rscd("REMARKS"),
                rscd("COLUMN_DEF"),
                rscd("SQL_DATA_TYPE", ElasticsearchType.INTEGER.getTypeName()),
                rscd("SQL_DATETIME_SUB", ElasticsearchType.INTEGER.getTypeName()),
                rscd("CHAR_OCTET_LENGTH", ElasticsearchType.INTEGER.getTypeName()),
                rscd("ORDINAL_POSITION", ElasticsearchType.INTEGER.getTypeName()),
                rscd("IS_NULLABLE"),
                rscd("SPECIFIC_NAME")
        );
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        // TODO - when server plugin supports PreparedStatement fully, implement this as a preparedStatment with params
        log.debug(() -> logMessage("getTables(%s, %s, %s, %s)",
                catalog, schemaPattern, tableNamePattern, Arrays.toString(types)));

        PreparedStatement pst = connection.prepareStatement("SHOW TABLES LIKE " +
                (tableNamePattern == null ? "%" : tableNamePattern));

        ResultSet resultSet = pst.executeQuery();

        log.debug(() -> logMessage("getTables returning: " + resultSet));
        return resultSet;
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        log.debug(() -> logMessage("getSchemas()"));

        ResultSet resultSet = getSchemasX(null, null);

        log.debug(() -> logMessage("getSchemas() returning: " + resultSet));
        return resultSet;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        log.debug(() -> logMessage("getSchemas(%s, %s)", catalog, schemaPattern));

        ResultSet resultSet = getSchemasX(catalog, schemaPattern);

        log.debug(() -> logMessage("getSchemas() returning: %s", resultSet));
        return resultSet;
    }

    private ResultSet getSchemasX(String catalog, String schemaPattern) throws SQLException {
        List<ResultSetColumnDescriptor> columnDescriptors = new ArrayList<>();
        columnDescriptors.add(rscd("TABLE_SCHEM"));
        columnDescriptors.add(rscd("TABLE_CATALOG"));

        List<List<Object>> dataRows = new ArrayList<>();

        if (clusterCatalogMatches(catalog) && clusterSchemaMatches(schemaPattern)) {
            dataRows.add(Arrays.asList("", getClusterCatalogName()));
        }
        return new ResultSetImpl(null, columnDescriptors, dataRows, log);
    }

    public Logger getLog() {
        return log;
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        log.debug(() -> logMessage("getCatalogs()"));
        ResultSet resultSet;

        List<ResultSetColumnDescriptor> columnDescriptors = new ArrayList<>();
        columnDescriptors.add(rscd("TABLE_CAT"));

        List<List<Object>> dataRows = new ArrayList<>();
        dataRows.add(Arrays.asList(getClusterCatalogName()));

        resultSet = new ResultSetImpl(null, columnDescriptors, dataRows, log);

        log.debug(() -> logMessage("getCatalogs() returning: %s", resultSet));
        return resultSet;
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        log.debug(() -> logMessage("getTableTypes()"));
        ResultSet resultSet;

        List<ResultSetColumnDescriptor> columnDescriptors = new ArrayList<>();
        columnDescriptors.add(rscd("TABLE_TYPE"));

        List<List<Object>> dataRows = new ArrayList<>();
        dataRows.add(Arrays.asList("BASE TABLE"));

        resultSet = new ResultSetImpl(null, columnDescriptors, dataRows, log);

        log.debug(() -> logMessage("getTableTypes() returning: %s", resultSet));
        return resultSet;
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        log.debug(() -> logMessage("getColumns(%s, %s, %s, %s)",
                catalog, schemaPattern, tableNamePattern, columnNamePattern));
        ColumnMetadataStatement statement = new ColumnMetadataStatement(connection, tableNamePattern, columnNamePattern, log);
        ResultSet resultSet = statement.executeQuery();
        log.debug(() -> logMessage("getColumns() returning: %s", resultSet));
        return resultSet;
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException("Privileges are not supported");
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        throw new SQLFeatureNotSupportedException("Privileges are not supported");
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        throw new SQLFeatureNotSupportedException("Row identifiers are not supported");
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        throw new SQLFeatureNotSupportedException("Version columns are not supported");
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        throw new SQLFeatureNotSupportedException("Primary keys are not supported");
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        throw new SQLFeatureNotSupportedException("Primary and Foreign keys are not supported");
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        throw new SQLFeatureNotSupportedException("Primary and Foreign keys are not supported");
    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog,
                                       String foreignSchema, String foreignTable) throws SQLException {
        throw new SQLFeatureNotSupportedException("Primary and Foreign keys are not supported");
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        log.debug(() -> logMessage("getTypeInfo()"));
        ResultSet resultSet;

        List<ResultSetColumnDescriptor> columnDescriptors = new ArrayList<>();
        columnDescriptors.add(rscd("TYPE_NAME"));
        columnDescriptors.add(rscd("DATA_TYPE", ElasticsearchType.INTEGER.getTypeName()));
        columnDescriptors.add(rscd("PRECISION", ElasticsearchType.INTEGER.getTypeName()));
        columnDescriptors.add(rscd("LITERAL_PREFIX"));
        columnDescriptors.add(rscd("LITERAL_SUFFIX"));
        columnDescriptors.add(rscd("CREATE_PARAMS"));
        columnDescriptors.add(rscd("NULLABLE", ElasticsearchType.SHORT.getTypeName()));
        columnDescriptors.add(rscd("CASE_SENSITIVE", ElasticsearchType.BOOLEAN.getTypeName()));
        columnDescriptors.add(rscd("SEARCHABLE", ElasticsearchType.SHORT.getTypeName()));
        columnDescriptors.add(rscd("UNSIGNED_ATTRIBUTE", ElasticsearchType.BOOLEAN.getTypeName()));
        columnDescriptors.add(rscd("FIXED_PREC_SCALE", ElasticsearchType.BOOLEAN.getTypeName()));
        columnDescriptors.add(rscd("AUTO_INCREMENT", ElasticsearchType.BOOLEAN.getTypeName()));
        columnDescriptors.add(rscd("LOCAL_TYPE_NAME"));
        columnDescriptors.add(rscd("MINIMUM_SCALE", ElasticsearchType.SHORT.getTypeName()));
        columnDescriptors.add(rscd("MAXIMUM_SCALE", ElasticsearchType.SHORT.getTypeName()));
        columnDescriptors.add(rscd("SQL_DATA_TYPE", ElasticsearchType.INTEGER.getTypeName()));
        columnDescriptors.add(rscd("SQL_DATETIME_SUB", ElasticsearchType.INTEGER.getTypeName()));
        columnDescriptors.add(rscd("NUM_PREC_RADIX", ElasticsearchType.INTEGER.getTypeName()));

        List<List<Object>> dataRows = new ArrayList<>();
        for (ElasticsearchType esType : ElasticsearchType.values()) {
            dataRows.add(Arrays.asList(
                    esType.name(),
                    esType.getJdbcType().getVendorTypeNumber(),
                    esType.getPrecision(),
                    "'",
                    "'",
                    null,
                    typeNullableUnknown,
                    (esType == ElasticsearchType.TEXT || esType == ElasticsearchType.KEYWORD), // case sensitive
                    typeSearchable,
                    !esType.isSigned(),
                    false,
                    false,
                    null,
                    null, // min scale - derive from Java type?
                    null, // max scale - derive from Java type?
                    null,
                    null,
                    10
            ));
        }

        resultSet = new ResultSetImpl(null, columnDescriptors, dataRows, log);

        log.debug(() -> logMessage("getTypeInfo() returning: %s", resultSet));
        return resultSet;
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        throw new SQLFeatureNotSupportedException("Table indices are not supported");
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        return type == ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        return type == ResultSet.TYPE_FORWARD_ONLY && ResultSet.CONCUR_READ_ONLY == concurrency;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        return emptyResultSet(log,
                rscd("TYPE_CAT"),
                rscd("TYPE_SCHEM"),
                rscd("TYPE_NAME"),
                rscd("CLASS_NAME"),
                rscd("DATA_TYPE", ElasticsearchType.INTEGER.getTypeName()),
                rscd("REMARKS"),
                rscd("BASE_TYPE", ElasticsearchType.SHORT.getTypeName())
        );
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        return emptyResultSet(log,
                rscd("TYPE_CAT"),
                rscd("TYPE_SCHEM"),
                rscd("TYPE_NAME"),
                rscd("SUPERTYPE_CAT"),
                rscd("SUPERTYPE_SCHEM"),
                rscd("SUPERTYPE_NAME")
        );
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return emptyResultSet(log,
                rscd("TABLE_CAT"),
                rscd("TABLE_SCHEM"),
                rscd("TABLE_NAME"),
                rscd("SUPERTABLE_NAME")
        );
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern)
            throws SQLException {
        return emptyResultSet(log,
                rscd("TYPE_CAT"),
                rscd("TYPE_SCHEM"),
                rscd("TYPE_NAME"),
                rscd("ATTR_NAME"),
                rscd("DATA_TYPE", ElasticsearchType.INTEGER.getTypeName()),
                rscd("ATTR_TYPE_NAME"),
                rscd("ATTR_SIZE", ElasticsearchType.INTEGER.getTypeName()),
                rscd("DECIMAL_DIGITS", ElasticsearchType.INTEGER.getTypeName()),
                rscd("NUM_PREC_RADIX", ElasticsearchType.INTEGER.getTypeName()),
                rscd("NULLABLE", ElasticsearchType.INTEGER.getTypeName()),
                rscd("REMARKS"),
                rscd("ATTR_DEF"),
                rscd("SQL_DATA_TYPE", ElasticsearchType.INTEGER.getTypeName()),
                rscd("SQL_DATETIME_SUB", ElasticsearchType.INTEGER.getTypeName()),
                rscd("CHAR_OCTET_LENGTH", ElasticsearchType.INTEGER.getTypeName()),
                rscd("ORDINAL_POSITION", ElasticsearchType.INTEGER.getTypeName()),
                rscd("IS_NULLABLE"),
                rscd("SCOPE_CATALOG"),
                rscd("SCOPE_SCHEMA"),
                rscd("SCOPE_TABLE"),
                rscd("SOURCE_DATA_TYPE", ElasticsearchType.SHORT.getTypeName())
        );
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return connection.getClusterMetadata().getVersion().getMajor();
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return connection.getClusterMetadata().getVersion().getMinor();
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 4;
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 2;
    }

    @Override
    public int getSQLStateType() throws SQLException {
        return DatabaseMetaData.sqlStateSQL;
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        throw new SQLException("Client info not implemented yet");
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return emptyResultSet(log,
                rscd("FUNCTION_CAT"),
                rscd("FUNCTION_SCHEM"),
                rscd("FUNCTION_NAME"),
                rscd("REMARKS"),
                rscd("FUNCTION_TYPE", ElasticsearchType.SHORT.getTypeName()),
                rscd("SPECIFIC_NAME")
        );
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern)
            throws SQLException {
        return emptyResultSet(log,
                rscd("FUNCTION_CAT"),
                rscd("FUNCTION_SCHEM"),
                rscd("FUNCTION_NAME"),
                rscd("COLUMN_NAME"),
                rscd("COLUMN_TYPE", ElasticsearchType.SHORT.getTypeName()),
                rscd("DATA_TYPE", ElasticsearchType.INTEGER.getTypeName()),
                rscd("TYPE_NAME"),
                rscd("PRECISION", ElasticsearchType.INTEGER.getTypeName()),
                rscd("LENGTH", ElasticsearchType.INTEGER.getTypeName()),
                rscd("SCALE", ElasticsearchType.SHORT.getTypeName()),
                rscd("RADIX", ElasticsearchType.SHORT.getTypeName()),
                rscd("NULLABLE", ElasticsearchType.SHORT.getTypeName()),
                rscd("REMARKS"),
                rscd("CHAR_OCTET_LENGTH", ElasticsearchType.INTEGER.getTypeName()),
                rscd("ORDINAL_POSITION", ElasticsearchType.INTEGER.getTypeName()),
                rscd("IS_NULLABLE"),
                rscd("SPECIFIC_NAME")
        );
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {

        return emptyResultSet(log,
                rscd("TABLE_CAT"),
                rscd("TABLE_SCHEM"),
                rscd("TABLE_NAME"),
                rscd("COLUMN_NAME"),
                rscd("DATA_TYPE", ElasticsearchType.INTEGER.getTypeName()),
                rscd("COLUMN_SIZE", ElasticsearchType.INTEGER.getTypeName()),
                rscd("DECIMAL_DIGITS", ElasticsearchType.INTEGER.getTypeName()),
                rscd("NUM_PREC_RADIX", ElasticsearchType.INTEGER.getTypeName()),
                rscd("COLUMN_USAGE"),
                rscd("REMARKS"),
                rscd("CHAR_OCTET_LENGTH", ElasticsearchType.INTEGER.getTypeName()),
                rscd("IS_NULLABLE")
        );
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return false;
    }

    static ResultSetColumnDescriptor rscd(String name) {
        return rscd(name, ElasticsearchType.TEXT.getTypeName(), null);
    }

    static ResultSetColumnDescriptor rscd(String name, String type) {
        return rscd(name, type, null);
    }

    static ResultSetColumnDescriptor rscd(String name, String type, String label) {
        return new ResultSetColumnDescriptor(name, type, label);
    }

    private String getClusterCatalogName() throws SQLException {
        return connection.getClusterMetadata().getClusterName();
    }

    private boolean clusterCatalogMatches(String catalog) throws SQLException {
        return catalog == null || "%".equalsIgnoreCase(catalog) || catalog.equalsIgnoreCase(getClusterCatalogName());
    }

    private boolean clusterSchemaMatches(String schema) {
        return schema == null || schema.equals("%") || schema.equals("");
    }

    private static ResultSetImpl emptyResultSet(Logger log, ResultSetColumnDescriptor... resultSetColumnDescriptors)
            throws SQLException {
        List<List<Object>> rows = new ArrayList<>(0);
        return new ResultSetImpl(null, Arrays.asList(resultSetColumnDescriptors), rows, log);
    }

    public static class ResultSetColumnDescriptor implements ColumnDescriptor {

        private String name;
        private String type;
        private String label;

        public ResultSetColumnDescriptor(String name, String type, String label) {
            this.name = name;
            this.type = type;
            this.label = label;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }


    static class ColumnMetadataStatement extends PreparedStatementImpl {
        // a special statement with custom logic for building the
        // ResultSet it returns on execution

        ColumnMetadataStatement(ConnectionImpl connection, String tableNamePattern, String columnNamePattern, Logger log)
                throws SQLException {
            // TODO - once sql plugin supports PreparedStatement fully, do this through a preparedStatement with params
            super(connection, "DESCRIBE TABLES LIKE " + tableNamePattern +
                (columnNamePattern != null ? (" COLUMNS LIKE " + columnNamePattern) : ""),
                log);
        }

        static class ColumnMetadataResultSet extends ResultSetImpl {

            ColumnMetadataResultSet(StatementImpl statement, List<? extends ColumnDescriptor> columnDescriptors,
                                    List<List<Object>> dataRows, Logger log) throws SQLException {
                super(statement, columnDescriptors, dataRows, log);
            }

            private int getDataType() {
                String esDataType = (String) cursor.getColumn(5);
                return ElasticsearchType.fromTypeName(esDataType, false).sqlTypeNumber();
            }

            private String getDataTypeName() {
                String esDataType = (String) cursor.getColumn(5);
                return ElasticsearchType.fromTypeName(esDataType, false).name();
            }

            private int getColumnSize() {
                String esDataType = (String) cursor.getColumn(5);
                return ElasticsearchType.fromTypeName(esDataType, false).getPrecision();
            }

            @Override
            protected Object getColumnFromCursor(int columnIndex) {
                // override behavior/return value of some of the columns
                // received from the server
                Object columnData = null;

                switch (columnIndex) {
                    case 5:
                        columnData = getDataType();
                        break;
                    case 6:
                        columnData = getDataTypeName();
                        break;
                    case 7:
                        columnData = getColumnSize();
                        break;
                    default:
                        columnData = super.getColumnFromCursor(columnIndex);

                }

                return columnData;
            }
        }

        @Override
        protected ResultSetImpl buildResultSet(QueryResponse queryResponse) throws SQLException {
            // enrich/update the resultSet with some JDBC specific data type info
            List<ResultSetColumnDescriptor> columnDescriptors = new ArrayList<>();

            for (ColumnDescriptor cd : queryResponse.getColumnDescriptors()) {
                if ("DATA_TYPE".equals(cd.getName()) || "COLUMN_SIZE".equals(cd.getName())) {
                    columnDescriptors.add(
                            rscd(cd.getName(), ElasticsearchType.INTEGER.getTypeName()));
                } else {
                    columnDescriptors.add(rscd(cd.getName(), cd.getType()));
                }
            }

            return new ColumnMetadataResultSet(this, columnDescriptors, queryResponse.getDatarows(), log);
        }
    }
}
