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

// clang-format off
#define NOMINMAX 1
#include "pch.h"
#include "unit_test_helper.h"
#include "it_odbc_helper.h"
// clang-format on

// General test constants and structures
#define BIND_SIZE 512
typedef struct bind_info {
    SQLUSMALLINT ordinal;
    SQLSMALLINT target_type;
    SQLPOINTER target;
    SQLLEN buffer_len;
    SQLLEN out_len;
    bind_info(SQLUSMALLINT _ordinal, SQLSMALLINT _target_type) {
        ordinal = _ordinal;
        target_type = _target_type;
        out_len = 0;
        data.resize(BIND_SIZE, '\0');
        buffer_len = data.size();
        target = data.data();
    }
    std::string AsString() {
        switch (target_type) {
            case SQL_C_CHAR:
                return reinterpret_cast< char* >(data.data());
                break;
            case SQL_C_LONG:
                return std::to_string(
                    *reinterpret_cast< unsigned long* >(data.data()));
                break;
            case SQL_C_SLONG:
                return std::to_string(
                    *reinterpret_cast< signed long* >(data.data()));
                break;
            case SQL_C_SHORT:
                return std::to_string(
                    *reinterpret_cast< signed short* >(data.data()));
                break;
            case SQL_C_SSHORT:
                return std::to_string(
                    *reinterpret_cast< unsigned short* >(data.data()));
                break;
            default:
                return "Unknown conversion type (" + std::to_string(target_type)
                       + ")";
                break;
        }
    }

   private:
    std::vector< SQLCHAR > data;
} bind_info;

// Column test constants and macro
const std::vector< std::string > flights_column_name = {
    "FlightNum",      "Origin",         "OriginLocation",  "DestLocation",
    "FlightDelay",    "DistanceMiles",  "FlightTimeMin",   "OriginWeather",
    "dayOfWeek",      "AvgTicketPrice", "Carrier",         "FlightDelayMin",
    "OriginRegion",   "DestAirportID",  "FlightDelayType", "timestamp",
    "Dest",           "FlightTimeHour", "Cancelled",       "DistanceKilometers",
    "OriginCityName", "DestWeather",    "OriginCountry",   "DestCountry",
    "DestRegion",     "DestCityName",   "OriginAirportID"};
const std::vector< std::string > flights_data_type = {
    "keyword", "keyword", "geo_point", "geo_point", "boolean", "float",
    "float",   "keyword", "integer",   "float",     "keyword", "integer",
    "keyword", "keyword", "keyword",   "date",      "keyword", "keyword",
    "boolean", "float",   "keyword",   "keyword",   "keyword", "keyword",
    "keyword", "keyword", "keyword"};
const std::string flights_catalog_odfe = "odfe-cluster";
const std::string flights_catalog_elas = "elasticsearch";
const std::string flights_table_name = "kibana_sample_data_flights";
const std::string flights_decimal_digits = "10";
const std::string flights_num_prec_radix = "2";

class TestSQLColumns : public testing::Test {
   public:
    TestSQLColumns() {
    }
    void SetUp() {
        AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env, &m_conn,
                       &m_hstmt, true, true);
    }
    void TearDown() {
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }
    ~TestSQLColumns() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

#define TEST_SQL_COLUMNS(test_name, catalog_patt, schema_patt, table_patt,   \
                         column_patt, enable_pattern, empty)                 \
    TEST_F(TestSQLColumns, test_name) {                                      \
        EXPECT_EQ(SQL_SUCCESS, SQLSetStmtAttr(m_hstmt, SQL_ATTR_METADATA_ID, \
                                              (void*)(!enable_pattern), 0)); \
        SQLColumns(m_hstmt, catalog_patt, SQL_NTS, schema_patt, SQL_NTS,     \
                   table_patt, SQL_NTS, column_patt, SQL_NTS);               \
        size_t result_count = 0;                                             \
        SQLRETURN ret;                                                       \
        while ((ret = SQLFetch(m_hstmt)) == SQL_SUCCESS)                     \
            result_count++;                                                  \
        EXPECT_EQ(ret, SQL_NO_DATA);                                         \
        if (empty)                                                           \
            EXPECT_EQ(result_count, static_cast< size_t >(0));               \
        else                                                                 \
            EXPECT_FALSE(result_count == 0);                                 \
    }

// Table test constants and macro
typedef struct table_data {
    std::string catalog_name;
    std::string schema_name;
    std::string table_name;
    std::string table_type;
    std::string remarks;
} table_data;

const std::vector< table_data > table_data_filtered{
    {"", "", "kibana_sample_data_ecommerce", "BASE TABLE", ""},
    {"", "", "kibana_sample_data_flights", "BASE TABLE", ""},
    {"", "", "kibana_sample_data_types", "BASE TABLE", ""}};
const std::vector< table_data > table_data_single{
    {"", "", "kibana_sample_data_flights", "BASE TABLE", ""}};
const std::vector< table_data > table_data_all{
    {"", "", "kibana_sample_data_ecommerce", "BASE TABLE", ""},
    {"", "", "kibana_sample_data_flights", "BASE TABLE", ""},
    {"", "", "kibana_sample_data_types", "BASE TABLE", ""},
};
const std::vector< table_data > excel_table_data_all{
    {"", "", "kibana_sample_data_ecommerce", "TABLE", ""},
    {"", "", "kibana_sample_data_flights", "TABLE", ""},
    {"", "", "kibana_sample_data_types", "TABLE", ""},
};
const std::vector< table_data > table_data_types{
    {"", "", "", "BASE TABLE", ""}};
const std::vector< table_data > table_data_schemas{{"", "", "", "", ""}};
const std::vector< table_data > table_data_catalogs{
    {"odfe-cluster", "", "", "", ""}};

class TestSQLTables : public testing::Test {
   public:
    TestSQLTables() {
    }
    void SetUp() {
        AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env, &m_conn,
                       &m_hstmt, true, true);
    }
    void TearDown() {
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }
    ~TestSQLTables() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

void CheckTableData(SQLHSTMT m_hstmt,
                    const std::vector< table_data >& expected_tables) {
    std::vector< bind_info > binds;
    binds.push_back(bind_info(1, SQL_C_CHAR));
    binds.push_back(bind_info(2, SQL_C_CHAR));
    binds.push_back(bind_info(3, SQL_C_CHAR));
    binds.push_back(bind_info(4, SQL_C_CHAR));

    for (auto& it : binds)
        SQLBindCol(m_hstmt, it.ordinal, it.target_type, it.target,
                   it.buffer_len, &it.out_len);

    SQLRETURN ret = SQL_ERROR;
    if (expected_tables.empty()) {
        // Verify that there is at least one table row.
        size_t result_count = 0;
        while ((ret = SQLFetch(m_hstmt)) == SQL_SUCCESS) {
            result_count++;
        }
        EXPECT_TRUE(result_count != 0);
    } else {
        // Fetch list of table rows from the Statement.
        std::vector< table_data > server_tables;
        while ((ret = SQLFetch(m_hstmt)) == SQL_SUCCESS) {
            table_data table = {binds[0].AsString(), binds[1].AsString(),
                                binds[2].AsString(), binds[3].AsString(), ""};
            server_tables.emplace_back(table);
        }

        // Make sure that all expected tables are found.
        for (auto expected_table : expected_tables) {
            EXPECT_TRUE(std::any_of(server_tables.begin(), server_tables.end(),
                                    [&](const table_data& d) {
                                        return d.table_name
                                               == expected_table.table_name;
                                    }));
        }
    }
    EXPECT_EQ(ret, SQL_NO_DATA);
}

#define TEST_SQL_TABLES(test_name, catalog, schema, table, table_type, exp,    \
                        enable_pattern, empty)                                 \
    TEST_F(TestSQLTables, test_name) {                                         \
        EXPECT_EQ(SQL_SUCCESS, SQLSetStmtAttr(m_hstmt, SQL_ATTR_METADATA_ID,   \
                                              (void*)(!enable_pattern), 0));   \
        EXPECT_TRUE(SQL_SUCCEEDED(SQLTables(m_hstmt, catalog, SQL_NTS, schema, \
                                            SQL_NTS, table, SQL_NTS,           \
                                            table_type, SQL_NTS)));            \
        if (empty) {                                                           \
            size_t result_count = 0;                                           \
            SQLRETURN ret;                                                     \
            while ((ret = SQLFetch(m_hstmt)) == SQL_SUCCESS)                   \
                result_count++;                                                \
            EXPECT_EQ(ret, SQL_NO_DATA);                                       \
            EXPECT_EQ(result_count, static_cast< size_t >(0));                 \
        } else                                                                 \
            CheckTableData(m_hstmt, exp);                                      \
    }

class TestSQLCatalogKeys : public testing::Test {
   public:
    TestSQLCatalogKeys() {
    }
    void SetUp() {
        AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env, &m_conn,
                       &m_hstmt, true, true);
    }
    void TearDown() {
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }
    ~TestSQLCatalogKeys() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

#define TEST_SQL_KEYS(test_name, test_function, ...)                     \
    TEST_F(TestSQLCatalogKeys, test_name) {                              \
        EXPECT_TRUE(SQL_SUCCEEDED(test_function(m_hstmt, __VA_ARGS__))); \
        size_t result_count = 0;                                         \
        SQLRETURN ret;                                                   \
        while ((ret = SQLFetch(m_hstmt)) == SQL_SUCCESS)                 \
            result_count++;                                              \
        EXPECT_EQ(ret, SQL_NO_DATA);                                     \
        EXPECT_EQ(result_count, static_cast< size_t >(0));               \
    }

// SQL Tables Tests
// NULL test
TEST_SQL_TABLES(Null, NULL, NULL, NULL, NULL, table_data_all, true, false);

// Catalog tests
TEST_SQL_TABLES(WildCatalogs, (SQLTCHAR*)L"%", (SQLTCHAR*)L"", (SQLTCHAR*)L"",
                NULL, table_data_catalogs, false, false)

// Schema tests
TEST_SQL_TABLES(WildSchema, (SQLTCHAR*)L"", (SQLTCHAR*)L"%", (SQLTCHAR*)L"",
                NULL, table_data_schemas, false, false)

// Table tests
TEST_SQL_TABLES(ValidTable, NULL, NULL, (SQLTCHAR*)L"kibana_sample_data%", NULL,
                table_data_filtered, true, false);
TEST_SQL_TABLES(SingleTable, NULL, NULL,
                (SQLTCHAR*)L"kibana_sample_data_flights", NULL,
                table_data_single, false, false);
TEST_SQL_TABLES(WildTable, NULL, NULL, (SQLTCHAR*)L"%", NULL, table_data_all,
                true, false);
TEST_SQL_TABLES(InvalidTable, NULL, NULL, (SQLTCHAR*)L"invalid_table", NULL, {},
                false, true);

// Table types tests
TEST_SQL_TABLES(ValidTableType, (SQLTCHAR*)L"", (SQLTCHAR*)L"", (SQLTCHAR*)L"",
                (SQLTCHAR*)L"%", table_data_types, false, false)
TEST_SQL_TABLES(InvalidTableType, (SQLTCHAR*)L"", (SQLTCHAR*)L"",
                (SQLTCHAR*)L"", (SQLTCHAR*)L"invalid_table_type",
                table_data_types, false, true)

// Excel SQLTables test
TEST_SQL_TABLES(ExcelSQLTables, (SQLTCHAR*)L"%", NULL, NULL,
                (SQLTCHAR*)L"TABLE,VIEW", excel_table_data_all, false, false);

// SQL Columns Tests
// NULL test
TEST_SQL_COLUMNS(Null, NULL, NULL, NULL, NULL, true, false)

// Table tests
TEST_SQL_COLUMNS(ValidTable, NULL, NULL, (SQLTCHAR*)L"kibana_%", NULL, true,
                 false)
TEST_SQL_COLUMNS(InvalidTable, NULL, NULL, (SQLTCHAR*)L"invalid_table", NULL,
                 true, true)

// Column tests
TEST_SQL_COLUMNS(ValidColumn, NULL, NULL, NULL, (SQLTCHAR*)L"FlightNum", true,
                 false)
TEST_SQL_COLUMNS(InvalidColumn, NULL, NULL, NULL, (SQLTCHAR*)L"invalid_column",
                 true, true)

// Table and column tests
TEST_SQL_COLUMNS(ValidTable_ValidColumn, NULL, NULL, (SQLTCHAR*)L"kibana_%",
                 NULL, true, false)
TEST_SQL_COLUMNS(ValidTable_InvalidColumn, NULL, NULL, (SQLTCHAR*)L"kibana_%",
                 (SQLTCHAR*)L"invalid_column", true, true)
TEST_SQL_COLUMNS(InvalidTable_ValidColumn, NULL, NULL,
                 (SQLTCHAR*)L"invalid_table", (SQLTCHAR*)L"FlightNum", true,
                 true)
TEST_SQL_COLUMNS(InvalidTable_InvalidColumn, NULL, NULL,
                 (SQLTCHAR*)L"invalid_table", (SQLTCHAR*)L"invalid_column",
                 true, true)

// Data validation
TEST_F(TestSQLColumns, FlightsValidation) {
    EXPECT_EQ(SQL_SUCCESS, SQLColumns(m_hstmt, NULL, SQL_NTS, NULL, SQL_NTS,
                                      (SQLTCHAR*)L"kibana_sample_data_flights",
                                      SQL_NTS, NULL, SQL_NTS));
    std::vector< bind_info > binds;
    binds.push_back(bind_info(1, SQL_C_CHAR));
    binds.push_back(bind_info(2, SQL_C_CHAR));
    binds.push_back(bind_info(3, SQL_C_CHAR));
    binds.push_back(bind_info(4, SQL_C_CHAR));
    binds.push_back(bind_info(5, SQL_C_SSHORT));
    binds.push_back(bind_info(6, SQL_C_CHAR));
    binds.push_back(bind_info(8, SQL_C_SLONG));
    binds.push_back(bind_info(9, SQL_C_SSHORT));
    binds.push_back(bind_info(10, SQL_C_SSHORT));
    binds.push_back(bind_info(11, SQL_C_SSHORT));
    binds.push_back(bind_info(12, SQL_C_CHAR));
    binds.push_back(bind_info(13, SQL_C_CHAR));
    binds.push_back(bind_info(14, SQL_C_SSHORT));
    binds.push_back(bind_info(15, SQL_C_SSHORT));
    binds.push_back(bind_info(16, SQL_C_SLONG));
    binds.push_back(bind_info(17, SQL_C_SLONG));
    binds.push_back(bind_info(18, SQL_C_CHAR));

    for (auto& it : binds)
        SQLBindCol(m_hstmt, it.ordinal, it.target_type, it.target,
                   it.buffer_len, &it.out_len);

    size_t column_idx = 0;
    while ((SQL_SUCCESS == SQLFetch(m_hstmt))
           && (column_idx < std::min(flights_column_name.size(),
                                     flights_data_type.size()))) {
        size_t ordinal = 0;
        for (auto& it : binds) {
            ordinal++;
            switch (ordinal) {
                case 1:
                    EXPECT_TRUE((it.AsString() == flights_catalog_elas)
                                || (it.AsString() == flights_catalog_odfe));
                    break;
                case 3:
                    EXPECT_EQ(it.AsString(), flights_table_name);
                    break;
                case 4:
                    EXPECT_EQ(it.AsString(), flights_column_name[column_idx]);
                    break;
                case 6:
                    EXPECT_EQ(it.AsString(), flights_data_type[column_idx]);
                    break;
                case 9:
                    EXPECT_EQ(it.AsString(), flights_decimal_digits);
                    break;
                case 10:
                    EXPECT_EQ(it.AsString(), flights_num_prec_radix);
                    break;
                case 16:
                    EXPECT_EQ(it.AsString(), std::to_string(column_idx + 1));
                    break;
                default:
                    EXPECT_TRUE(
                        ((it.AsString() == "0") || (it.AsString() == "")));
                    break;
            }
        }
        column_idx++;
    }
    EXPECT_EQ(column_idx, static_cast< size_t >(27));
}

// We expect an empty result set for PrimaryKeys and ForeignKeys
// Tableau specified catalog and table
// NULL args
TEST_SQL_KEYS(PrimaryKeys_NULL, SQLPrimaryKeys, NULL, SQL_NTS, NULL, SQL_NTS,
              NULL, SQL_NTS)
TEST_SQL_KEYS(ForeignKeys_NULL, SQLForeignKeys, NULL, SQL_NTS, NULL, SQL_NTS,
              NULL, SQL_NTS, NULL, SQL_NTS, NULL, SQL_NTS, NULL, SQL_NTS)

// Catalog specified
TEST_SQL_KEYS(PrimaryKeys_Catalog, SQLPrimaryKeys, NULL, SQL_NTS,
              (SQLTCHAR*)L"odfe-cluster", SQL_NTS, NULL, SQL_NTS)
TEST_SQL_KEYS(ForeignKeys_Catalog, SQLForeignKeys, NULL, SQL_NTS, NULL, SQL_NTS,
              NULL, SQL_NTS, NULL, SQL_NTS, (SQLTCHAR*)L"odfe-cluster", SQL_NTS,
              NULL, SQL_NTS)

// Table specified
TEST_SQL_KEYS(PrimaryKeys_Table, SQLPrimaryKeys, NULL, SQL_NTS, NULL, SQL_NTS,
              (SQLTCHAR*)L"kibana_sample_data_flights", SQL_NTS)
TEST_SQL_KEYS(ForeignKeys_Table, SQLForeignKeys, NULL, SQL_NTS, NULL, SQL_NTS,
              NULL, SQL_NTS, NULL, SQL_NTS, NULL, SQL_NTS,
              (SQLTCHAR*)L"kibana_sample_data_flights", SQL_NTS)

// Catalog and table specified
TEST_SQL_KEYS(PrimaryKeys_CatalogTable, SQLPrimaryKeys, NULL, SQL_NTS,
              (SQLTCHAR*)L"odfe-cluster", SQL_NTS,
              (SQLTCHAR*)L"kibana_sample_data_flights", SQL_NTS)
TEST_SQL_KEYS(ForeignKeys_CatalogTable, SQLForeignKeys, NULL, SQL_NTS, NULL,
              SQL_NTS, NULL, SQL_NTS, NULL, SQL_NTS, (SQLTCHAR*)L"odfe-cluster",
              SQL_NTS, (SQLTCHAR*)L"kibana_sample_data_flights", SQL_NTS)

// GetTypeInfo expected output struct
typedef struct sample_data_getTypeInfo_struct {
    std::string TYPE_NAME;
    SQLSMALLINT DATA_TYPE;
    SQLINTEGER COLUMN_SIZE;
    std::string LITERAL_PREFIX;
    std::string LITERAL_SUFFIX;
    std::string CREATE_PARAMS;
    SQLSMALLINT NULLABLE;
    SQLSMALLINT CASE_SENSITIVE;
    SQLSMALLINT SEARCHABLE;
    SQLSMALLINT UNSIGNED_ATTRIBUTE;
    SQLSMALLINT FIXED_PREC_SCALE;
    SQLSMALLINT AUTO_UNIQUE_VALUE;
    std::string LOCAL_TYPE_NAME;
    SQLSMALLINT MINIMUM_SCALE;
    SQLSMALLINT MAXIMUM_SCALE;
    SQLSMALLINT SQL_DATA_TYPE;
    SQLSMALLINT SQL_DATETIME_SUB;
    SQLINTEGER NUM_PREC_RADIX;
    SQLSMALLINT INTERVAL_PRECISION;
} sample_data_getTypeInfo_struct;

// GetTypeInfo expected output
const std::vector< sample_data_getTypeInfo_struct > sample_data_all_types_info{
    {"boolean", SQL_BIT, 1, "", "", "", 2, 0, 3, 1, 0, 0, "", 0, 0, SQL_BIT, 0,
     10, 0},
    {"byte", SQL_TINYINT, 3, "", "", "", 2, 0, 3, 0, 0, 0, "", 0, 0,
     SQL_TINYINT, 0, 10, 0},
    {"short", SQL_SMALLINT, 5, "", "", "", 2, 0, 3, 0, 0, 0, "", 0, 0,
     SQL_SMALLINT, 0, 10, 0},
    {"keyword", SQL_WVARCHAR, 256, "\"", "\"", "", 2, 1, 3, 1, 0, 0, "", 0, 0,
     SQL_WVARCHAR, 0, 10, 0},
    {"text", SQL_WVARCHAR, 2147483647, "\"", "\"", "", 2, 1, 3, 1, 0, 0, "", 0,
     0, SQL_WVARCHAR, 0, 10, 0},
    {"nested", SQL_WVARCHAR, 0, "\"", "\"", "", 2, 0, 3, 1, 0, 0, "", 0, 0,
     SQL_WVARCHAR, 0, 10, 0},
    {"object", SQL_WVARCHAR, 0, "\"", "\"", "", 2, 0, 3, 1, 0, 0, "", 0, 0,
     SQL_WVARCHAR, 0, 10, 0},
    {"integer", SQL_INTEGER, 10, "", "", "", 2, 0, 3, 0, 0, 0, "", 0, 0,
     SQL_INTEGER, 0, 10, 0},
    {"double", SQL_DOUBLE, 15, "", "", "", 2, 0, 3, 0, 0, 0, "", 0, 0,
     SQL_DOUBLE, 0, 10, 0},
    {"scaled_float", SQL_DOUBLE, 15, "", "", "", 2, 0, 3, 0, 0, 0, "", 0, 0,
     SQL_DOUBLE, 0, 10, 0},
    {"long", SQL_BIGINT, 19, "", "", "", 2, 0, 3, 0, 0, 0, "", 0, 0, SQL_BIGINT,
     0, 10, 0},
    {"half_float", SQL_REAL, 7, "", "", "", 2, 0, 3, 0, 0, 0, "", 0, 0,
     SQL_REAL, 0, 10, 0},
    {"float", SQL_REAL, 7, "", "", "", 2, 0, 3, 0, 0, 0, "", 0, 0, SQL_REAL, 0,
     10, 0},
    {"date", SQL_TYPE_TIMESTAMP, 24, "", "", "", 2, 0, 3, 1, 0, 0, "", 0, 0,
     SQL_TYPE_TIMESTAMP, 0, 10, 0}};

const std::vector< sample_data_getTypeInfo_struct >
    sample_data_single_type_multiple_row{
        {"keyword", SQL_WVARCHAR, 256, "\"", "\"", "", 2, 1, 3, 1, 0, 0, "", 0,
         0, SQL_WVARCHAR, 0, 10, 0},
        {"text", SQL_WVARCHAR, 2147483647, "\"", "\"", "", 2, 1, 3, 1, 0, 0, "",
         0, 0, SQL_WVARCHAR, 0, 10, 0},
        {"nested", SQL_WVARCHAR, 0, "\"", "\"", "", 2, 0, 3, 1, 0, 0, "", 0, 0,
         SQL_WVARCHAR, 0, 10, 0},
        {"object", SQL_WVARCHAR, 0, "\"", "\"", "", 2, 0, 3, 1, 0, 0, "", 0, 0,
         SQL_WVARCHAR, 0, 10, 0}};

const std::vector< sample_data_getTypeInfo_struct >
    sample_data_single_type_info{{"boolean", SQL_BIT, 1, "", "", "", 2, 0, 3, 1,
                                  0, 0, "", 0, 0, SQL_BIT, 0, 10, 0}};

const std::vector< sample_data_getTypeInfo_struct > sample_data_empty{};

void CheckGetTypeInfoData(
    SQLHSTMT m_hstmt,
    const std::vector< sample_data_getTypeInfo_struct >& sample_data) {
    std::vector< bind_info > binds;
    binds.push_back(bind_info(1, SQL_C_CHAR));
    binds.push_back(bind_info(2, SQL_C_SHORT));
    binds.push_back(bind_info(3, SQL_C_LONG));
    binds.push_back(bind_info(4, SQL_C_CHAR));
    binds.push_back(bind_info(5, SQL_C_CHAR));
    binds.push_back(bind_info(6, SQL_C_CHAR));
    binds.push_back(bind_info(7, SQL_C_SHORT));
    binds.push_back(bind_info(8, SQL_C_SHORT));
    binds.push_back(bind_info(9, SQL_C_SHORT));
    binds.push_back(bind_info(10, SQL_C_SHORT));
    binds.push_back(bind_info(11, SQL_C_SHORT));
    binds.push_back(bind_info(12, SQL_C_SHORT));
    binds.push_back(bind_info(13, SQL_C_CHAR));
    binds.push_back(bind_info(14, SQL_C_SHORT));
    binds.push_back(bind_info(15, SQL_C_SHORT));
    binds.push_back(bind_info(16, SQL_C_SHORT));
    binds.push_back(bind_info(17, SQL_C_SHORT));
    binds.push_back(bind_info(18, SQL_C_LONG));
    binds.push_back(bind_info(19, SQL_C_SHORT));

    for (auto& it : binds)
        SQLBindCol(m_hstmt, it.ordinal, it.target_type, it.target,
                   it.buffer_len, &it.out_len);

    SQLRETURN ret = SQL_ERROR;
    if (sample_data.empty()) {
        size_t result_count = 0;
        while ((ret = SQLFetch(m_hstmt)) == SQL_SUCCESS)
            result_count++;
        EXPECT_TRUE(result_count != 0);
    } else {
        size_t result_count = 0;
        for (; ((ret = SQLFetch(m_hstmt)) == SQL_SUCCESS)
               && (result_count < sample_data.size());
             result_count++) {
            auto it =
                std::find_if(sample_data.begin(), sample_data.end(),
                             [&](const sample_data_getTypeInfo_struct& d) {
                                 return d.TYPE_NAME == binds[0].AsString();
                             });
            ASSERT_NE(it, sample_data.end());
            EXPECT_EQ(binds[0].AsString(), it->TYPE_NAME);
            EXPECT_EQ(binds[1].AsString(), std::to_string(it->DATA_TYPE));
            EXPECT_EQ(binds[2].AsString(), std::to_string(it->COLUMN_SIZE));
            EXPECT_EQ(binds[3].AsString(), it->LITERAL_PREFIX);
            EXPECT_EQ(binds[4].AsString(), it->LITERAL_SUFFIX);
            EXPECT_EQ(binds[5].AsString(), it->CREATE_PARAMS);
            EXPECT_EQ(binds[6].AsString(), std::to_string(it->NULLABLE));
            EXPECT_EQ(binds[7].AsString(), std::to_string(it->CASE_SENSITIVE));
            EXPECT_EQ(binds[8].AsString(), std::to_string(it->SEARCHABLE));
            EXPECT_EQ(binds[9].AsString(),
                      std::to_string(it->UNSIGNED_ATTRIBUTE));
            EXPECT_EQ(binds[10].AsString(),
                      std::to_string(it->FIXED_PREC_SCALE));
            EXPECT_EQ(binds[11].AsString(),
                      std::to_string(it->AUTO_UNIQUE_VALUE));
            EXPECT_EQ(binds[12].AsString(), it->LOCAL_TYPE_NAME);
            EXPECT_EQ(binds[13].AsString(), std::to_string(it->MINIMUM_SCALE));
            EXPECT_EQ(binds[14].AsString(), std::to_string(it->MAXIMUM_SCALE));
            EXPECT_EQ(binds[15].AsString(), std::to_string(it->SQL_DATA_TYPE));
            EXPECT_EQ(binds[16].AsString(),
                      std::to_string(it->SQL_DATETIME_SUB));
            EXPECT_EQ(binds[17].AsString(), std::to_string(it->NUM_PREC_RADIX));
            EXPECT_EQ(binds[18].AsString(),
                      std::to_string(it->INTERVAL_PRECISION));
        }
        EXPECT_EQ(result_count, sample_data.size());
    }
    EXPECT_EQ(ret, SQL_NO_DATA);
}

class TestSQLGetTypeInfo : public testing::Test {
   public:
    TestSQLGetTypeInfo() {
    }
    void SetUp() {
        AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env, &m_conn,
                       &m_hstmt, true, true);
    }
    void TearDown() {
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }
    ~TestSQLGetTypeInfo() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

#define TEST_SQL_GET_TYPE_INFO(test_name, data_type, empty, exp_out)    \
    TEST_F(TestSQLGetTypeInfo, test_name) {                             \
        EXPECT_TRUE(SQL_SUCCEEDED(SQLGetTypeInfo(m_hstmt, data_type))); \
        if (empty) {                                                    \
            size_t result_count = 0;                                    \
            SQLRETURN ret;                                              \
            while ((ret = SQLFetch(m_hstmt)) == SQL_SUCCESS)            \
                result_count++;                                         \
            EXPECT_EQ(ret, SQL_NO_DATA);                                \
            EXPECT_EQ(result_count, static_cast< size_t >(0));          \
        } else                                                          \
            CheckGetTypeInfoData(m_hstmt, exp_out);                     \
    }

TEST_SQL_GET_TYPE_INFO(AllTypes, SQL_ALL_TYPES, 0, sample_data_all_types_info)

TEST_SQL_GET_TYPE_INFO(SingleTypeMultipleRows, SQL_WVARCHAR, 0,
                       sample_data_single_type_multiple_row)

TEST_SQL_GET_TYPE_INFO(SingleType, SQL_BIT, 0, sample_data_single_type_info)

TEST_SQL_GET_TYPE_INFO(UnsupportedType, SQL_DECIMAL, 1, sample_data_empty)

int main(int argc, char** argv) {
#ifdef __APPLE__
    // Enable malloc logging for detecting memory leaks.
    system("export MallocStackLogging=1");
#endif
    testing::internal::CaptureStdout();
    ::testing::InitGoogleTest(&argc, argv);

    int failures = RUN_ALL_TESTS();

    std::string output = testing::internal::GetCapturedStdout();
    std::cout << output << std::endl;
    std::cout << (failures ? "Not all tests passed." : "All tests passed")
              << std::endl;
    WriteFileIfSpecified(argv, argv + argc, "-fout", output);

#ifdef __APPLE__
    // Disable malloc logging and report memory leaks
    system("unset MallocStackLogging");
    system("leaks itodbc_catalog > leaks_itodbc_catalog");
#endif
    return failures;
}
