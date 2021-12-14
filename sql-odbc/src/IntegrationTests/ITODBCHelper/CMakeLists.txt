project(itodbc_helper)

# Source, headers, and include dirs
set(SOURCE_FILES it_odbc_helper.cpp)
set(HEADER_FILES it_odbc_helper.h)	
include_directories(	
                        ${UT_HELPER}
                        ${ODFEODBC_SRC}
						${VLD_SRC}  
                    )

# Generate dll (SHARED)
add_library(itodbc_helper SHARED ${SOURCE_FILES} ${HEADER_FILES})

# Library dependencies
target_link_libraries(itodbc_helper odfesqlodbc ut_helper gtest_main)
target_compile_definitions(itodbc_helper PUBLIC _UNICODE UNICODE)
