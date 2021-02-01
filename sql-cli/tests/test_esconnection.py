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
import pytest
import mock
from textwrap import dedent

from elasticsearch.exceptions import ConnectionError
from elasticsearch import Elasticsearch, RequestsHttpConnection

from .utils import estest, load_data, run, TEST_INDEX_NAME
from src.odfe_sql_cli.esconnection import ESConnection

INVALID_ENDPOINT = "http://invalid:9200"
OPEN_DISTRO_ENDPOINT = "https://opedistro:9200"
AES_ENDPOINT = "https://fake.es.amazonaws.com"
AUTH = ("username", "password")


class TestExecutor:
    def load_data_to_es(self, connection):
        doc = {"a": "aws"}
        load_data(connection, doc)

    @estest
    def test_query(self, connection):
        self.load_data_to_es(connection)

        assert run(connection, "select * from %s" % TEST_INDEX_NAME) == dedent(
            """\
            fetched rows / total rows = 1/1
            +-----+
            | a   |
            |-----|
            | aws |
            +-----+"""
        )

    @estest
    def test_query_nonexistent_index(self, connection):
        self.load_data_to_es(connection)

        expected = {
            "reason": "Error occurred in Elasticsearch engine: no such index [non-existed]",
            "details": "org.elasticsearch.index.IndexNotFoundException: no such index [non-existed]\nFor more "
            "details, please send request for Json format to see the raw response from elasticsearch "
            "engine.",
            "type": "IndexNotFoundException",
        }

        with mock.patch("src.odfe_sql_cli.esconnection.click.secho") as mock_secho:
            run(connection, "select * from non-existed")

        mock_secho.assert_called_with(message=str(expected), fg="red")

    def test_connection_fail(self):
        test_executor = ESConnection(endpoint=INVALID_ENDPOINT)
        err_message = "Can not connect to endpoint %s" % INVALID_ENDPOINT

        with mock.patch("sys.exit") as mock_sys_exit, mock.patch("src.odfe_sql_cli.esconnection.click.secho") as mock_secho:
            test_executor.set_connection()

        mock_sys_exit.assert_called()
        mock_secho.assert_called_with(message=err_message, fg="red")

    def test_lost_connection(self):
        test_esexecutor = ESConnection(endpoint=INVALID_ENDPOINT)

        def side_effect_set_connection(is_reconnected):
            if is_reconnected:
                pass
            else:
                return ConnectionError()

        with mock.patch("src.odfe_sql_cli.esconnection.click.secho") as mock_secho, mock.patch.object(
            test_esexecutor, "set_connection"
        ) as mock_set_connection:
            # Assume reconnection success
            mock_set_connection.side_effect = side_effect_set_connection(is_reconnected=True)
            test_esexecutor.handle_server_close_connection()

            mock_secho.assert_any_call(message="Reconnecting...", fg="green")
            mock_secho.assert_any_call(message="Reconnected! Please run query again", fg="green")
            # Assume reconnection fail
            mock_set_connection.side_effect = side_effect_set_connection(is_reconnected=False)
            test_esexecutor.handle_server_close_connection()

            mock_secho.assert_any_call(message="Reconnecting...", fg="green")
            mock_secho.assert_any_call(
                message="Connection Failed. Check your ES is running and then come back", fg="red"
            )

    def test_reconnection_exception(self):
        test_executor = ESConnection(endpoint=INVALID_ENDPOINT)

        with pytest.raises(ConnectionError) as error:
            assert test_executor.set_connection(True)

    def test_select_client(self):
        od_test_executor = ESConnection(endpoint=OPEN_DISTRO_ENDPOINT, http_auth=AUTH)
        aes_test_executor = ESConnection(endpoint=AES_ENDPOINT, use_aws_authentication=True)

        with mock.patch.object(od_test_executor, "get_open_distro_client") as mock_od_client, mock.patch.object(
            ESConnection, "is_sql_plugin_installed", return_value=True
        ):
            od_test_executor.set_connection()
            mock_od_client.assert_called()

        with mock.patch.object(aes_test_executor, "get_aes_client") as mock_aes_client, mock.patch.object(
            ESConnection, "is_sql_plugin_installed", return_value=True
        ):
            aes_test_executor.set_connection()
            mock_aes_client.assert_called()

    def test_get_od_client(self):
        od_test_executor = ESConnection(endpoint=OPEN_DISTRO_ENDPOINT, http_auth=AUTH)

        with mock.patch.object(Elasticsearch, "__init__", return_value=None) as mock_es:
            od_test_executor.get_open_distro_client()

            mock_es.assert_called_with(
                [OPEN_DISTRO_ENDPOINT], http_auth=AUTH, verify_certs=False, ssl_context=od_test_executor.ssl_context,
                connection_class=RequestsHttpConnection
            )

    def test_get_aes_client(self):
        aes_test_executor = ESConnection(endpoint=AES_ENDPOINT, use_aws_authentication=True)

        with mock.patch.object(Elasticsearch, "__init__", return_value=None) as mock_es:
            aes_test_executor.get_aes_client()

            mock_es.assert_called_with(
                hosts=[AES_ENDPOINT],
                http_auth=aes_test_executor.aws_auth,
                use_ssl=True,
                verify_certs=True,
                connection_class=RequestsHttpConnection,
            )
