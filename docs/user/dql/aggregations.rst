===========
Aggregation
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

Aggregate functions operate on sets of values. They are often used with a GROUP BY clause to group values into subsets.


GROUP BY Clause
===============

Description
-----------

The GROUP BY expression could be

1. Identifier
2. Ordinal
3. Expression

Identifier
----------

The group by expression could be identifier::

    od> SELECT gender, sum(age) FROM accounts GROUP BY gender;
    fetched rows / total rows = 2/2
    +----------+------------+
    | gender   | sum(age)   |
    |----------+------------|
    | F        | 28         |
    | M        | 101        |
    +----------+------------+


Ordinal
-------

The group by expression could be ordinal::

    od> SELECT gender, sum(age) FROM accounts GROUP BY 1;
    fetched rows / total rows = 2/2
    +----------+------------+
    | gender   | sum(age)   |
    |----------+------------|
    | F        | 28         |
    | M        | 101        |
    +----------+------------+


Expression
----------

The group by expression could be expression::

    od> SELECT abs(account_number), sum(age) FROM accounts GROUP BY abs(account_number);
    fetched rows / total rows = 4/4
    +-----------------------+------------+
    | abs(account_number)   | sum(age)   |
    |-----------------------+------------|
    | 1                     | 32         |
    | 13                    | 28         |
    | 18                    | 33         |
    | 6                     | 36         |
    +-----------------------+------------+


Aggregation
===========

Description
-----------

1. The aggregation could be used select.
2. The aggregation could be used as arguments of expression.
3. The aggregation could has expression as arguments.

Aggregation in Select
---------------------

The aggregation could be used select::

    od> SELECT gender, sum(age) FROM accounts GROUP BY gender;
    fetched rows / total rows = 2/2
    +----------+------------+
    | gender   | sum(age)   |
    |----------+------------|
    | F        | 28         |
    | M        | 101        |
    +----------+------------+

Expression over Aggregation
---------------------------

The aggregation could be used as arguments of expression::

    od> SELECT gender, sum(age) * 2 as sum2 FROM accounts GROUP BY gender;
    fetched rows / total rows = 2/2
    +----------+--------+
    | gender   | sum2   |
    |----------+--------|
    | F        | 56     |
    | M        | 202    |
    +----------+--------+

Expression as argument of Aggregation
-------------------------------------

The aggregation could has expression as arguments::

    od> SELECT gender, sum(age * 2) as sum2 FROM accounts GROUP BY gender;
    fetched rows / total rows = 2/2
    +----------+--------+
    | gender   | sum2   |
    |----------+--------|
    | F        | 56     |
    | M        | 202    |
    +----------+--------+

