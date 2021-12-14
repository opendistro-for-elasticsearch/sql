project(ut_helper)

# Source, headers, and include dirs
set(SOURCE_FILES unit_test_helper.cpp)	
set(HEADER_FILES unit_test_helper.h)
include_directories(${ODFEODBC_SRC} ${VLD_SRC})

# Generate dll (SHARED)
add_library(ut_helper SHARED ${SOURCE_FILES} ${HEADER_FILES})

if (WIN32 AND BITNESS EQUAL 64)
find_library(   VLD
				vld	
				HINTS "${LIBRARY_DIRECTORY}/VisualLeakDetector/lib64"
			)
target_link_libraries(ut_helper ${VLD})
elseif (WIN32 AND BITNESS EQUAL 32)
find_library(   VLD
				vld	
				HINTS "${LIBRARY_DIRECTORY}/VisualLeakDetector/lib"
			)
target_link_libraries(ut_helper ${VLD})
endif()

# Library dependencies
target_link_libraries(ut_helper odfesqlodbc gtest_main)
target_compile_definitions(ut_helper PUBLIC _UNICODE UNICODE)
