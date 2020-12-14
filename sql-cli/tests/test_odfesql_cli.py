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
import mock
import pytest
from prompt_toolkit.shortcuts import PromptSession
from prompt_toolkit.input.defaults import create_pipe_input

from src.odfe_sql_cli.esbuffer import es_is_multiline
from .utils import estest, load_data, TEST_INDEX_NAME, ENDPOINT
from src.odfe_sql_cli.odfesql_cli import OdfeSqlCli
from src.odfe_sql_cli.esconnection import ESConnection
from src.odfe_sql_cli.esstyle import style_factory

AUTH = None
QUERY_WITH_CTRL_D = "select * from %s;\r\x04\r" % TEST_INDEX_NAME
USE_AWS_CREDENTIALS = False
QUERY_LANGUAGE = "sql"


@pytest.fixture()
def cli(default_config_location):
    return OdfeSqlCli(clirc_file=default_config_location, always_use_pager=False)


class TestOdfeSqlCli:
    def test_connect(self, cli):
        with mock.patch.object(ESConnection, "__init__", return_value=None) as mock_ESConnection, mock.patch.object(
            ESConnection, "set_connection"
        ) as mock_set_connectiuon:
            cli.connect(endpoint=ENDPOINT)

            mock_ESConnection.assert_called_with(ENDPOINT, AUTH, USE_AWS_CREDENTIALS, QUERY_LANGUAGE)
            mock_set_connectiuon.assert_called()

    @estest
    @pytest.mark.skip(reason="due to prompt_toolkit throwing error, no way of currently testing this")
    def test_run_cli(self, connection, cli, capsys):
        doc = {"a": "aws"}
        load_data(connection, doc)

        # the title is colored by formatter
        expected = (
            "fetched rows / total rows = 1/1" "\n+-----+\n| \x1b[38;5;47;01ma\x1b[39;00m   |\n|-----|\n| aws |\n+-----+"
        )

        with mock.patch.object(OdfeSqlCli, "echo_via_pager") as mock_pager, mock.patch.object(
            cli, "build_cli"
        ) as mock_prompt:
            inp = create_pipe_input()
            inp.send_text(QUERY_WITH_CTRL_D)

            mock_prompt.return_value = PromptSession(
                input=inp, multiline=es_is_multiline(cli), style=style_factory(cli.syntax_style, cli.cli_style)
            )

            cli.connect(ENDPOINT)
            cli.run_cli()
            out, err = capsys.readouterr()
            inp.close()

            mock_pager.assert_called_with(expected)
            assert out.__contains__("Endpoint: %s" % ENDPOINT)
            assert out.__contains__("See you next search!")
