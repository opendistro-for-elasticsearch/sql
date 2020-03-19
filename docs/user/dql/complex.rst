
=============
Complex Query
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Subqueries
==========

Example: IN/EXISTS
------------------

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


JOINs
=====

Description
-----------

A ``JOIN`` clause combines columns from one or more indices by using values common to each.

Example 1: Inner Join
---------------------

Inner join is very commonly used that creates a new result set by combining columns of two indices based on the join predicates specified. It iterates both indices and compare each document to find all that satisfy the join predicates. Keyword ``JOIN`` is used and preceded by ``INNER`` keyword optionally. The join predicates is specified by ``ON`` clause.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT a.firstname, a.lastname, e.name FROM accounts a JOIN employees_nested e  ON a.account_number = e.id"
	}

Explain::

	{
	  "Physical Plan" : {
	    "Project [ columns=[a.firstname, a.lastname, e.name] ]" : {
	      "Top [ count=200 ]" : {
	        "BlockHashJoin[ conditions=( a.account_number = e.id ), type=JOIN, blockSize=[FixedBlockSize with size=10000] ]" : {
	          "Scroll [ employees_nested as e, pageSize=10000 ]" : {
	            "request" : {
	              "size" : 200,
	              "from" : 0,
	              "_source" : {
	                "excludes" : [ ],
	                "includes" : [
	                  "name",
	                  "id"
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
	                  "firstname",
	                  "lastname",
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
	    "Project [ columns=[a.firstname, a.lastname, e.name] ]" : {
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

+-----------+----------+----------+
|a.firstname|a.lastname|    e.name|
+===========+==========+==========+
|     Hattie|      Bond|Jane Smith|
+-----------+----------+----------+


Example 2: Cross Join
---------------------

Cross join or Cartesian join combines each document from the first index with each from the second. The result set is the Cartesian Product of documents from both indices. It appears to be similar to inner join without ``ON`` clause to specify join condition. Caveat: It is risky to do cross join even on two indices of medium size. This may trigger our circuit breaker to terminate the query to avoid out of memory issue.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT a.firstname, a.lastname, e.name FROM accounts a JOIN employees_nested e "
	}

Explain::

	{
	  "Physical Plan" : {
	    "Project [ columns=[a.firstname, a.lastname, e.name] ]" : {
	      "Top [ count=200 ]" : {
	        "BlockHashJoin[ conditions=, type=JOIN, blockSize=[FixedBlockSize with size=10000] ]" : {
	          "Scroll [ employees_nested as e, pageSize=10000 ]" : {
	            "request" : {
	              "size" : 200,
	              "from" : 0,
	              "_source" : {
	                "excludes" : [ ],
	                "includes" : [
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
	    "Project [ columns=[a.firstname, a.lastname, e.name] ]" : {
	      "Top [ count=200 ]" : {
	        "Join [ conditions= type=JOIN ]" : {
	          "Group" : [
	            {
	              "Project [ columns=[a.firstname, a.lastname] ]" : {
	                "TableScan" : {
	                  "tableAlias" : "a",
	                  "tableName" : "accounts"
	                }
	              }
	            },
	            {
	              "Project [ columns=[e.name] ]" : {
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

+-----------+----------+-----------+
|a.firstname|a.lastname|     e.name|
+===========+==========+===========+
|      Amber|      Duke|  Bob Smith|
+-----------+----------+-----------+
|      Amber|      Duke| Jane Smith|
+-----------+----------+-----------+
|      Amber|      Duke|Susan Smith|
+-----------+----------+-----------+
|       Dale|     Adams|  Bob Smith|
+-----------+----------+-----------+
|       Dale|     Adams| Jane Smith|
+-----------+----------+-----------+
|       Dale|     Adams|Susan Smith|
+-----------+----------+-----------+
|     Hattie|      Bond|  Bob Smith|
+-----------+----------+-----------+
|     Hattie|      Bond| Jane Smith|
+-----------+----------+-----------+
|     Hattie|      Bond|Susan Smith|
+-----------+----------+-----------+
|    Nanette|     Bates|  Bob Smith|
+-----------+----------+-----------+
|    Nanette|     Bates| Jane Smith|
+-----------+----------+-----------+
|    Nanette|     Bates|Susan Smith|
+-----------+----------+-----------+


Example 3: Outer Join
---------------------

Outer join is used to retain documents from one or both indices although it does not satisfy join predicate. For now, only ``LEFT OUTER JOIN`` is supported to retain rows from first index. Note that keyword ``OUTER`` is optional.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT a.account_number FROM accounts a LEFT JOIN employees_nested e  ON a.account_number = e.id "
	}

Explain::

	{
	  "Physical Plan" : {
	    "Project [ columns=[a.account_number] ]" : {
	      "Top [ count=200 ]" : {
	        "BlockHashJoin[ conditions=( a.account_number = e.id ), type=LEFT_OUTER_JOIN, blockSize=[FixedBlockSize with size=10000] ]" : {
	          "Scroll [ employees_nested as e, pageSize=10000 ]" : {
	            "request" : {
	              "size" : 200,
	              "from" : 0
	            }
	          },
	          "Scroll [ accounts as a, pageSize=10000 ]" : {
	            "request" : {
	              "size" : 200,
	              "from" : 0,
	              "_source" : {
	                "excludes" : [ ],
	                "includes" : [
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
	    "Project [ columns=[a.account_number] ]" : {
	      "Top [ count=200 ]" : {
	        "Join [ conditions=( a.account_number = e.id ) type=LEFT_OUTER_JOIN ]" : {
	          "Group" : [
	            {
	              "Project [ columns=[a.account_number] ]" : {
	                "TableScan" : {
	                  "tableAlias" : "a",
	                  "tableName" : "accounts"
	                }
	              }
	            },
	            {
	              "Project [ columns=[e.id] ]" : {
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

+----------------+
|a.account_number|
+================+
|               1|
+----------------+
|               6|
+----------------+
|              13|
+----------------+
|              18|
+----------------+


