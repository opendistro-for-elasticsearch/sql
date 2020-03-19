
=====================
PartiQL Compatibility
=====================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2

Introduction
============

PartiQL is a SQL-compatible query language that makes it easy and efficient to query semi-structured and nested data regardless of data format. For now our OpenDistro For Elasticsearch SQL plugin is partially compatible with PartiQL specification. More support and integration will be provided in future.
Test Data
=========

Description
-----------

The test index ``employees_nested`` used by all examples in this document is the same as the one used in official PartiQL documentation.

Example
-------

Result set:

+--------------+-----------+--+----------------------------------------------------------------------------------------------------------------------------------------------------------+----+
|         title|       name|id|                                                                                                                                                          |    |
+==============+===========+==+==========================================================================================================================================================+====+
|          null|  Bob Smith| 3|[{name=AWS Redshift Spectrum querying, started_year=1990}, {name=AWS Redshift security, started_year=1999}, {name=AWS Aurora security, started_year=2015}]|null|
+--------------+-----------+--+----------------------------------------------------------------------------------------------------------------------------------------------------------+----+
|       Dev Mgr|Susan Smith| 4|                                                                                                                                                        []|null|
+--------------+-----------+--+----------------------------------------------------------------------------------------------------------------------------------------------------------+----+
|Software Eng 2| Jane Smith| 6|                        [{name=AWS Redshift security, started_year=1998}, {address=[{city=Dallas, state=TX}], name=AWS Hello security, started_year=2015}]|null|
+--------------+-----------+--+----------------------------------------------------------------------------------------------------------------------------------------------------------+----+


Querying Nested Collection
==========================

Description
-----------

In SQL-92, a database table can only have tuples that consists of scalar values. PartiQL extends SQL-92 to allow you query and unnest nested collection conveniently. In Elasticsearch world, this is very useful for index with object or nested field.

Example: Unnesting a Nested Collection
--------------------------------------

In the following example, it finds nested document (project) with field value (name) that satisfies the predicate (contains 'security'). Note that because each parent document can have more than one nested documents, the matched nested document is flattened. In other word, the final result is the Cartesian Product between parent and nested documents.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT e.name AS employeeName,        p.name AS projectName FROM employees_nested AS e,      e.projects AS p WHERE p.name LIKE '%security%'"
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
	                "nested" : {
	                  "query" : {
	                    "wildcard" : {
	                      "projects.name" : {
	                        "wildcard" : "*security*",
	                        "boost" : 1.0
	                      }
	                    }
	                  },
	                  "path" : "projects",
	                  "ignore_unmapped" : false,
	                  "score_mode" : "none",
	                  "boost" : 1.0,
	                  "inner_hits" : {
	                    "ignore_unmapped" : false,
	                    "from" : 0,
	                    "size" : 3,
	                    "version" : false,
	                    "seq_no_primary_term" : false,
	                    "explain" : false,
	                    "track_scores" : false,
	                    "_source" : {
	                      "includes" : [
	                        "projects.name"
	                      ],
	                      "excludes" : [ ]
	                    }
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
	      "name"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+------------+---------------------+
|employeeName|          projectName|
+============+=====================+
|   Bob Smith|  AWS Aurora security|
+------------+---------------------+
|   Bob Smith|AWS Redshift security|
+------------+---------------------+
|  Jane Smith|   AWS Hello security|
+------------+---------------------+
|  Jane Smith|AWS Redshift security|
+------------+---------------------+


