set PROJECT_DIR=%CD%
set TEST_RUNNER_DIR=%PROJECT_DIR%\src\TestRunner
set WORKING_DIR=%PROJECT_DIR%\build\Debug64\odbc\bin\Debug

cd %WORKING_DIR%

py -m pip install mako

py %TEST_RUNNER_DIR%\test_runner.py -i %TEST_RUNNER_DIR%\mako_template.html -o test_output.html -e %TEST_RUNNER_DIR%\test_exclude_list.txt

set ERROR_CODE=%ERRORLEVEL%

cd %PROJECT_DIR%

echo %ERROR_CODE%

EXIT /b %ERROR_CODE%
