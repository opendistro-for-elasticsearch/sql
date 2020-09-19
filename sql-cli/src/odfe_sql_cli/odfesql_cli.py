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
from __future__ import unicode_literals

import click
import re
import pyfiglet
import os
import json

from prompt_toolkit.completion import WordCompleter
from prompt_toolkit.enums import DEFAULT_BUFFER
from prompt_toolkit.shortcuts import PromptSession
from prompt_toolkit.filters import HasFocus, IsDone
from prompt_toolkit.lexers import PygmentsLexer
from prompt_toolkit.layout.processors import ConditionalProcessor, HighlightMatchingBracketProcessor
from prompt_toolkit.auto_suggest import AutoSuggestFromHistory
from pygments.lexers.sql import SqlLexer

from .config import get_config
from .esconnection import ESConnection
from .esbuffer import es_is_multiline
from .esstyle import style_factory, style_factory_output
from .formatter import Formatter
from .utils import OutputSettings
from . import __version__


# Ref: https://stackoverflow.com/questions/30425105/filter-special-chars-such-as-color-codes-from-shell-output
COLOR_CODE_REGEX = re.compile(r"\x1b(\[.*?[@-~]|\].*?(\x07|\x1b\\))")

click.disable_unicode_literals_warning = True


class OdfeSqlCli:
    """OdfeSqlCli instance is used to build and run the ODFE SQL CLI."""

    def __init__(self, clirc_file=None, always_use_pager=False, use_aws_authentication=False):
        # Load conf file
        config = self.config = get_config(clirc_file)
        literal = self.literal = self._get_literals()

        self.prompt_app = None
        self.es_executor = None
        self.always_use_pager = always_use_pager
        self.use_aws_authentication = use_aws_authentication
        self.keywords_list = literal["keywords"]
        self.functions_list = literal["functions"]
        self.syntax_style = config["main"]["syntax_style"]
        self.cli_style = config["colors"]
        self.table_format = config["main"]["table_format"]
        self.multiline_continuation_char = config["main"]["multiline_continuation_char"]
        self.multi_line = config["main"].as_bool("multi_line")
        self.multiline_mode = config["main"].get("multi_line_mode", "src")
        self.null_string = config["main"].get("null_string", "null")
        self.style_output = style_factory_output(self.syntax_style, self.cli_style)

    def build_cli(self):
        # TODO: Optimize index suggestion to serve indices options only at the needed position, such as 'from'
        indices_list = self.es_executor.indices_list
        sql_completer = WordCompleter(self.keywords_list + self.functions_list + indices_list, ignore_case=True)

        # https://stackoverflow.com/a/13726418 denote multiple unused arguments of callback in Python
        def get_continuation(width, *_):
            continuation = self.multiline_continuation_char * (width - 1) + " "
            return [("class:continuation", continuation)]

        prompt_app = PromptSession(
            lexer=PygmentsLexer(SqlLexer),
            completer=sql_completer,
            complete_while_typing=True,
            # TODO: add history, refer to pgcli approach
            # history=history,
            style=style_factory(self.syntax_style, self.cli_style),
            prompt_continuation=get_continuation,
            multiline=es_is_multiline(self),
            auto_suggest=AutoSuggestFromHistory(),
            input_processors=[
                ConditionalProcessor(
                    processor=HighlightMatchingBracketProcessor(chars="[](){}"),
                    filter=HasFocus(DEFAULT_BUFFER) & ~IsDone(),
                )
            ],
            tempfile_suffix=".sql",
        )

        return prompt_app

    def run_cli(self):
        """
        Print welcome page, goodbye message.

        Run the CLI and keep listening to user's input.
        """
        self.prompt_app = self.build_cli()

        settings = OutputSettings(
            max_width=self.prompt_app.output.get_size().columns,
            style_output=self.style_output,
            table_format=self.table_format,
            missingval=self.null_string,
        )

        # print Banner
        banner = pyfiglet.figlet_format("Open Distro", font="slant")
        print(banner)

        # print info on the welcome page
        print("Server: Open Distro for ES %s" % self.es_executor.es_version)
        print("CLI Version: %s" % __version__)
        print("Endpoint: %s" % self.es_executor.endpoint)

        while True:
            try:
                text = self.prompt_app.prompt(message="odfesql> ")
            except KeyboardInterrupt:
                continue  # Control-C pressed. Try again.
            except EOFError:
                break  # Control-D pressed.

            try:
                output = self.es_executor.execute_query(text)
                if output:
                    formatter = Formatter(settings)
                    formatted_output = formatter.format_output(output)
                    self.echo_via_pager("\n".join(formatted_output))

            except Exception as e:
                print(repr(e))

        print("See you next search!")

    def is_too_wide(self, line):
        """Will this line be too wide to fit into terminal?"""
        if not self.prompt_app:
            return False
        return len(COLOR_CODE_REGEX.sub("", line)) > self.prompt_app.output.get_size().columns

    def is_too_tall(self, lines):
        """Are there too many lines to fit into terminal?"""
        if not self.prompt_app:
            return False
        return len(lines) >= (self.prompt_app.output.get_size().rows - 4)

    def echo_via_pager(self, text, color=None):
        lines = text.split("\n")
        if self.always_use_pager:
            click.echo_via_pager(text, color=color)

        elif self.is_too_tall(lines) or any(self.is_too_wide(l) for l in lines):
            click.echo_via_pager(text, color=color)
        else:
            click.echo(text, color=color)

    def connect(self, endpoint, http_auth=None):
        self.es_executor = ESConnection(endpoint, http_auth, self.use_aws_authentication)
        self.es_executor.set_connection()

    def _get_literals(self):
        """Parse "esliterals.json" with literal type of SQL "keywords" and "functions", which
        are SQL keywords and functions supported by Open Distro SQL Plugin.

        :return: a dict that is parsed from esliterals.json
        """
        from .esliterals import __file__ as package_root

        package_root = os.path.dirname(package_root)

        literal_file = os.path.join(package_root, "esliterals.json")
        with open(literal_file) as f:
            literals = json.load(f)
            return literals
