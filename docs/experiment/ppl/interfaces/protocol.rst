.. highlight:: sh

========
Protocol
========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1


Introduction
============

For the protocol, PPL endpoint provides response formats in the JDBC format. JDBC format is widely used because it provides schema information and more functionality such as pagination. Besides JDBC driver, various clients can benefit from the detailed and well formatted response.


Request/Response Format
==============

Description
-----------

The body of HTTP POST request can take PPL query.

Example 1
---------
PPL query::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_ppl \
    ... -d '{"query" : "source=accounts | fields firstname, lastname"}'
    {
      "schema": [
        {
          "name": "firstname",
          "type": "string"
        },
        {
          "name": "lastname",
          "type": "string"
        }
      ],
      "total": 4,
      "datarows": [
        [
          "Amber",
          "Duke"
        ],
        [
          "Hattie",
          "Bond"
        ],
        [
          "Nanette",
          "Bates"
        ],
        [
          "Dale",
          "Adams"
        ]
      ],
      "size": 4
    }

JDBC Format
===========

Description
-----------

By default the plugin return JDBC format. JDBC format is provided for JDBC driver and client side that needs both schema and result set well formatted.

Example 1
---------

Here is an example for normal response. The `schema` includes field name and its type and `datarows` includes the result set.

PPL query::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_ppl \
    ... -d '{"query" : "source=accounts | fields firstname, lastname"}'
    {
      "schema": [
        {
          "name": "firstname",
          "type": "string"
        },
        {
          "name": "lastname",
          "type": "string"
        }
      ],
      "total": 4,
      "datarows": [
        [
          "Amber",
          "Duke"
        ],
        [
          "Hattie",
          "Bond"
        ],
        [
          "Nanette",
          "Bates"
        ],
        [
          "Dale",
          "Adams"
        ]
      ],
      "size": 4
    }

Example 2
---------

If any error occurred, error message and the cause will be returned instead.

PPL query::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_ppl \
    ... -d '{"query" : "source=unknown | fields firstname, lastname"}'
    {
      "error": {
        "reason": "Error occurred in Elasticsearch engine: no such index [unknown]",
        "details": "org.elasticsearch.index.IndexNotFoundException: no such index [unknown]\nFor more details, please send request for Json format to see the raw response from elasticsearch engine.",
        "type": "IndexNotFoundException"
      },
      "status": 404
    }

