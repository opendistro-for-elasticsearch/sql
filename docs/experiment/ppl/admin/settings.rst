.. highlight:: sh

============
PPL Settings
============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1


Introduction
============

When Elasticsearch bootstraps, PPL plugin will register a few settings in Elasticsearch cluster settings. Most of the settings are able to change dynamically so you can control the behavior of PPL plugin without need to bounce your cluster.

opendistro.ppl.query.memory_limit
=================================

Description
-----------

You can set heap memory usage limit for PPL query. When query running, it will detected whether the heap memory usage under the limit, if not, it will terminated the current query. The default value is: 85%

Example
-------

PPL query::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X PUT localhost:9200/_cluster/settings \
    ... -d '{"persistent" : {"opendistro.ppl.query.memory_limit" : "80%"}}'
    {
      "acknowledged": true,
      "persistent": {
        "opendistro": {
          "ppl": {
            "query": {
              "memory_limit": "80%"
            }
          }
        }
      },
      "transient": {}
    }

opendistro.query.size_limit
=================================

Description
-----------

The size configure the maximum amount of documents to be pull from Elasticsearch. The default value is: 200

Notes: This setting will impact the correctness of the aggregation operation, for example, there are 1000 docs in the index, by default, only 200 docs will be extract from index and do aggregation.

Example
-------

PPL query::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X PUT localhost:9200/_cluster/settings \
    ... -d '{"persistent" : {"opendistro.query.size_limit" : "1000"}}'
    {
      "acknowledged": true,
      "persistent": {
        "opendistro": {
          "query": {
            "size_limit": "1000"
          }
        }
      },
      "transient": {}
    }

