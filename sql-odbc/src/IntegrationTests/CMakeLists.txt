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

project(integration_tests)

set(HELPER_ITEST "${CMAKE_CURRENT_SOURCE_DIR}/ITODBCHelper")
set(CATALOG_ITEST "${CMAKE_CURRENT_SOURCE_DIR}/ITODBCCatalog")
set(CONNECTION_ITEST "${CMAKE_CURRENT_SOURCE_DIR}/ITODBCConnection")
set(DESCRIPTOR_ITEST "${CMAKE_CURRENT_SOURCE_DIR}/ITODBCDescriptors")
set(EXECUTION_ITEST "${CMAKE_CURRENT_SOURCE_DIR}/ITODBCExecution")
set(INFO_ITEST "${CMAKE_CURRENT_SOURCE_DIR}/ITODBCInfo")
set(RESULTS_ITEST "${CMAKE_CURRENT_SOURCE_DIR}/ITODBCResults")
set(TABLEAU_QUERIES_ITEST "${CMAKE_CURRENT_SOURCE_DIR}/ITODBCTableauQueries")
set(AWS_AUTH_ITEST "${CMAKE_CURRENT_SOURCE_DIR}/ITODBCAwsAuth")
set(PAGINATION_ITEST "${CMAKE_CURRENT_SOURCE_DIR}/ITODBCPagination")

# Projects to build
add_subdirectory(${HELPER_ITEST})
add_subdirectory(${CATALOG_ITEST})
add_subdirectory(${CONNECTION_ITEST})
add_subdirectory(${DESCRIPTOR_ITEST})
add_subdirectory(${EXECUTION_ITEST})
add_subdirectory(${INFO_ITEST})
add_subdirectory(${RESULTS_ITEST})
add_subdirectory(${TABLEAU_QUERIES_ITEST})
add_subdirectory(${AWS_AUTH_ITEST})
add_subdirectory(${PAGINATION_ITEST})