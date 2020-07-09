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

git clone "https://github.com/aws/aws-sdk-cpp.git"

$prefix_path = (pwd).path

mkdir sdk-build

cd sdk-build

cmake ..\\aws-sdk-cpp\\ -D CMAKE_INSTALL_PREFIX=$prefix_path\AWSSDK\ -D CMAKE_BUILD_TYPE=Release -D BUILD_ONLY="core" -D CUSTOM_MEMORY_MANAGEMENT="OFF" -D ENABLE_RTTI="OFF" -D ENABLE_TESTING="OFF"

msbuild ALL_BUILD.vcxproj /p:Configuration=Release

msbuild INSTALL.vcxproj /p:Configuration=Release

cd ..