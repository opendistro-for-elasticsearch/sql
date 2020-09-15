.. highlight:: sh

========
Endpoint
========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1


Introduction
============

To send query request to PPL plugin, you MUST use HTTP POST request. POST request doesn't have length limitation and allows for other parameters passed to plugin for other functionality such as prepared statement. And also the explain endpoint is used very often for query translation and troubleshooting.

POST
====

Description
-----------

You can send HTTP POST request to endpoint **/_opendistro/_ppl** with your query in request body.

Example
-------

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
      "total": 4,
      "size": 4
    }

