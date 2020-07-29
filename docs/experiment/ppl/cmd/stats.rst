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


Syntax
============
stats <aggregation>... [by-clause]...


* aggregation: mandatory. A statistical aggregation function. The argument of aggregation must be field.
* by-clause: optional. The one or more fields to group the results by. **Default**: If no <by-clause> is specified, the stats command returns only one row, which is the aggregation over the entire result set.


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
    | F        | 28                 |
    | M        | 33.666666666666664 |
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
    | F        | 28                 | 28         |
    | M        | 33.666666666666664 | 101        |
    +----------+--------------------+------------+

