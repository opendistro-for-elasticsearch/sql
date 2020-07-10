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

import logging

import pygments.styles
from pygments.token import string_to_tokentype, Token
from pygments.style import Style as PygmentsStyle
from pygments.util import ClassNotFound
from prompt_toolkit.styles.pygments import style_from_pygments_cls
from prompt_toolkit.styles import merge_styles, Style

logger = logging.getLogger(__name__)

# map Pygments tokens (ptk 1.0) to class names (ptk 2.0).
TOKEN_TO_PROMPT_STYLE = {
    Token.Menu.Completions.Completion.Current: "completion-menu.completion.current",
    Token.Menu.Completions.Completion: "completion-menu.completion",
    Token.Menu.Completions.Meta.Current: "completion-menu.meta.completion.current",
    Token.Menu.Completions.Meta: "completion-menu.meta.completion",
    Token.Menu.Completions.MultiColumnMeta: "completion-menu.multi-column-meta",
    Token.Menu.Completions.ProgressButton: "scrollbar.arrow",  # best guess
    Token.Menu.Completions.ProgressBar: "scrollbar",  # best guess
    Token.SelectedText: "selected",
    Token.SearchMatch: "search",
    Token.SearchMatch.Current: "search.current",
    Token.Toolbar: "bottom-toolbar",
    Token.Toolbar.Off: "bottom-toolbar.off",
    Token.Toolbar.On: "bottom-toolbar.on",
    Token.Toolbar.Search: "search-toolbar",
    Token.Toolbar.Search.Text: "search-toolbar.text",
    Token.Toolbar.System: "system-toolbar",
    Token.Toolbar.Arg: "arg-toolbar",
    Token.Toolbar.Arg.Text: "arg-toolbar.text",
    Token.Toolbar.Transaction.Valid: "bottom-toolbar.transaction.valid",
    Token.Toolbar.Transaction.Failed: "bottom-toolbar.transaction.failed",
    Token.Output.Header: "output.header",
    Token.Output.OddRow: "output.odd-row",
    Token.Output.EvenRow: "output.even-row",
}

# reverse dict for cli_helpers, because they still expect Pygments tokens.
PROMPT_STYLE_TO_TOKEN = {v: k for k, v in TOKEN_TO_PROMPT_STYLE.items()}


def style_factory(name, cli_style):
    try:
        style = pygments.styles.get_style_by_name(name)
    except ClassNotFound:
        style = pygments.styles.get_style_by_name("native")

    prompt_styles = []

    for token in cli_style:
        # treat as prompt style name (2.0). See default style names here:
        # https://github.com/jonathanslenders/python-prompt-toolkit/blob/master/prompt_toolkit/styles/defaults.py
        prompt_styles.append((token, cli_style[token]))

    override_style = Style([("bottom-toolbar", "noreverse")])
    return merge_styles([style_from_pygments_cls(style), override_style, Style(prompt_styles)])


def style_factory_output(name, cli_style):
    try:
        style = pygments.styles.get_style_by_name(name).styles
    except ClassNotFound:
        style = pygments.styles.get_style_by_name("native").styles

    for token in cli_style:

        if token in PROMPT_STYLE_TO_TOKEN:
            token_type = PROMPT_STYLE_TO_TOKEN[token]
            style.update({token_type: cli_style[token]})
        else:
            # TODO: cli helpers will have to switch to ptk.Style
            logger.error("Unhandled style / class name: %s", token)

    class OutputStyle(PygmentsStyle):
        default_style = ""
        styles = style

    return OutputStyle
