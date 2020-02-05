.. highlight:: json

===========
Basic Query
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

SELECT
======

Description
-----------

``SELECT`` clause specifies which fields in Elasticsearch index should be retrieved.

Example 1: Selecting All Fields
-------------------------------

You can use ``*`` to fetch all fields in the index which is very convenient when you just want to have a quick look at your data.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT * FROM accounts"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 200
	}

Result set:

+--------------+---------+------+------+-------+--------+-----+------------------------+--------------------+--------+---+
|account_number|firstname|gender|  city|balance|employer|state|                   email|             address|lastname|age|
+==============+=========+======+======+=======+========+=====+========================+====================+========+===+
|            13|  Nanette|     F| Nogal|  32838| Quility|   VA|nanettebates@quility.com|  789 Madison Street|   Bates| 28|
+--------------+---------+------+------+-------+--------+-----+------------------------+--------------------+--------+---+
|             1|    Amber|     M|Brogan|  39225|  Pyrami|   IL|    amberduke@pyrami.com|     880 Holmes Lane|    Duke| 32|
+--------------+---------+------+------+-------+--------+-----+------------------------+--------------------+--------+---+
|             6|   Hattie|     M| Dante|   5686|  Netagy|   TN|   hattiebond@netagy.com|  671 Bristol Street|    Bond| 36|
+--------------+---------+------+------+-------+--------+-----+------------------------+--------------------+--------+---+
|            18|     Dale|     M| Orick|   4180|    null|   MD|     daleadams@boink.com|467 Hutchinson Court|   Adams| 33|
+--------------+---------+------+------+-------+--------+-----+------------------------+--------------------+--------+---+


Example 2: Selecting Specific Field(s)
--------------------------------------

More often you would give specific field name(s) in ``SELECT`` clause to avoid large and unnecessary data retrieved.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT firstname, lastname FROM accounts"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 200,
	  "_source" : {
	    "includes" : [
	      "firstname",
	      "lastname"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+---------+--------+
|firstname|lastname|
+=========+========+
|  Nanette|   Bates|
+---------+--------+
|    Amber|    Duke|
+---------+--------+
|   Hattie|    Bond|
+---------+--------+
|     Dale|   Adams|
+---------+--------+


Example 3: Selecting Distinct Field(s)
--------------------------------------

``DISTINCT`` is useful when you want to de-duplicate and get unique field value. You can also provide one or more field names.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT DISTINCT age FROM accounts"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 0,
	  "_source" : {
	    "includes" : [
	      "age"
	    ],
	    "excludes" : [ ]
	  },
	  "stored_fields" : "age",
	  "aggregations" : {
	    "age" : {
	      "terms" : {
	        "field" : "age",
	        "size" : 200,
	        "min_doc_count" : 1,
	        "shard_min_doc_count" : 0,
	        "show_term_doc_count_error" : false,
	        "order" : [
	          {
	            "_count" : "desc"
	          },
	          {
	            "_key" : "asc"
	          }
	        ]
	      }
	    }
	  }
	}

Result set:

+---+
|age|
+===+
| 28|
+---+
| 32|
+---+
| 33|
+---+
| 36|
+---+


FROM
====

Description
-----------

``FROM`` clause specifies Elasticsearch index where the data should be retrieved from. You've seen how to specify a single index in FROM clause in last section. Here we list more use cases additionally.
 Subquery in ``FROM`` clause is also supported. Please check out our documentation for more details.

Example 1: Selecting From Multiple Indices by Index Pattern
-----------------------------------------------------------

Alternatively you can query from multiple indices of similar names by index pattern. This is very convenient for indices created by Logstash index template with date as suffix.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM account*"
	}

Example 2: [Deprecating] Selecting From Specific Index Type
-----------------------------------------------------------

You can also specify type name explicitly though this has been deprecated in later Elasticsearch version.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM accounts/account"
	}

WHERE
=====

Description
-----------

`WHERE` clause specifies only Elasticsearch documents that meet the criteria should be affected. It consists of predicates that uses ``=``, ``<>``, ``>``, ``>=``, ``<``, ``<=``, ``IN``, ``BETWEEN``, ``LIKE``, ``IS NULL`` or ``IS NOT NULL``. These predicates can be combined by logical operator ``NOT``, ``AND`` or ``OR``.
 For ``LIKE`` and other full text search topics, please refer to Full Text Search documentation.
 Besides SQL query, WHERE clause can also be used in SQL statement such as ``DELETE``. Please refer to Data Manipulation Language documentation for details.

Example 1: Comparison Operators
-------------------------------

Basic comparison operators, such as ``=``, ``<>``, ``>``, ``>=``, ``<``, ``<=``, can work for number, string or date. ``IN`` and ``BETWEEN`` is convenient for comparison with multiple values or a range.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM accounts WHERE account_number = 1"
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
	                "term" : {
	                  "account_number" : {
	                    "value" : 1,
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
	      "account_number"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+--------------+
|account_number|
+==============+
|             1|
+--------------+


Example 2: Missing Fields
-------------------------

As NoSQL database, Elasticsearch allows for flexible schema that documents in an index may have different fields. In this case, you can use ``IS NULL`` or ``IS NOT NULL`` to retrieve missing fields or existing fields only.
 Note that for now we don't differentiate missing field and field set to ``NULL`` explicitly.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number, employer FROM accounts WHERE employer IS NULL"
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
	                "bool" : {
	                  "must_not" : [
	                    {
	                      "exists" : {
	                        "field" : "employer.keyword",
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
	        }
	      ],
	      "adjust_pure_negative" : true,
	      "boost" : 1.0
	    }
	  },
	  "_source" : {
	    "includes" : [
	      "account_number",
	      "employer"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+--------------+--------+
|account_number|employer|
+==============+========+
|            18|    null|
+--------------+--------+


Alias
=====

Description
-----------

Alias makes your query more readable by aliasing your index or field to clearer or shorter name.

Example
-------

Here is an example of how to use table alias as well as field alias.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT acc.account_number AS num FROM accounts acc WHERE acc.age > 30"
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
	        }
	      ],
	      "adjust_pure_negative" : true,
	      "boost" : 1.0
	    }
	  },
	  "_source" : {
	    "includes" : [
	      "account_number"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+--------------+
|account_number|
+==============+
|             1|
+--------------+
|             6|
+--------------+
|            18|
+--------------+


GROUP BY
========

Description
-----------

Limitation because ES ... NULL (missing value) won't be taken into account in aggregation.

Example 1: Grouping By Fields
-----------------------------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT age FROM accounts GROUP BY age"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 0,
	  "_source" : {
	    "includes" : [
	      "age"
	    ],
	    "excludes" : [ ]
	  },
	  "stored_fields" : "age",
	  "aggregations" : {
	    "age" : {
	      "terms" : {
	        "field" : "age",
	        "size" : 200,
	        "min_doc_count" : 1,
	        "shard_min_doc_count" : 0,
	        "show_term_doc_count_error" : false,
	        "order" : [
	          {
	            "_count" : "desc"
	          },
	          {
	            "_key" : "asc"
	          }
	        ]
	      }
	    }
	  }
	}

Result set:

+---+
|age|
+===+
| 28|
+---+
| 32|
+---+
| 33|
+---+
| 36|
+---+


Example 2: Grouping By Alias
----------------------------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT age AS a FROM accounts GROUP BY a"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 0,
	  "_source" : {
	    "includes" : [
	      "age"
	    ],
	    "excludes" : [ ]
	  },
	  "stored_fields" : "age",
	  "aggregations" : {
	    "a" : {
	      "terms" : {
	        "field" : "age",
	        "size" : 200,
	        "min_doc_count" : 1,
	        "shard_min_doc_count" : 0,
	        "show_term_doc_count_error" : false,
	        "order" : [
	          {
	            "_count" : "desc"
	          },
	          {
	            "_key" : "asc"
	          }
	        ]
	      }
	    }
	  }
	}

Result set:

+--+
| a|
+==+
|28|
+--+
|32|
+--+
|33|
+--+
|36|
+--+


Example 3: Grouping By Field Ordinal in Select
----------------------------------------------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT age FROM accounts GROUP BY 1"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 0,
	  "_source" : {
	    "includes" : [
	      "age"
	    ],
	    "excludes" : [ ]
	  },
	  "stored_fields" : "age",
	  "aggregations" : {
	    "age" : {
	      "terms" : {
	        "field" : "age",
	        "size" : 200,
	        "min_doc_count" : 1,
	        "shard_min_doc_count" : 0,
	        "show_term_doc_count_error" : false,
	        "order" : [
	          {
	            "_count" : "desc"
	          },
	          {
	            "_key" : "asc"
	          }
	        ]
	      }
	    }
	  }
	}

Result set:

+---+
|age|
+===+
| 28|
+---+
| 32|
+---+
| 33|
+---+
| 36|
+---+


Example 4: Grouping By Scalar Function
--------------------------------------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT age AS a FROM accounts GROUP BY 1"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 0,
	  "_source" : {
	    "includes" : [
	      "age"
	    ],
	    "excludes" : [ ]
	  },
	  "stored_fields" : "age",
	  "aggregations" : {
	    "a" : {
	      "terms" : {
	        "field" : "age",
	        "size" : 200,
	        "min_doc_count" : 1,
	        "shard_min_doc_count" : 0,
	        "show_term_doc_count_error" : false,
	        "order" : [
	          {
	            "_count" : "desc"
	          },
	          {
	            "_key" : "asc"
	          }
	        ]
	      }
	    }
	  }
	}

Result set:

+--+
| a|
+==+
|28|
+--+
|32|
+--+
|33|
+--+
|36|
+--+


HAVING
======

Example
-------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT age FROM accounts GROUP BY age"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 0,
	  "_source" : {
	    "includes" : [
	      "age"
	    ],
	    "excludes" : [ ]
	  },
	  "stored_fields" : "age",
	  "aggregations" : {
	    "age" : {
	      "terms" : {
	        "field" : "age",
	        "size" : 200,
	        "min_doc_count" : 1,
	        "shard_min_doc_count" : 0,
	        "show_term_doc_count_error" : false,
	        "order" : [
	          {
	            "_count" : "desc"
	          },
	          {
	            "_key" : "asc"
	          }
	        ]
	      }
	    }
	  }
	}

Result set:

+---+
|age|
+===+
| 28|
+---+
| 32|
+---+
| 33|
+---+
| 36|
+---+


ORDER BY
========

Description
-----------

``ORDER BY`` clause specifies which fields used to sort the result and in which direction.

Example 1
---------

Besides regular field names, ordinal, alias or scalar function can also be used similarly as in ``GROUP BY``. ``ASC`` (by default) or ``DESC`` can be appended to indicate sorting in ascending or descending order.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM accounts ORDER BY account_number DESC"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 200,
	  "_source" : {
	    "includes" : [
	      "account_number"
	    ],
	    "excludes" : [ ]
	  },
	  "sort" : [
	    {
	      "account_number" : {
	        "order" : "desc"
	      }
	    }
	  ]
	}

Result set:

+--------------+
|account_number|
+==============+
|            18|
+--------------+
|            13|
+--------------+
|             6|
+--------------+
|             1|
+--------------+


Example 2
---------

Additionally you can specify if documents with missing field be put first or last. The default behavior of Elasticsearch is to return nulls or missing last. You can make them present before non-nulls by using ``IS NOT NULL``.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT employer FROM accounts ORDER BY employer IS NOT NULL"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 200,
	  "_source" : {
	    "includes" : [
	      "employer"
	    ],
	    "excludes" : [ ]
	  },
	  "sort" : [
	    {
	      "employer.keyword" : {
	        "order" : "asc",
	        "missing" : "_first"
	      }
	    }
	  ]
	}

Result set:

+--------+
|employer|
+========+
|    null|
+--------+
|  Netagy|
+--------+
|  Pyrami|
+--------+
| Quility|
+--------+


LIMIT
=====

Description
-----------

Mostly specifying maximum number of documents returned is necessary to prevent fetching large amount of data into memory. `LIMIT` clause is helpful in this case.

Example 1: Limiting Result Size
-------------------------------

Given a positive number, ``LIMIT`` uses it as page size to fetch result of that size at most.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM accounts ORDER BY account_number LIMIT 1"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 1,
	  "_source" : {
	    "includes" : [
	      "account_number"
	    ],
	    "excludes" : [ ]
	  },
	  "sort" : [
	    {
	      "account_number" : {
	        "order" : "asc"
	      }
	    }
	  ]
	}

Result set:

+--------------+
|account_number|
+==============+
|             1|
+--------------+


Example 2: Fetching at Offset
-----------------------------

Offset position can be given as first argument to indicate where to start fetching. This can be used as simple pagination solution though it's inefficient on large index. Generally ``ORDER BY`` is required in this case to ensure the same order between pages.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM accounts ORDER BY account_number LIMIT 1, 1"
	}

Explain::

	{
	  "from" : 1,
	  "size" : 1,
	  "_source" : {
	    "includes" : [
	      "account_number"
	    ],
	    "excludes" : [ ]
	  },
	  "sort" : [
	    {
	      "account_number" : {
	        "order" : "asc"
	      }
	    }
	  ]
	}

Result set:

+--------------+
|account_number|
+==============+
|             6|
+--------------+


