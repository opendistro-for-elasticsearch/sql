
================
Full-text Search
================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2

Introduction
============

Full-text search is for searching a single stored document which is distinguished from regular search based on original texts in database. It tries to match search criteria by examining all of the words in each document. In Elasticsearch, full-text queries provided enables you to search text fields analyzed during indexing.

Match Query
===========

Description
-----------

Match query is the standard query for full-text search in Elasticsearch. Both ``MATCHQUERY`` and ``MATCH_QUERY`` are functions for performing match query.

Example 1
---------

Both functions can accept field name as first argument and text as second.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT account_number, address
		FROM accounts
		WHERE MATCH_QUERY(address, 'Holmes')
		"""
	}

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
	                "match" : {
	                  "address" : {
	                    "query" : "Holmes",
	                    "operator" : "OR",
	                    "prefix_length" : 0,
	                    "max_expansions" : 50,
	                    "fuzzy_transpositions" : true,
	                    "lenient" : false,
	                    "zero_terms_query" : "NONE",
	                    "auto_generate_synonyms_phrase_query" : true,
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
	      "account_number",
	      "address"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+--------------+---------------+
|account_number|        address|
+==============+===============+
|             1|880 Holmes Lane|
+--------------+---------------+


Example 2
---------

Both functions can be used in the following manner.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT account_number, address
		FROM accounts
		WHERE address = MATCH_QUERY('Holmes')
		"""
	}

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
	                "match" : {
	                  "address" : {
	                    "query" : "Holmes",
	                    "operator" : "OR",
	                    "prefix_length" : 0,
	                    "max_expansions" : 50,
	                    "fuzzy_transpositions" : true,
	                    "lenient" : false,
	                    "zero_terms_query" : "NONE",
	                    "auto_generate_synonyms_phrase_query" : true,
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
	      "account_number",
	      "address"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+--------------+---------------+
|account_number|        address|
+==============+===============+
|             1|880 Holmes Lane|
+--------------+---------------+


Query String Query
==================

Description
-----------

Query string query parses and splits a query string provided based on Lucene query string syntax. The mini language supports logical connectives, wildcard, regex and proximity search. Please refer to official documentation for more details. Note that an error is thrown in the case of any invalid syntax in query string.

Example
-------

``QUERY`` function accepts query string and returns true or false respectively for document that matches the query string or not.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT account_number, address
		FROM accounts
		WHERE QUERY('address:Lane OR address:Street')
		"""
	}

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
	                "query_string" : {
	                  "query" : "address:Lane OR address:Street",
	                  "fields" : [ ],
	                  "type" : "best_fields",
	                  "default_operator" : "or",
	                  "max_determinized_states" : 10000,
	                  "enable_position_increments" : true,
	                  "fuzziness" : "AUTO",
	                  "fuzzy_prefix_length" : 0,
	                  "fuzzy_max_expansions" : 50,
	                  "phrase_slop" : 0,
	                  "escape" : false,
	                  "auto_generate_synonyms_phrase_query" : true,
	                  "fuzzy_transpositions" : true,
	                  "boost" : 1.0
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
	      "account_number",
	      "address"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+--------------+------------------+
|account_number|           address|
+==============+==================+
|             1|   880 Holmes Lane|
+--------------+------------------+
|             6|671 Bristol Street|
+--------------+------------------+
|            13|789 Madison Street|
+--------------+------------------+


Match Phrase Query
==================

Description
-----------

Match phrase query is similar to match query but it is used for matching exact phrases. ``MATCHPHRASE``, ``MATCH_PHRASE`` and ``MATCHPHRASEQUERY`` are provided for this purpose.

Example
-------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT account_number, address
		FROM accounts
		WHERE MATCH_PHRASE(address, '880 Holmes Lane')
		"""
	}

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
	                "match_phrase" : {
	                  "address" : {
	                    "query" : "880 Holmes Lane",
	                    "slop" : 0,
	                    "zero_terms_query" : "NONE",
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
	      "account_number",
	      "address"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+--------------+---------------+
|account_number|        address|
+==============+===============+
|             1|880 Holmes Lane|
+--------------+---------------+


