project(itodbc_catalog)

# Source, headers, and include dirs
set(SOURCE_FILES test_odbc_catalog.cpp)
include_directories(	${UT_HELPER}
						${IT_HELPER}
						${ODFEODBC_SRC}
						${VLD_SRC}  )

# Generate executable
add_executable(itodbc_catalog ${SOURCE_FILES})

# Library dependencies
target_code_coverage(itodbc_catalog PUBLIC AUTO ALL)
target_link_libraries(itodbc_catalog odfesqlodbc itodbc_helper ut_helper gtest_main)
target_compile_definitions(itodbc_catalog PUBLIC _UNICODE UNICODE)
