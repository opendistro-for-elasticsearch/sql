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
