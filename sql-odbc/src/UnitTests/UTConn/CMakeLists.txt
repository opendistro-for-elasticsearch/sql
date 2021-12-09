project(ut_conn)

# Source, headers, and include dirs
set(SOURCE_FILES test_conn.cpp test_query_execution.cpp)
include_directories(	${UT_HELPER}
						${ODFEODBC_SRC} 
						${RAPIDJSON_SRC} 
						${RABBIT_SRC}		
						${LIBCURL_SRC}
						${VLD_SRC} )

# Generate executable
add_executable(ut_conn ${SOURCE_FILES})

# Library dependencies
target_link_libraries(ut_conn odfesqlodbc ut_helper gtest_main)
target_compile_definitions(ut_conn PUBLIC _UNICODE UNICODE)
