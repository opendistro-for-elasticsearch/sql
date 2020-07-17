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
#include "it_odbc_helper.h"
// clang-format on

std::wstring dsn_name = L"test_aws_auth_dsn";
std::wstring aws_auth_conn_string =
    L"Driver={Elasticsearch};DataBase=database_name;"
    L"Host=https://"
    L"search-bit-quill-cx3hpfoxvasohujxkllmgjwqde.us-west-2."
    L"es.amazonaws.com;"
    L"Auth=AWS_SIGV4;Region=us-west-2;LogLevel=1";
std::wstring aws_auth_conn_string_invalid_region =
    L"Driver={Elasticsearch};DataBase=database_name;"
    L"Host=https://"
    L"search-bit-quill-cx3hpfoxvasohujxkllmgjwqde.us-west-2."
    L"es.amazonaws.com;"
    L"Auth=AWS_SIGV4;Region=us-west-3;LogLevel=1";
std::wstring aws_auth_conn_string_invalid_authtype =
    L"Driver={Elasticsearch};DataBase=database_name;"
    L"Host=https://"
    L"search-bit-quill-cx3hpfoxvasohujxkllmgjwqde.us-west-2."
    L"es.amazonaws.com;"
    L"Auth=AWS;Region=us-west-2;LogLevel=1";

class TestAwsAuthConnection : public testing::Test {
   public:
    TestAwsAuthConnection(){
    }

    void SetUp() override {
    }

    void AllocConnection() {
        ASSERT_NO_FATAL_FAILURE(SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &m_env));
        ASSERT_NO_FATAL_FAILURE(SQLSetEnvAttr(m_env, SQL_ATTR_ODBC_VERSION, (void*)SQL_OV_ODBC3, 0));
        ASSERT_NO_FATAL_FAILURE(SQLAllocHandle(SQL_HANDLE_DBC, m_env, &m_conn));
    }

    void TearDown() override {
        SQLDisconnect(m_conn);
        SQLFreeHandle(SQL_HANDLE_DBC, m_conn);
        SQLFreeHandle(SQL_HANDLE_ENV,m_env);
    }

    ~TestAwsAuthConnection() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHENV m_env = SQL_NULL_HENV;

	private:
	void TestBody() override {
    }
};

TEST(TestAwsAuthConnection, SqlConnectSuccess) {
    SQLRETURN ret = SQL_ERROR;
    TestAwsAuthConnection test;
    ASSERT_NO_FATAL_FAILURE(test.AllocConnection());
    ret = SQLConnect(test.m_conn, (SQLTCHAR*)dsn_name.c_str(), SQL_NTS,
                     (SQLTCHAR*)NULL, 0, (SQLTCHAR*)NULL, 0);
    EXPECT_TRUE(SQL_SUCCEEDED(ret));
}

TEST(TestAwsAuthConnection, SqlDriverConnectSuccess) {
    SQLRETURN ret;
    TestAwsAuthConnection test;
    SQLTCHAR out_conn_string[1024];
    SQLSMALLINT out_conn_string_length;
    ASSERT_NO_FATAL_FAILURE(test.AllocConnection());
    ret = SQLDriverConnect(test.m_conn, NULL,
                           (SQLTCHAR*)aws_auth_conn_string.c_str(), SQL_NTS,
                            out_conn_string, IT_SIZEOF(out_conn_string),
                           &out_conn_string_length, SQL_DRIVER_COMPLETE);
    EXPECT_TRUE(SQL_SUCCEEDED(ret));
}

TEST(TestAwsAuthConnection, SqlDriverConnectInvalidRegion) {
    SQLRETURN ret;
    TestAwsAuthConnection test;
    SQLTCHAR out_conn_string[1024];
    SQLSMALLINT out_conn_string_length;
    ASSERT_NO_FATAL_FAILURE(test.AllocConnection());
    ret = SQLDriverConnect(test.m_conn, NULL,
                         (SQLTCHAR*)aws_auth_conn_string_invalid_region.c_str(),
                         SQL_NTS, out_conn_string, IT_SIZEOF(out_conn_string),
                         &out_conn_string_length, SQL_DRIVER_COMPLETE);
    EXPECT_EQ(SQL_ERROR,ret);
}

TEST(TestAwsAuthConnection, SqlDriverConnectInvalidAuthType) {
    SQLRETURN ret;
    TestAwsAuthConnection test;
    SQLTCHAR out_conn_string[1024];
    SQLSMALLINT out_conn_string_length;
    ASSERT_NO_FATAL_FAILURE(test.AllocConnection());
    ret = SQLDriverConnect(test.m_conn, NULL,
                         (SQLTCHAR*)aws_auth_conn_string_invalid_authtype.c_str(),
                         SQL_NTS, out_conn_string, IT_SIZEOF(out_conn_string),
                         &out_conn_string_length, SQL_DRIVER_COMPLETE);
    EXPECT_EQ(SQL_ERROR, ret);
}

int main(int argc, char** argv) {
    testing::internal::CaptureStdout();
    ::testing::InitGoogleTest(&argc, argv);
    int failures = RUN_ALL_TESTS();
    std::string output = testing::internal::GetCapturedStdout();
    std::cout << output << std::endl;
    std::cout << (failures ? "Not all tests passed." : "All tests passed")
              << std::endl;
    WriteFileIfSpecified(argv, argv + argc, "-fout", output);
    return failures;
}
