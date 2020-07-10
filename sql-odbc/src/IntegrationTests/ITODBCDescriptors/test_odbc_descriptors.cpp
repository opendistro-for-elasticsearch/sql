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
#include "pch.h"
#include "unit_test_helper.h"
#include "it_odbc_helper.h"
// clang-format on

const std::wstring data_set = L"kibana_sample_data_flights";
const std::wstring single_col = L"Origin";
const std::wstring single_row = L"1";
const uint64_t multi_row_cnt = 25;
const uint64_t multi_col_cnt = 25;
const uint64_t single_col_cnt = 1;
const std::wstring multi_col = L"*";

inline void ExecuteQuery(const std::wstring& column,
                         const std::wstring& dataset, const std::wstring& count,
                         SQLHSTMT* hstmt) {
    std::wstring statement = QueryBuilder(column, dataset, count);
    SQLRETURN ret = SQLExecDirect(*hstmt, (SQLTCHAR*)statement.c_str(),
                                  (SQLINTEGER)statement.length());
    LogAnyDiagnostics(SQL_HANDLE_STMT, *hstmt, ret);
    ASSERT_TRUE(SQL_SUCCEEDED(ret));
}

class TestSQLCopyDesc : public testing::Test {
   public:
    TestSQLCopyDesc() {
    }

    void SetUp() {
        AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env, &m_conn,
                       &m_hstmt, true, true);
        SQLAllocHandle(SQL_HANDLE_STMT, m_conn, &m_hstmt_copy);
    }

    void TearDown() {
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }

    ~TestSQLCopyDesc() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
    SQLHSTMT m_hstmt_copy = SQL_NULL_HSTMT;
    SQLHDESC m_ard_hdesc = SQL_NULL_HDESC;
    SQLHDESC m_ard_hdesc_copy = SQL_NULL_HDESC;
    SQLHDESC m_ird_hdesc_copy = SQL_NULL_HDESC;
};

TEST_F(TestSQLCopyDesc, TestCopyArdToArd) {
    ExecuteQuery(multi_col, data_set, std::to_wstring(multi_row_cnt), &m_hstmt);

    SQLGetStmtAttr(m_hstmt, SQL_ATTR_APP_ROW_DESC, &m_ard_hdesc, 0, NULL);
    SQLGetStmtAttr(m_hstmt_copy, SQL_ATTR_APP_ROW_DESC, &m_ard_hdesc_copy, 0,
                   NULL);

    EXPECT_EQ(SQL_SUCCESS, SQLCopyDesc(m_ard_hdesc, m_ard_hdesc_copy));
}

TEST_F(TestSQLCopyDesc, TestNotCopyArdToIrd) {
    ExecuteQuery(multi_col, data_set, std::to_wstring(multi_row_cnt), &m_hstmt);

    SQLGetStmtAttr(m_hstmt, SQL_ATTR_APP_ROW_DESC, &m_ard_hdesc, 0, NULL);
    SQLGetStmtAttr(m_hstmt_copy, SQL_ATTR_IMP_ROW_DESC, &m_ird_hdesc_copy, 0,
                   NULL);

    EXPECT_EQ(SQL_ERROR, SQLCopyDesc(m_ard_hdesc, m_ird_hdesc_copy));
    EXPECT_TRUE(CheckSQLSTATE(SQL_HANDLE_DESC, m_ird_hdesc_copy,
                              SQLSTATE_GENERAL_ERROR, true));
}

class TestSQLSetDescField : public testing::Test {
   public:
    TestSQLSetDescField() {
    }

    void SetUp() {
        AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env, &m_conn,
                       &m_hstmt, true, true);
        SQLGetStmtAttr(m_hstmt, SQL_ATTR_APP_ROW_DESC, &m_ard_hdesc, 0, NULL);
        SQLGetStmtAttr(m_hstmt, SQL_ATTR_IMP_ROW_DESC, &m_ird_hdesc, 0, NULL);
    }

    void TearDown() {
        if (m_ard_hdesc != SQL_NULL_HDESC) {
            SQLFreeHandle(SQL_HANDLE_DESC, m_ard_hdesc);
        }
        if (m_ird_hdesc != SQL_NULL_HDESC) {
            SQLFreeHandle(SQL_HANDLE_DESC, m_ird_hdesc);
        }
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }

    ~TestSQLSetDescField() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
    SQLHDESC m_ard_hdesc = SQL_NULL_HDESC;
    SQLHDESC m_ird_hdesc = SQL_NULL_HDESC;
    SQLSMALLINT m_rec_number = 0;
    SQLSMALLINT m_field_identifier;
    SQLINTEGER m_buffer_length = SQL_NTS;
};

// Template for tests of SQLSetDescField
#define TEST_SQL_SET_DESC_FIELD(test_name, identifier, buffer_length, rec_num, \
                                value_ptr_assignment, expected_val, hdesc,     \
                                check_state)                                   \
    TEST_F(TestSQLSetDescField, test_name) {                                   \
        ExecuteQuery(multi_col, data_set, std::to_wstring(multi_row_cnt),      \
                     &m_hstmt);                                                \
        m_field_identifier = identifier;                                       \
        m_buffer_length = buffer_length;                                       \
        m_rec_number = rec_num;                                                \
        value_ptr_assignment;                                                  \
        EXPECT_EQ(expected_val,                                                \
                  SQLSetDescField(hdesc, m_rec_number, m_field_identifier,     \
                                  (SQLPOINTER)m_value_ptr, m_buffer_length));  \
        if (check_state)                                                       \
            EXPECT_TRUE(                                                       \
                CheckSQLSTATE(SQL_HANDLE_DESC, hdesc,                          \
                              SQLSTATE_INVALID_DESCRIPTOR_FIELD_IDENTIFIER));  \
    }
#ifdef WIN32
#pragma warning(push)
// This warning detects an attempt to assign a 32-bit value to a 64-bit pointer
// type
#pragma warning(disable : 4312)
#elif defined(__APPLE__)
// This warning detects casts from integer types to void*.
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wint-to-void-pointer-cast"
#endif  // WIN32

// Descriptor Header Fields Tests

TEST_SQL_SET_DESC_FIELD(Test_SQL_DESC_ALLOC_TYPE, SQL_DESC_ALLOC_TYPE,
                        SQL_IS_SMALLINT, 0,
                        SQLSMALLINT m_value_ptr = SQL_DESC_ALLOC_USER;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(Test_SQL_DESC_ARRAY_SIZE, SQL_DESC_ARRAY_SIZE, SQL_NTS,
                        0, SQLULEN m_value_ptr = single_col_cnt;
                        , SQL_SUCCESS, m_ard_hdesc, 0);

TEST_SQL_SET_DESC_FIELD(Test_SQL_DESC_ARRAY_STATUS_PTR,
                        SQL_DESC_ARRAY_STATUS_PTR, SQL_NTS, 0, SQLUSMALLINT foo;
                        SQLUSMALLINT* m_value_ptr = &foo;
                        , SQL_SUCCESS, m_ard_hdesc, 0);

TEST_SQL_SET_DESC_FIELD(Test_SQL_DESC_BIND_OFFSET_PTR, SQL_DESC_BIND_OFFSET_PTR,
                        SQL_NTS, 0, SQLLEN foo;
                        SQLLEN* m_value_ptr = &foo;
                        , SQL_SUCCESS, m_ard_hdesc, 0);

TEST_SQL_SET_DESC_FIELD(Test_SQL_DESC_BIND_TYPE, SQL_DESC_BIND_TYPE, SQL_NTS, 0,
                        SQLINTEGER m_value_ptr = SQL_BIND_BY_COLUMN;
                        , SQL_SUCCESS, m_ard_hdesc, 0);

TEST_SQL_SET_DESC_FIELD(Test_SQL_DESC_COUNT, SQL_DESC_COUNT, SQL_IS_SMALLINT, 0,
                        SQLSMALLINT m_value_ptr = 25;
                        , SQL_SUCCESS, m_ard_hdesc, 0);

TEST_SQL_SET_DESC_FIELD(Test_SQL_DESC_ROWS_PROCESSED_PTR,
                        SQL_DESC_ROWS_PROCESSED_PTR, SQL_NTS, 0, SQLULEN foo;
                        SQLULEN* m_value_ptr = &foo;
                        , SQL_SUCCESS, m_ird_hdesc, 0);

// Descriptor Record Fields Tests

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_AUTO_UNIQUE_VALUE,
                        SQL_DESC_AUTO_UNIQUE_VALUE, SQL_NTS, 1,
                        SQLINTEGER m_value_ptr = 0;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_BASE_COLUMN_NAME,
                        SQL_DESC_BASE_COLUMN_NAME, SQL_NTS, 1,
                        SQLCHAR m_value_ptr[255] = "Origin";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_BASE_TABLE_NAME,
                        SQL_DESC_BASE_TABLE_NAME, SQL_NTS, 1,
                        SQLCHAR m_value_ptr[255] = "kibana_sample_data_flights";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_CASE_SENSITIVE,
                        SQL_DESC_CASE_SENSITIVE, SQL_NTS, 1,
                        SQLINTEGER m_value_ptr = 1;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_CATALOG_NAME,
                        SQL_DESC_CATALOG_NAME, SQL_NTS, 1,
                        SQLCHAR m_value_ptr[255] = "";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_CONCISE_TYPE,
                        SQL_DESC_CONCISE_TYPE, SQL_IS_INTEGER, 1,
                        SQLSMALLINT m_value_ptr = SQL_WLONGVARCHAR;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_DATA_PTR, SQL_DESC_DATA_PTR,
                        SQL_IS_POINTER, 1, SQLPOINTER m_value_ptr = NULL;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_DATETIME_INTERVAL_CODE,
                        SQL_DESC_DATETIME_INTERVAL_CODE, SQL_IS_SMALLINT, 1,
                        SQLSMALLINT m_value_ptr = 0;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_DATETIME_INTERVAL_PRECISION,
                        SQL_DESC_DATETIME_INTERVAL_PRECISION, SQL_IS_INTEGER, 1,
                        SQLINTEGER m_value_ptr = 0;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_DISPLAY_SIZE,
                        SQL_DESC_DISPLAY_SIZE, SQL_IS_POINTER, 1,
                        SQLLEN m_value_ptr = 32766;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_FIXED_PREC_SCALE,
                        SQL_DESC_FIXED_PREC_SCALE, SQL_IS_INTEGER, 1,
                        SQLSMALLINT m_value_ptr = 0;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_INDICATOR_PTR,
                        SQL_DESC_INDICATOR_PTR, SQL_IS_INTEGER, 1,
                        SQLLEN m_value_ptr = 0;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_LABEL, SQL_DESC_LABEL,
                        SQL_NTS, 1, SQLCHAR m_value_ptr[255] = "Origin";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_LENGTH, SQL_DESC_LENGTH,
                        SQL_IS_INTEGER, 1, SQLULEN m_value_ptr = 32766;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_LITERAL_PREFIX,
                        SQL_DESC_LITERAL_PREFIX, SQL_NTS, 1,
                        SQLCHAR m_value_ptr[255] = "";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_LITERAL_SUFFIX,
                        SQL_DESC_LITERAL_SUFFIX, SQL_NTS, 1,
                        SQLCHAR m_value_ptr[255] = "";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_LOCAL_TYPE_NAME,
                        SQL_DESC_LOCAL_TYPE_NAME, SQL_NTS, 1,
                        SQLCHAR m_value_ptr[255] = "varchar";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_NAME, SQL_DESC_NAME,
                        SQL_NTS, 1, SQLCHAR m_value_ptr[255] = "Origin";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_NULLABLE, SQL_DESC_NULLABLE,
                        SQL_IS_SMALLINT, 1, SQLSMALLINT m_value_ptr = 1;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_NUM_PREC_RADIX,
                        SQL_DESC_NUM_PREC_RADIX, SQL_IS_INTEGER, 1,
                        SQLINTEGER m_value_ptr = 0;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_OCTET_LENGTH,
                        SQL_DESC_OCTET_LENGTH, SQL_NTS, 1,
                        SQLLEN m_value_ptr = 0;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_OCTET_LENGTH_PTR,
                        SQL_DESC_OCTET_LENGTH_PTR, SQL_IS_INTEGER, 1,
                        SQLLEN m_value_ptr = 0;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_PARAMETER_TYPE,
                        SQL_DESC_PARAMETER_TYPE, SQL_IS_SMALLINT, 1,
                        SQLSMALLINT m_value_ptr = 1;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_PRECISION,
                        SQL_DESC_PRECISION, SQL_IS_SMALLINT, 1,
                        SQLSMALLINT m_value_ptr = 30585;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_ROWVER, SQL_DESC_ROWVER,
                        SQL_NTS, 1, SQLSMALLINT m_value_ptr = 1;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_SCALE, SQL_DESC_SCALE,
                        SQL_IS_SMALLINT, 1, SQLSMALLINT m_value_ptr = 0;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_SCHEMA_NAME,
                        SQL_DESC_SCHEMA_NAME, SQL_NTS, 1,
                        SQLCHAR m_value_ptr[255] = "";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_SEARCHABLE,
                        SQL_DESC_SEARCHABLE, SQL_IS_SMALLINT, 1,
                        SQLSMALLINT m_value_ptr = SQL_PRED_SEARCHABLE;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_TABLE_NAME,
                        SQL_DESC_TABLE_NAME, SQL_NTS, 1,
                        SQLCHAR m_value_ptr[255] = "kibana_sample_data_flights";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_TYPE, SQL_DESC_TYPE,
                        SQL_IS_SMALLINT, 1,
                        SQLSMALLINT m_value_ptr = SQL_WLONGVARCHAR;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_TYPE_NAME,
                        SQL_DESC_TYPE_NAME, SQL_NTS, 1,
                        SQLCHAR m_value_ptr[255] = "varchar";
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_UNNAMED, SQL_DESC_UNNAMED,
                        SQL_IS_SMALLINT, 1, SQLSMALLINT m_value_ptr = SQL_NAMED;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_UNSIGNED, SQL_DESC_UNSIGNED,
                        SQL_IS_SMALLINT, 1, SQLSMALLINT m_value_ptr = SQL_TRUE;
                        , SQL_ERROR, m_ird_hdesc, 1);

TEST_SQL_SET_DESC_FIELD(TestUndefinedError_SQL_DESC_UPDATABLE,
                        SQL_DESC_UPDATABLE, SQL_IS_SMALLINT, 1,
                        SQLSMALLINT m_value_ptr = SQL_ATTR_READONLY;
                        , SQL_ERROR, m_ird_hdesc, 1);

class TestSQLGetDescField : public testing::Test {
   public:
    TestSQLGetDescField() {
    }

    void SetUp() {
        AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env, &m_conn,
                       &m_hstmt, true, true);
        SQLGetStmtAttr(m_hstmt, SQL_ATTR_APP_ROW_DESC, &m_ard_hdesc, 0, NULL);
        SQLGetStmtAttr(m_hstmt, SQL_ATTR_IMP_ROW_DESC, &m_ird_hdesc, 0, NULL);
    }

    void TearDown() {
        if (m_ard_hdesc != SQL_NULL_HDESC) {
            SQLFreeHandle(SQL_HANDLE_DESC, m_ard_hdesc);
        }
        if (m_ird_hdesc != SQL_NULL_HDESC) {
            SQLFreeHandle(SQL_HANDLE_DESC, m_ird_hdesc);
        }
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }

    ~TestSQLGetDescField() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
    SQLHDESC m_ard_hdesc = SQL_NULL_HDESC;
    SQLHDESC m_ird_hdesc = SQL_NULL_HDESC;
    SQLSMALLINT m_rec_number = 0;
    SQLSMALLINT m_field_identifier;
    SQLINTEGER m_buffer_length = 0;
    SQLINTEGER m_string_length_ptr = 0;
};

// Template for tests of SQLGetDescField
#define TEST_SQL_GET_DESC_FIELD(test_name, identifier, buffer_length, rec_num, \
                                value_ptr_assignment, expected_val, hdesc,     \
                                check_state, check_data, check_data_value)     \
    TEST_F(TestSQLGetDescField, test_name) {                                   \
        ExecuteQuery(multi_col, data_set, std::to_wstring(multi_row_cnt),      \
                     &m_hstmt);                                                \
        m_field_identifier = identifier;                                       \
        m_buffer_length = buffer_length;                                       \
        m_rec_number = rec_num;                                                \
        value_ptr_assignment;                                                  \
        EXPECT_EQ(expected_val,                                                \
                  SQLGetDescField(hdesc, m_rec_number, m_field_identifier,     \
                                  &m_value_ptr, m_buffer_length,               \
                                  &m_string_length_ptr));                      \
        if (check_state)                                                       \
            EXPECT_TRUE(                                                       \
                CheckSQLSTATE(SQL_HANDLE_DESC, hdesc,                          \
                              SQLSTATE_INVALID_DESCRIPTOR_FIELD_IDENTIFIER));  \
        if (check_data)                                                        \
            EXPECT_EQ((uint64_t)check_data_value, (uint64_t)m_value_ptr);      \
    }

// Descriptor Header Fields Tests

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_ALLOC_TYPE, SQL_DESC_ALLOC_TYPE, 0, 0,
                        SQLSMALLINT m_value_ptr = 0;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 1, SQL_DESC_ALLOC_AUTO);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_ARRAY_SIZE, SQL_DESC_ARRAY_SIZE, 0, 0,
                        SQLULEN m_value_ptr = 0;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 1, single_col_cnt);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_ARRAY_STATUS_PTR,
                        SQL_DESC_ARRAY_STATUS_PTR, 0, 0,
                        SQLUSMALLINT* m_value_ptr;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_BIND_OFFSET_PTR, SQL_DESC_BIND_OFFSET_PTR,
                        0, 0, SQLLEN* m_value_ptr;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_BIND_TYPE, SQL_DESC_BIND_TYPE, 0, 0,
                        SQLINTEGER m_value_ptr = 0;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 1, SQL_BIND_BY_COLUMN);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_COUNT, SQL_DESC_COUNT, 0, 0,
                        SQLSMALLINT m_value_ptr = 0;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 1, multi_col_cnt);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_ROWS_PROCESSED_PTR,
                        SQL_DESC_ROWS_PROCESSED_PTR, 0, 0, SQLULEN* m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

// Descriptor Record Fields Tests

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_AUTO_UNIQUE_VALUE,
                        SQL_DESC_AUTO_UNIQUE_VALUE, 0, 1,
                        SQLINTEGER m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_BASE_COLUMN_NAME,
                        SQL_DESC_BASE_COLUMN_NAME, 255, 1,
                        SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_BASE_TABLE_NAME, SQL_DESC_BASE_TABLE_NAME,
                        255, 1, SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_CASE_SENSITIVE, SQL_DESC_CASE_SENSITIVE,
                        0, 1, SQLINTEGER m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_CATALOG_NAME, SQL_DESC_CATALOG_NAME, 255,
                        1, SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_CONCISE_TYPE, SQL_DESC_CONCISE_TYPE, 0, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_DATA_PTR, SQL_DESC_DATA_PTR, 0, 1,
                        SQLPOINTER m_value_ptr;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_DATETIME_INTERVAL_CODE,
                        SQL_DESC_DATETIME_INTERVAL_CODE, 0, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 0, 0);

// This field contains the interval leading precision if the SQL_DESC_TYPE field
// is SQL_INTERVAL. As SQL_INTERVAL support is disabled because some
// applications are unhappy with it, this test should return SQL_ERROR as
// DESC_INVALID_DESCRIPTOR_IDENTIFIER
TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_DATETIME_INTERVAL_PRECISION,
                        SQL_DESC_DATETIME_INTERVAL_PRECISION, 0, 1,
                        SQLINTEGER m_value_ptr = 0;
                        , SQL_ERROR, m_ard_hdesc, 1, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_DISPLAY_SIZE, SQL_DESC_DISPLAY_SIZE, 0, 1,
                        SQLLEN m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_FIXED_PREC_SCALE,
                        SQL_DESC_FIXED_PREC_SCALE, 0, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_INDICATOR_PTR, SQL_DESC_INDICATOR_PTR, 0,
                        1, SQLLEN m_value_ptr;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_LABEL, SQL_DESC_LABEL, 255, 1,
                        SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_LENGTH, SQL_DESC_LENGTH, 0, 1,
                        SQLULEN m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_LITERAL_PREFIX, SQL_DESC_LITERAL_PREFIX,
                        255, 1, SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_LITERAL_SUFFIX, SQL_DESC_LITERAL_SUFFIX,
                        255, 1, SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_LOCAL_TYPE_NAME, SQL_DESC_LOCAL_TYPE_NAME,
                        255, 1, SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_NAME, SQL_DESC_NAME, 255, 1,
                        SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_NULLABLE, SQL_DESC_NULLABLE, 0, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_NUM_PREC_RADIX, SQL_DESC_NUM_PREC_RADIX,
                        0, 1, SQLINTEGER m_value_ptr;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_OCTET_LENGTH, SQL_DESC_OCTET_LENGTH, 0, 1,
                        SQLLEN m_value_ptr;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_OCTET_LENGTH_PTR,
                        SQL_DESC_OCTET_LENGTH_PTR, 0, 1, SQLLEN m_value_ptr;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_PRECISION, SQL_DESC_PRECISION, 0, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ard_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_ROWVER, SQL_DESC_ROWVER, 0, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_SCALE, SQL_DESC_SCALE, 0, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_SCHEMA_NAME, SQL_DESC_SCHEMA_NAME, 255, 1,
                        SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_SEARCHABLE, SQL_DESC_SEARCHABLE, 0, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_TABLE_NAME, SQL_DESC_TABLE_NAME, 255, 1,
                        SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_TYPE, SQL_DESC_TYPE, 255, 1,
                        SQLSMALLINT m_value_ptr = 0;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_TYPE_NAME, SQL_DESC_TYPE_NAME, 255, 1,
                        SQLCHAR m_value_ptr[255];
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_UNNAMED, SQL_DESC_UNNAMED, 0, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_UNSIGNED, SQL_DESC_UNSIGNED, 0, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);

TEST_SQL_GET_DESC_FIELD(Test_SQL_DESC_UPDATABLE, SQL_DESC_UPDATABLE, 255, 1,
                        SQLSMALLINT m_value_ptr;
                        , SQL_SUCCESS, m_ird_hdesc, 0, 0, 0);
#ifdef WIN32
#pragma warning(pop)
#elif __APPLE__
#pragma clang diagnostic pop
#endif

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
    system("leaks itodbc_descriptors > leaks_itodbc_descriptors");
#endif
    return failures;
}
