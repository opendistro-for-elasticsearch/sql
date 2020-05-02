
===========
Basic Query
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

``SELECT`` statement in SQL is the most common query that retrieves data from Elasticsearch index. In this doc, only simple ``SELECT`` statement with single index and query involved is covered. A ``SELECT`` statement includes ``SELECT``, ``FROM``, ``WHERE``, ``GROUP BY``, ``HAVING``, ``ORDER BY`` and ``LIMIT`` clause. Among these clauses, ``SELECT`` and ``FROM`` are the foundation to specify which fields to be fetched and which index they should be fetched from. All others are optional and used according to your needs. Please read on for their description, syntax and use cases in details.

Syntax
------

The syntax of ``SELECT`` statement is as follows::

  SELECT [DISTINCT] (* | expression) [[AS] alias] [, ...]
  FROM index_name
  [WHERE predicates]
  [GROUP BY expression [, ...]
   [HAVING predicates]]
  [ORDER BY expression [IS [NOT] NULL] [ASC | DESC] [, ...]]
  [LIMIT [offset, ] size]

Fundamentals
------------

Apart from predefined keyword of SQL language, the most basic element is literal and identifier. Literal is numeric, string, date or boolean constant. Identifier represents Elasticsearch index or field name. With arithmetic operators and SQL functions applied, the basic literals and identifiers can be built into complex expression.

Rule ``expressionAtom``:

.. image:: /docs/user/img/rdd/expressionAtom.png

The expression in turn can be combined into predicate with logical operator. Typically, predicate is used in ``WHERE`` and ``HAVING`` clause to filter out data by conditions specified.

Rule ``expression``:

.. image:: /docs/user/img/rdd/expression.png

Rule ``predicate``:

.. image:: /docs/user/img/rdd/predicate.png

Execution Order
---------------

The actual order of execution is very different from its appearance::

  FROM index
   WHERE predicates
    GROUP BY expressions
     HAVING predicates
      SELECT expressions
       ORDER BY expressions
        LIMIT size

SELECT
======

Description
-----------

``SELECT`` clause specifies which fields in Elasticsearch index should be retrieved.

Syntax
------

Rule ``selectElements``:

.. image:: /docs/user/img/rdd/selectElements.png

Rule ``selectElement``:

.. image:: /docs/user/img/rdd/selectElement.png

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
|             1|    Amber|     M|Brogan|  39225|  Pyrami|   IL|    amberduke@pyrami.com|     880 Holmes Lane|    Duke| 32|
+--------------+---------+------+------+-------+--------+-----+------------------------+--------------------+--------+---+
|             6|   Hattie|     M| Dante|   5686|  Netagy|   TN|   hattiebond@netagy.com|  671 Bristol Street|    Bond| 36|
+--------------+---------+------+------+-------+--------+-----+------------------------+--------------------+--------+---+
|            13|  Nanette|     F| Nogal|  32838| Quility|   VA|nanettebates@quility.com|  789 Madison Street|   Bates| 28|
+--------------+---------+------+------+-------+--------+-----+------------------------+--------------------+--------+---+
|            18|     Dale|     M| Orick|   4180|    null|   MD|     daleadams@boink.com|467 Hutchinson Court|   Adams| 33|
+--------------+---------+------+------+-------+--------+-----+------------------------+--------------------+--------+---+


Example 2: Selecting Specific Fields
------------------------------------

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
|    Amber|    Duke|
+---------+--------+
|     Dale|   Adams|
+---------+--------+
|   Hattie|    Bond|
+---------+--------+
|  Nanette|   Bates|
+---------+--------+


Example 3: Using Field Alias
----------------------------

Alias is often used to make your query more readable by giving your field a shorter name.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number AS num FROM accounts"
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
	  }
	}

Result set:

+---+
|num|
+===+
|  1|
+---+
|  6|
+---+
| 13|
+---+
| 18|
+---+


Example 4: Selecting Distinct Fields
------------------------------------

``DISTINCT`` is useful when you want to de-duplicate and get unique field value. You can provide one or more field names.

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

``FROM`` clause specifies Elasticsearch index where the data should be retrieved from. You've seen how to specify a single index in FROM clause in last section. Here we provide examples for more use cases.

Subquery in ``FROM`` clause is also supported. Please check out the documentation for more details.

Syntax
------

Rule ``tableName``:

.. image:: /docs/user/img/rdd/tableName.png

Example 1: Using Index Alias
----------------------------

Similarly you can give index in ``FROM`` clause an alias and use it across clauses in query.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT acc.account_number FROM accounts acc"
	}

Example 2: Selecting From Multiple Indices by Index Pattern
-----------------------------------------------------------

Alternatively you can query from multiple indices of similar names by index pattern. This is very convenient for indices created by Logstash index template with date as suffix.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM account*"
	}

Example 3: [Deprecating] Selecting From Specific Index Type
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

`WHERE` clause specifies only Elasticsearch documents that meet the criteria should be affected. It consists of predicates that uses ``=``, ``<>``, ``>``, ``>=``, ``<``, ``<=``, ``IN``, ``BETWEEN``, ``LIKE``, ``IS NULL`` or ``IS NOT NULL``. These predicates can be combined by logical operator ``NOT``, ``AND`` or ``OR`` to build more complex expression.

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


GROUP BY
========

Description
-----------

``GROUP BY`` groups documents with same field value into buckets. It is often used along with aggregation functions to aggregate inside each bucket. Please refer to SQL Functions documentation for more details.

Note that ``WHERE`` clause is applied before ``GROUP BY`` clause.

Example 1: Grouping by Fields
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


Example 2: Grouping by Field Alias
----------------------------------

Field alias is accessible in ``GROUP BY`` clause.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number AS num FROM accounts GROUP BY num"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 0,
	  "_source" : {
	    "includes" : [
	      "account_number"
	    ],
	    "excludes" : [ ]
	  },
	  "stored_fields" : "account_number",
	  "aggregations" : {
	    "num" : {
	      "terms" : {
	        "field" : "account_number",
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
|num|
+===+
|  1|
+---+
|  6|
+---+
| 13|
+---+
| 18|
+---+


Example 3: Grouping by Ordinal
------------------------------

Alternatively field ordinal in ``SELECT`` clause can be used too. However this is not recommended because your ``GROUP BY`` clause depends on fields in ``SELECT`` clause and require to change accordingly.

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


Example 4: Grouping by Scalar Function
--------------------------------------

Scalar function can be used in ``GROUP BY`` clause and it's required to be present in ``SELECT`` clause too.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT ABS(age) AS a FROM accounts GROUP BY ABS(age)"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 0,
	  "_source" : {
	    "includes" : [
	      "script"
	    ],
	    "excludes" : [ ]
	  },
	  "stored_fields" : "a",
	  "script_fields" : {
	    "a" : {
	      "script" : {
	        "source" : "def abs_1 = Math.abs(doc['age'].value);return abs_1;",
	        "lang" : "painless"
	      },
	      "ignore_failure" : false
	    }
	  },
	  "aggregations" : {
	    "a" : {
	      "terms" : {
	        "script" : {
	          "source" : "def abs_1 = Math.abs(doc['age'].value);return abs_1;",
	          "lang" : "painless"
	        },
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

+----+
|   a|
+====+
|28.0|
+----+
|32.0|
+----+
|33.0|
+----+
|36.0|
+----+


HAVING
======

Description
-----------

``HAVING`` clause filters result from ``GROUP BY`` clause by predicate(s). Because of this, aggregation function, even different from those on ``SELECT`` clause, can be used in predicate.

Example
-------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT age, MAX(balance) FROM accounts GROUP BY age HAVING MIN(balance) > 10000"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 0,
	  "_source" : {
	    "includes" : [
	      "age",
	      "MAX"
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
	      },
	      "aggregations" : {
	        "MAX_0" : {
	          "max" : {
	            "field" : "balance"
	          }
	        },
	        "min_0" : {
	          "min" : {
	            "field" : "balance"
	          }
	        },
	        "bucket_filter" : {
	          "bucket_selector" : {
	            "buckets_path" : {
	              "min_0" : "min_0",
	              "MAX_0" : "MAX_0"
	            },
	            "script" : {
	              "source" : "params.min_0 > 10000",
	              "lang" : "painless"
	            },
	            "gap_policy" : "skip"
	          }
	        }
	      }
	    }
	  }
	}

Result set:

+---+------------+
|age|MAX(balance)|
+===+============+
| 28|       32838|
+---+------------+
| 32|       39225|
+---+------------+


ORDER BY
========

Description
-----------

``ORDER BY`` clause specifies which fields used to sort the result and in which direction.

Example 1: Ordering by Fields
-----------------------------

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


Example 2: Specifying Order for Null
------------------------------------

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


