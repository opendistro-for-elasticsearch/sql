project(performance_info)

# Source, headers, and include dirs
set(SOURCE_FILES performance_odbc_info.cpp)
include_directories(	${UT_HELPER}
						${IT_HELPER}
						${ODFEODBC_SRC}
						${VLD_SRC}  
						${RABBIT_SRC}
						${RAPIDJSON_SRC})

# Generate executable
add_executable(performance_info ${SOURCE_FILES})

# Library dependencies
target_link_libraries(performance_info odfesqlodbc itodbc_helper ut_helper gtest_main)
target_compile_definitions(performance_info PUBLIC _UNICODE UNICODE)
