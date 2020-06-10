=============
stats
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| Using ``sort`` command to calculate the aggregation from search result.


Syntax
============
stats <aggregation>... [by-clause]...


* count: optional. The maximum number results to return from the sorted result. **Default:** 10000
* [+|-]: optional. The plus [+] for ascending order and a minus [-] for descending order. **Default:** ascending order.
* sort-field: mandatory. The field used to sort.


Example 1: Calculate the average of a field
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


Example 2: Calculate the average of a field by group
====================================================

The example show calculate the average age of all the accounts group by gender.

PPL query::

    od> source=accounts | stats avg(age) by gender;
    fetched rows / total rows = 2/2
    +----------+--------------------+
    | gender   | avg(age)           |
    |----------+--------------------|
    | M        | 33.666666666666664 |
    | F        | 28                 |
    +----------+--------------------+


Example 3: Calculate the average and sum of a field by group
============================================================

The example show calculate the average age and sum age of all the accounts group by gender.

PPL query::

    od> source=accounts | stats avg(age), sum(age) by gender;
    fetched rows / total rows = 2/2
    +----------+--------------------+------------+
    | gender   | avg(age)           | sum(age)   |
    |----------+--------------------+------------|
    | M        | 33.666666666666664 | 101        |
    | F        | 28                 | 28         |
    +----------+--------------------+------------+

