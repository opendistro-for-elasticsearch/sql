
=============
Optimizations
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

In this doc, we will cover the optimization solution in the query engine and the limitations.

Logical Plan Optimization
=========================

The Logical Plan optimization transfer the logical operator by predefined rules. The following section will cover the detail rules.

Where Clause Optimization
-------------------------
The where clause will reduce the size of rows markedly, thus the where clause optimization is the most important optimization.

There are two rules involved in the core engine.

Filter Merge Rule
-----------------

The consecutive Filter operator will be merged as one Filter operator::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_ppl/_explain \
    ... -d '{"query" : "source=accounts | where age > 10 | where age < 20 | fields age"}'
    {
      "root": {
        "name": "ProjectOperator",
        "description": {
          "fields": "[age]"
        },
        "children": [
          {
            "name": "ElasticsearchIndexScan",
            "description": {
              "request": "ElasticsearchQueryRequest(indexName=accounts, sourceBuilder={\"from\":0,\"size\":200,\"timeout\":\"1m\",\"query\":{\"bool\":{\"filter\":[{\"range\":{\"age\":{\"from\":null,\"to\":20,\"include_lower\":true,\"include_upper\":false,\"boost\":1.0}}},{\"range\":{\"age\":{\"from\":10,\"to\":null,\"include_lower\":false,\"include_upper\":true,\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"sort\":[{\"_doc\":{\"order\":\"asc\"}}]}, searchDone=false)"
            },
            "children": []
          }
        ]
      }
    }


Filter Push Down Under Sort
---------------------------

The Filter operator should be push down under Sort operator::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_ppl/_explain \
    ... -d '{"query" : "source=accounts | sort age | where age < 20 | fields age"}'
    {
      "root": {
        "name": "ProjectOperator",
        "description": {
          "fields": "[age]"
        },
        "children": [
          {
            "name": "ElasticsearchIndexScan",
            "description": {
              "request": "ElasticsearchQueryRequest(indexName=accounts, sourceBuilder={\"from\":0,\"size\":200,\"timeout\":\"1m\",\"query\":{\"range\":{\"age\":{\"from\":null,\"to\":20,\"include_lower\":true,\"include_upper\":false,\"boost\":1.0}}},\"sort\":[{\"age\":{\"order\":\"asc\",\"missing\":\"_first\"}}]}, searchDone=false)"
            },
            "children": []
          }
        ]
      }
    }


Elasticsearch Specific Optimization
===================================

The Elasticsearch `Query DSL <https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html>`_ and `Aggregation <https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations.html>`_ also enabling the storage engine specific optimization.

Filter Merge Into Query DSL
---------------------------

The Filter operator will merge into Elasticsearch Query DSL::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_sql/_explain \
    ... -d '{"query" : "SELECT age FROM accounts WHERE age > 30"}'
    {
      "root": {
        "name": "ProjectOperator",
        "description": {
          "fields": "[age]"
        },
        "children": [
          {
            "name": "ElasticsearchIndexScan",
            "description": {
              "request": "ElasticsearchQueryRequest(indexName=accounts, sourceBuilder={\"from\":0,\"size\":200,\"timeout\":\"1m\",\"query\":{\"range\":{\"age\":{\"from\":30,\"to\":null,\"include_lower\":false,\"include_upper\":true,\"boost\":1.0}}},\"sort\":[{\"_doc\":{\"order\":\"asc\"}}]}, searchDone=false)"
            },
            "children": []
          }
        ]
      }
    }

Sort Merge Into Query DSL
-------------------------

The Sort operator will merge into Elasticsearch Query DSL::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_sql/_explain \
    ... -d '{"query" : "SELECT age FROM accounts ORDER BY age"}'
    {
      "root": {
        "name": "ProjectOperator",
        "description": {
          "fields": "[age]"
        },
        "children": [
          {
            "name": "ElasticsearchIndexScan",
            "description": {
              "request": "ElasticsearchQueryRequest(indexName=accounts, sourceBuilder={\"from\":0,\"size\":200,\"timeout\":\"1m\",\"sort\":[{\"age\":{\"order\":\"asc\",\"missing\":\"_first\"}}]}, searchDone=false)"
            },
            "children": []
          }
        ]
      }
    }

Because the Elasticsearch Script Based Sorting can't handle NULL/MISSING value, there is one exception is that if the sort list include expression other than field reference, it will not been merge into Query DSL::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_sql/_explain \
    ... -d '{"query" : "SELECT age FROM accounts ORDER BY abs(age)"}'
    {
      "root": {
        "name": "ProjectOperator",
        "description": {
          "fields": "[age]"
        },
        "children": [
          {
            "name": "SortOperator",
            "description": {
              "sortList": {
                "abs(age)": {
                  "sortOrder": "ASC",
                  "nullOrder": "NULL_FIRST"
                }
              }
            },
            "children": [
              {
                "name": "ElasticsearchIndexScan",
                "description": {
                  "request": "ElasticsearchQueryRequest(indexName=accounts, sourceBuilder={\"from\":0,\"size\":200,\"timeout\":\"1m\"}, searchDone=false)"
                },
                "children": []
              }
            ]
          }
        ]
      }
    }

Aggregation Merge Into Elasticsearch Aggregation
------------------------------------------------

The Aggregation operator will merge into Elasticsearch Aggregation::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_sql/_explain \
    ... -d '{"query" : "SELECT gender, avg(age) FROM accounts GROUP BY gender"}'
    {
      "root": {
        "name": "ProjectOperator",
        "description": {
          "fields": "[gender, avg(age)]"
        },
        "children": [
          {
            "name": "ElasticsearchIndexScan",
            "description": {
              "request": "ElasticsearchQueryRequest(indexName=accounts, sourceBuilder={\"from\":0,\"size\":0,\"timeout\":\"1m\",\"aggregations\":{\"composite_buckets\":{\"composite\":{\"size\":1000,\"sources\":[{\"gender\":{\"terms\":{\"field\":\"gender.keyword\",\"missing_bucket\":true,\"order\":\"asc\"}}}]},\"aggregations\":{\"avg(age)\":{\"avg\":{\"field\":\"age\"}}}}}}, searchDone=false)"
            },
            "children": []
          }
        ]
      }
    }

Sort Merge Into Elasticsearch Aggregation
-----------------------------------------

The Sort operator will merge into Elasticsearch Aggregation.::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_sql/_explain \
    ... -d '{"query" : "SELECT gender, avg(age) FROM accounts GROUP BY gender ORDER BY gender DESC NULLS LAST"}'
    {
      "root": {
        "name": "ProjectOperator",
        "description": {
          "fields": "[gender, avg(age)]"
        },
        "children": [
          {
            "name": "ElasticsearchIndexScan",
            "description": {
              "request": "ElasticsearchQueryRequest(indexName=accounts, sourceBuilder={\"from\":0,\"size\":0,\"timeout\":\"1m\",\"aggregations\":{\"composite_buckets\":{\"composite\":{\"size\":1000,\"sources\":[{\"gender\":{\"terms\":{\"field\":\"gender.keyword\",\"missing_bucket\":true,\"order\":\"desc\"}}}]},\"aggregations\":{\"avg(age)\":{\"avg\":{\"field\":\"age\"}}}}}}, searchDone=false)"
            },
            "children": []
          }
        ]
      }
    }

Because the Elasticsearch Composite Aggregation order doesn't support separate NULL_FIRST/NULL_LAST option. only the default sort option (ASC NULL_FIRST/DESC NULL_LAST) will be supported for push down to Elasticsearch Aggregation, otherwise it will fall back to the default memory based operator::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_sql/_explain \
    ... -d '{"query" : "SELECT gender, avg(age) FROM accounts GROUP BY gender ORDER BY gender ASC NULLS LAST"}'
    {
      "root": {
        "name": "ProjectOperator",
        "description": {
          "fields": "[gender, avg(age)]"
        },
        "children": [
          {
            "name": "SortOperator",
            "description": {
              "sortList": {
                "gender": {
                  "sortOrder": "ASC",
                  "nullOrder": "NULL_LAST"
                }
              }
            },
            "children": [
              {
                "name": "ElasticsearchIndexScan",
                "description": {
                  "request": "ElasticsearchQueryRequest(indexName=accounts, sourceBuilder={\"from\":0,\"size\":0,\"timeout\":\"1m\",\"aggregations\":{\"composite_buckets\":{\"composite\":{\"size\":1000,\"sources\":[{\"gender\":{\"terms\":{\"field\":\"gender.keyword\",\"missing_bucket\":true,\"order\":\"asc\"}}}]},\"aggregations\":{\"avg(age)\":{\"avg\":{\"field\":\"age\"}}}}}}, searchDone=false)"
                },
                "children": []
              }
            ]
          }
        ]
      }
    }

Because the Elasticsearch Composite Aggregation doesn't support order by metrics field, then if the sort list include fields which refer to metrics aggregation, then the sort operator can't be push down to Elasticsearch Aggregation::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_sql/_explain \
    ... -d '{"query" : "SELECT gender, avg(age) FROM accounts GROUP BY gender ORDER BY avg(age)"}'
    {
      "root": {
        "name": "ProjectOperator",
        "description": {
          "fields": "[gender, avg(age)]"
        },
        "children": [
          {
            "name": "SortOperator",
            "description": {
              "sortList": {
                "avg(age)": {
                  "sortOrder": "ASC",
                  "nullOrder": "NULL_FIRST"
                }
              }
            },
            "children": [
              {
                "name": "ElasticsearchIndexScan",
                "description": {
                  "request": "ElasticsearchQueryRequest(indexName=accounts, sourceBuilder={\"from\":0,\"size\":0,\"timeout\":\"1m\",\"aggregations\":{\"composite_buckets\":{\"composite\":{\"size\":1000,\"sources\":[{\"gender\":{\"terms\":{\"field\":\"gender.keyword\",\"missing_bucket\":true,\"order\":\"asc\"}}}]},\"aggregations\":{\"avg(age)\":{\"avg\":{\"field\":\"age\"}}}}}}, searchDone=false)"
                },
                "children": []
              }
            ]
          }
        ]
      }
    }

Limitations on Query Optimizations
==================================

Multi-fields in WHERE Conditions
--------------------------------

The filter expressions in ``WHERE`` clause may be pushed down to Elasticsearch DSL queries to avoid large amounts of data retrieved. In this case, for Elasticsearch multi-field (a text field with another keyword field inside), assumption is made that the keyword field name is always "keyword" which is true by default.

Multiple Window Functions
-------------------------

At the moment there is no optimization to merge similar sort operators to avoid unnecessary sort. In this case, only one sort operator associated with window function will be pushed down to Elasticsearch DSL queries. Others will sort the intermediate results in memory and return to its window operator in the upstream. This cost can be avoided by optimization aforementioned though in-memory sorting operation can still happen. Therefore a custom circuit breaker is in use to monitor sort operator and protect memory usage.

Sort Push Down
--------------
Without sort push down optimization, the sort operator will sort the result from child operator. By default, only 200 docs will extracted from the source index, `you can change this value by using size_limit setting <https://github.com/penghuo/sql/blob/sort-aggregation-push-down/docs/experiment/ppl/admin/settings.rst#opendistro-query-size-limit>`_.