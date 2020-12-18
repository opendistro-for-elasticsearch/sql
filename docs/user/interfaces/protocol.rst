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

For the protocol, SQL plugin provides multiple response formats for different purposes while the request format is same for all. Among them JDBC format is widely used because it provides schema information and more functionality such as pagination. Besides JDBC driver, various clients can benefit from the detailed and well formatted response.


Request Format
==============

Description
-----------

The body of HTTP POST request can take a few more other fields with SQL query.

Example 1
---------

Use `filter` to add more conditions to Elasticsearch DSL directly.

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
	  "query" : "SELECT firstname, lastname, balance FROM accounts",
	  "filter" : {
	    "range" : {
	      "balance" : {
	        "lt" : 10000
	      }
	    }
	  }
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
	            "filter" : [
	              {
	                "range" : {
	                  "balance" : {
	                    "from" : null,
	                    "to" : 10000,
	                    "include_lower" : true,
	                    "include_upper" : false,
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
	      "lastname",
	      "balance"
	    ],
	    "excludes" : [ ]
	  }
	}

Example 2
---------

Use `parameters` for actual parameter value in prepared SQL query.

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
	  "query" : "SELECT * FROM accounts WHERE age = ?",
	  "parameters" : [
	    {
	      "type" : "integer",
	      "value" : 30
	    }
	  ]
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
	                "term" : {
	                  "age" : {
	                    "value" : 30,
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
	  }
	}

JDBC Format
===========

Description
-----------

By default the plugin return JDBC format. JDBC format is provided for JDBC driver and client side that needs both schema and result set well formatted.

Example 1
---------

Here is an example for normal response. The `schema` includes field name and its type and `datarows` includes the result set.

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
	  "query" : "SELECT firstname, lastname, age FROM accounts ORDER BY age LIMIT 2"
	}'

Result set::

	{
	  "schema" : [
	    {
	      "name" : "firstname",
	      "type" : "text"
	    },
	    {
	      "name" : "lastname",
	      "type" : "text"
	    },
	    {
	      "name" : "age",
	      "type" : "long"
	    }
	  ],
	  "total" : 4,
	  "datarows" : [
	    [
	      "Nanette",
	      "Bates",
	      28
	    ],
	    [
	      "Amber",
	      "Duke",
	      32
	    ]
	  ],
	  "size" : 2,
	  "status" : 200
	}

Example 2
---------

If any error occurred, error message and the cause will be returned instead.

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=jdbc -d '{
	  "query" : "SELECT unknown FROM accounts"
	}'

Result set::

	{
	  "error" : {
	    "reason" : "Invalid SQL query",
	    "details" : "Field [unknown] cannot be found or used here.",
	    "type" : "SemanticAnalysisException"
	  },
	  "status" : 400
	}

Elasticsearch DSL
=================

Description
-----------

The plugin returns original response from Elasticsearch in JSON. Because this is the native response from Elasticsearch, extra efforts are needed to parse and interpret it.

Example
-------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=json -d '{
	  "query" : "SELECT firstname, lastname, age FROM accounts ORDER BY age LIMIT 2"
	}'

Result set::

	{
	  "_shards" : {
	    "total" : 5,
	    "failed" : 0,
	    "successful" : 5,
	    "skipped" : 0
	  },
	  "hits" : {
	    "hits" : [
	      {
	        "_index" : "accounts",
	        "_type" : "_doc",
	        "_source" : {
	          "firstname" : "Nanette",
	          "age" : 28,
	          "lastname" : "Bates"
	        },
	        "_id" : "13",
	        "sort" : [
	          28
	        ],
	        "_score" : null
	      },
	      {
	        "_index" : "accounts",
	        "_type" : "_doc",
	        "_source" : {
	          "firstname" : "Amber",
	          "age" : 32,
	          "lastname" : "Duke"
	        },
	        "_id" : "1",
	        "sort" : [
	          32
	        ],
	        "_score" : null
	      }
	    ],
	    "total" : {
	      "value" : 4,
	      "relation" : "eq"
	    },
	    "max_score" : null
	  },
	  "took" : 100,
	  "timed_out" : false
	}

CSV Format
==========

Description
-----------

You can also use CSV format to download result set as CSV.

Example
-------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=csv -d '{
	  "query" : "SELECT firstname, lastname, age FROM accounts ORDER BY age"
	}'

Result set::

	firstname,lastname,age
	Nanette,Bates,28
	Amber,Duke,32
	Dale,Adams,33
	Hattie,Bond,36


The formatter sanitizes the csv result with the following rules:

1. If a header cell or data cell is starting with special character including '+', '-', '=' , '@', the sanitizer will insert a single-quote at the start of the cell.

2. If there exists one or more commas (','), the sanitizer will quote the cell with double quotes.

For example::

    >> curl -H 'Content-Type: application/json' -X PUT localhost:9200/userdata/_doc/1?refresh=true -d '{
      "+firstname": "-Hattie",
      "=lastname": "@Bond",
      "address": "671 Bristol Street, Dente, TN"
    }'
	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=csv -d '{
	  "query" : "SELECT firstname, lastname, address FROM userdata"
	}'

Result set::

    '+firstname,'=lastname,address
    'Hattie,'@Bond,"671 Bristol Street, Dente, TN"


If you prefer escaping the sanitization and keeping the original csv result, you can add a "sanitize" param and set it to false value to skip sanitizing. For example::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=csv&sanitize=false -d '{
	  "query" : "SELECT firstname, lastname, address FROM userdata"
	}'

Result set::

    +firstname,=lastname,address
    Hattie,@Bond,671 Bristol Street, Dente, TN
	

Raw Format
==========

Description
-----------

Additionally raw format can be used to pipe the result to other command line tool for post processing.

Example
-------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=raw -d '{
	  "query" : "SELECT firstname, lastname, age FROM accounts ORDER BY age"
	}'

Result set::

	Nanette|Bates|28
	Amber|Duke|32
	Dale|Adams|33
	Hattie|Bond|36
	

