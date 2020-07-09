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
#include <string>
#include <fstream>
#include <locale>
#include <codecvt>
#include <fstream>
// clang-format on

const std::string all_queries_file = "queries_all.txt";
const std::string output_file = "odbc_result.txt";

inline void GetAllLinesInFile(const std::string file_name,
                              std::vector< std::string >& lines) {
    std::ifstream file(file_name);
    if (file.is_open()) {
        std::string line;
        while (getline(file, line)) {
            lines.push_back(line);
        }
        file.close();
    }
}

TEST(TableauQuery, IssueQueriesAll) {
    // Get lines from file
    std::vector< std::string > lines;
    GetAllLinesInFile(all_queries_file, lines);

    // Connect to database
    SQLHENV env = SQL_NULL_HENV;
    SQLHDBC conn = SQL_NULL_HDBC;
    SQLHSTMT stmt = SQL_NULL_HSTMT;
    ASSERT_NO_THROW(AllocStatement((SQLTCHAR*)conn_string.c_str(), &env, &conn,
                                   &stmt, true, false));

    // Execute queries
    size_t idx = 1;
    std::wofstream output(output_file);
    ASSERT_TRUE(output.is_open());

    for (auto& query : lines) {
        SQLRETURN ret = SQLExecDirect(stmt, (SQLTCHAR*)query.c_str(), SQL_NTS);
        output << "\"" << idx++ << "\", \"";
        output << (SQL_SUCCEEDED(ret) ? "PASS" : "FAIL") << "\", \""
               << query.c_str() << "\"" << std::endl;
        if (SQL_SUCCEEDED(ret))
            SQLCloseCursor(stmt);
    }
    output.close();

    SQLFreeHandle(SQL_HANDLE_STMT, stmt);
    SQLDisconnect(conn);
    SQLFreeHandle(SQL_HANDLE_ENV, env);
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
    system("leaks itodbc_tableau_queries > leaks_itodbc_tableau_queries");
#endif
    return failures;
}
