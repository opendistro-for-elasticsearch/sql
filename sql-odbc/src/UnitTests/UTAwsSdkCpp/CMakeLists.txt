project(ut_aws_sdk_cpp)

# Source, headers, and include dirs
set(SOURCE_FILES test_aws_sdk_cpp.cpp)
include_directories(	${UT_HELPER}
						${AWSSDK_INCLUDE_DIR}
						${VLD_SRC})

# Generate executable
add_executable(ut_aws_sdk_cpp ${SOURCE_FILES})

# Library dependencies
target_link_libraries(ut_aws_sdk_cpp ut_helper gtest_main aws-cpp-sdk-core ${VLD})
target_compile_definitions(ut_aws_sdk_cpp PUBLIC _UNICODE UNICODE)
