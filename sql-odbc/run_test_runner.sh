#!/bin/bash

PROJECT_DIR=$(pwd)
TEST_RUNNER_DIR=${PROJECT_DIR}/src/TestRunner
WORKING_DIR=${PROJECT_DIR}/bin64

cd ${WORKING_DIR}

pip3 install mako

python3 ${TEST_RUNNER_DIR}/test_runner.py -i ${TEST_RUNNER_DIR}/mako_template.html -o ${PROJECT_DIR}/test_output.html -e ${TEST_RUNNER_DIR}/test_exclude_list.txt 

ERROR_CODE=$?

cd ..

exit ${ERROR_CODE}
