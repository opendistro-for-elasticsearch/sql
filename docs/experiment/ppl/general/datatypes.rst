
==========
Data Types
==========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Overview
========

PPL Data Types
-------------------

The PPL support the following data types.

+---------------+
| PPL Data Type |
+===============+
| boolean       |
+---------------+
| byte          |
+---------------+
| short         |
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
| datetime      |
+---------------+
| date          |
+---------------+
| time          |
+---------------+
| interval      |
+---------------+
| ip            |
+---------------+
| geo_point     |
+---------------+
| binary        |
+---------------+
| struct        |
+---------------+
| array         |
+---------------+

Data Types Mapping
------------------

The table below list the mapping between Elasticsearch Data Type, PPL Data Type and SQL Type.

+--------------------+---------------+-----------+
| Elasticsearch Type | PPL Type      | SQL Type  |
+====================+===============+===========+
| boolean            | boolean       | BOOLEAN   |
+--------------------+---------------+-----------+
| byte               | byte          | TINYINT   |
+--------------------+---------------+-----------+
| short              | byte          | SMALLINT  |
+--------------------+---------------+-----------+
| integer            | integer       | INTEGER   |
+--------------------+---------------+-----------+
| long               | long          | BIGINT    |
+--------------------+---------------+-----------+
| float              | float         | REAL      |
+--------------------+---------------+-----------+
| half_float         | float         | FLOAT     |
+--------------------+---------------+-----------+
| scaled_float       | float         | DOUBLE    |
+--------------------+---------------+-----------+
| double             | double        | DOUBLE    |
+--------------------+---------------+-----------+
| keyword            | string        | VARCHAR   |
+--------------------+---------------+-----------+
| text               | text          | VARCHAR   |
+--------------------+---------------+-----------+
| date               | timestamp     | TIMESTAMP |
+--------------------+---------------+-----------+
| ip                 | ip            | VARCHAR   |
+--------------------+---------------+-----------+
| date               | timestamp     | TIMESTAMP |
+--------------------+---------------+-----------+
| binary             | binary        | VARBINARY |
+--------------------+---------------+-----------+
| object             | struct        | STRUCT    |
+--------------------+---------------+-----------+
| nested             | array         | STRUCT    |
+--------------------+---------------+-----------+

Notes: Not all the PPL Type has correspond Elasticsearch Type. e.g. data and time. To use function which required such data type, user should explict convert the data type.



Numeric Data Types
==================

Numeric values ranged from -2147483648 to +2147483647 are recognized as integer with type name ``INTEGER``. For others outside the range, ``LONG`` integer will be the data type after parsed.


Date and Time Data Types
========================

The date and time data types are the types that represent temporal values and PPL plugin supports types including DATE, TIME, DATETIME, TIMESTAMP and INTERVAL. By default, the Elasticsearch DSL uses date type as the only date and time related type, which has contained all information about an absolute time point. To integrate with PPL language, each of the types other than timestamp is holding part of temporal or timezone information, and the usage to explicitly clarify the date and time types is reflected in the datetime functions (see `Functions <functions.rst>`_ for details), where some functions might have restrictions in the input argument type.


Date
----

Date represents the calendar date regardless of the time zone. A given date value represents a 24-hour period, or say a day, but this period varies in different timezones and might have flexible hours during Daylight Savings Time programs. Besides, the date type does not contain time information as well. The supported range is '1000-01-01' to '9999-12-31'.

+------+--------------+------------------------------+
| Type | Syntax       | Range                        |
+======+==============+==============================+
| Date | 'yyyy-MM-dd' | '0001-01-01' to '9999-12-31' |
+------+--------------+------------------------------+


Time
----

Time represents the time on the clock or watch with no regard for which timezone it might be related with. Time type data does not have date information.

+------+-----------------------+----------------------------------------+
| Type | Syntax                | Range                                  |
+======+=======================+========================================+
| Time | 'hh:mm:ss[.fraction]' | '00:00:00.000000' to '23:59:59.999999' |
+------+-----------------------+----------------------------------------+


Datetime
--------

Datetime type is the combination of date and time. The conversion rule of date or time to datetime is described in `Conversion between date and time types`_. Datetime type does not contain timezone information. For an absolute time point that contains both date time and timezone information, see `Timestamp`_.

+----------+----------------------------------+--------------------------------------------------------------+
| Type     | Syntax                           | Range                                                        |
+==========+==================================+==============================================================+
| Datetime | 'yyyy-MM-dd hh:mm:ss[.fraction]' | '0001-01-01 00:00:00.000000' to '9999-12-31 23:59:59.999999' |
+----------+----------------------------------+--------------------------------------------------------------+



Timestamp
---------

A timestamp instance is an absolute instant independent of timezone or convention. For example, for a given point of time, if we set the timestamp of this time point into another timezone, the value should also be different accordingly. Besides, the storage of timestamp type is also different from the other types. The timestamp is converted from the current timezone to UTC for storage, and is converted back to the set timezone from UTC when retrieving.

+-----------+----------------------------------+------------------------------------------------------------------+
| Type      | Syntax                           | Range                                                            |
+===========+==================================+==================================================================+
| Timestamp | 'yyyy-MM-dd hh:mm:ss[.fraction]' | '0001-01-01 00:00:01.000000' UTC to '9999-12-31 23:59:59.999999' |
+-----------+----------------------------------+------------------------------------------------------------------+


Interval
--------

Interval data type represents a temporal duration or a period. The syntax is as follows:

+----------+--------------------+
| Type     | Syntax             |
+==========+====================+
| Interval | INTERVAL expr unit |
+----------+--------------------+

The expr is any expression that can be iterated to a quantity value eventually, see `Expressions <expressions.rst>`_ for details. The unit represents the unit for interpreting the quantity, including MICROSECOND, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, QUARTER and YEAR.The INTERVAL keyword and the unit specifier are not case sensitive. Note that there are two classes of intervals. Year-week intervals can store years, quarters, months and weeks. Day-time intervals can store days, hours, minutes, seconds and microseconds. Year-week intervals are comparable only with another year-week intervals. These two types of intervals can only comparable with the same type of themselves.


Conversion between date and time types
--------------------------------------

Basically the date and time types except interval can be converted to each other, but might suffer some alteration of the value or some information loss, for example extracting the time value from a datetime value, or convert a date value to a datetime value and so forth. Here lists the summary of the conversion rules that PPL plugin supports for each of the types:

Conversion from DATE
>>>>>>>>>>>>>>>>>>>>

- Since the date value does not have any time information, conversion to `Time`_ type is not useful, and will always return a zero time value '00:00:00'.

- Conversion from date to datetime has a data fill-up due to the lack of time information, and it attaches the time '00:00:00' to the original date by default and forms a datetime instance. For example, the result to covert date '2020-08-17' to datetime type is datetime '2020-08-17 00:00:00'.

- Conversion to timestamp is to alternate both the time value and the timezone information, and it attaches the zero time value '00:00:00' and the session timezone (UTC by default) to the date. For example, the result to covert date '2020-08-17' to datetime type with session timezone UTC is datetime '2020-08-17 00:00:00' UTC.


Conversion from TIME
>>>>>>>>>>>>>>>>>>>>

- Time value cannot be converted to any other date and time types since it does not contain any date information, so it is not meaningful to give no date info to a date/datetime/timestamp instance.


Conversion from DATETIME
>>>>>>>>>>>>>>>>>>>>>>>>

- Conversion from datetime to date is to extract the date part from the datetime value. For example, the result to convert datetime '2020-08-17 14:09:00' to date is date '2020-08-08'.

- Conversion to time is to extract the time part from the datetime value. For example, the result to convert datetime '2020-08-17 14:09:00' to time is time '14:09:00'.

- Since the datetime type does not contain timezone information, the conversion to timestamp needs to fill up the timezone part with the session timezone. For example, the result to convert datetime '2020-08-17 14:09:00' with system timezone of UTC, to timestamp is timestamp '2020-08-17 14:09:00' UTC.


Conversion from TIMESTAMP
>>>>>>>>>>>>>>>>>>>>>>>>>

- Conversion from timestamp is much more straightforward. To convert it to date is to extract the date value, and conversion to time is to extract the time value. Conversion to datetime, it will extracts the datetime value and leave the timezone information over. For example, the result to convert datetime '2020-08-17 14:09:00' UTC to date is date '2020-08-17', to time is '14:09:00' and to datetime is datetime '2020-08-17 14:09:00'.


String Data Types
=================

A string is a sequence of characters enclosed in either single or double quotes. For example, both 'text' and "text" will be treated as string literal.


Query Struct Data Types
=======================

In PPL, the Struct Data Types corresponding to the `Object field type in Elasticsearch <https://www.elastic.co/guide/en/elasticsearch/reference/current/object.html>`_. The "." is used as the path selector when access the inner attribute of the struct data.

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

Here is the mapping for test index ``employees_nested``. Note that field ``projects`` is a nested field::

    {
      "mappings": {
        "properties": {
          "id": {
            "type": "long"
          },
          "name": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "projects": {
            "type": "nested",
            "properties": {
              "name": {
                "type": "text",
                "fields": {
                  "keyword": {
                    "type": "keyword"
                  }
                },
                "fielddata": true
              },
              "started_year": {
                "type": "long"
              }
            }
          },
          "title": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          }
        }
      }
    }


Result set::

	{
	  "employees_nested" : [
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


Example 1: Select struct inner attribute
----------------------------------------

The example show fetch city (top level), city.name (second level), city.location.latitude (deeper level) struct type data from people results.

PPL query::

    od> source=people | fields city, city.name, city.location.latitude;
    fetched rows / total rows = 1/1
    +-----------------------------------------------------+-------------+--------------------------+
    | city                                                | city.name   | city.location.latitude   |
    |-----------------------------------------------------+-------------+--------------------------|
    | {'name': 'Seattle', 'location': {'latitude': 10.5}} | Seattle     | 10.5                     |
    +-----------------------------------------------------+-------------+--------------------------+


Example 2: Group by struct inner attribute
------------------------------------------

The example show group by object field inner attribute.

PPL query::

    od> source=people | stats count() by city.name;
    fetched rows / total rows = 1/1
    +-----------+-------------+
    | count()   | city.name   |
    |-----------+-------------|
    | 1         | Seattle     |
    +-----------+-------------+

Example 3: Selecting Field of Array Value
-----------------------------------------

Select deeper level for object fields of array value which returns the first element in the array. For example, because inner field ``accounts.id`` has three values instead of a tuple in this document, the first entry is returned.::

    od> source = people | fields accounts, accounts.id;
    fetched rows / total rows = 1/1
    +------------+---------------+
    | accounts   | accounts.id   |
    |------------+---------------|
    | {'id': 1}  | 1             |
    +------------+---------------+