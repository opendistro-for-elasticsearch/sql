# Build AWS SDK
# $BITNESS=64

cd src
git clone -b "1.7.329" "https://github.com/aws/aws-sdk-cpp.git"
cd ..

PREFIX_PATH=$(pwd)
mkdir cmake-build64
cd cmake-build64
cmake ../src -DCMAKE_INSTALL_PREFIX=${PREFIX_PATH}/AWSSDK/ -DCMAKE_BUILD_TYPE=Release -DBUILD_ONLY="core" -DCUSTOM_MEMORY_MANAGEMENT="OFF" -DENABLE_RTTI="OFF" -DENABLE_TESTING="OFF"
cd ..

cmake --build cmake-build64 -- -j 4
