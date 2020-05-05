
================
DELETE Statement
================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


DELETE
======

Description
-----------

``DELETE`` statement deletes documents that satisfy the predicates in ``WHERE`` clause. Note that all documents are deleted in the case of ``WHERE`` clause absent.

Syntax
------

Rule ``singleDeleteStatement``:

.. image:: /docs/user/img/rdd/singleDeleteStatement.png

Example
-------

The ``datarows`` field in this case shows rows impacted, in other words how many documents were just deleted.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		DELETE FROM accounts
		WHERE age > 30
		"""
	}

Explain::

	{
	  "size" : 1000,
	  "query" : {
	    "bool" : {
	      "must" : [
	        {
	          "range" : {
	            "age" : {
	              "from" : 30,
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
	  },
	  "_source" : false
	}

Result set::

	{
	  "schema" : [
	    {
	      "name" : "deleted_rows",
	      "type" : "long"
	    }
	  ],
	  "total" : 1,
	  "datarows" : [
	    [
	      3
	    ]
	  ],
	  "size" : 1,
	  "status" : 200
	}

