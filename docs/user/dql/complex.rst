
=============
Complex Query
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Subqueries
==========

Example 1: IN/EXISTS
--------------------

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


Example 2: Subqueries in FROM clause
------------------------------------

SQL query::

	POST /_opendistro/_sql
	{
	  "query" : "SELECT * FROM (SELECT * FROM accounts) AS a"
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

Example 1: UNION
----------------

A ``UNION`` clause combines the results of two queries into a single result. Any duplicate record are removed unless ``UNION ALL`` is used. A common use case of ``UNION`` is to combine results from an index rotated on a daily or monthly basis.

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


Example 2: MINUS
----------------

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


