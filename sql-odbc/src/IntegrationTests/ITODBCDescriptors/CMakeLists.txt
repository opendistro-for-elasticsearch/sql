project(itodbc_descriptors)

# Source, headers, and include dirs
set(SOURCE_FILES test_odbc_descriptors.cpp)
include_directories(	${UT_HELPER}
						${IT_HELPER}
						${ODFEODBC_SRC}
						${VLD_SRC}  )

# Generate executable
add_executable(itodbc_descriptors ${SOURCE_FILES})

# Library dependencies
target_code_coverage(itodbc_descriptors PUBLIC AUTO ALL)
target_link_libraries(itodbc_descriptors odfesqlodbc itodbc_helper ut_helper gtest_main)
target_compile_definitions(itodbc_descriptors PUBLIC _UNICODE UNICODE)
