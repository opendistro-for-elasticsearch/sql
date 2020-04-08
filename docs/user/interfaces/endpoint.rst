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

To send query request to SQL plugin, you can either use a request parameter in HTTP GET or request body by HTTP POST request. POST request is recommended because it doesn't have length limitation and allows for other parameters passed to plugin for other functionality such as prepared statement. And also the explain endpoint is used very often for query translation and troubleshooting.

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

