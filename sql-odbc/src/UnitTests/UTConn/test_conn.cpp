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
#include "es_communication.h"
// clang-format on

const size_t valid_option_count = 4;
const size_t invalid_option_count = 4;
const size_t missing_option_count = 3;
const std::string valid_host = (use_ssl ? "https://localhost" : "localhost");
const std::string valid_port = "9200";
const std::string valid_user = "admin";
const std::string valid_pw = "admin";
const std::string valid_region = "us-west-3";
const std::string invalid_host = "10.1.1.189";
const std::string invalid_port = "920";
const std::string invalid_user = "amin";
const std::string invalid_pw = "amin";
const std::string invalid_region = "bad-region";
runtime_options valid_opt_val = {{valid_host, valid_port, "1", "0"},
                                 {"BASIC", valid_user, valid_pw, valid_region},
                                 {use_ssl, false, "", "", "", ""}};
runtime_options invalid_opt_val = {
    {invalid_host, invalid_port, "1", "0"},
    {"BASIC", invalid_user, invalid_pw, valid_region},
    {use_ssl, false, "", "", "", ""}};
runtime_options missing_opt_val = {{"", "", "1", "0"},
                                   {"BASIC", "", invalid_pw, valid_region},
                                   {use_ssl, false, "", "", "", ""}};

TEST(TestESConnConnectionOptions, ValidParameters) {
    ESCommunication conn;
    EXPECT_EQ(true,
              conn.ConnectionOptions(valid_opt_val, 1, 1, valid_option_count));
}

TEST(TestESConnConnectionOptions, MissingParameters) {
    ESCommunication conn;
    EXPECT_EQ(false, conn.ConnectionOptions(missing_opt_val, 1, 1,
                                            missing_option_count));
}

class TestESConnConnectDBStart : public testing::Test {
   public:
    TestESConnConnectDBStart() {
    }

    void SetUp() {
    }

    void TearDown() {
        m_conn.DropDBConnection();
    }

    ~TestESConnConnectDBStart() {
        // cleanup any pending stuff, but no exceptions allowed
    }

    ESCommunication m_conn;
};

TEST_F(TestESConnConnectDBStart, ValidParameters) {
    ASSERT_NE(false, m_conn.ConnectionOptions(valid_opt_val, 1, 1,
                                              valid_option_count));
    EXPECT_EQ(true, m_conn.ConnectDBStart());
    EXPECT_EQ(CONNECTION_OK, m_conn.GetConnectionStatus());
}

TEST_F(TestESConnConnectDBStart, InvalidParameters) {
    ASSERT_TRUE(
        m_conn.ConnectionOptions(invalid_opt_val, 1, 1, invalid_option_count));
    EXPECT_EQ(false, m_conn.ConnectDBStart());
    EXPECT_EQ(CONNECTION_BAD, m_conn.GetConnectionStatus());
}

TEST_F(TestESConnConnectDBStart, MissingParameters) {
    ASSERT_NE(true, m_conn.ConnectionOptions(missing_opt_val, 1, 1,
                                             missing_option_count));
    EXPECT_EQ(false, m_conn.ConnectDBStart());
    EXPECT_EQ(CONNECTION_BAD, m_conn.GetConnectionStatus());
}

TEST(TestESConnDropDBConnection, InvalidParameters) {
    ESCommunication conn;
    ASSERT_EQ(CONNECTION_BAD, conn.GetConnectionStatus());
    ASSERT_TRUE(
        conn.ConnectionOptions(invalid_opt_val, 1, 1, invalid_option_count));
    ASSERT_NE(true, conn.ConnectDBStart());
    ASSERT_EQ(CONNECTION_BAD, conn.GetConnectionStatus());
    conn.DropDBConnection();
    EXPECT_EQ(CONNECTION_BAD, conn.GetConnectionStatus());
}

TEST(TestESConnDropDBConnection, MissingParameters) {
    ESCommunication conn;
    ASSERT_EQ(CONNECTION_BAD, conn.GetConnectionStatus());
    ASSERT_NE(true, conn.ConnectionOptions(missing_opt_val, 1, 1,
                                           missing_option_count));
    ASSERT_NE(true, conn.ConnectDBStart());
    ASSERT_EQ(CONNECTION_BAD, conn.GetConnectionStatus());
    conn.DropDBConnection();
    EXPECT_EQ(CONNECTION_BAD, conn.GetConnectionStatus());
}

TEST(TestESConnDropDBConnection, ValidParameters) {
    ESCommunication conn;
    ASSERT_NE(false,
              conn.ConnectionOptions(valid_opt_val, 1, 1, valid_option_count));
    ASSERT_NE(false, conn.ConnectDBStart());
    ASSERT_EQ(CONNECTION_OK, conn.GetConnectionStatus());
    conn.DropDBConnection();
    EXPECT_EQ(CONNECTION_BAD, conn.GetConnectionStatus());
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
