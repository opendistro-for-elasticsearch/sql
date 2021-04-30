
===============
Complex Queries
===============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2

Besides simple SFW queries (SELECT-FROM-WHERE), there is also support for complex queries such as Subquery, ``JOIN``, ``UNION`` and ``MINUS``. For these queries, more than one Elasticsearch index and DSL query is involved. You can check out how they are performed behind the scene by our explain API.

Subquery
========

Description
-----------

A subquery is a complete ``SELECT`` statement which is used within another statement and enclosed in parenthesis. From the explain output, you can notice that some subquery are actually transformed to an equivalent join query to execute.

Example 1: Table Subquery
-------------------------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT a1.firstname, a1.lastname, a1.balance
		FROM accounts a1
		WHERE a1.account_number IN (
		  SELECT a2.account_number
		  FROM accounts a2
		  WHERE a2.balance > 10000
		)
		"""
	}

Explain::

	{
	  "Physical Plan" : {
	    "Project [ columns=[a1.balance, a1.firstname, a1.lastname] ]" : {
	      "Top [ count=200 ]" : {
	        "BlockHashJoin[ conditions=( a1.account_number = a2.account_number ), type=JOIN, blockSize=[FixedBlockSize with size=10000] ]" : {
	          "Scroll [ accounts as a2, pageSize=10000 ]" : {
	            "request" : {
	              "size" : 200,
	              "query" : {
	                "bool" : {
	                  "filter" : [
	                    {
	                      "bool" : {
	                        "adjust_pure_negative" : true,
	                        "must" : [
	                          {
	                            "bool" : {
	                              "adjust_pure_negative" : true,
	                              "must" : [
	                                {
	                                  "bool" : {
	                                    "adjust_pure_negative" : true,
	                                    "must_not" : [
	                                      {
	                                        "bool" : {
	                                          "adjust_pure_negative" : true,
	                                          "must_not" : [
	                                            {
	                                              "exists" : {
	                                                "field" : "account_number",
	                                                "boost" : 1
	                                              }
	                                            }
	                                          ],
	                                          "boost" : 1
	                                        }
	                                      }
	                                    ],
	                                    "boost" : 1
	                                  }
	                                },
	                                {
	                                  "range" : {
	                                    "balance" : {
	                                      "include_lower" : false,
	                                      "include_upper" : true,
	                                      "from" : 10000,
	                                      "boost" : 1,
	                                      "to" : null
	                                    }
	                                  }
	                                }
	                              ],
	                              "boost" : 1
	                            }
	                          }
	                        ],
	                        "boost" : 1
	                      }
	                    }
	                  ],
	                  "adjust_pure_negative" : true,
	                  "boost" : 1
	                }
	              },
	              "from" : 0
	            }
	          },
	          "Scroll [ accounts as a1, pageSize=10000 ]" : {
	            "request" : {
	              "size" : 200,
	              "from" : 0,
	              "_source" : {
	                "excludes" : [ ],
	                "includes" : [
	                  "firstname",
	                  "lastname",
	                  "balance",
	                  "account_number"
	                ]
	              }
	            }
	          },
	          "useTermsFilterOptimization" : false
	        }
	      }
	    }
	  },
	  "description" : "Hash Join algorithm builds hash table based on result of first query, and then probes hash table to find matched rows for each row returned by second query",
	  "Logical Plan" : {
	    "Project [ columns=[a1.balance, a1.firstname, a1.lastname] ]" : {
	      "Top [ count=200 ]" : {
	        "Join [ conditions=( a1.account_number = a2.account_number ) type=JOIN ]" : {
	          "Group" : [
	            {
	              "Project [ columns=[a1.balance, a1.firstname, a1.lastname, a1.account_number] ]" : {
	                "TableScan" : {
	                  "tableAlias" : "a1",
	                  "tableName" : "accounts"
	                }
	              }
	            },
	            {
	              "Project [ columns=[a2.account_number] ]" : {
	                "Filter [ conditions=[AND ( AND account_number ISN null, AND balance GT 10000 ) ] ]" : {
	                  "TableScan" : {
	                    "tableAlias" : "a2",
	                    "tableName" : "accounts"
	                  }
	                }
	              }
	            }
	          ]
	        }
	      }
	    }
	  }
	}

Result set:

+------------+-----------+----------+
|a1.firstname|a1.lastname|a1.balance|
+============+===========+==========+
|       Amber|       Duke|     39225|
+------------+-----------+----------+
|     Nanette|      Bates|     32838|
+------------+-----------+----------+


Example 2: Subquery in FROM Clause
----------------------------------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT a.f, a.l, a.a
		FROM (
		  SELECT firstname AS f, lastname AS l, age AS a
		  FROM accounts
		  WHERE age > 30
		) AS a
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
	      "firstname",
	      "lastname",
	      "age"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+------+-----+--+
|     f|    l| a|
+======+=====+==+
| Amber| Duke|32|
+------+-----+--+
|  Dale|Adams|33|
+------+-----+--+
|Hattie| Bond|36|
+------+-----+--+


Here is another example with aggregation function and GROUP BY in subquery::

    od> SELECT avg_balance FROM (
    ...   SELECT AVG(balance) AS avg_balance FROM accounts GROUP BY gender, age
    ... ) AS a;
    fetched rows / total rows = 4/4
    +---------------+
    | avg_balance   |
    |---------------|
    | 32838.0       |
    | 39225.0       |
    | 4180.0        |
    | 5686.0        |
    +---------------+


Query with multiple layers of subquery is supported as well, here follows a example::

    od> SELECT name FROM (
    ...   SELECT lastname AS name, age FROM (
    ...     SELECT * FROM accounts WHERE gender = 'M'
    ...   ) AS accounts WHERE age < 35
    ... ) AS accounts
    fetched rows / total rows = 2/2
    +--------+
    | name   |
    |--------|
    | Duke   |
    | Adams  |
    +--------+


JOINs
=====

Description
-----------

A ``JOIN`` clause combines columns from one or more indices by using values common to each.

Syntax
------

Rule ``tableSource``:

.. image:: /docs/user/img/rdd/tableSource.png

Rule ``joinPart``:

.. image:: /docs/user/img/rdd/joinPart.png

Example 1: Inner Join
---------------------

Inner join is very commonly used that creates a new result set by combining columns of two indices based on the join predicates specified. It iterates both indices and compare each document to find all that satisfy the join predicates. Keyword ``JOIN`` is used and preceded by ``INNER`` keyword optionally. The join predicate(s) is specified by ``ON`` clause.

 Remark that the explain API output for join queries looks complicated. This is because a join query is associated with two Elasticsearch DSL queries underlying and execute in the separate query planner framework. You can interpret it by looking into the logical plan and physical plan.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT
		  a.account_number, a.firstname, a.lastname,
		  e.id, e.name
		FROM accounts a
		JOIN employees_nested e
		 ON a.account_number = e.id
		"""
	}

Explain::

	{
	  "Physical Plan" : {
	    "Project [ columns=[a.account_number, a.firstname, a.lastname, e.name, e.id] ]" : {
	      "Top [ count=200 ]" : {
	        "BlockHashJoin[ conditions=( a.account_number = e.id ), type=JOIN, blockSize=[FixedBlockSize with size=10000] ]" : {
	          "Scroll [ employees_nested as e, pageSize=10000 ]" : {
	            "request" : {
	              "size" : 200,
	              "from" : 0,
	              "_source" : {
	                "excludes" : [ ],
	                "includes" : [
	                  "id",
	                  "name"
	                ]
	              }
	            }
	          },
	          "Scroll [ accounts as a, pageSize=10000 ]" : {
	            "request" : {
	              "size" : 200,
	              "from" : 0,
	              "_source" : {
	                "excludes" : [ ],
	                "includes" : [
	                  "account_number",
	                  "firstname",
	                  "lastname"
	                ]
	              }
	            }
	          },
	          "useTermsFilterOptimization" : false
	        }
	      }
	    }
	  },
	  "description" : "Hash Join algorithm builds hash table based on result of first query, and then probes hash table to find matched rows for each row returned by second query",
	  "Logical Plan" : {
	    "Project [ columns=[a.account_number, a.firstname, a.lastname, e.name, e.id] ]" : {
	      "Top [ count=200 ]" : {
	        "Join [ conditions=( a.account_number = e.id ) type=JOIN ]" : {
	          "Group" : [
	            {
	              "Project [ columns=[a.account_number, a.firstname, a.lastname] ]" : {
	                "TableScan" : {
	                  "tableAlias" : "a",
	                  "tableName" : "accounts"
	                }
	              }
	            },
	            {
	              "Project [ columns=[e.name, e.id] ]" : {
	                "TableScan" : {
	                  "tableAlias" : "e",
	                  "tableName" : "employees_nested"
	                }
	              }
	            }
	          ]
	        }
	      }
	    }
	  }
	}

Result set:

+----------------+-----------+----------+----+----------+
|a.account_number|a.firstname|a.lastname|e.id|    e.name|
+================+===========+==========+====+==========+
|               6|     Hattie|      Bond|   6|Jane Smith|
+----------------+-----------+----------+----+----------+


Example 2: Cross Join
---------------------

Cross join or Cartesian join combines each document from the first index with each from the second. The result set is the Cartesian Product of documents from both indices. It appears to be similar to inner join without ``ON`` clause to specify join condition.

 Caveat: It is risky to do cross join even on two indices of medium size. This may trigger our circuit breaker to terminate the query to avoid out of memory issue.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT
		  a.account_number, a.firstname, a.lastname,
		  e.id, e.name
		FROM accounts a
		JOIN employees_nested e
		"""
	}

Result set:

+----------------+-----------+----------+----+-----------+
|a.account_number|a.firstname|a.lastname|e.id|     e.name|
+================+===========+==========+====+===========+
|               1|      Amber|      Duke|   3|  Bob Smith|
+----------------+-----------+----------+----+-----------+
|               1|      Amber|      Duke|   4|Susan Smith|
+----------------+-----------+----------+----+-----------+
|               1|      Amber|      Duke|   6| Jane Smith|
+----------------+-----------+----------+----+-----------+
|               6|     Hattie|      Bond|   3|  Bob Smith|
+----------------+-----------+----------+----+-----------+
|               6|     Hattie|      Bond|   4|Susan Smith|
+----------------+-----------+----------+----+-----------+
|               6|     Hattie|      Bond|   6| Jane Smith|
+----------------+-----------+----------+----+-----------+
|              13|    Nanette|     Bates|   3|  Bob Smith|
+----------------+-----------+----------+----+-----------+
|              13|    Nanette|     Bates|   4|Susan Smith|
+----------------+-----------+----------+----+-----------+
|              13|    Nanette|     Bates|   6| Jane Smith|
+----------------+-----------+----------+----+-----------+
|              18|       Dale|     Adams|   3|  Bob Smith|
+----------------+-----------+----------+----+-----------+
|              18|       Dale|     Adams|   4|Susan Smith|
+----------------+-----------+----------+----+-----------+
|              18|       Dale|     Adams|   6| Jane Smith|
+----------------+-----------+----------+----+-----------+


Example 3: Outer Join
---------------------

Outer join is used to retain documents from one or both indices although it does not satisfy join predicate. For now, only ``LEFT OUTER JOIN`` is supported to retain rows from first index. Note that keyword ``OUTER`` is optional.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : """
		SELECT
		  a.account_number, a.firstname, a.lastname,
		  e.id, e.name
		FROM accounts a
		LEFT JOIN employees_nested e
		 ON a.account_number = e.id
		"""
	}

Result set:

+----------------+-----------+----------+----+----------+
|a.account_number|a.firstname|a.lastname|e.id|    e.name|
+================+===========+==========+====+==========+
|               1|      Amber|      Duke|null|      null|
+----------------+-----------+----------+----+----------+
|               6|     Hattie|      Bond|   6|Jane Smith|
+----------------+-----------+----------+----+----------+
|              13|    Nanette|     Bates|null|      null|
+----------------+-----------+----------+----+----------+
|              18|       Dale|     Adams|null|      null|
+----------------+-----------+----------+----+----------+


