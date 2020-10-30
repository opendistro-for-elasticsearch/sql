
================
Window Functions
================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Overview
========

Introduction
------------

A window function consists of 2 pieces: a function and a window definition. A window definition defines a window of data rows with a given scope around the current row. The window is also called window frame sometimes. In a window frame, the function performs a calculation across the set of data and generate a result value for each row.

There are three categories of common window functions:

1. **Aggregate Functions**: COUNT(), MIN(), MAX(), AVG() and SUM().
2. **Ranking Functions**: ROW_NUMBER(), RANK(), DENSE_RANK(), PERCENT_RANK() and NTILE().
3. **Analytic Functions**: CUME_DIST(), LAG() and LEAD().

Syntax
------

The syntax of a window function is as follows in which both ``PARTITION BY`` and ``ORDER BY`` clause are optional::

  function_name (expression [, expression...])
  OVER (
    PARTITION BY expression [, expression...]
    ORDER BY expression [ASC | DESC] [, ...]
  )


Ranking Functions
=================

Ranking functions are window functions that assign an incremental rank to each row in the window. How the rank number gets increased is up to ranking function implementation, though the rank is mostly determined by field values in ``ORDER BY`` list. If ``PARTITION BY`` clause present, the state of ranking functions (incremental rank number maintained) will be reset.

Determinism
-----------

Note that normally ranking functions are supposed to be used with window definition that defines the order of data rows in the window. Otherwise the result is undetermined. In this case, ``ROW_NUMBER`` assigns row number to data rows in random order. ``RANK`` and ``DENSE_RANK`` always assigns rank 1 to each row.

ROW_NUMBER
----------

``ROW_NUMBER`` function assigns a row number to each row. As a special case, the row number is always increased by one regardless of the fields specified in ``ORDER BY`` list. Here is an example for ``ROW_NUMBER`` function::

    od> SELECT gender, balance, ROW_NUMBER() OVER(PARTITION BY gender ORDER BY balance) AS num FROM accounts;
    fetched rows / total rows = 4/4
    +----------+-----------+-------+
    | gender   | balance   | num   |
    |----------+-----------+-------|
    | F        | 32838     | 1     |
    | M        | 4180      | 1     |
    | M        | 5686      | 2     |
    | M        | 39225     | 3     |
    +----------+-----------+-------+


RANK
----

``RANK`` function assigns a rank to each row. For rows that have same values for fields specified in ``ORDER BY`` list, same rank is assigned. If this is the case, the next few ranks will be skipped depending on how many ties. Here is an example for ``RANK`` function::

    od> SELECT gender, RANK() OVER(ORDER BY gender DESC) AS rnk FROM accounts;
    fetched rows / total rows = 4/4
    +----------+-------+
    | gender   | rnk   |
    |----------+-------|
    | M        | 1     |
    | M        | 1     |
    | M        | 1     |
    | F        | 4     |
    +----------+-------+


DENSE_RANK
----------

Similarly as ``RANK``, ``DENSE_RANK`` function also assigns a rank to each row. The difference is there is no gap between ranks. Here is an example for ``DENSE_RANK`` function::

    od> SELECT gender, DENSE_RANK() OVER(ORDER BY gender DESC) AS rnk FROM accounts;
    fetched rows / total rows = 4/4
    +----------+-------+
    | gender   | rnk   |
    |----------+-------|
    | M        | 1     |
    | M        | 1     |
    | M        | 1     |
    | F        | 2     |
    +----------+-------+

