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

#ifdef WIN32
#include <windows.h>
#else
#endif
#include <sql.h>
#include <sqlext.h>
#include <iostream>
#include <thread>
#include <chrono>
// clang-format on

class TestSQLExecute : public testing::Test {
   public:
    TestSQLExecute() {
    }

    void SetUp() {
        ASSERT_NO_THROW(AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env,
                                       &m_conn, &m_hstmt, true, true));
    }

    void TearDown() {
        CloseCursor(&m_hstmt, true, true);
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }

    ~TestSQLExecute() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    std::wstring m_query =
        L"SELECT Origin FROM kibana_sample_data_flights LIMIT 5";
    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

class TestSQLPrepare : public testing::Test {
   public:
    TestSQLPrepare() {
    }

    void SetUp() {
        ASSERT_NO_THROW(AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env,
                                       &m_conn, &m_hstmt, true, true));
    }

    void TearDown() {
        CloseCursor(&m_hstmt, true, true);
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }

    ~TestSQLPrepare() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    std::wstring m_query =
        L"SELECT Origin FROM kibana_sample_data_flights LIMIT 5";
    std::wstring m_1_col =
        L"SELECT Origin FROM kibana_sample_data_flights LIMIT 5";
    std::wstring m_2_col =
        L"SELECT Origin, AvgTicketPrice FROM kibana_sample_data_flights LIMIT "
        L"5";
    std::wstring m_all_col =
        L"SELECT * FROM kibana_sample_data_flights LIMIT 5";
    const SQLSMALLINT m_1_col_cnt = 1;
    const SQLSMALLINT m_2_col_cnt = 2;
    const SQLSMALLINT m_all_col_cnt = 25;
    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

class TestSQLExecDirect : public testing::Test {
   public:
    TestSQLExecDirect() {
    }

    void SetUp() {
        ASSERT_NO_THROW(AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env,
                                       &m_conn, &m_hstmt, true, true));
    }

    void TearDown() {
        CloseCursor(&m_hstmt, true, true);
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }

    ~TestSQLExecDirect() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    std::wstring m_query =
        L"SELECT Origin FROM kibana_sample_data_flights LIMIT 5";
    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

class TestSQLSetCursorName : public testing::Test {
   public:
    TestSQLSetCursorName() {
    }

    void SetUp() {
        ASSERT_NO_THROW(AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env,
                                       &m_conn, &m_hstmt, true, true));
    }

    void TearDown() {
        CloseCursor(&m_hstmt, true, true);
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }

    ~TestSQLSetCursorName() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    std::wstring m_cursor_name = L"test_cursor";
    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

class TestSQLGetCursorName : public testing::Test {
   public:
    TestSQLGetCursorName() {
    }

    void SetUp() {
        ASSERT_NO_THROW(AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env,
                                       &m_conn, &m_hstmt, true, true));
        ASSERT_EQ(SQLSetCursorName(m_hstmt, (SQLTCHAR*)m_cursor_name.c_str(),
                                   SQL_NTS),
                  SQL_SUCCESS);
    }

    void TearDown() {
        CloseCursor(&m_hstmt, true, true);
        SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }

    ~TestSQLGetCursorName() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    std::wstring m_cursor_name = L"test_cursor";
    SQLSMALLINT m_wrong_buffer_length = 1;
    SQLTCHAR m_cursor_name_buf[20];
    SQLSMALLINT m_cursor_name_length;
    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

class TestSQLCancel : public testing::Test {
   public:
    TestSQLCancel() {
    }

    void SetUp() {
        ASSERT_NO_THROW(AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env,
                                       &m_conn, &m_hstmt, true, true));
    }

    void TearDown() {
        if (m_hstmt != SQL_NULL_HSTMT) {
            CloseCursor(&m_hstmt, true, true);
            SQLFreeHandle(SQL_HANDLE_STMT, m_hstmt);
            SQLDisconnect(m_conn);
            SQLFreeHandle(SQL_HANDLE_ENV, m_env);
        }
    }

    ~TestSQLCancel() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    typedef struct SQLCancelInfo {
        SQLHDBC hstmt;
        SQLRETURN ret_code;
    } SQLCancelInfo;

    const long long m_min_time_diff = 20;
    std::wstring m_query =
        L"SELECT * FROM kibana_sample_data_flights AS f WHERE "
        L"f.Origin=f.Origin";
    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

TEST_F(TestSQLExecute, NoPrepareCallError) {
    SQLRETURN ret = SQLExecute(m_hstmt);
    EXPECT_EQ(SQL_ERROR, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
}

TEST_F(TestSQLExecute, Success) {
    SQLRETURN ret = SQLPrepare(m_hstmt, (SQLTCHAR*)m_query.c_str(), SQL_NTS);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
    ASSERT_EQ(SQL_SUCCESS, ret);
    ret = SQLExecute(m_hstmt);
    EXPECT_EQ(SQL_SUCCESS, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
}

TEST_F(TestSQLExecute, ResetPrepareError) {
    SQLRETURN ret = SQLPrepare(m_hstmt, (SQLTCHAR*)m_query.c_str(), SQL_NTS);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
    ASSERT_EQ(SQL_SUCCESS, ret);
    ret = SQLPrepare(m_hstmt, NULL, SQL_NTS);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
    ASSERT_EQ(SQL_ERROR, ret);
    ret = SQLExecute(m_hstmt);
    EXPECT_EQ(SQL_ERROR, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
}

TEST_F(TestSQLPrepare, Success) {
    SQLRETURN ret = SQLPrepare(m_hstmt, (SQLTCHAR*)m_query.c_str(), SQL_NTS);
    EXPECT_EQ(SQL_SUCCESS, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
}

TEST_F(TestSQLPrepare, PrepareMetadata) {
    SQLRETURN ret = SQLPrepare(m_hstmt, (SQLTCHAR*)m_all_col.c_str(), SQL_NTS);
    EXPECT_EQ(SQL_SUCCESS, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
    SQLSMALLINT column_count = 0;
    EXPECT_TRUE(SQL_SUCCEEDED(SQLNumResultCols(m_hstmt, &column_count)));
    EXPECT_EQ(column_count, m_all_col_cnt);
    EXPECT_TRUE(SQL_SUCCEEDED(SQLFreeStmt(m_hstmt, SQL_CLOSE)));

    ret = SQLPrepare(m_hstmt, (SQLTCHAR*)m_2_col.c_str(), SQL_NTS);
    EXPECT_EQ(SQL_SUCCESS, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
    EXPECT_TRUE(SQL_SUCCEEDED(SQLNumResultCols(m_hstmt, &column_count)));
    EXPECT_EQ(column_count, m_2_col_cnt);
    EXPECT_TRUE(SQL_SUCCEEDED(SQLFreeStmt(m_hstmt, SQL_CLOSE)));

    ret = SQLPrepare(m_hstmt, (SQLTCHAR*)m_all_col.c_str(), SQL_NTS);
    EXPECT_EQ(SQL_SUCCESS, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
    EXPECT_TRUE(SQL_SUCCEEDED(SQLNumResultCols(m_hstmt, &column_count)));
    EXPECT_EQ(column_count, m_all_col_cnt);
    EXPECT_TRUE(SQL_SUCCEEDED(SQLFreeStmt(m_hstmt, SQL_CLOSE)));

    ret = SQLPrepare(m_hstmt, (SQLTCHAR*)m_1_col.c_str(), SQL_NTS);
    EXPECT_EQ(SQL_SUCCESS, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
    EXPECT_TRUE(SQL_SUCCEEDED(SQLNumResultCols(m_hstmt, &column_count)));
    EXPECT_EQ(column_count, m_1_col_cnt);
    EXPECT_TRUE(SQL_SUCCEEDED(SQLFreeStmt(m_hstmt, SQL_CLOSE)));
}

TEST_F(TestSQLPrepare, NullQueryError) {
    SQLRETURN ret = SQLPrepare(m_hstmt, NULL, SQL_NTS);
    EXPECT_EQ(SQL_ERROR, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
}

TEST_F(TestSQLExecDirect, Success) {
    SQLRETURN ret = SQLExecDirect(m_hstmt, (SQLTCHAR*)m_query.c_str(), SQL_NTS);
    EXPECT_EQ(SQL_SUCCESS, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
}

TEST_F(TestSQLExecDirect, NullQueryError) {
    SQLRETURN ret = SQLExecDirect(m_hstmt, NULL, SQL_NTS);
    EXPECT_EQ(SQL_ERROR, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
}

TEST_F(TestSQLSetCursorName, Success) {
    SQLRETURN ret =
        SQLSetCursorName(m_hstmt, (SQLTCHAR*)m_cursor_name.c_str(), SQL_NTS);
    EXPECT_EQ(SQL_SUCCESS, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
}

TEST_F(TestSQLGetCursorName, Success) {
    SQLRETURN ret =
        SQLGetCursorName(m_hstmt, m_cursor_name_buf,
                         IT_SIZEOF(m_cursor_name_buf), &m_cursor_name_length);
    EXPECT_EQ(SQL_SUCCESS, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
}

TEST_F(TestSQLGetCursorName, WrongLengthForCursorName) {
    SQLRETURN ret =
        SQLGetCursorName(m_hstmt, m_cursor_name_buf, m_wrong_buffer_length,
                         &m_cursor_name_length);
    EXPECT_EQ(SQL_SUCCESS_WITH_INFO, ret);
    LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
}

TEST_F(TestSQLCancel, NULLHandle) {
    SQLRETURN ret_exec = SQLCancel(NULL);
    EXPECT_EQ(ret_exec, SQL_INVALID_HANDLE);
}

// This test will fail because we are not cancelling in flight queries at this time. 
#if 0
TEST_F(TestSQLCancel, QueryInProgress) {
    // Create lambda thread
    auto f = [](SQLCancelInfo* info) {
        Sleep(10);
        info->ret_code = SQLCancel(info->hstmt);
    };

    // Launch cancel thread
    SQLCancelInfo cancel_info;
    cancel_info.hstmt = m_hstmt;
    cancel_info.ret_code = SQL_ERROR;
    std::thread thread_object(f, &cancel_info);

    // Time ExecDirect execution
    auto start = std::chrono::steady_clock::now();
    SQLRETURN ret_exec =
        SQLExecDirect(m_hstmt, (SQLTCHAR*)m_query.c_str(), SQL_NTS);
    auto end = std::chrono::steady_clock::now();
    auto time =
        std::chrono::duration_cast< std::chrono::milliseconds >(end - start)
            .count();

    // Join thread
    thread_object.join();

    // Check return codes and time diff
    ASSERT_LE(m_min_time_diff, time);
    EXPECT_EQ(ret_exec, SQL_ERROR);
    EXPECT_EQ(cancel_info.ret_code, SQL_SUCCESS);
}
#endif

TEST_F(TestSQLCancel, QueryNotSent) {
    SQLRETURN ret_exec = SQLCancel(m_hstmt);
    EXPECT_EQ(ret_exec, SQL_SUCCESS);
}

TEST_F(TestSQLCancel, QueryFinished) {
    SQLRETURN ret_exec =
        SQLExecDirect(m_hstmt, (SQLTCHAR*)m_query.c_str(), SQL_NTS);
    ASSERT_EQ(ret_exec, SQL_SUCCESS);

    ret_exec = SQLCancel(m_hstmt);
    EXPECT_EQ(ret_exec, SQL_SUCCESS);
}

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
    system("leaks itodbc_execution > leaks_itodbc_execution");
#endif
    return failures;
}
