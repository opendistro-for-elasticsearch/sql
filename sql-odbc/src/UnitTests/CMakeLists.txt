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

project(unit_tests)

set(HELPER_UTEST "${CMAKE_CURRENT_SOURCE_DIR}/UTHelper")
set(CONN_UTEST "${CMAKE_CURRENT_SOURCE_DIR}/UTConn")
set(RABBIT_UTEST "${CMAKE_CURRENT_SOURCE_DIR}/UTRabbit")
set(CRITICALSECTION_UTEST "${CMAKE_CURRENT_SOURCE_DIR}/UTCriticalSection")
set(AWSSDKCPP_UTEST "${CMAKE_CURRENT_SOURCE_DIR}/UTAwsSdkCpp")

# Projects to build
add_subdirectory(${HELPER_UTEST})
add_subdirectory(${CONN_UTEST})
add_subdirectory(${RABBIT_UTEST})
add_subdirectory(${CRITICALSECTION_UTEST})
add_subdirectory(${AWSSDKCPP_UTEST})