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

SQL plugin provides multiple protocols for different purposes.

Elasticsearch DSL
=================

Description
-----------

By default the plugin returns original response from Elasticsearch in JSON. Because this is the native response from Elasticsearch, extra efforts are needed to parse and interpret it. Meanwhile mutation like field alias will not be present in it.

Examples
--------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
	  "query": "SELECT firstname, lastname, age, city FROM accounts LIMIT 2"
	}'

Result set::

	{
	  "took" : 131,
	  "timed_out" : false,
	  "_shards" : {
	    "total" : 8,
	    "successful" : 8,
	    "skipped" : 0,
	    "failed" : 0
	  },
	  "hits" : {
	    "total" : {
	      "value" : 4,
	      "relation" : "eq"
	    },
	    "max_score" : 1.0,
	    "hits" : [
	      {
	        "_index" : "accounts",
	        "_type" : "account",
	        "_id" : "1",
	        "_score" : 1.0,
	        "_source" : {
	          "firstname" : "Amber",
	          "city" : "Brogan",
	          "age" : 32,
	          "lastname" : "Duke"
	        }
	      },
	      {
	        "_index" : "accounts",
	        "_type" : "account",
	        "_id" : "6",
	        "_score" : 1.0,
	        "_source" : {
	          "firstname" : "Hattie",
	          "city" : "Dante",
	          "age" : 36,
	          "lastname" : "Bond"
	        }
	      }
	    ]
	  }
	}

JDBC Format
===========

Description
-----------

JDBC format is provided for JDBC driver and client side that needs both schema and result set well formatted.

Examples
--------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=jdbc -d '{
	  "query": "SELECT firstname, lastname, age, city FROM accounts LIMIT 2"
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
	      "Amber",
	      "Duke",
	      32,
	      "Brogan"
	    ],
	    [
	      "Hattie",
	      "Bond",
	      36,
	      "Dante"
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

Examples
--------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=csv -d '{
	  "query": "SELECT firstname, lastname, age, city FROM accounts"
	}'

Result set::

	firstname,lastname,age,city
	Amber,Duke,32,Brogan
	Hattie,Bond,36,Dante
	Dale,Adams,33,Orick
	Nanette,Bates,28,Nogal
	

Raw Format
==========

Description
-----------

Additionally you can also use RAW format to pipe the result with other command line tool for post processing.

Examples
--------

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=raw -d '{
	  "query": "SELECT firstname, lastname, age, city FROM accounts"
	}'

Result set::

	Amber|Duke|32|Brogan
	Hattie|Bond|36|Dante
	Dale|Adams|33|Orick
	Nanette|Bates|28|Nogal
	

