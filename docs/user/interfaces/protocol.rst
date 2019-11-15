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

For the protocol, SQL plugin provides multiple response formats for different purposes while the request format is same for all. Except JDBC format, all other responses are basically in their original format. Because of the requirements for schema information and functionality such as pagination, JDBC driver requires a different and well defined response format.


Request Format
==============

Description
-----------

Request body by HTTP POST accepts a few more fields than SQL query.

Example 1
---------

Use filter to work with Elasticsearch DSL directly. Note that the content is present in final Elasticsearch request DSL as it is.

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

Use parameters for placeholder in prepared SQL query to be replaced.

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

Elasticsearch DSL
=================

Description
-----------

By default the plugin returns original response from Elasticsearch in JSON. Because this is the native response from Elasticsearch, extra efforts are needed to parse and interpret it. Meanwhile mutation like field alias will not be present in it.

Example
-------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
	  "query" : "SELECT firstname, lastname, age, city FROM accounts ORDER BY age LIMIT 2"
	}'

Result set::

	{
	  "took" : 310,
	  "timed_out" : false,
	  "_shards" : {
	    "total" : 4,
	    "successful" : 4,
	    "skipped" : 0,
	    "failed" : 0
	  },
	  "hits" : {
	    "total" : {
	      "value" : 4,
	      "relation" : "eq"
	    },
	    "max_score" : null,
	    "hits" : [
	      {
	        "_index" : "accounts",
	        "_type" : "account",
	        "_id" : "13",
	        "_score" : null,
	        "_source" : {
	          "firstname" : "Nanette",
	          "city" : "Nogal",
	          "age" : 28,
	          "lastname" : "Bates"
	        },
	        "sort" : [
	          28
	        ]
	      },
	      {
	        "_index" : "accounts",
	        "_type" : "account",
	        "_id" : "1",
	        "_score" : null,
	        "_source" : {
	          "firstname" : "Amber",
	          "city" : "Brogan",
	          "age" : 32,
	          "lastname" : "Duke"
	        },
	        "sort" : [
	          32
	        ]
	      }
	    ]
	  }
	}

JDBC Format
===========

Description
-----------

JDBC format is provided for JDBC driver and client side that needs both schema and result set well formatted.

Example
-------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=jdbc -d '{
	  "query" : "SELECT firstname, lastname, age, city FROM accounts ORDER BY age LIMIT 2"
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
	    },
	    {
	      "name" : "city",
	      "type" : "text"
	    }
	  ],
	  "total" : 4,
	  "datarows" : [
	    [
	      "Nanette",
	      "Bates",
	      28,
	      "Nogal"
	    ],
	    [
	      "Amber",
	      "Duke",
	      32,
	      "Brogan"
	    ]
	  ],
	  "size" : 2,
	  "status" : 200
	}

CSV Format
==========

Description
-----------

You can also use CSV format to download result set in csv format.

Example
-------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=csv -d '{
	  "query" : "SELECT firstname, lastname, age, city FROM accounts ORDER BY age"
	}'

Result set::

	firstname,lastname,age,city
	Nanette,Bates,28,Nogal
	Amber,Duke,32,Brogan
	Dale,Adams,33,Orick
	Hattie,Bond,36,Dante
	

Raw Format
==========

Description
-----------

Additionally you can also use RAW format to pipe the result with other command line tool for post processing.

Example
-------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=raw -d '{
	  "query" : "SELECT firstname, lastname, age, city FROM accounts ORDER BY age"
	}'

Result set::

	Nanette|Bates|28|Nogal
	Amber|Duke|32|Brogan
	Dale|Adams|33|Orick
	Hattie|Bond|36|Dante
	

