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

To send query request to SQL plugin, you MUST use HTTP POST request. POST request doesn't have length limitation and allows for other parameters passed to plugin for other functionality such as prepared statement. And also the explain endpoint is used very often for query translation and troubleshooting.

POST
====

Description
-----------

You can also send HTTP POST request with your query in request body.

Example
-------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
	  "query" : "SELECT * FROM accounts"
	}'

Explain
=======

Description
-----------

To translate your query, send it to explain endpoint. The explain output is Elasticsearch domain specific language (DSL) in JSON format. You can just copy and paste it to your console to run it against Elasticsearch directly.

Example
-------

Explain query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql/_explain -d '{
	  "query" : "SELECT firstname, lastname FROM accounts WHERE age > 20"
	}'

Explain::

	{
	  "from" : 0,
	  "size" : 200,
	  "query" : {
	    "bool" : {
	      "filter" : [
	        {
	          "bool" : {
	            "must" : [
	              {
	                "range" : {
	                  "age" : {
	                    "from" : 20,
	                    "to" : null,
	                    "include_lower" : false,
	                    "include_upper" : true,
	                    "boost" : 1.0
	                  }
	                }
	              }
	            ],
	            "adjust_pure_negative" : true,
	            "boost" : 1.0
	          }
	        }
	      ],
	      "adjust_pure_negative" : true,
	      "boost" : 1.0
	    }
	  },
	  "_source" : {
	    "includes" : [
	      "firstname",
	      "lastname"
	    ],
	    "excludes" : [ ]
	  }
	}

Cursor
======

Description
-----------

To get paginated response for a query, user needs to provide `fetch_size` parameter as part of normal query. The value of `fetch_size` should be greater than `0`. In absence of `fetch_size`, default value of 1000 is used. A value of `0` will fallback to non-paginated response. This feature is only available over `jdbc` format for now.

Example
-------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
	  "fetch_size" : 5,
	  "query" : "SELECT firstname, lastname FROM accounts WHERE age > 20 ORDER BY state ASC"
	}'

Result set::

    {
      "schema": [
        {
          "name": "firstname",
          "type": "text"
        },
        {
          "name": "lastname",
          "type": "text"
        }
      ],
      "cursor": "d:eyJhIjp7fSwicyI6IkRYRjFaWEo1UVc1a1JtVjBZMmdCQUFBQUFBQUFBQU1XZWpkdFRFRkZUMlpTZEZkeFdsWnJkRlZoYnpaeVVRPT0iLCJjIjpbeyJuYW1lIjoiZmlyc3RuYW1lIiwidHlwZSI6InRleHQifSx7Im5hbWUiOiJsYXN0bmFtZSIsInR5cGUiOiJ0ZXh0In1dLCJmIjo1LCJpIjoiYWNjb3VudHMiLCJsIjo5NTF9",
      "total": 956,
      "datarows": [
        [
          "Cherry",
          "Carey"
        ],
        [
          "Lindsey",
          "Hawkins"
        ],
        [
          "Sargent",
          "Powers"
        ],
        [
          "Campos",
          "Olsen"
        ],
        [
          "Savannah",
          "Kirby"
        ]
      ],
      "size": 5,
      "status": 200
    }

