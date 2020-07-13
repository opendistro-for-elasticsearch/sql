"""
Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License").
You may not use this file except in compliance with the License.
A copy of the License is located at

    http://www.apache.org/licenses/LICENSE-2.0

or in the "license" file accompanying this file. This file is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
express or implied. See the License for the specific language governing
permissions and limitations under the License.
"""

"""
We can define the fixture functions in this file to make them
accessible across multiple test modules.
"""
import os
import pytest

from .utils import create_index, delete_index, get_connection


@pytest.fixture(scope="function")
def connection():
    test_connection = get_connection()
    create_index(test_connection)

    yield test_connection
    delete_index(test_connection)


@pytest.fixture(scope="function")
def default_config_location():
    from src.odfe_sql_cli.conf import __file__ as package_root

    package_root = os.path.dirname(package_root)
    default_config = os.path.join(package_root, "clirc")

    yield default_config


@pytest.fixture(scope="session", autouse=True)
def temp_config(tmpdir_factory):
    # this function runs on start of test session.
    # use temporary directory for conf home so user conf will not be used
    os.environ["XDG_CONFIG_HOME"] = str(tmpdir_factory.mktemp("data"))
