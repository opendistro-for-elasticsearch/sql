project(ut_rabbit)

# Source, headers, and include dirs
set(SOURCE_FILES test_rabbit.cpp)
include_directories(	${UT_HELPER}
						${RAPIDJSON_SRC} 
						${RABBIT_SRC} 
						${VLD_SRC} )

# Generate executable
add_executable(ut_rabbit ${SOURCE_FILES})

target_link_libraries(ut_rabbit ut_helper gtest_main ${VLD})
target_compile_definitions(ut_rabbit PUBLIC _UNICODE UNICODE)
