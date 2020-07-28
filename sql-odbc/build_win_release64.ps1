# Build AWS SDK
$BITNESS = 64

# Compare Bitness for 32
# $ARCH="x64"

mkdir sdk-build64
cd .\sdk-build64

git clone "https://github.com/aws/aws-sdk-cpp.git"

$prefix_path = (pwd).path
cmake .\aws-sdk-cpp -A x64 -D CMAKE_INSTALL_PREFIX=${prefix_path}\AWSSDK\ -D CMAKE_BUILD_TYPE=Release -D BUILD_ONLY="core" -D CUSTOM_MEMORY_MANAGEMENT="OFF" -D ENABLE_RTTI="OFF" -D ENABLE_TESTING="OFF"

msbuild ALL_BUILD.vcxproj /m /p:Configuration=Release
msbuild INSTALL.vcxproj /m /p:Configuration=Release

cd ..

# # Configure Project
cmake -S src -B cmake-build64 -A x64 -D CMAKE_INSTALL_PREFIX=sdk-build64\AWSSDK\ -D BUILD_WITH_TESTS=ON

# # Build Project
cmake --build .\cmake-build64 --config Release --parallel 4 

cp .\sdk-build64\bin\Release\* .\bin64\Release
cp .\cmake-build64\bin\Release\* .\bin64\Release
