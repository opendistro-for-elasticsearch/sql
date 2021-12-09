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
