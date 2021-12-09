project(odfeenlist)

# Source files for odfeenlist
set(SOURCE_FILES msdtc_enlist.cpp)
set(HEADER_FILES elasticenlist.h)

# Generate static lib
add_library(odfeenlist STATIC ${SOURCE_FILES} ${HEADER_FILES})
 		
# Library dependencies
target_link_libraries(odfeenlist kernel32 advapi32 Delayimp XOleHlp)

# Platform specific library dependencies
if(WIN32)
	# Windows specifiec 
	target_link_libraries(odfeenlist wsock32 winspool user32 gdi32 comdlg32 shell32 uuid)
else()
	# Unix specific
endif()
