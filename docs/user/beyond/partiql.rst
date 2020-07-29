
======================
PartiQL (JSON) Support
======================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2

Introduction
============

In Elasticsearch, there are two types of JSON field in Elasticsarch (called "properties"): ``object`` and ``nested``. An object field can have inner field(s) which could be a simple one or another object field recursively. A nested field is a special version of object type that allows inner field be queried independently.

To support queries for both types, we follow the query language syntax defined in PartiQL specification. PartiQL is a SQL-compatible query language that makes it easy and efficient to query semi-structured and nested data regardless of data format. For now our implementation is only partially compatible with PartiQL specification and more support will be provided in future.

Test Data
=========

Description
-----------

The test index ``people`` is to demonstrate our support for queries with deep nested object fields.
The test index ``employees_nested`` used by all examples in this document is very similar to the one used in official PartiQL documentation.

Example: People
---------------

There are three fields in test index ``people``: 1) deep nested object field ``city``; 2) object field of array value ``account``; 3) nested field ``projects``::

    {
      "mappings": {
        "properties": {
          "city": {
            "properties": {
              "name": {
                "type": "keyword"
              },
              "location": {
                "properties": {
                  "latitude": {
                    "type": "double"
                  }
                }
              }
            }
          },
          "account": {
            "properties": {
              "id": {
                "type": "keyword"
              }
            }
          },
          "projects": {
            "type": "nested",
            "properties": {
              "name": {
                "type": "keyword"
              }
            }
          }
        }
      }
    }

Example: Employees
------------------

Result set::

	{
	  "employees" : [
	    {
	      "id" : 3,
	      "name" : "Bob Smith",
	      "title" : null,
	      "projects" : [
	        {
	          "name" : "AWS Redshift Spectrum querying",
	          "started_year" : 1990
	        },
	        {
	          "name" : "AWS Redshift security",
	          "started_year" : 1999
	        },
	        {
	          "name" : "AWS Aurora security",
	          "started_year" : 2015
	        }
	      ]
	    },
	    {
	      "id" : 4,
	      "name" : "Susan Smith",
	      "title" : "Dev Mgr",
	      "projects" : [ ]
	    },
	    {
	      "id" : 6,
	      "name" : "Jane Smith",
	      "title" : "Software Eng 2",
	      "projects" : [
	        {
	          "name" : "AWS Redshift security",
	          "started_year" : 1998
	        },
	        {
	          "name" : "AWS Hello security",
	          "started_year" : 2015,
	          "address" : [
	            {
	              "city" : "Dallas",
	              "state" : "TX"
	            }
	          ]
	        }
	      ]
	    }
	  ]
	}

Querying Nested Tuple Values
============================

Description
-----------

Before looking into how nested object field (tuple values) be queried, we need to figure out how many cases are there and how it being handled by our SQL implementation. Therefore, first of all, let's examine different cases by the query support matrix as follows. This matrix summerizes what has been supported so far for queries with the object and nested fields involved. Note that another complexity is that any field in Elasticsearch, regular or property, can have contain more than one values in a single document. This makes object field not always a tuple value which needs to be handled separately.

+-------------------------+---------------+-----------------------+---------------------------------------------+-------------------------+
|     Level/Field Type    | Object Fields | Object Fields (array) |                Nested Fields                |         Comment         |
+=========================+===============+=======================+=============================================+=========================+
| Selecting top level     | Yes           | Yes                   | Yes                                         | The original JSON of    |
|                         |               |                       |                                             | field value is returned |
|                         |               |                       |                                             | which is either a JSON  |
|                         |               |                       |                                             | object or JSON array.   |
+-------------------------+---------------+-----------------------+---------------------------------------------+-------------------------+
| Selecting second level  | Yes           | No                    | Yes                                         |                         |
|                         |               | (null returned)       | (or null returned if not in PartiQL syntax) |                         |
+-------------------------+---------------+-----------------------+---------------------------------------------+ PartiQL specification   |
| Selecting deeper levels | Yes           | No                    | No                                          | is followed             |
|                         |               | (null returned)       | (exception may                              |                         |
|                         |               |                       | be thrown)                                  |                         |
+-------------------------+---------------+-----------------------+---------------------------------------------+-------------------------+

Example 1: Selecting Top Level
------------------------------

Selecting top level for object fields, object fields of array value and nested fields returns original JSON object or array of the field. For example, object field ``city`` is a JSON object, object field (of array value) ``accounts`` and nested field ``projects`` are JSON arrays::

    od> SELECT city, accounts, projects FROM people;
    fetched rows / total rows = 1/1
    +-----------------------------------------------------+-----------------------+----------------------------------------------------------------------------------------------------------------+
    | city                                                | accounts              | projects                                                                                                       |
    |-----------------------------------------------------+-----------------------+----------------------------------------------------------------------------------------------------------------|
    | {'name': 'Seattle', 'location': {'latitude': 10.5}} | [{'id': 1},{'id': 2}] | [{'name': 'AWS Redshift Spectrum querying'},{'name': 'AWS Redshift security'},{'name': 'AWS Aurora security'}] |
    +-----------------------------------------------------+-----------------------+----------------------------------------------------------------------------------------------------------------+

Example 2: Selecting Deeper Levels
----------------------------------

Selecting at deeper levels for object fields of regular value returns inner field value. For example, ``city.location`` is an inner object field and ``city.location.altitude`` is a regular double field::

    od> SELECT city.location, city.location.latitude FROM people;
    fetched rows / total rows = 1/1
    +--------------------+--------------------------+
    | city.location      | city.location.latitude   |
    |--------------------+--------------------------|
    | {'latitude': 10.5} | 10.5                     |
    +--------------------+--------------------------+

Example 3: Selecting Field of Array Value
-----------------------------------------

Select deeper level for object fields of array value which returns ``NULL``. For example, because inner field ``accounts.id`` has three values instead of a tuple in this document, null is returned. Similarly, selecting inner field ``projects.name`` directly in nested field returns null::

    od> SELECT accounts.id, projects.name FROM people;
    fetched rows / total rows = 1/1
    +---------------+-----------------+
    | accounts.id   | projects.name   |
    |---------------+-----------------|
    | null          | null            |
    +---------------+-----------------+

For selecting second level for nested fields, please read on and find more details in the following sections.

Querying Nested Collection
==========================

Description
-----------

In SQL-92, a database table can only have tuples that consists of scalar values. PartiQL extends SQL-92 to allow you query and unnest nested collection conveniently. In Elasticsearch world, this is very useful for index with object or nested field.

Example 1: Unnesting a Nested Collection
----------------------------------------

In the following example, it finds nested document (project) with field value (name) that satisfies the predicate (contains 'security'). Note that because each parent document can have more than one nested documents, the matched nested document is flattened. In other word, the final result is the Cartesian Product between parent and nested documents.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT e.name AS employeeName,
		       p.name AS projectName
		FROM employees_nested AS e,
		     e.projects AS p
		WHERE p.name LIKE '%security%'
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


Example 2: Unnesting in Existential Subquery
--------------------------------------------

Alternatively, a nested collection can be unnested in subquery to check if it satisfies a condition.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT e.name AS employeeName
		FROM employees_nested AS e
		WHERE EXISTS (
		  SELECT *
		  FROM e.projects AS p
		  WHERE p.name LIKE '%security%'
		)
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
	                "nested" : {
	                  "query" : {
	                    "bool" : {
	                      "must" : [
	                        {
	                          "bool" : {
	                            "must" : [
	                              {
	                                "bool" : {
	                                  "must_not" : [
	                                    {
	                                      "bool" : {
	                                        "must_not" : [
	                                          {
	                                            "exists" : {
	                                              "field" : "projects",
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
	                              {
	                                "wildcard" : {
	                                  "projects.name" : {
	                                    "wildcard" : "*security*",
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
	                  "path" : "projects",
	                  "ignore_unmapped" : false,
	                  "score_mode" : "none",
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
	      "name"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+------------+
|employeeName|
+============+
|   Bob Smith|
+------------+
|  Jane Smith|
+------------+


