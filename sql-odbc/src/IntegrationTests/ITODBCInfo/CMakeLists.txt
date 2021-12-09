project(itodbc_info)

# Source, headers, and include dirs
set(SOURCE_FILES test_odbc_info.cpp)
include_directories(	${UT_HELPER}
						${IT_HELPER}
						${ODFEODBC_SRC}
						${VLD_SRC}  )

# Generate executable
add_executable(itodbc_info ${SOURCE_FILES})

# Library dependencies
target_code_coverage(itodbc_info PUBLIC AUTO ALL)
target_link_libraries(itodbc_info odfesqlodbc itodbc_helper ut_helper gtest_main)
target_compile_definitions(itodbc_info PUBLIC _UNICODE UNICODE)
