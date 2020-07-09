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

#define BIND_SIZE 255
#define SINGLE_ROW 1
typedef struct Col {
    SQLLEN data_len;
    SQLCHAR data_dat[BIND_SIZE];
} Col;

class TestPagination : public testing::Test {
   public:
    TestPagination() {
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

    int GetTotalRowsAfterQueryExecution() {
        SQLAllocHandle(SQL_HANDLE_STMT, m_conn, &m_hstmt);
        SQLRETURN ret = SQLExecDirect(m_hstmt, (SQLTCHAR*)m_query.c_str(), SQL_NTS);
        EXPECT_EQ(SQL_SUCCESS, ret);

        // Get column count
        SQLSMALLINT total_columns = -1;
        SQLNumResultCols(m_hstmt, &total_columns);
        std::vector< std::vector< Col > > cols(total_columns);
        for (size_t i = 0; i < cols.size(); i++) {
            cols[i].resize(SINGLE_ROW);
        }

        // Bind and fetch
        for (size_t i = 0; i < cols.size(); i++) {
            ret = SQLBindCol(m_hstmt, (SQLUSMALLINT)i + 1, SQL_C_CHAR,
                             (SQLPOINTER)&cols[i][0].data_dat[i], 255,
                             &cols[i][0].data_len);
        }

        // Get total number of rows
        int row_count = 0;
        while (SQLFetch(m_hstmt) == SQL_SUCCESS) {
            row_count++;
        }
        return row_count;
    }

    ~TestPagination() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
    SQLTCHAR m_out_conn_string[1024];
    SQLSMALLINT m_out_conn_string_length;
    std::wstring m_query =
        L"SELECT Origin FROM kibana_sample_data_flights";
};

TEST_F(TestPagination, EnablePagination) {
    // Default fetch size is -1 for driver.
    // Server default page size for all cursor requests is 1000.

    //Total number of rows in kibana_sample_data_flights table
    int total_rows = 13059;
    std::wstring fetch_size_15_conn_string =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://localhost;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=localhost;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;";
    ASSERT_EQ(SQL_SUCCESS,
              SQLDriverConnect(
                  m_conn, NULL, (SQLTCHAR*)fetch_size_15_conn_string.c_str(),
                  SQL_NTS, m_out_conn_string, IT_SIZEOF(m_out_conn_string),
                  &m_out_conn_string_length, SQL_DRIVER_PROMPT));
    EXPECT_EQ(total_rows, GetTotalRowsAfterQueryExecution());
}

TEST_F(TestPagination, DisablePagination) {
    // Fetch size 0 implies no pagination
    int total_rows = 200;
    std::wstring fetch_size_15_conn_string =
        use_ssl ? L"Driver={Elasticsearch ODBC};"
                  L"host=https://localhost;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"1;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;fetchSize=0;"
                : L"Driver={Elasticsearch ODBC};"
                  L"host=localhost;port=9200;"
                  L"user=admin;password=admin;auth=BASIC;useSSL="
                  L"0;hostnameVerification=0;logLevel=0;logOutput=C:\\;"
                  L"responseTimeout=10;fetchSize=0;";
    ASSERT_EQ(SQL_SUCCESS,
              SQLDriverConnect(
                  m_conn, NULL, (SQLTCHAR*)fetch_size_15_conn_string.c_str(),
                  SQL_NTS, m_out_conn_string, IT_SIZEOF(m_out_conn_string),
                  &m_out_conn_string_length, SQL_DRIVER_PROMPT));
    EXPECT_EQ(total_rows, GetTotalRowsAfterQueryExecution());
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
    system("leaks itodbc_pagination > leaks_itodbc_pagination");
#endif
    return failures;
}
