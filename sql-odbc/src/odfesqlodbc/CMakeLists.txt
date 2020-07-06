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

project(odfesqlodbc)

# Source files for odfesqlodbc
set(					SOURCE_FILES 
							bind.c					columninfo.c			connection.c 				convert.c
							descriptor.c			dlg_specific.c			drvconn.c					options.c	
							environ.c				execute.c				info.c						loadlib.c
							misc.c					multibyte.c				mylog.c						tuple.c
							parse.c					results.c				statement.c					odbcapi30.c	
							qresult.c				odbcapi30w.c			es_api30.c					es_types.c					
							es_utility.cpp			es_communication.cpp	es_connection.cpp			es_odbc.c
							es_driver_connect.cpp	es_helper.cpp			es_info.cpp					es_parse_result.cpp
							es_semaphore.cpp		es_statement.cpp		win_unicode.c				odbcapi.c
							odbcapiw.c				es_result_queue.cpp
	)
if(WIN32)
set(SOURCE_FILES ${SOURCE_FILES} dlg_wingui.c setup.c)
endif()

set(					HEADER_FILES 
							bind.h					catfunc.h				columninfo.h
							convert.h				descriptor.h			dlg_specific.h		drvconn.h
							environ.h				es_apifunc.h			es_communication.h	es_parse_result.h
							es_connection.h			es_driver_connect.h		es_helper.h			es_info.h			
							es_statement.h 			es_types.h 				loadlib.h 		
							misc.h					multibyte.h				mylog.h				es_utility.h
							resource.h				statement.h				tuple.h				unicode_support.h
							es_apifunc.h			es_odbc.h				es_semaphore.h		qresult.h
							version.h				win_setup.h				es_result_queue.h
	)

# Generate dll (SHARED)
if(WIN32)
set(RESOURCE_FILES es_odbc.rc)
add_library(odfesqlodbc SHARED ${SOURCE_FILES} ${HEADER_FILES} ${RESOURCE_FILES} ${AWSSDK_LIB_DIR})
else()
add_library(odfesqlodbc SHARED ${SOURCE_FILES} ${HEADER_FILES})
endif()

include_directories(
						${LIBRARY_DIRECTORY}/../src
						${CMAKE_CURRENT_SOURCE_DIR}
						${ODFEENLIST_SRC} 
						${RABBIT_SRC}
						${RAPIDJSON_SRC}
						${AWSSDK_INCLUDE_DIR}
					)

# Platform specific library dependencies
if(WIN32)
	# Windows specifiec
	target_link_libraries(odfesqlodbc wsock32 ws2_32 winmm user32 gdi32 legacy_stdio_definitions aws-cpp-sdk-core kernel32 advapi32 secur32 XOleHlp Wldap32 crypt32 Normaliz odbccp32 odbc32)
	target_link_libraries(odfesqlodbc debug msvcrtd)
	target_link_libraries(odfesqlodbc optimized msvcrt)
elseif(APPLE)
	# Apple specific
	target_link_libraries(odfesqlodbc iodbc iodbcinst aws-cpp-sdk-core)
elseif(UNIX)
	# Unix specific
	include_directories(/usr/src/linux-headers-5.0.0-27/include)
	target_link_libraries(odfesqlodbc aws-cpp-sdk-core odbc odbcinst)
endif()
