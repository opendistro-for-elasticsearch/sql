
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

ROW_NUMBER
----------

Here is an example for ``ROW_NUMBER`` function::

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

Here is an example for ``RANK`` function::

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

Here is an example for ``DENSE_RANK`` function::

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

