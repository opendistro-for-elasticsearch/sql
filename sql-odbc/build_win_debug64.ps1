# Build AWS SDK
$BITNESS=64

# Compare Bitness for 32
# $ARCH="x64"

mkdir sdk-build${BITNESS}
cd sdk-build${BITNESS}

git clone "https://github.com/aws/aws-sdk-cpp.git"

$prefix_path = (pwd).path
cmake .\aws-sdk-cpp -A x64 -D CMAKE_INSTALL_PREFIX=${prefix_path}\AWSSDK\ -D CMAKE_BUILD_TYPE=Debug -D BUILD_ONLY="core" -D CUSTOM_MEMORY_MANAGEMENT="OFF" -D ENABLE_RTTI="OFF" -D ENABLE_TESTING="OFF"

msbuild ALL_BUILD.vcxproj /p:Configuration=Debug
msbuild INSTALL.vcxproj /p:Configuration=Debug

cd ..

# # Configure Project
cmake -S src -B cmake-build${BITNESS} -A x64 -D CMAKE_INSTALL_PREFIX=sdk-build${BITNESS}\AWSSDK\ -D BUILD_WITH_TESTS=ON

# # Build Project
cmake --build .\cmake-build${BITNESS} --config Debug 

msbuild cmake-build32\PACKAGE.vcxproj -p:Configuration=Debug