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

# Pre 3.16 versions of Windows set MD/MT incorrectly and cause linker 'warnings' which are actually serious issues
if(WIN32)
cmake_minimum_required(VERSION 3.16) 
else()
cmake_minimum_required(VERSION 3.13) 
endif()

project(global_make_list)

include("${CMAKE_CURRENT_SOURCE_DIR}/modules/code-coverage.cmake")
add_code_coverage_all_targets(EXCLUDE libraries aws-cpp-sdk googletest IntegrationTests)

# This wasn't being picked up on mac, causes some symbol errors
if(APPLE)
	set(CMAKE_CXX_STANDARD 20)
	add_definitions(-DHAVE_STRLCAT)
	# Do not use shared libraries for AWS SDK.
	option(BUILD_SHARED_LIBS "Build shared libraries" OFF)
	# Set BUILD_WITH_TESTS to OFF before building installer package for size optimization.
	option(BUILD_WITH_TESTS "Enable testing" ON)
else()
	set(CMAKE_CXX_STANDARD 17)
	# Use shared libraries for AWS SDK.
	option(BUILD_SHARED_LIBS "Build shared libraries" ON)
	# Set BUILD_WITH_TESTS to OFF before building installer package for size optimization.
	option(BUILD_WITH_TESTS "Enable testing" ON)
endif()

if(MSVC)
  add_compile_options(/W4 /WX)
else()
  add_compile_options(-Wall -Wextra -pedantic -Werror)
endif()

if(CMAKE_SIZEOF_VOID_P EQUAL 8)
	set(BITNESS 64)
	set(BITNESS_NAME "x64")
	set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../lib64")
	set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../lib64")
	set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../bin64")
elseif(CMAKE_SIZEOF_VOID_P EQUAL 4)
	set(BITNESS 32)
	set(BITNESS_NAME "x86")
	set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../lib32")
	set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../lib32")
	set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../bin32")
endif()

if(NOT WIN32)
	# Unix builds require autoconf
	option(AUTOCONF_ENABLE "Enable autoconf" ON)
	configure_file(autoconf.h.in generated/autoconf.h @ONLY)
endif()

# Base directories
set(PROJECT_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/..")
set(ODFEODBC_SRC "${CMAKE_CURRENT_SOURCE_DIR}/odfesqlodbc")
set(ODFEENLIST_SRC "${CMAKE_CURRENT_SOURCE_DIR}/odfeenlist")
set(INTEGRATION_TESTS "${CMAKE_CURRENT_SOURCE_DIR}/IntegrationTests")
set(UNIT_TESTS "${CMAKE_CURRENT_SOURCE_DIR}/UnitTests")
set(LIBRARY_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/../libraries)
set(INSTALL_SRC "${CMAKE_CURRENT_SOURCE_DIR}/installer")
set(DSN_INSTALLER_SRC "${CMAKE_CURRENT_SOURCE_DIR}/DSNInstaller")

# ODBC Driver version 
set(DRIVER_PACKAGE_VERSION "1.9.0.0")
set(DRIVER_PACKAGE_VERSION_COMMA_SEPARATED "1,9,0,0")
add_compile_definitions( ES_ODBC_VERSION="${DRIVER_PACKAGE_VERSION}"
                         # Comma separated version is required for odbc administrator's driver file.
                         ES_ODBC_DRVFILE_VERSION=${DRIVER_PACKAGE_VERSION_COMMA_SEPARATED} )

# Extensions of base directories
set(PERFORMANCE_TESTS "${CMAKE_CURRENT_SOURCE_DIR}/PerformanceTests")
set(UT_HELPER "${UNIT_TESTS}/UTHelper")
set(IT_HELPER "${INTEGRATION_TESTS}/ITODBCHelper")
set(RABBIT_SRC ${LIBRARY_DIRECTORY}/rabbit/include)
set(RAPIDJSON_SRC ${LIBRARY_DIRECTORY}/rapidjson/include)
set(VLD_SRC ${LIBRARY_DIRECTORY}/VisualLeakDetector/include)

# Without this symbols will be exporting to Unix but not Windows
set(CMAKE_WINDOWS_EXPORT_ALL_SYMBOLS TRUE)	

# Set path for AWS SDK
set(aws-cpp-sdk-base "${CMAKE_CURRENT_SOURCE_DIR}/aws-sdk-cpp")
set(aws-cpp-sdk-core_DIR "${PROJECT_ROOT}/sdk-build${BITNESS}/AWSSDK/lib/cmake/aws-cpp-sdk-core")
set(aws-c-event-stream_DIR "${PROJECT_ROOT}/sdk-build${BITNESS}/AWSSDK/lib/aws-c-event-stream/cmake")
set(aws-c-common_DIR "${PROJECT_ROOT}/sdk-build${BITNESS}/AWSSDK/lib/aws-c-common/cmake")
set(aws-checksums_DIR "${PROJECT_ROOT}/sdk-build${BITNESS}/AWSSDK/lib/aws-checksums/cmake")

if (WIN32)
	find_package(AWSSDK REQUIRED core)
endif()

# General compiler definitions
add_compile_definitions		(	_SILENCE_TR1_NAMESPACE_DEPRECATION_WARNING
								UNICODE_SUPPORT	
								DYNAMIC_LOAD	
								_MBCS
								_CRT_SECURE_NO_DEPRECATE	
								_USRDLL	
								_SILENCE_CXX17_ITERATOR_BASE_CLASS_DEPRECATION_WARNING
								_SILENCE_CXX17_CODECVT_HEADER_DEPRECATION_WARNING
								# USE_SSL					
							)

# Platform specific compiler definitions
if (WIN32 AND BITNESS EQUAL 64)
	# Windows specific
	add_compile_definitions	(	_WINDLL						
								_WIN64			
								_WINDOWS
								WIN_EXPORT				
							)
elseif (WIN32 AND BITNESS EQUAL 32)
	# Windows specific
	add_compile_definitions	(	_WINDLL						
								_WIN32			
								_WINDOWS
								WIN_EXPORT				
							)
elseif(APPLE)
	# macOS specific
	add_compile_definitions	(	WITH_IODBC	
								SQLCOLATTRIBUTE_SQLLEN
							)
	# iODBC includes
	include_directories(/usr/local/include)
elseif(UNIX)
	# Unix specific
	add_compile_definitions	(	WITH_UNIXODBC	
								SQLCOLATTRIBUTE_SQLLEN
							)
endif()

if(BUILD_WITH_TESTS)
	# GTest import
	include(gtest/googletest.cmake)
	fetch_googletest(
		${PROJECT_SOURCE_DIR}/gtest
		${PROJECT_BINARY_DIR}/googletest
		)
	enable_testing()
endif()

# Projects to build
if (APPLE)
	add_subdirectory(${aws-cpp-sdk-base})
endif()
add_subdirectory(${ODFEODBC_SRC})
add_subdirectory(${ODFEENLIST_SRC})
add_subdirectory(${INSTALL_SRC})

# Only build & package DSN installer for Mac platforms
if(APPLE)
	add_subdirectory(${DSN_INSTALLER_SRC})
endif()

if(BUILD_WITH_TESTS)
	add_subdirectory(${INTEGRATION_TESTS})
	add_subdirectory(${UNIT_TESTS})
	add_subdirectory(${PERFORMANCE_TESTS})
endif()
