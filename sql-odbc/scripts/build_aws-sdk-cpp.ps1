$CONFIGURATION = $args[0]
$WIN_ARCH = $args[1]
$SRC_DIR = $args[2]
$BUILD_DIR = $args[3]
$INSTALL_DIR = $args[4]

Write-Host $args

# Clone the AWS SDK CPP repo
# $SDK_VER = "1.7.29"
# -b "$SDK_VER" `
git clone `
    --single-branch `
    "https://github.com/aws/aws-sdk-cpp.git" `
    $SRC_DIR

# Make and move to build directory
New-Item -Path $BUILD_DIR -ItemType Directory -Force | Out-Null
Set-Location $BUILD_DIR

# Configure and build 
cmake $SRC_DIR `
    -A $WIN_ARCH `
    -D CMAKE_INSTALL_PREFIX=$INSTALL_DIR `
    -D CMAKE_BUILD_TYPE=$CONFIGURATION `
    -D BUILD_ONLY="core" `
    -D ENABLE_UNITY_BUILD="ON" `
    -D CUSTOM_MEMORY_MANAGEMENT="OFF" `
    -D ENABLE_RTTI="OFF" `
    -D ENABLE_TESTING="OFF"

# Build AWS SDK and install to $INSTALL_DIR 
msbuild ALL_BUILD.vcxproj /m /p:Configuration=$CONFIGURATION
msbuild INSTALL.vcxproj /m /p:Configuration=$CONFIGURATION
