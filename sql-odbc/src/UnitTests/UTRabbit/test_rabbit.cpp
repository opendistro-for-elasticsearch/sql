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
#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif  // __APPLE__
#include "rabbit.hpp"
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
#include "unit_test_helper.h"

const std::string invalid_json_schema = "{ invalid schema }";
const std::string valid_json_schema = "{" // This was generated from the example elasticsearch data
  "\"type\": \"object\","
  "\"properties\": {"
    "\"schema\": {"
      "\"type\": \"array\","
      "\"items\": [{"
          "\"type\": \"object\","
          "\"properties\": {"
            "\"name\": { \"type\": \"string\" },"
            "\"type\": { \"type\": \"string\" }"
          "},"
          "\"required\": [ \"name\", \"type\" ]"
      "}]"
    "},"
    "\"total\": { \"type\": \"integer\" },"
    "\"datarows\": {"
      "\"type\": \"array\","
      "\"items\": {}"
    "},"
    "\"size\": { \"type\": \"integer\" },"
    "\"status\": { \"type\": \"integer\" }"
  "},"
  "\"required\": [\"schema\", \"total\", \"datarows\", \"size\", \"status\"]"
"}";
const std::string valid_json_for_schema = "{" // This was taken from the example elasticsearch data
  "\"schema\": [{"
	"\"name\": \"valid_name1\","
	"\"type\": \"valid_type1\"},{"
	"\"name\": \"valid_name2\","
	"\"type\": \"valid_type2\"},{"
	"\"name\": \"valid_name3\","
	"\"type\": \"valid_type3\"}],"
  "\"total\": 10,"
  "\"datarows\": [],"
  "\"size\": 3,"
  "\"status\": 200"
"}";
const std::string invalid_json_for_schema = "{"
  "\"schema\": [{"
	"\"name\": 1,"
	"\"type\": \"valid_type1\"},{"
	"\"name\": 2,"
	"\"type\": \"valid_type2\"},{"
	"\"name\": 3,"
	"\"type\": \"valid_type3\"}],"
  "\"total\": \"10\","
  "\"datarows\": {},"
  "\"size\": \"string_size\","
  "\"status\": 200"
"}";
const std::string invalid_json = "invalid json";
const std::string valid_json_int = "{ \"value\" : 123 }";
const std::string invalid_json_int = "{ \"value\" : invalid }";
const std::string valid_json_str = "{ \"value\" : \"123\"}";
const std::string invalid_json_str = "{ \"value\" : \"123}";
const std::string valid_json_arr = "{ \"value\" : [ 1, \"2\", true] }";
const std::string invalid_json_arr = "{ \"value\" : [ 1, 2 3] }";
const std::string valid_json_obj = "{"
	"\"value\" : {"
		"\"subval_str\" : \"1\","
		"\"subval_int\" : 2,"
		"\"subval_bool\" : true,"
		"\"subval_flt\" : 3.4"
	"}"
"}";
const std::string invalid_json_obj = "{"
	"\"value\" : {"
		"\"subval_str\" : \"1\""
		"\"subval_int\" : 2,"
		"\"subval_bool\" : true,"
		"\"subval_flt\" : 3.4"
	"}"
"}";
// Intentionally serialized because it will be compared to a str parsed by rabbit, which is serialized by default
const std::string valid_sub_obj_for_conversion = "{\"subval_obj\":{\"subval_str\":\"1\",\"subval_int\":2,\"subval_bool\":true,\"subval_flt\":3.4}}";
const std::string valid_obj_for_conversion = "{ \"value\" : " + valid_sub_obj_for_conversion + "}";
// clang-format on

const std::vector< size_t > distances = {0, 1, 5, 30};

TEST(StandardDistance, ValidIterator) {
    rabbit::array arr;
    for (size_t i = 0; i < distances.size(); i++) {
        rabbit::array sub_array;
        for (size_t j = 0; j < distances[i]; j++) {
            sub_array.push_back(static_cast< uint64_t >(j));
        }
        arr.push_back(sub_array);
    }

    ASSERT_EQ(static_cast< size_t >(std::distance(arr.begin(), arr.end())),
              distances.size());
    size_t i = 0;
    for (auto it = arr.begin(); it < arr.end(); it++, i++) {
        EXPECT_EQ(static_cast< size_t >(
                      std::distance(it->value_begin(), it->value_end())),
                  distances[i]);
    }
}

TEST(ConvertObjectToString, IteratorAtStringConvert) {
    rabbit::document doc;
    ASSERT_NO_THROW(doc.parse(valid_json_for_schema));
    rabbit::array arr;
    ASSERT_NO_THROW(arr = doc["schema"]);
    size_t i = 1;
    std::string valid_name = "valid_name";
    std::string valid_type = "valid_type";
    for (auto it = arr.begin(); it < arr.end(); ++it, ++i) {
        std::string name, type;
        ASSERT_NO_THROW(name = it->at("name").as_string());
        ASSERT_NO_THROW(type = it->at("type").as_string());
        EXPECT_EQ(name, valid_name + std::to_string(i));
        EXPECT_EQ(type, valid_type + std::to_string(i));
    }
}

TEST(ConvertObjectToString, ValidObject) {
    rabbit::document doc;
    EXPECT_NO_THROW(doc.parse(valid_obj_for_conversion));
    ASSERT_TRUE(doc.is_object());
    ASSERT_TRUE(doc.has("value"));
    ASSERT_TRUE(doc["value"].is_object());
    std::string value_str = doc["value"].str();
    EXPECT_EQ(value_str, valid_sub_obj_for_conversion);
}

TEST(ParseSchema, ValidSchemaValidDoc) {
    rabbit::document doc;
    EXPECT_NO_THROW(doc.parse(valid_json_for_schema, valid_json_schema));
}

TEST(ParseSchema, InvalidSchemaValidDoc) {
    rabbit::document doc;
    EXPECT_THROW(doc.parse(valid_json_for_schema, invalid_json_schema),
                 rabbit::parse_error);
}

TEST(ParseSchema, ValidSchemaInvalidDoc) {
    rabbit::document doc;
    EXPECT_THROW(doc.parse(invalid_json_for_schema, valid_json_schema),
                 rabbit::parse_error);
}

TEST(ParseSchema, InvalidSchemaInvalidDoc) {
    rabbit::document doc;
    EXPECT_THROW(doc.parse(invalid_json, invalid_json_schema),
                 rabbit::parse_error);
}

TEST(ParseObj, ValidObj) {
    rabbit::document doc;
    EXPECT_NO_THROW(doc.parse(valid_json_obj));
    ASSERT_TRUE(doc.is_object());
    ASSERT_TRUE(doc.has("value"));
    ASSERT_TRUE(doc["value"].is_object());
    ASSERT_TRUE(doc["value"].has("subval_str"));
    ASSERT_TRUE(doc["value"].has("subval_int"));
    ASSERT_TRUE(doc["value"].has("subval_bool"));
    ASSERT_TRUE(doc["value"].has("subval_flt"));
    ASSERT_TRUE(doc["value"]["subval_str"].is_string());
    ASSERT_TRUE(doc["value"]["subval_int"].is_int());
    ASSERT_TRUE(doc["value"]["subval_bool"].is_bool());
    ASSERT_TRUE(doc["value"]["subval_flt"].is_number());
    EXPECT_EQ("1", doc["value"]["subval_str"].as_string());
    EXPECT_EQ(2, doc["value"]["subval_int"].as_int());
    EXPECT_EQ(true, doc["value"]["subval_bool"].as_bool());
    EXPECT_EQ(3.4, doc["value"]["subval_flt"].as_double());
}

TEST(ParseObj, InvalidObj) {
    rabbit::document doc;
    EXPECT_THROW(doc.parse(invalid_json_obj), rabbit::parse_error);
}

TEST(ParseArr, ValidArr) {
    rabbit::document doc;
    ASSERT_NO_THROW(doc.parse(valid_json_arr));
    ASSERT_TRUE(doc.is_object());
    ASSERT_TRUE(doc.has("value"));
    ASSERT_TRUE(doc["value"].is_array());

    rabbit::array arr;
    ASSERT_NO_THROW(arr = doc["value"]);
    size_t i = 0;
    for (rabbit::array::iterator it = arr.begin(); it != arr.end(); ++it, ++i) {
        switch (i) {
            case 0:
                ASSERT_TRUE(it->is_int());
                EXPECT_EQ(1, it->as_int());
                break;
            case 1:
                ASSERT_TRUE(it->is_string());
                EXPECT_EQ("2", it->as_string());
                break;
            case 2:
                ASSERT_TRUE(it->is_bool());
                EXPECT_EQ(true, it->as_bool());
                break;
            default:
                FAIL() << "Array iterator exceeded bounds";
                return;
        }
    }
}
TEST(ParseArr, InvalidArr) {
    rabbit::document doc;
    EXPECT_THROW(doc.parse(invalid_json_arr), rabbit::parse_error);
}

TEST(ParseStr, ValidStr) {
    rabbit::document doc;
    ASSERT_NO_THROW(doc.parse(valid_json_str));
    ASSERT_TRUE(doc.is_object());
    ASSERT_TRUE(doc.has("value"));
    ASSERT_TRUE(doc["value"].is_string());
    EXPECT_EQ("123", doc["value"].as_string());
}

TEST(ParseStr, InvalidStr) {
    rabbit::document doc;
    EXPECT_THROW(doc.parse(invalid_json_str), rabbit::parse_error);
}

TEST(ParseInt, ValidInt) {
    rabbit::document doc;
    ASSERT_NO_THROW(doc.parse(valid_json_int));
    ASSERT_TRUE(doc.is_object());
    ASSERT_TRUE(doc.has("value"));
    ASSERT_TRUE(doc["value"].is_int());
    EXPECT_EQ(123, doc["value"].as_int());
}

TEST(ParseInt, InvalidInt) {
    rabbit::document doc;
    EXPECT_THROW(doc.parse(invalid_json_int), rabbit::parse_error);
}

TEST(Parse, InvalidJson) {
    rabbit::document doc;
    EXPECT_THROW(doc.parse(invalid_json), rabbit::parse_error);
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
