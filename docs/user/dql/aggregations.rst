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

COUNT Aggregations
------------------

Besides regular identifiers, ``COUNT`` aggregate function also accepts arguments such as ``*`` or literals like ``1``. The meaning of these different forms are as follows:

1. ``COUNT(field)`` will count only if given field (or expression) is not null or missing in the input rows.
2. ``COUNT(*)`` will count the number of all its input rows.
3. ``COUNT(1)`` is same as ``COUNT(*)`` because any non-null literal will count.

HAVING Clause
=============

Description
-----------

A ``HAVING`` clause can serve as aggregation filter that filters out aggregated values satisfy the condition expression given.

HAVING with GROUP BY
--------------------

Aggregate expressions or its alias defined in ``SELECT`` clause can be used in ``HAVING`` condition.

1. It's recommended to use non-aggregate expression in ``WHERE`` although it's allowed to do this in ``HAVING`` clause.
2. The aggregation in ``HAVING`` clause is not necessarily same as that on select list. As extension to SQL standard, it's also not restricted to involve identifiers only on group by list.

Here is an example for typical use of ``HAVING`` clause::

    od> SELECT
    ...  gender, sum(age)
    ... FROM accounts
    ... GROUP BY gender
    ... HAVING sum(age) > 100;
    fetched rows / total rows = 1/1
    +----------+------------+
    | gender   | sum(age)   |
    |----------+------------|
    | M        | 101        |
    +----------+------------+

Here is another example for using alias in ``HAVING`` condition. Note that if an identifier is ambiguous, for example present both as a select alias and an index field, preference is alias. This means the identifier will be replaced by expression aliased in ``SELECT`` clause::

    od> SELECT
    ...  gender, sum(age) AS s
    ... FROM accounts
    ... GROUP BY gender
    ... HAVING s > 100;
    fetched rows / total rows = 1/1
    +----------+-----+
    | gender   | s   |
    |----------+-----|
    | M        | 101 |
    +----------+-----+

HAVING without GROUP BY
-----------------------

Additionally, a ``HAVING`` clause can work without ``GROUP BY`` clause. This is useful because aggregation is not allowed to be present in ``WHERE`` clause::

    od> SELECT
    ...  'Total of age > 100'
    ... FROM accounts
    ... HAVING sum(age) > 100;
    fetched rows / total rows = 1/1
    +------------------------+
    | 'Total of age > 100'   |
    |------------------------|
    | Total of age > 100     |
    +------------------------+

