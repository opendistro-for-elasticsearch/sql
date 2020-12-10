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
import boto3
import click
import logging
import ssl
import sys
import urllib3

from elasticsearch import Elasticsearch, RequestsHttpConnection
from elasticsearch.exceptions import ConnectionError, RequestError
from elasticsearch.connection import create_ssl_context
from requests_aws4auth import AWS4Auth


class ESConnection:
    """ESConnection instances are used to set up and maintain client to Elasticsearch cluster,
    as well as send user's SQL query to Elasticsearch.
    """

    def __init__(self, endpoint=None, http_auth=None, use_aws_authentication=False, query_language="sql"):
        """Initialize an ESConnection instance.

        Set up client and get indices list.

        :param endpoint: an url in the format of "http://localhost:9200"
        :param http_auth: a tuple in the format of (username, password)
        """
        self.client = None
        self.ssl_context = None
        self.es_version = None
        self.plugins = None
        self.aws_auth = None
        self.indices_list = []
        self.endpoint = endpoint
        self.http_auth = http_auth
        self.use_aws_authentication = use_aws_authentication
        self.query_language = query_language

    def get_indices(self):
        if self.client:
            res = self.client.indices.get_alias().keys()
            self.indices_list = list(res)

    def get_aes_client(self):
        service = "es"
        session = boto3.Session()
        credentials = session.get_credentials()
        region = session.region_name

        if credentials is not None:
            self.aws_auth = AWS4Auth(credentials.access_key, credentials.secret_key, region, service)
        else:
            click.secho(message="Can not retrieve your AWS credentials, check your AWS config", fg="red")

        aes_client = Elasticsearch(
            hosts=[self.endpoint],
            http_auth=self.aws_auth,
            use_ssl=True,
            verify_certs=True,
            connection_class=RequestsHttpConnection,
        )

        return aes_client

    def get_open_distro_client(self):
        ssl_context = self.ssl_context = create_ssl_context()
        ssl_context.check_hostname = False
        ssl_context.verify_mode = ssl.CERT_NONE

        open_distro_client = Elasticsearch(
            [self.endpoint],
            http_auth=self.http_auth,
            verify_certs=False,
            ssl_context=ssl_context,
            connection_class=RequestsHttpConnection,
        )

        return open_distro_client

    def is_sql_plugin_installed(self, es_client):
        self.plugins = es_client.cat.plugins(params={"s": "component", "v": "true"})
        sql_plugin_name_list = ["opendistro-sql", "opendistro_sql"]
        return any(x in self.plugins for x in sql_plugin_name_list)

    def set_connection(self, is_reconnect=False):
        urllib3.disable_warnings()
        logging.captureWarnings(True)

        if self.http_auth:
            es_client = self.get_open_distro_client()

        elif self.use_aws_authentication:
            es_client = self.get_aes_client()
        else:
            es_client = Elasticsearch([self.endpoint], verify_certs=True)

        # check connection. check Open Distro Elasticsearch SQL plugin availability.
        try:
            if not self.is_sql_plugin_installed(es_client):
                click.secho(
                    message="Must have Open Distro SQL plugin installed in your Elasticsearch "
                    "instance!\nCheck this out: https://github.com/opendistro-for-elasticsearch/sql",
                    fg="red",
                )
                click.echo(self.plugins)
                sys.exit()

            # info() may throw ConnectionError, if connection fails to establish
            info = es_client.info()
            self.es_version = info["version"]["number"]
            self.client = es_client
            self.get_indices()

        except ConnectionError as error:
            if is_reconnect:
                # re-throw error
                raise error
            else:
                click.secho(message="Can not connect to endpoint %s" % self.endpoint, fg="red")
                click.echo(repr(error))
                sys.exit(0)

    def handle_server_close_connection(self):
        """Used during CLI execution."""
        try:
            click.secho(message="Reconnecting...", fg="green")
            self.set_connection(is_reconnect=True)
            click.secho(message="Reconnected! Please run query again", fg="green")
        except ConnectionError as reconnection_err:
            click.secho(message="Connection Failed. Check your ES is running and then come back", fg="red")
            click.secho(repr(reconnection_err), err=True, fg="red")

    def execute_query(self, query, output_format="jdbc", explain=False, use_console=True):
        """
        Handle user input, send SQL query and get response.

        :param use_console: use console to interact with user, otherwise it's single query
        :param query: SQL query
        :param output_format: jdbc/csv
        :param explain: if True, use _explain API.
        :return: raw http response
        """

        # TODO: consider add evaluator/handler to filter obviously-invalid input,
        #  to save cost of http client.
        # deal with input
        final_query = query.strip().strip(";")

        try:
            if self.query_language == "sql":
                data = self.client.transport.perform_request(
                    url="/_opendistro/_sql/_explain" if explain else "/_opendistro/_sql/",
                    method="POST",
                    params=None if explain else {"format": output_format},
                    body={"query": final_query},
                )
            else:
                data = self.client.transport.perform_request(
                    url="/_opendistro/_ppl/_explain" if explain else "/_opendistro/_ppl/",
                    method="POST",
                    params=None if explain else {"format": output_format},
                    body={"query": final_query},
                )
            return data

        # handle client lost during execution
        except ConnectionError:
            if use_console:
                self.handle_server_close_connection()
        except RequestError as error:
            click.secho(message=str(error.info["error"]), fg="red")
