::
::   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
::
::   Licensed under the Apache License, Version 2.0 (the "License").
::   You may not use this file except in compliance with the License.
::   A copy of the License is located at
::
::      http://www.apache.org/licenses/LICENSE-2.0
::
::   or in the "license" file accompanying this file. This file is distributed
::   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
::   express or implied. See the License for the specific language governing
::   permissions and limitations under the License.
::

set PROJECT_DIR=%CD%
set TEST_RUNNER_DIR=%PROJECT_DIR%\src\TestRunner
set WORKING_DIR=%PROJECT_DIR%\bin64\Release

cd %WORKING_DIR%

py -m pip install mako

py %TEST_RUNNER_DIR%\test_runner.py -i %TEST_RUNNER_DIR%\mako_template.html -o test_output.html -e %TEST_RUNNER_DIR%\test_exclude_list.txt

set ERROR_CODE=%ERRORLEVEL%

cd %PROJECT_DIR%

echo %ERROR_CODE%

EXIT /b %ERROR_CODE%