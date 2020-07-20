# Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License").
# You may not use this file except in compliance with the License.
# A copy of the License is located at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# or in the "license" file accompanying this file. This file is distributed
# on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
# express or implied. See the License for the specific language governing
# permissions and limitations under the License.

import doctest
import os
import os.path
import zc.customdoctests
import json
import re
import random
import subprocess
import unittest
import click

from functools import partial
from odfe_sql_cli.esconnection import ESConnection
from odfe_sql_cli.utils import OutputSettings
from odfe_sql_cli.formatter import Formatter
from elasticsearch import Elasticsearch, helpers

ENDPOINT = "http://localhost:9200"
ACCOUNTS = "accounts"
EMPLOYEES = "employees"
PEOPLE = "people"


class DocTestConnection(ESConnection):

    def __init__(self, query_language="sql"):
        super(DocTestConnection, self).__init__(endpoint=ENDPOINT, query_language=query_language)
        self.set_connection()

        settings = OutputSettings(table_format="psql", is_vertical=False)
        self.formatter = Formatter(settings)

    def process(self, statement):
        data = self.execute_query(statement, use_console=False)
        output = self.formatter.format_output(data)
        output = "\n".join(output)

        click.echo(output)


def pretty_print(s):
    try:
        d = json.loads(s)
        print(json.dumps(d, indent=2))
    except json.decoder.JSONDecodeError:
        print(s)


sql_cmd = DocTestConnection(query_language="sql")
ppl_cmd = DocTestConnection(query_language="ppl")
test_data_client = Elasticsearch([ENDPOINT], verify_certs=True)


def sql_cli_transform(s):
    return u'sql_cmd.process({0})'.format(repr(s.strip().rstrip(';')))


def ppl_cli_transform(s):
    return u'ppl_cmd.process({0})'.format(repr(s.strip().rstrip(';')))


def bash_transform(s):
    # TODO: add ppl support, be default cli uses sql
    if s.startswith("odfesql"):
        s = re.search(r"odfesql\s+-q\s+\"(.*?)\"", s).group(1)
        return u'cmd.process({0})'.format(repr(s.strip().rstrip(';')))
    return (r'pretty_print(sh("""%s""").stdout.decode("utf-8"))' % s) + '\n'


sql_cli_parser = zc.customdoctests.DocTestParser(
    ps1='od>', comment_prefix='#', transform=sql_cli_transform)

ppl_cli_parser = zc.customdoctests.DocTestParser(
    ps1='od>', comment_prefix='#', transform=ppl_cli_transform)

bash_parser = zc.customdoctests.DocTestParser(
    ps1=r'sh\$', comment_prefix='#', transform=bash_transform)


def set_up_test_indices(test):
    set_up(test)
    load_file("accounts.json", index_name=ACCOUNTS)
    load_file("people.json", index_name=PEOPLE)


def load_file(filename, index_name):
    # Create index with the mapping if mapping file exists
    mapping_file_path = './test_mapping/' + filename
    if os.path.isfile(mapping_file_path):
        with open(mapping_file_path, 'r') as f:
            test_data_client.indices.create(index=index_name, body=f.read())

    # generate iterable data
    data_file_path = './test_data/' + filename
    def load_json():
        with open(data_file_path, 'r') as f:
            for line in f:
                yield json.loads(line)

    # Need to enable refresh, because the load won't be visible to search immediately
    # https://stackoverflow.com/questions/57840161/elasticsearch-python-bulk-helper-api-with-refresh
    helpers.bulk(test_data_client, load_json(), stats_only=True, index=index_name, refresh='wait_for')


def set_up(test):
    test.globs['sql_cmd'] = sql_cmd
    test.globs['ppl_cmd'] = ppl_cmd


def tear_down(test):
    # drop leftover tables after each test
    test_data_client.indices.delete(index=[ACCOUNTS, EMPLOYEES, PEOPLE], ignore_unavailable=True)


docsuite = partial(doctest.DocFileSuite,
                   tearDown=tear_down,
                   parser=sql_cli_parser,
                   optionflags=doctest.NORMALIZE_WHITESPACE | doctest.ELLIPSIS,
                   encoding='utf-8')


doctest_file = partial(os.path.join, '../docs')


def doctest_files(items):
    return (doctest_file(item) for item in items)


class DocTests(unittest.TestSuite):
    def run(self, result, debug=False):
        super().run(result, debug)


def doc_suite(fn):
    return docsuite(
        fn,
        parser=bash_parser,
        setUp=set_up_test_indices,
        globs={
            'sh': partial(
                subprocess.run,
                stdin=subprocess.PIPE,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                timeout=60,
                shell=True
            ),
            'pretty_print': pretty_print
        }
    )


def load_tests(loader, suite, ignore):
    tests = []
    # Load doctest docs by category
    with open('../docs/category.json') as json_file:
        category = json.load(json_file)

    bash_docs = category['bash']
    ppl_cli_docs = category['ppl_cli']
    sql_cli_docs = category['sql_cli']

    # docs with bash-based examples
    for fn in doctest_files(bash_docs):
        tests.append(doc_suite(fn))

    # docs with sql-cli based examples
    # TODO: add until the migration to new architecture is done, then we have an artifact including ppl and sql both
    # for fn in doctest_files('sql/basics.rst'):
    #     tests.append(docsuite(fn, setUp=set_up_accounts))
    for fn in doctest_files(sql_cli_docs):
        tests.append(
            docsuite(
                fn,
                parser=sql_cli_parser,
                setUp=set_up_test_indices
            )
        )

    # docs with ppl-cli based examples
    for fn in doctest_files(ppl_cli_docs):
        tests.append(
            docsuite(
                fn,
                parser=ppl_cli_parser,
                setUp=set_up_test_indices
            )
        )

    # randomize order of tests to make sure they don't depend on each other
    random.shuffle(tests)

    # prepend a temporary doc to enable new engine so new SQL docs followed can pass
    tests.insert(0, doc_suite('../docs/user/dql/newsql.rst'))

    return DocTests(tests)
