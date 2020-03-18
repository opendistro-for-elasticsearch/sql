
=====================
PartiQL Compatibility
=====================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2

Introduction
============

PartiQL is a SQL-compatible query language that makes it easy and efficient to query semi-structured and nested data regardless of data format. Our OpenDistro For Elasticsearch SQL plugin is partially compatible with PartiQL specification and more support will be provided in future.
Unnesting a Nested Collection
=============================

Description
-----------

...

Example
-------

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


