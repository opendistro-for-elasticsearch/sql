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

opendistro.ppl.enabled
======================

Description
-----------

You can disable SQL plugin to reject all coming requests.

1. The default value is true.
2. This setting is node scope.
3. This setting can be updated dynamically.

Notes. Calls to _opendistro/_ppl include index names in the request body, so they have the same access policy considerations as the bulk, mget, and msearch operations. if rest.action.multi.allow_explicit_index set to false, PPL plugin will be disabled.

Example 1
---------

You can update the setting with a new value like this.

PPL query::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X PUT localhost:9200/_cluster/settings \
    ... -d '{"transient" : {"opendistro.ppl.enabled" : "false"}}'
    {
      "acknowledged": true,
      "persistent": {},
      "transient": {
        "opendistro": {
          "ppl": {
            "enabled": "false"
          }
        }
      }
    }

Example 2
---------

Query result after the setting updated is like:

PPL query::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_ppl \
    {
      "error": {
        "reason": "Invalid Query",
        "details": "Either opendistro.ppl.enabled or rest.action.multi.allow_explicit_index setting is false",
        "type": "IllegalAccessException"
      },
      "status": 400
    }

Example 3
---------

You can reset the setting to default value like this.

PPL query::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X PUT localhost:9200/_cluster/settings \
    ... -d '{"transient" : {"opendistro.ppl.enabled" : null}}'
    {
      "acknowledged": true,
      "persistent": {},
      "transient": {}
    }

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

