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

project(ut_critical_section)

# Source, headers, and include dirs
set(SOURCE_FILES test_critical_section.cpp)
include_directories(	${UT_HELPER}
						${ODFEODBC_SRC}
						${RAPIDJSON_SRC}
						${VLD_SRC}
						${RABBIT_SRC}  )

# Generate executable
add_executable(ut_critical_section ${SOURCE_FILES})

# Library dependencies
target_link_libraries(ut_critical_section odfesqlodbc ut_helper gtest_main)
target_compile_definitions(ut_critical_section PUBLIC _UNICODE UNICODE)
