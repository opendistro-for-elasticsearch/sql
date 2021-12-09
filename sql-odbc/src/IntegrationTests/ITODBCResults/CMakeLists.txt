project(itodbc_results)

# Source, headers, and include dirs
set(SOURCE_FILES test_odbc_results.cpp)
include_directories(	${UT_HELPER}
						${IT_HELPER}
						${ODFEODBC_SRC}
						${VLD_SRC}  )

# Generate executable
add_executable(itodbc_results ${SOURCE_FILES})

# Library dependencies
target_code_coverage(itodbc_results PUBLIC AUTO ALL)
target_link_libraries(itodbc_results odfesqlodbc itodbc_helper ut_helper gtest_main)
target_compile_definitions(itodbc_results PUBLIC _UNICODE UNICODE)
