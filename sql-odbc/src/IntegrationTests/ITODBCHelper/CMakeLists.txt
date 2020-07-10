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

project(itodbc_helper)

# Source, headers, and include dirs
set(SOURCE_FILES it_odbc_helper.cpp)
set(HEADER_FILES it_odbc_helper.h)	
include_directories(	
                        ${UT_HELPER}
                        ${ODFEODBC_SRC}
						${VLD_SRC}  
                    )

# Generate dll (SHARED)
add_library(itodbc_helper SHARED ${SOURCE_FILES} ${HEADER_FILES})

# Library dependencies
target_link_libraries(itodbc_helper odfesqlodbc ut_helper gtest_main)
target_compile_definitions(itodbc_helper PUBLIC _UNICODE UNICODE)	