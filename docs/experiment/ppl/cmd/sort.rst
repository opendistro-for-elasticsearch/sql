=============
sort
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| Using ``sort`` command to sorts all the search result by the specified fields.


Syntax
============
sort <[+|-] sort-field>...


* [+|-]: optional. The plus [+] for ascending order and a minus [-] for descending order. **Default:** ascending order.
* sort-field: mandatory. The field used to sort.


Example 1: Sort by one field
=============================

The example show sort all the document with age field in ascending order.

PPL query::

    od> source=accounts | sort age | fields account_number, age;
    fetched rows / total rows = 4/4
    +------------------+-------+
    | account_number   | age   |
    |------------------+-------|
    | 13               | 28    |
    | 1                | 32    |
    | 18               | 33    |
    | 6                | 36    |
    +------------------+-------+


Example 2: Sort by one field return all the result
==================================================

The example show sort all the document with age field in ascending order.

PPL query::

    od> source=accounts | sort age | fields account_number, age;
    fetched rows / total rows = 4/4
    +------------------+-------+
    | account_number   | age   |
    |------------------+-------|
    | 13               | 28    |
    | 1                | 32    |
    | 18               | 33    |
    | 6                | 36    |
    +------------------+-------+


Example 3: Sort by one field in descending order
================================================

The example show sort all the document with age field in descending order.

PPL query::

    od> source=accounts | sort - age | fields account_number, age;
    fetched rows / total rows = 4/4
    +------------------+-------+
    | account_number   | age   |
    |------------------+-------|
    | 6                | 36    |
    | 18               | 33    |
    | 1                | 32    |
    | 13               | 28    |
    +------------------+-------+

Example 4: Sort by multiple field
=============================

The example show sort all the document with gender field in ascending order and age field in descending.

PPL query::

    od> source=accounts | sort + gender, - age | fields account_number, gender, age;
    fetched rows / total rows = 4/4
    +------------------+----------+-------+
    | account_number   | gender   | age   |
    |------------------+----------+-------|
    | 13               | F        | 28    |
    | 6                | M        | 36    |
    | 18               | M        | 33    |
    | 1                | M        | 32    |
    +------------------+----------+-------+

