==========
Data Types
==========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


ODFE SQL Data Types
===================

The ODFE SQL Engine support the following data types.

+---------------+
| ODFE SQL Type |
+===============+
| boolean       |
+---------------+
| integer       |
+---------------+
| long          |
+---------------+
| float         |
+---------------+
| double        |
+---------------+
| string        |
+---------------+
| text          |
+---------------+
| timestamp     |
+---------------+
| date          |
+---------------+
| time          |
+---------------+
| struct        |
+---------------+
| array         |
+---------------+

Data Types Mapping
==================

The table below list the mapping between Elasticsearch Data Type, ODFE SQL Data Type and SQL Type.

+--------------------+---------------+-----------+
| Elasticsearch Type | ODFE SQL Type | SQL Type  |
+====================+===============+===========+
| boolean            | boolean       | BOOLEAN   |
+--------------------+---------------+-----------+
| integer            | integer       | INTEGER   |
+--------------------+---------------+-----------+
| long               | long          | LONG      |
+--------------------+---------------+-----------+
| float              | float         | FLOAT     |
+--------------------+---------------+-----------+
| double             | double        | DOUBLE    |
+--------------------+---------------+-----------+
| keyword            | string        | VARCHAR   |
+--------------------+---------------+-----------+
| text               | text          | VARCHAR   |
+--------------------+---------------+-----------+
| date               | timestamp     | TIMESTAMP |
+--------------------+---------------+-----------+
| object             | struct        | STRUCT    |
+--------------------+---------------+-----------+
| nested             | array         | TBD       |
+--------------------+---------------+-----------+

Notes: Not all the ODFE SQL Type has correspond Elasticsearch Type. e.g. data and time. To use function which required such data type, user should explict convert the data type.
