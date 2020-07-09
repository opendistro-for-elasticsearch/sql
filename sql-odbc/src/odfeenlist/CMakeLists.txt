#
#   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
#   Licensed under the Apache License, Version 2.0 (the "License").
#   You may not use this file except in compliance with the License.
#   A copy of the License is located at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#   or in the "license" file accompanying this file. This file is distributed
#   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
#   express or implied. See the License for the specific language governing
#   permissions and limitations under the License.
#

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