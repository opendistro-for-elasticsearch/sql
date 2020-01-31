.. highlight:: json

===========
Basic Query
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1


Introduction
============

SELECT
======

Description
-----------

SELECT clause specifies data of which fields should be retrieved.

Example 1
---------

You can use '*' to fetch all fields in the index which is useful to have a quick look at your data.

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

+---------------------+----------------+-------------+-----------+--------------+---------------+------------+------------------------+--------------------+---------------+----------+
|account_number (long)|firstname (text)|gender (text)|city (text)|balance (long)|employer (text)|state (text)|            email (text)|      address (text)|lastname (text)|age (long)|
+=====================+================+=============+===========+==============+===============+============+========================+====================+===============+==========+
|                    1|           Amber|            M|     Brogan|         39225|         Pyrami|          IL|    amberduke@pyrami.com|     880 Holmes Lane|           Duke|        32|
+---------------------+----------------+-------------+-----------+--------------+---------------+------------+------------------------+--------------------+---------------+----------+
|                    6|          Hattie|            M|      Dante|          5686|         Netagy|          TN|   hattiebond@netagy.com|  671 Bristol Street|           Bond|        36|
+---------------------+----------------+-------------+-----------+--------------+---------------+------------+------------------------+--------------------+---------------+----------+
|                   18|            Dale|            M|      Orick|          4180|          Boink|          MD|     daleadams@boink.com|467 Hutchinson Court|          Adams|        33|
+---------------------+----------------+-------------+-----------+--------------+---------------+------------+------------------------+--------------------+---------------+----------+
|                   13|         Nanette|            F|      Nogal|         32838|        Quility|          VA|nanettebates@quility.com|  789 Madison Street|          Bates|        28|
+---------------------+----------------+-------------+-----------+--------------+---------------+------------+------------------------+--------------------+---------------+----------+


Example 2
---------

More often you would give specific field names in SELECT clause to avoid large and unnecessary data.

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

+----------------+---------------+
|firstname (text)|lastname (text)|
+================+===============+
|           Amber|           Duke|
+----------------+---------------+
|          Hattie|           Bond|
+----------------+---------------+
|            Dale|          Adams|
+----------------+---------------+
|         Nanette|          Bates|
+----------------+---------------+


Example 3
---------

DISTINCT is useful when you want to de-duplicate and get unique field value. You can provide one or more field name in SELECT.

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

+----------+
|age (long)|
+==========+
|        28|
+----------+
|        32|
+----------+
|        33|
+----------+
|        36|
+----------+


FROM
====

Description
-----------

FROM clause specifies Elasticsearch index where the data should be retrieved from.

Example 1
---------

Typically a single Elasticsearch index name is expected to present in FROM clause.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM accounts"
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

+---------------------+
|account_number (long)|
+=====================+
|                    1|
+---------------------+
|                    6|
+---------------------+
|                   18|
+---------------------+
|                   13|
+---------------------+


Example 2
---------

It is convenient to query from multiple indices by index pattern using wildcard.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM account*"
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

+---------------------+
|account_number (long)|
+=====================+
|                    1|
+---------------------+
|                    6|
+---------------------+
|                   18|
+---------------------+
|                   13|
+---------------------+


Example 3
---------

You can also specify type name explicitly though this has been deprecated in later Elasticsearch version.

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM accounts/account"
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

+---------------------+
|account_number (long)|
+=====================+
|                    1|
+---------------------+
|                    6|
+---------------------+
|                   18|
+---------------------+
|                   13|
+---------------------+


WHERE
=====

Example 1
---------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM accounts"
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

+---------------------+
|account_number (long)|
+=====================+
|                    1|
+---------------------+
|                    6|
+---------------------+
|                   18|
+---------------------+
|                   13|
+---------------------+


Example 2
---------

logical operator to combine expression

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM accounts"
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

+---------------------+
|account_number (long)|
+=====================+
|                    1|
+---------------------+
|                    6|
+---------------------+
|                   18|
+---------------------+
|                   13|
+---------------------+


Example 3
---------

You can ... but this would be deprecated...

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT account_number FROM accounts"
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

+---------------------+
|account_number (long)|
+=====================+
|                    1|
+---------------------+
|                    6|
+---------------------+
|                   18|
+---------------------+
|                   13|
+---------------------+


Alias
=====

Description
-----------

Alias makes your query more readable by renaming your index or field to clear and short alias.

Example
-------

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

+---------------------+
|account_number (long)|
+=====================+
|                    1|
+---------------------+
|                    6|
+---------------------+
|                   18|
+---------------------+


FROM
====

Example 1
---------

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

+----------+
|age (long)|
+==========+
|        28|
+----------+
|        32|
+----------+
|        33|
+----------+
|        36|
+----------+


Example 2
---------

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

+--------+
|a (long)|
+========+
|      28|
+--------+
|      32|
+--------+
|      33|
+--------+
|      36|
+--------+


Example 3
---------

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

+----------+
|age (long)|
+==========+
|        28|
+----------+
|        32|
+----------+
|        33|
+----------+
|        36|
+----------+


HAVING
======

Example 1
---------

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

+----------+
|age (long)|
+==========+
|        28|
+----------+
|        32|
+----------+
|        33|
+----------+
|        36|
+----------+


Example 2
---------

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

+--------+
|a (long)|
+========+
|      28|
+--------+
|      32|
+--------+
|      33|
+--------+
|      36|
+--------+


Example 3
---------

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

+----------+
|age (long)|
+==========+
|        28|
+----------+
|        32|
+----------+
|        33|
+----------+
|        36|
+----------+


ORDER BY
========

Example 1
---------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT age FROM accounts ORDER BY age"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 200,
	  "_source" : {
	    "includes" : [
	      "age"
	    ],
	    "excludes" : [ ]
	  },
	  "sort" : [
	    {
	      "age" : {
	        "order" : "asc"
	      }
	    }
	  ]
	}

Result set:

+----------+
|age (long)|
+==========+
|        28|
+----------+
|        32|
+----------+
|        33|
+----------+
|        36|
+----------+


Example 2
---------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT age AS a FROM accounts ORDER BY a"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 200,
	  "_source" : {
	    "includes" : [
	      "age"
	    ],
	    "excludes" : [ ]
	  },
	  "sort" : [
	    {
	      "age" : {
	        "order" : "asc"
	      }
	    }
	  ]
	}

Result set:

+----------+
|age (long)|
+==========+
|        28|
+----------+
|        32|
+----------+
|        33|
+----------+
|        36|
+----------+


Example 3
---------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT age FROM accounts ORDER BY 1"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 200,
	  "_source" : {
	    "includes" : [
	      "age"
	    ],
	    "excludes" : [ ]
	  },
	  "sort" : [
	    {
	      "age" : {
	        "order" : "asc"
	      }
	    }
	  ]
	}

Result set:

+----------+
|age (long)|
+==========+
|        28|
+----------+
|        32|
+----------+
|        33|
+----------+
|        36|
+----------+


LIMIT
=====

Example
-------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT age FROM accounts LIMIT 1"
	}

Explain::

	{
	  "from" : 0,
	  "size" : 1,
	  "_source" : {
	    "includes" : [
	      "age"
	    ],
	    "excludes" : [ ]
	  }
	}

Result set:

+----------+
|age (long)|
+==========+
|        32|
+----------+


