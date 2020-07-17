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
#include "chrono"
#include <vector>
#include <numeric>
// clang-format on

#define BIND_SIZE 255
#define ROWSET_SIZE_5 5
#define ROWSET_SIZE_50 50
#define SINGLE_ROW 1
#define ITERATION_COUNT 10

#ifndef WIN32
typedef SQLULEN SQLROWCOUNT;
typedef SQLULEN SQLROWSETSIZE;
typedef SQLULEN SQLTRANSID;
typedef SQLLEN SQLROWOFFSET;
#endif

const wchar_t* const m_query =
    L"SELECT * FROM kibana_sample_data_flights limit 10000";

typedef struct Col {
    SQLLEN data_len;
    SQLCHAR data_dat[BIND_SIZE];
} Col;

class TestPerformance : public testing::Test {
   public:
    TestPerformance() {
    }
    void SetUp() {
        AllocStatement((SQLTCHAR*)conn_string.c_str(), &m_env, &m_conn,
                       &m_hstmt, true, true);
    }
    void TearDown() {
        SQLDisconnect(m_conn);
    }

   protected:
    SQLHENV m_env = SQL_NULL_HENV;
    SQLHDBC m_conn = SQL_NULL_HDBC;
    SQLHSTMT m_hstmt = SQL_NULL_HSTMT;
};

const std::string sync_start = "%%__PARSE__SYNC__START__%%";
const std::string sync_query = "%%__QUERY__%%";
const std::string sync_case = "%%__CASE__%%";
const std::string sync_min = "%%__MIN__%%";
const std::string sync_max = "%%__MAX__%%";
const std::string sync_mean = "%%__MEAN__%%";
const std::string sync_median = "%%__MEDIAN__%%";
const std::string sync_end = "%%__PARSE__SYNC__END__%%";

void ReportTime(const std::string test_case, std::vector< long long > data) {
    size_t size = data.size();
    ASSERT_EQ(size, (size_t)ITERATION_COUNT);

    // Get max and min
    long long time_max = *std::max_element(data.begin(), data.end());
    long long time_min = *std::min_element(data.begin(), data.end());

    // Get median
    long long time_mean =
        std::accumulate(std::begin(data), std::end(data), 0ll) / data.size();

    // Get median
    std::sort(data.begin(), data.end());
    long long time_median = (size % 2)
                                ? data[size / 2]
                                : ((data[(size / 2) - 1] + data[size / 2]) / 2);

    // Output results
    std::cout << sync_start << std::endl;
    std::cout << sync_query;
    std::wcout << std::wstring(m_query) << std::endl;
    std::cout << sync_case << test_case << std::endl;
    std::cout << sync_min << time_min << " ms" << std::endl;
    std::cout << sync_max << time_max << " ms" << std::endl;
    std::cout << sync_mean << time_mean << " ms" << std::endl;
    std::cout << sync_median << time_median << " ms" << std::endl;
    std::cout << sync_end << std::endl;

    std::cout << "Time dump: ";
    for (size_t i = 0; i < data.size(); i++) {
        std::cout << data[i] << " ms";
        if (i != (data.size() - 1))
            std::cout << ", ";
    }
    std::cout << std::endl;
}

TEST_F(TestPerformance, Time_Execute) {
    // Execute a query just to wake the server up in case it has been sleeping
    // for a while
    SQLRETURN ret = SQLExecDirect(m_hstmt, (SQLTCHAR*)m_query, SQL_NTS);
    ASSERT_TRUE(SQL_SUCCEEDED(ret));
    ASSERT_TRUE(SQL_SUCCEEDED(SQLCloseCursor(m_hstmt)));

    std::vector< long long > times;
    for (size_t iter = 0; iter < ITERATION_COUNT; iter++) {
        auto start = std::chrono::steady_clock::now();
        ret = SQLExecDirect(m_hstmt, (SQLTCHAR*)m_query, SQL_NTS);
        auto end = std::chrono::steady_clock::now();
        LogAnyDiagnostics(SQL_HANDLE_STMT, m_hstmt, ret);
        ASSERT_TRUE(SQL_SUCCEEDED(ret));
        ASSERT_TRUE(SQL_SUCCEEDED(SQLCloseCursor(m_hstmt)));
        times.push_back(
            std::chrono::duration_cast< std::chrono::milliseconds >(end - start)
                .count());
    }
    ReportTime("Execute Query", times);
}

TEST_F(TestPerformance, Time_BindColumn_FetchSingleRow) {
    SQLSMALLINT total_columns = 0;
    int row_count = 0;

    std::vector< long long > times;
    for (size_t iter = 0; iter < ITERATION_COUNT; iter++) {
        // Execute query
        SQLRETURN ret = SQLExecDirect(m_hstmt, (SQLTCHAR*)m_query, SQL_NTS);
        ASSERT_TRUE(SQL_SUCCEEDED(ret));

        // Get column count
        SQLNumResultCols(m_hstmt, &total_columns);
        std::vector< std::vector< Col > > cols(total_columns);
        for (size_t i = 0; i < cols.size(); i++)
            cols[i].resize(SINGLE_ROW);

        // Bind and fetch
        auto start = std::chrono::steady_clock::now();
        for (size_t i = 0; i < cols.size(); i++)
            ret = SQLBindCol(m_hstmt, (SQLUSMALLINT)i + 1, SQL_C_CHAR,
                             (SQLPOINTER)&cols[i][0].data_dat[i], 255,
                             &cols[i][0].data_len);
        while (SQLFetch(m_hstmt) == SQL_SUCCESS)
            row_count++;
        auto end = std::chrono::steady_clock::now();
        ASSERT_TRUE(SQL_SUCCEEDED(SQLCloseCursor(m_hstmt)));
        times.push_back(
            std::chrono::duration_cast< std::chrono::milliseconds >(end - start)
                .count());
    }
    ReportTime("Bind and (1 row) Fetch", times);
}

TEST_F(TestPerformance, Time_BindColumn_Fetch5Rows) {
    SQLROWSETSIZE row_count = 0;
    SQLSMALLINT total_columns = 0;
    SQLROWSETSIZE rows_fetched = 0;
    SQLUSMALLINT row_status[ROWSET_SIZE_5];
    SQLSetStmtAttr(m_hstmt, SQL_ROWSET_SIZE, (void*)ROWSET_SIZE_5, 0);

    std::vector< long long > times;
    for (size_t iter = 0; iter < ITERATION_COUNT; iter++) {
        // Execute query
        SQLRETURN ret = SQLExecDirect(m_hstmt, (SQLTCHAR*)m_query, SQL_NTS);
        ASSERT_TRUE(SQL_SUCCEEDED(ret));

        // Get column count
        SQLNumResultCols(m_hstmt, &total_columns);
        std::vector< std::vector< Col > > cols(total_columns);
        for (size_t i = 0; i < cols.size(); i++)
            cols[i].resize(ROWSET_SIZE_5);

        // Bind and fetch
        auto start = std::chrono::steady_clock::now();
        for (size_t i = 0; i < cols.size(); i++)
            ret = SQLBindCol(m_hstmt, (SQLUSMALLINT)i + 1, SQL_C_CHAR,
                             (SQLPOINTER)&cols[i][0].data_dat[i], BIND_SIZE,
                             &cols[i][0].data_len);
        while (SQLExtendedFetch(m_hstmt, SQL_FETCH_NEXT, 0, &rows_fetched,
                                row_status)
               == SQL_SUCCESS) {
            row_count += rows_fetched;
            if (rows_fetched < ROWSET_SIZE_5)
                break;
        }
        auto end = std::chrono::steady_clock::now();
        ASSERT_TRUE(SQL_SUCCEEDED(SQLCloseCursor(m_hstmt)));
        times.push_back(
            std::chrono::duration_cast< std::chrono::milliseconds >(end - start)
                .count());
    }
    ReportTime("Bind and (5 row) Fetch", times);
}

TEST_F(TestPerformance, Time_BindColumn_Fetch50Rows) {
    SQLROWSETSIZE row_count = 0;
    SQLSMALLINT total_columns = 0;
    SQLROWSETSIZE rows_fetched = 0;
    SQLUSMALLINT row_status[ROWSET_SIZE_50];
    SQLSetStmtAttr(m_hstmt, SQL_ROWSET_SIZE, (void*)ROWSET_SIZE_50, 0);

    std::vector< long long > times;
    for (size_t iter = 0; iter < ITERATION_COUNT; iter++) {
        // Execute query
        SQLRETURN ret = SQLExecDirect(m_hstmt, (SQLTCHAR*)m_query, SQL_NTS);
        ASSERT_TRUE(SQL_SUCCEEDED(ret));

        // Get column count
        SQLNumResultCols(m_hstmt, &total_columns);
        std::vector< std::vector< Col > > cols(total_columns);
        for (size_t i = 0; i < cols.size(); i++)
            cols[i].resize(ROWSET_SIZE_50);

        // Bind and fetch
        auto start = std::chrono::steady_clock::now();
        for (size_t i = 0; i < cols.size(); i++)
            ret = SQLBindCol(m_hstmt, (SQLUSMALLINT)i + 1, SQL_C_CHAR,
                             (SQLPOINTER)&cols[i][0].data_dat[i], BIND_SIZE,
                             &cols[i][0].data_len);
        while (SQLExtendedFetch(m_hstmt, SQL_FETCH_NEXT, 0, &rows_fetched,
                                row_status)
               == SQL_SUCCESS) {
            row_count += rows_fetched;
            if (rows_fetched < ROWSET_SIZE_50)
                break;
        }

        auto end = std::chrono::steady_clock::now();
        ASSERT_TRUE(SQL_SUCCEEDED(SQLCloseCursor(m_hstmt)));
        times.push_back(
            std::chrono::duration_cast< std::chrono::milliseconds >(end - start)
                .count());
    }
    ReportTime("Bind and (50 row) Fetch", times);
}

TEST_F(TestPerformance, Time_Execute_FetchSingleRow) {
    SQLSMALLINT total_columns = 0;
    int row_count = 0;

    std::vector< long long > times;
    for (size_t iter = 0; iter < ITERATION_COUNT; iter++) {
        // Execute query
        auto start = std::chrono::steady_clock::now();
        SQLRETURN ret = SQLExecDirect(m_hstmt, (SQLTCHAR*)m_query, SQL_NTS);
        ASSERT_TRUE(SQL_SUCCEEDED(ret));

        // Get column count
        SQLNumResultCols(m_hstmt, &total_columns);
        std::vector< std::vector< Col > > cols(total_columns);
        for (size_t i = 0; i < cols.size(); i++)
            cols[i].resize(SINGLE_ROW);

        // Bind and fetch
        for (size_t i = 0; i < cols.size(); i++)
            ret = SQLBindCol(m_hstmt, (SQLUSMALLINT)i + 1, SQL_C_CHAR,
                             (SQLPOINTER)&cols[i][0].data_dat[i], BIND_SIZE,
                             &cols[i][0].data_len);
        while (SQLFetch(m_hstmt) == SQL_SUCCESS)
            row_count++;

        auto end = std::chrono::steady_clock::now();
        ASSERT_TRUE(SQL_SUCCEEDED(SQLCloseCursor(m_hstmt)));
        times.push_back(
            std::chrono::duration_cast< std::chrono::milliseconds >(end - start)
                .count());
    }
    ReportTime("Execute Query, Bind and (1 row) Fetch", times);
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
    system("leaks performance_results > leaks_performance_results");
#endif
    return failures;
}
