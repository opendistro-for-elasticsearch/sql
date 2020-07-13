#
#   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
#   Licensed under the Apache License, Version 2.0 (the "License").
#   You may not use this file except in compliance with the License.
#   A copy of the License is located at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#   or in the "license" file accompanying this file. This file is distributed
#   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
#   express or implied. See the License for the specific language governing
#   permissions and limitations under the License.
#
 
project(itodbc_aws_auth)

# Source, headers, and include dirs
set(SOURCE_FILES test_odbc_aws_auth.cpp)
include_directories(	${UT_HELPER}
						${IT_HELPER}
						${ODFEODBC_SRC}
						${VLD_SRC}
						${AWSSDK_INCLUDE_DIR}
						)

# Generate executable
add_executable(itodbc_aws_auth ${SOURCE_FILES})

# Library dependencies
target_link_libraries(itodbc_aws_auth odfesqlodbc itodbc_helper ut_helper gtest_main aws-cpp-sdk-core)
target_compile_definitions(itodbc_aws_auth PUBLIC _UNICODE UNICODE)
