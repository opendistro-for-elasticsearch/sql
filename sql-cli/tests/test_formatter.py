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
from __future__ import unicode_literals, print_function

import mock
import pytest
from collections import namedtuple

from src.odfe_sql_cli.odfesql_cli import OdfeSqlCli, COLOR_CODE_REGEX
from src.odfe_sql_cli.formatter import Formatter
from src.odfe_sql_cli.utils import OutputSettings


class TestFormatter:
    @pytest.fixture
    def pset_pager_mocks(self):
        cli = OdfeSqlCli()
        with mock.patch("src.odfe_sql_cli.main.click.echo") as mock_echo, mock.patch(
            "src.odfe_sql_cli.main.click.echo_via_pager"
        ) as mock_echo_via_pager, mock.patch.object(cli, "prompt_app") as mock_app:
            yield cli, mock_echo, mock_echo_via_pager, mock_app

    termsize = namedtuple("termsize", ["rows", "columns"])
    test_line = "-" * 10
    test_data = [
        (10, 10, "\n".join([test_line] * 7)),
        (10, 10, "\n".join([test_line] * 6)),
        (10, 10, "\n".join([test_line] * 5)),
        (10, 10, "-" * 11),
        (10, 10, "-" * 10),
        (10, 10, "-" * 9),
    ]

    use_pager_when_on = [True, True, False, True, False, False]

    test_ids = [
        "Output longer than terminal height",
        "Output equal to terminal height",
        "Output shorter than terminal height",
        "Output longer than terminal width",
        "Output equal to terminal width",
        "Output shorter than terminal width",
    ]

    pager_test_data = [l + (r,) for l, r in zip(test_data, use_pager_when_on)]

    def test_format_output(self):
        settings = OutputSettings(table_format="psql")
        formatter = Formatter(settings)
        data = {
            "schema": [{"name": "name", "type": "text"}, {"name": "age", "type": "long"}],
            "total": 1,
            "datarows": [["Tim", 24]],
            "size": 1,
            "status": 200,
        }

        results = formatter.format_output(data)

        expected = [
            "fetched rows / total rows = 1/1",
            "+--------+-------+",
            "| name   | age   |",
            "|--------+-------|",
            "| Tim    | 24    |",
            "+--------+-------+",
        ]
        assert list(results) == expected

    def test_format_alias_output(self):
        settings = OutputSettings(table_format="psql")
        formatter = Formatter(settings)
        data = {
            "schema": [{"name": "name", "alias": "n", "type": "text"}],
            "total": 1,
            "datarows": [["Tim"]],
            "size": 1,
            "status": 200,
        }

        results = formatter.format_output(data)

        expected = [
            "fetched rows / total rows = 1/1",
            "+-----+",
            "| n   |",
            "|-----|",
            "| Tim |",
            "+-----+",
        ]
        assert list(results) == expected

    def test_format_array_output(self):
        settings = OutputSettings(table_format="psql")
        formatter = Formatter(settings)
        data = {
            "schema": [{"name": "name", "type": "text"}, {"name": "age", "type": "long"}],
            "total": 1,
            "datarows": [["Tim", [24, 25]]],
            "size": 1,
            "status": 200,
        }

        results = formatter.format_output(data)

        expected = [
            "fetched rows / total rows = 1/1",
            "+--------+---------+",
            "| name   | age     |",
            "|--------+---------|",
            "| Tim    | [24,25] |",
            "+--------+---------+",
        ]
        assert list(results) == expected

    def test_format_output_vertical(self):
        settings = OutputSettings(table_format="psql", max_width=1)
        formatter = Formatter(settings)
        data = {
            "schema": [{"name": "name", "type": "text"}, {"name": "age", "type": "long"}],
            "total": 1,
            "datarows": [["Tim", 24]],
            "size": 1,
            "status": 200,
        }

        expanded = [
            "fetched rows / total rows = 1/1",
            "-[ RECORD 1 ]-------------------------",
            "name | Tim",
            "age  | 24",
        ]

        with mock.patch("src.odfe_sql_cli.main.click.secho") as mock_secho, mock.patch("src.odfe_sql_cli.main.click.confirm") as mock_confirm:
            expanded_results = formatter.format_output(data)

        mock_secho.assert_called_with(message="Output longer than terminal width", fg="red")
        mock_confirm.assert_called_with("Do you want to display data vertically for better visual effect?")

        assert "\n".join(expanded_results) == "\n".join(expanded)

    def test_fake_large_output(self):
        settings = OutputSettings(table_format="psql")
        formatter = Formatter(settings)
        fake_large_data = {
            "schema": [{"name": "name", "type": "text"}, {"name": "age", "type": "long"}],
            "total": 1000,
            "datarows": [["Tim", [24, 25]]],
            "size": 200,
            "status": 200,
        }

        results = formatter.format_output(fake_large_data)

        expected = [
            "fetched rows / total rows = 200/1000\n"
            "Attention: Use LIMIT keyword when retrieving more than 200 rows of data",
            "+--------+---------+",
            "| name   | age     |",
            "|--------+---------|",
            "| Tim    | [24,25] |",
            "+--------+---------+",
        ]
        assert list(results) == expected

    @pytest.mark.parametrize("term_height,term_width,text,use_pager", pager_test_data, ids=test_ids)
    def test_pager(self, term_height, term_width, text, use_pager, pset_pager_mocks):
        cli, mock_echo, mock_echo_via_pager, mock_cli = pset_pager_mocks
        mock_cli.output.get_size.return_value = self.termsize(rows=term_height, columns=term_width)

        cli.echo_via_pager(text)

        if use_pager:
            mock_echo.assert_not_called()
            mock_echo_via_pager.assert_called()
        else:
            mock_echo_via_pager.assert_not_called()
            mock_echo.assert_called()

    @pytest.mark.parametrize(
        "text,expected_length",
        [
            (
                "22200K .......\u001b[0m\u001b[91m... .......... ...\u001b[0m\u001b[91m.\u001b[0m\u001b[91m...... "
                ".........\u001b[0m\u001b[91m.\u001b[0m\u001b[91m \u001b[0m\u001b[91m.\u001b[0m\u001b[91m.\u001b["
                "0m\u001b[91m.\u001b[0m\u001b[91m.\u001b[0m\u001b[91m...... 50% 28.6K 12m55s",
                78,
            ),
            ("=\u001b[m=", 2),
            ("-\u001b]23\u0007-", 2),
        ],
    )
    def test_color_pattern(self, text, expected_length):
        assert len(COLOR_CODE_REGEX.sub("", text)) == expected_length
