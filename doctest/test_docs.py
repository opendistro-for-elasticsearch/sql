import doctest
import os
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


class DocTestConnection(ESConnection):

    def __init__(self):
        super(DocTestConnection, self).__init__(endpoint=ENDPOINT)
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


cmd = DocTestConnection()
test_data_client = Elasticsearch([ENDPOINT], verify_certs=True)


def cli_transform(s):
    return u'cmd.process({0})'.format(repr(s.strip().rstrip(';')))


def bash_transform(s):
    if s.startswith("odfesql"):
        s = re.search(r"odfesql\s+-q\s+\"(.*?)\"", s).group(1)
        return u'cmd.process({0})'.format(repr(s.strip().rstrip(';')))
    return (r'pretty_print(sh("""%s""").stdout.decode("utf-8"))' % s) + '\n'


cli_parser = zc.customdoctests.DocTestParser(
    ps1='od>', comment_prefix='#', transform=cli_transform)

bash_parser = zc.customdoctests.DocTestParser(
    ps1=r'sh\$', comment_prefix='#', transform=bash_transform)


def set_up_accounts(test):
    set_up(test)
    load_file("accounts.json", index_name=ACCOUNTS)


def load_file(filename, index_name):
    # todo: using one client under the hood for both uploading test data and set up cli connection?
    #   cmd.client?
    filepath = "./test_data/" + filename

    # generate iterable data
    def load_json():
        with open(filepath, "r") as f:
            for line in f:
                yield json.loads(line)

    # Need to enable refresh, because the load won't be visible to search immediately
    # https://stackoverflow.com/questions/57840161/elasticsearch-python-bulk-helper-api-with-refresh
    helpers.bulk(test_data_client, load_json(), stats_only=True, index=index_name, refresh='wait_for')


def set_up(test):
    test.globs['cmd'] = cmd


def tear_down(test):
    # drop leftover tables after each test
    # TODO: delete all will potentially also delete AES FGAC metadata index
    test_data_client.indices.delete(index=[ACCOUNTS, EMPLOYEES], ignore_unavailable=True)


docsuite = partial(doctest.DocFileSuite,
                   tearDown=tear_down,
                   parser=cli_parser, # TODO: add bash parser for curl
                   optionflags=doctest.NORMALIZE_WHITESPACE | doctest.ELLIPSIS,
                   encoding='utf-8')


doctest_file = partial(os.path.join, 'docs')


def doctest_files(*items):
    return (doctest_file(item) for item in items)


class DocTests(unittest.TestSuite):

    def run(self, result, debug=False):
        super().run(result, debug)


def load_tests(loader, suite, ignore):
    tests = []
    for fn in doctest_files('dql/explain.rst'):
        tests.append(
            docsuite(
                fn,
                parser=bash_parser,
                setUp=set_up_accounts,
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
        )

    for fn in doctest_files('dql/basics.rst'):  # todo: add more rst to test shuffle
        tests.append(docsuite(fn, setUp=set_up_accounts))

    # randomize order of tests to make sure they don't depend on each other
    random.shuffle(tests)
    return DocTests(tests)
