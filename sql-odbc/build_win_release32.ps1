# Build AWS SDK
$BITNESS=32

# Compare Bitness for 32
# $ARCH="Win32"

mkdir sdk-build${BITNESS}
cd sdk-build${BITNESS}

git clone "https://github.com/aws/aws-sdk-cpp.git"

$prefix_path = (pwd).path
cmake .\aws-sdk-cpp -A Win32 -D CMAKE_INSTALL_PREFIX=${prefix_path}\AWSSDK\ -D CMAKE_BUILD_TYPE=Release -D BUILD_ONLY="core" -D CUSTOM_MEMORY_MANAGEMENT="OFF" -D ENABLE_RTTI="OFF" -D ENABLE_TESTING="OFF"

msbuild ALL_BUILD.vcxproj /p:Configuration=Release
msbuild INSTALL.vcxproj /p:Configuration=Release

cd ..

# # Configure Project
cmake -S src -B cmake-build${BITNESS} -A Win32 -D CMAKE_INSTALL_PREFIX=sdk-build${BITNESS}\AWSSDK\ -D BUILD_WITH_TESTS=ON

# # Build Project
cmake --build .\cmake-build${BITNESS} --config Release 
