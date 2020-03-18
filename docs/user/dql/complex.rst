
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


Set Operations
==============

Description
-----------

Set operations allow results of multiple queries to be combined into a single result set.

Example 1: UNION Operator
-------------------------

A ``UNION`` clause combines the results of two queries into a single result set. Duplicate rows are removed unless ``UNION ALL`` clause is being used. A common use case of ``UNION`` is to combine result set from data partitioned in indices daily or monthly.

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


Example 2: MINUS Operator
-------------------------

A ``MINUS`` clause takes two queries too but returns resulting rows of first query that do not appear in the other query. Duplicate rows are removed automatically as well.

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


