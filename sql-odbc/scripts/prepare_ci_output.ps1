$ODBC_BIN_PATH = $args[0]
$ODBC_LIB_PATH = $args[1]
$ODBC_BUILD_PATH = $args[2]

Write-Host $args

# Create staging directories for CI artifacts
$CI_OUTPUT_PATH = ".\ci-output"
New-Item -Path $CI_OUTPUT_PATH -ItemType Directory -Force | Out-Null
New-Item -Path $CI_OUTPUT_PATH\build -ItemType Directory -Force | Out-Null
New-Item -Path $CI_OUTPUT_PATH\installer -ItemType Directory -Force | Out-Null

# Copy CI artifacts to respective directories
Copy-Item $ODBC_BIN_PATH\*.dll $CI_OUTPUT_PATH\build
Copy-Item $ODBC_BIN_PATH\*.exe $CI_OUTPUT_PATH\build
Copy-Item $ODBC_LIB_PATH\*.lib $CI_OUTPUT_PATH\build
Copy-Item $ODBC_BUILD_PATH\*.msi $CI_OUTPUT_PATH\installer
#    mkdir $CI_OUTPUT_PATH\test
#    Copy-Item $ODBC_BIN_PATH\*.log $CI_OUTPUT_PATH\test
#    Copy-Item $ODBC_BIN_PATH\*.html $CI_OUTPUT_PATH\test