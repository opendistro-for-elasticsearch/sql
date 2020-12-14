=============
stats
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| Using ``stats`` command to calculate the aggregation from search result.

The following table catalogs the aggregation functions and also indicates how each one handles NULL/MISSING values is handled:

+----------+-------------+-------------+
| Function | NULL        | MISSING     |
+----------+-------------+-------------+
| COUNT    | Not counted | Not counted |
+----------+-------------+-------------+
| SUM      | Ignore      | Ignore      |
+----------+-------------+-------------+
| AVG      | Ignore      | Ignore      |
+----------+-------------+-------------+
| MAX      | Ignore      | Ignore      |
+----------+-------------+-------------+
| MIN      | Ignore      | Ignore      |
+----------+-------------+-------------+


Syntax
============
stats <aggregation>... [by-clause]...


* aggregation: mandatory. A statistical aggregation function. The argument of aggregation must be field.
* by-clause: optional. The one or more fields to group the results by. **Default**: If no <by-clause> is specified, the stats command returns only one row, which is the aggregation over the entire result set.

Example 1: Calculate the count of events
========================================

The example show calculate the count of events in the accounts.

PPL query::

    od> source=accounts | stats count();
    fetched rows / total rows = 1/1
    +-----------+
    | count()   |
    |-----------|
    | 4         |
    +-----------+


Example 2: Calculate the average of a field
===========================================

The example show calculate the average age of all the accounts.

PPL query::

    od> source=accounts | stats avg(age);
    fetched rows / total rows = 1/1
    +------------+
    | avg(age)   |
    |------------|
    | 32.25      |
    +------------+


Example 3: Calculate the average of a field by group
====================================================

The example show calculate the average age of all the accounts group by gender.

PPL query::

    od> source=accounts | stats avg(age) by gender;
    fetched rows / total rows = 2/2
    +--------------------+----------+
    | avg(age)           | gender   |
    |--------------------+----------|
    | 28.0               | F        |
    | 33.666666666666664 | M        |
    +--------------------+----------+


Example 4: Calculate the average, sum and count of a field by group
===================================================================

The example show calculate the average age, sum age and count of events of all the accounts group by gender.

PPL query::

    od> source=accounts | stats avg(age), sum(age), count() by gender;
    fetched rows / total rows = 2/2
    +--------------------+------------+-----------+----------+
    | avg(age)           | sum(age)   | count()   | gender   |
    |--------------------+------------+-----------+----------|
    | 28.0               | 28         | 1         | F        |
    | 33.666666666666664 | 101        | 3         | M        |
    +--------------------+------------+-----------+----------+

Example 5: Calculate the maximum of a field
===========================================

The example calculates the max age of all the accounts.

PPL query::

    od> source=accounts | stats max(age);
    fetched rows / total rows = 1/1
    +------------+
    | max(age)   |
    |------------|
    | 36         |
    +------------+

Example 6: Calculate the maximum and minimum of a field by group
================================================================

The example calculates the max and min age values of all the accounts group by gender.

PPL query::

    od> source=accounts | stats max(age), min(age) by gender;
    fetched rows / total rows = 2/2
    +------------+------------+----------+
    | max(age)   | min(age)   | gender   |
    |------------+------------+----------|
    | 28         | 28         | F        |
    | 36         | 32         | M        |
    +------------+------------+----------+

