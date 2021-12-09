project(ut_critical_section)

# Source, headers, and include dirs
set(SOURCE_FILES test_critical_section.cpp)
include_directories(	${UT_HELPER}
						${ODFEODBC_SRC}
						${RAPIDJSON_SRC}
						${VLD_SRC}
						${RABBIT_SRC}  )

# Generate executable
add_executable(ut_critical_section ${SOURCE_FILES})

# Library dependencies
target_link_libraries(ut_critical_section odfesqlodbc ut_helper gtest_main)
target_compile_definitions(ut_critical_section PUBLIC _UNICODE UNICODE)
