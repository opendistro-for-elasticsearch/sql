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

project(ut_helper)

# Source, headers, and include dirs
set(SOURCE_FILES unit_test_helper.cpp)	
set(HEADER_FILES unit_test_helper.h)
include_directories(${ODFEODBC_SRC} ${VLD_SRC})

# Generate dll (SHARED)
add_library(ut_helper SHARED ${SOURCE_FILES} ${HEADER_FILES})

if (WIN32 AND BITNESS EQUAL 64)
find_library(   VLD
				vld	
				HINTS "${LIBRARY_DIRECTORY}/VisualLeakDetector/lib64"
			)
target_link_libraries(ut_helper ${VLD})
elseif (WIN32 AND BITNESS EQUAL 32)
find_library(   VLD
				vld	
				HINTS "${LIBRARY_DIRECTORY}/VisualLeakDetector/lib"
			)
target_link_libraries(ut_helper ${VLD})
endif()

# Library dependencies
target_link_libraries(ut_helper odfesqlodbc gtest_main)
target_compile_definitions(ut_helper PUBLIC _UNICODE UNICODE)