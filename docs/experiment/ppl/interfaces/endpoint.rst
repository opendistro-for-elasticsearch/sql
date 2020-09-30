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

Explain
=======

Description
-----------

You can send HTTP explain request to endpoint **/_opendistro/_ppl/_explain** with your query in request body to understand the execution plan for the PPL query. The explain endpoint is useful when user want to get insight how the query is executed in the engine.

Example
-------

The following PPL query demonstrated that where and stats command were pushed down to Elasticsearch DSL aggregation query::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_ppl/_explain \
    ... -d '{"query" : "source=accounts | where age > 10 | stats avg(age)"}'
    {
      "root": {
        "name": "ProjectOperator",
        "description": {
          "fields": "[avg(age)]"
        },
        "children": [
          {
            "name": "ElasticsearchIndexScan",
            "description": {
              "request": "ElasticsearchQueryRequest(indexName=accounts, sourceBuilder={\"from\":0,\"size\":0,\"timeout\":\"1m\",\"query\":{\"range\":{\"age\":{\"from\":10,\"to\":null,\"include_lower\":false,\"include_upper\":true,\"boost\":1.0}}},\"sort\":[{\"_doc\":{\"order\":\"asc\"}}],\"aggregations\":{\"avg(age)\":{\"avg\":{\"field\":\"age\"}}}}, searchDone=false)"
            },
            "children": []
          }
        ]
      }
    }

