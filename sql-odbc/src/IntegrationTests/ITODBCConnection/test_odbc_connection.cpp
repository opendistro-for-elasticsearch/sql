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
#endif
#include <sql.h>
#include <sqlext.h>
#include <iostream>
#include <chrono>
// clang-format on

#define IT_SIZEOF(x) (NULL == (x) ? 0 : (sizeof((x)) / sizeof((x)[0])))

// SQLConnect constants
const SQLCHAR invalid_dsn_charwidth[]{"test_dsn"};
std::wstring wdsn_name = L"test_dsn";
std::wstring user = L"admin";
std::wstring pass = L"admin";

// SQLDriverConnect constants
std::wstring dsn_conn_string = L"DSN=test_dsn";

class TestSQLConnect : public testing::Test {
   public:
    TestSQLConnect() {
    }

    void SetUp() {
        AllocConnection(&m_env, &m_conn, true, true);
    }

    void TearDown() {
        if (SQL_NULL_HDBC != m_conn) {
            SQLFreeHandle(SQL_HANDLE_DBC, m_conn);
            SQLFreeHandle(SQL_HANDLE_ENV, m_env);
        }
    }

    ~TestSQLConnect() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
};

TEST_F(TestSQLConnect, SqlSuccess) {
    SQLRETURN ret = SQLConnect(
        m_conn, (SQLTCHAR*)wdsn_name.c_str(), SQL_NTS, (SQLTCHAR*)user.c_str(),
        static_cast< SQLSMALLINT >(user.length()), (SQLTCHAR*)pass.c_str(),
        static_cast< SQLSMALLINT >(pass.length()));

    LogAnyDiagnostics(SQL_HANDLE_DBC, m_conn, ret);
    EXPECT_EQ(SQL_SUCCESS, ret);
}

TEST_F(TestSQLConnect, SqlError) {
    SQLRETURN ret = SQLConnect(
        m_conn, (SQLTCHAR*)invalid_dsn_charwidth, SQL_NTS,
        (SQLTCHAR*)user.c_str(), static_cast< SQLSMALLINT >(user.length()),
        (SQLTCHAR*)pass.c_str(), static_cast< SQLSMALLINT >(pass.length()));

    LogAnyDiagnostics(SQL_HANDLE_DBC, m_conn, ret);
    EXPECT_EQ(SQL_ERROR, ret);
}

class TestSQLDriverConnect : public testing::Test {
   public:
    TestSQLDriverConnect() {
    }

    void SetUp() {
        AllocConnection(&m_env, &m_conn, true, true);
    }

    void TearDown() {
        if (SQL_NULL_HDBC != m_conn) {
            SQLFreeHandle(SQL_HANDLE_DBC, m_conn);
            SQLFreeHandle(SQL_HANDLE_ENV, m_env);
        }
    }

    ~TestSQLDriverConnect() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLTCHAR m_out_conn_string[1024];
    SQLSMALLINT m_out_conn_string_length;
};

TEST_F(TestSQLDriverConnect, DSNConnectionString) {
    SQLRETURN ret = SQLDriverConnect(
        m_conn, NULL, (SQLTCHAR*)dsn_conn_string.c_str(), SQL_NTS,
        m_out_conn_string, IT_SIZEOF(m_out_conn_string),
        &m_out_conn_string_length, SQL_DRIVER_COMPLETE);

    EXPECT_EQ(SQL_SUCCESS, ret);
}

TEST_F(TestSQLDriverConnect, SqlDriverPrompt) {
    SQLRETURN ret =
        SQLDriverConnect(m_conn, NULL, (SQLTCHAR*)conn_string.c_str(), SQL_NTS,
                         m_out_conn_string, IT_SIZEOF(m_out_conn_string),
                         &m_out_conn_string_length, SQL_DRIVER_PROMPT);

    EXPECT_EQ(SQL_SUCCESS, ret);
}

TEST_F(TestSQLDriverConnect, SqlDriverComplete) {
    SQLRETURN ret =
        SQLDriverConnect(m_conn, NULL, (SQLTCHAR*)conn_string.c_str(), SQL_NTS,
                         m_out_conn_string, IT_SIZEOF(m_out_conn_string),
                         &m_out_conn_string_length, SQL_DRIVER_COMPLETE);

    EXPECT_EQ(SQL_SUCCESS, ret);
}

TEST_F(TestSQLDriverConnect, SqlDriverCompleteRequired) {
    SQLRETURN ret = SQLDriverConnect(
        m_conn, NULL, (SQLTCHAR*)conn_string.c_str(), SQL_NTS,
        m_out_conn_string, IT_SIZEOF(m_out_conn_string),
        &m_out_conn_string_length, SQL_DRIVER_COMPLETE_REQUIRED);

    EXPECT_EQ(SQL_SUCCESS, ret);
}

TEST_F(TestSQLDriverConnect, SqlDriverNoprompt) {
    SQLRETURN ret =
        SQLDriverConnect(m_conn, NULL, (SQLTCHAR*)conn_string.c_str(), SQL_NTS,
                         m_out_conn_string, IT_SIZEOF(m_out_conn_string),
                         &m_out_conn_string_length, SQL_DRIVER_NOPROMPT);

    EXPECT_EQ(SQL_SUCCESS, ret);
}

// TODO #41 - Revisit when parser code
// This should return SQL_SUCCESS_WITH_INFO
TEST_F(TestSQLDriverConnect, InvalidDriver) {
    std::wstring invalid_driver_conn_string =
         use_ssl ? L"Driver=xxxx;"
                   L"host=https://localhost;port=5432;"
                   L"user=admin;password=admin;auth=BASIC;useSSL="
                   L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                   L"responseTimeout=10;"
                 : L"Driver=xxxx;"
                   L"host=localhost;port=5432;"
                   L"user=admin;password=admin;auth=BASIC;useSSL="
                   L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                   L"responseTimeout=10;";

    SQLRETURN ret = SQLDriverConnect(
        m_conn, NULL, (SQLTCHAR*)invalid_driver_conn_string.c_str(), SQL_NTS,
        m_out_conn_string, IT_SIZEOF(m_out_conn_string),
        &m_out_conn_string_length, SQL_DRIVER_COMPLETE);
    EXPECT_EQ(SQL_ERROR, ret);
 }

TEST_F(TestSQLDriverConnect, InvalidHost) {
    std::wstring invalid_host_conn_string =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://8.8.8.8;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=1;"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=8.8.8.8;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=1;";

    SQLRETURN ret = SQLDriverConnect(
        m_conn, NULL, (SQLTCHAR*)invalid_host_conn_string.c_str(), SQL_NTS,
        m_out_conn_string, IT_SIZEOF(m_out_conn_string),
        &m_out_conn_string_length, SQL_DRIVER_COMPLETE);
    EXPECT_EQ(SQL_ERROR, ret);
}

TEST_F(TestSQLDriverConnect, InvalidPort) {
    std::wstring invalid_port_conn_string =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://localhost;port=5432;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=localhost;port=5432;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;";

    SQLRETURN ret = SQLDriverConnect(
        m_conn, NULL, (SQLTCHAR*)invalid_port_conn_string.c_str(), SQL_NTS,
        m_out_conn_string, IT_SIZEOF(m_out_conn_string),
        &m_out_conn_string_length, SQL_DRIVER_COMPLETE);
    EXPECT_EQ(SQL_ERROR, ret);
}

// TODO #41 - Revisit when parser code
// This should return SQL_SUCCESS_WITH_INFO (SQLSTATE 01S00 - Invalid connection
// string attribute)
TEST_F(TestSQLDriverConnect, UnsupportedKeyword) {
    std::wstring unsupported_keyword_conn_string =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://localhost;port=5432;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;extra=1"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=localhost;port=5432;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;extra=1";

    SQLRETURN ret = SQLDriverConnect(
        m_conn, NULL, (SQLTCHAR*)unsupported_keyword_conn_string.c_str(),
        SQL_NTS, m_out_conn_string, IT_SIZEOF(m_out_conn_string),
        &m_out_conn_string_length, SQL_DRIVER_COMPLETE);
    EXPECT_EQ(SQL_ERROR, ret);
}

TEST_F(TestSQLDriverConnect, ConnStringAbbrevsUID) {
    std::wstring abbrev_str =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://localhost;port=9200;"
                  L"UID=admin;password=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=localhost;port=9200;"
                  L"UID=admin;password=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;";

    SQLRETURN ret =
        SQLDriverConnect(m_conn, NULL, (SQLTCHAR*)abbrev_str.c_str(), SQL_NTS,
                         m_out_conn_string, IT_SIZEOF(m_out_conn_string),
                         &m_out_conn_string_length, SQL_DRIVER_NOPROMPT);
    EXPECT_EQ(SQL_SUCCESS, ret);
}

TEST_F(TestSQLDriverConnect, ConnStringAbbrevsPWD) {
    std::wstring abbrev_str =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://localhost;port=9200;"
                  L"user=admin;PWD=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=localhost;port=9200;"
                  L"user=admin;PWD=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;";

    SQLRETURN ret =
        SQLDriverConnect(m_conn, NULL, (SQLTCHAR*)abbrev_str.c_str(), SQL_NTS,
                         m_out_conn_string, IT_SIZEOF(m_out_conn_string),
                         &m_out_conn_string_length, SQL_DRIVER_NOPROMPT);
    EXPECT_EQ(SQL_SUCCESS, ret);
}

TEST_F(TestSQLDriverConnect, ConnStringAbbrevsUIDPWD) {
    std::wstring abbrev_str =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://localhost;port=9200;"
                  L"UID=admin;PWD=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=localhost;port=9200;"
                  L"UID=admin;PWD=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;";

    SQLRETURN ret =
        SQLDriverConnect(m_conn, NULL, (SQLTCHAR*)abbrev_str.c_str(), SQL_NTS,
                         m_out_conn_string, IT_SIZEOF(m_out_conn_string),
                         &m_out_conn_string_length, SQL_DRIVER_NOPROMPT);
    EXPECT_EQ(SQL_SUCCESS, ret);
}

TEST_F(TestSQLDriverConnect, ConnStringAbbrevsServer) {
    std::wstring abbrev_str =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"server=https://localhost;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;"
                : L"Driver={Elasticsearch ODBC};"
                  L"server=localhost;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;";

    SQLRETURN ret =
        SQLDriverConnect(m_conn, NULL, (SQLTCHAR*)abbrev_str.c_str(), SQL_NTS,
                         m_out_conn_string, IT_SIZEOF(m_out_conn_string),
                         &m_out_conn_string_length, SQL_DRIVER_NOPROMPT);
    EXPECT_EQ(SQL_SUCCESS, ret);
}

TEST_F(TestSQLDriverConnect, ConnStringAbbrevsServerUIDPWD) {
    std::wstring abbrev_str =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"server=https://localhost;port=9200;"
                  L"UID=admin;PWD=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;"
                : L"Driver={Elasticsearch ODBC};"
                  L"server=localhost;port=9200;"
                  L"UID=admin;PWD=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;";

    SQLRETURN ret =
        SQLDriverConnect(m_conn, NULL, (SQLTCHAR*)abbrev_str.c_str(), SQL_NTS,
                         m_out_conn_string, IT_SIZEOF(m_out_conn_string),
                         &m_out_conn_string_length, SQL_DRIVER_NOPROMPT);
    EXPECT_EQ(SQL_SUCCESS, ret);
}

TEST_F(TestSQLDriverConnect, Timeout1Second) {
    std::wstring one_second_timeout =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://8.8.8.8;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=1;"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=8.8.8.8;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=1;";

    auto start = std::chrono::steady_clock::now();
    SQLRETURN ret = SQLDriverConnect(
        m_conn, NULL, (SQLTCHAR*)one_second_timeout.c_str(), SQL_NTS,
        m_out_conn_string, IT_SIZEOF(m_out_conn_string),
        &m_out_conn_string_length, SQL_DRIVER_COMPLETE);
    auto end = std::chrono::steady_clock::now();
    auto time =
        std::chrono::duration_cast< std::chrono::milliseconds >(end - start)
            .count();
    std::cout << "TIME: " << time << std::endl;
    EXPECT_EQ(SQL_ERROR, ret);
#ifdef WIN32
    // Windows rounds up to nearest 4s with timeout, another user reported this
    // issue:
    // https://social.msdn.microsoft.com/Forums/vstudio/en-US/42ae1b2f-b120-4b46-9417-e594c3d02a5f/does-winhttpsettimeouts-support-small-timeouts?forum=vcgeneral
    EXPECT_GT(time, 3400);
    EXPECT_LT(time, 4500);
#else
    EXPECT_GT(time, 500);
    EXPECT_LT(time, 1500);
#endif
}

TEST_F(TestSQLDriverConnect, Timeout3Second) {
    std::wstring one_second_timeout =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://8.8.8.8;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=3;"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=8.8.8.8;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=3;";

    auto start = std::chrono::steady_clock::now();
    SQLRETURN ret = SQLDriverConnect(
        m_conn, NULL, (SQLTCHAR*)one_second_timeout.c_str(), SQL_NTS,
        m_out_conn_string, IT_SIZEOF(m_out_conn_string),
        &m_out_conn_string_length, SQL_DRIVER_COMPLETE);
    auto end = std::chrono::steady_clock::now();
    auto time =
        std::chrono::duration_cast< std::chrono::milliseconds >(end - start)
            .count();
    std::cout << "TIME: " << time << std::endl;
    EXPECT_EQ(SQL_ERROR, ret);
#ifdef WIN32
    // Windows rounds up to nearest 4s with timeout, another user reported this
    // issue:
    // https://social.msdn.microsoft.com/Forums/vstudio/en-US/42ae1b2f-b120-4b46-9417-e594c3d02a5f/does-winhttpsettimeouts-support-small-timeouts?forum=vcgeneral
    EXPECT_GT(time, 3500);
    EXPECT_LT(time, 4500);
#else
    EXPECT_GT(time, 2500);
    EXPECT_LT(time, 3500);
#endif
}

TEST_F(TestSQLDriverConnect, Timeout7Second) {
    std::wstring seven_second_timeout =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://8.8.8.8;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=7;"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=8.8.8.8;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=7;";

    auto start = std::chrono::steady_clock::now();
    SQLRETURN ret = SQLDriverConnect(
        m_conn, NULL, (SQLTCHAR*)seven_second_timeout.c_str(), SQL_NTS,
        m_out_conn_string, IT_SIZEOF(m_out_conn_string),
        &m_out_conn_string_length, SQL_DRIVER_COMPLETE);
    auto end = std::chrono::steady_clock::now();
    auto time =
        std::chrono::duration_cast< std::chrono::milliseconds >(end - start)
            .count();
    std::cout << "TIME: " << time << std::endl;
    EXPECT_EQ(SQL_ERROR, ret);
#ifdef WIN32
    // Windows rounds up to nearest 4s with timeout, another user reported this
    // issue:
    // https://social.msdn.microsoft.com/Forums/vstudio/en-US/42ae1b2f-b120-4b46-9417-e594c3d02a5f/does-winhttpsettimeouts-support-small-timeouts?forum=vcgeneral
    EXPECT_GT(time, 7500);
    EXPECT_LT(time, 8500);
#else
    EXPECT_GT(time, 6500);
    EXPECT_LT(time, 7500);
#endif
}

class TestSQLDisconnect : public testing::Test {
   public:
    TestSQLDisconnect() {
    }

    void SetUp() {
    }

    void TearDown() {
        if (m_conn != SQL_NULL_HDBC) {
            SQLFreeHandle(SQL_HANDLE_DBC, m_conn);
        }
        SQLFreeHandle(SQL_HANDLE_ENV, m_env);
    }

    ~TestSQLDisconnect() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
};

TEST_F(TestSQLDisconnect, TestSuccess) {
    ASSERT_NO_THROW(ITDriverConnect((SQLTCHAR*)conn_string.c_str(), &m_env,
                                    &m_conn, true, true));
    EXPECT_EQ(SQL_SUCCESS, SQLDisconnect(m_conn));
}

TEST_F(TestSQLDisconnect, TestReconnectOnce) {
    for (int i = 0; i <= 1; i++) {
        ASSERT_NO_THROW((ITDriverConnect((SQLTCHAR*)conn_string.c_str(), &m_env,
                                         &m_conn, true, true)));
        EXPECT_EQ(SQL_SUCCESS, SQLDisconnect(m_conn));
    }
}

TEST_F(TestSQLDisconnect, TestReconnectMultipleTimes) {
    for (int i = 0; i <= 10; i++) {
        ASSERT_NO_THROW((ITDriverConnect((SQLTCHAR*)conn_string.c_str(), &m_env,
                                         &m_conn, true, true)));
        EXPECT_EQ(SQL_SUCCESS, SQLDisconnect(m_conn));
    }
}

TEST_F(TestSQLDisconnect, TestDisconnectWithoutConnect) {
    ASSERT_NO_THROW(SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &m_env));
    ASSERT_NO_THROW(SQLAllocHandle(SQL_HANDLE_DBC, m_env, &m_conn));
    EXPECT_EQ(SQL_ERROR, SQLDisconnect(m_conn));
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
    system("leaks itodbc_connection > leaks_itodbc_connection");
#endif
    return failures;
}
