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


* [+|-]: optional. The plus [+] stands for ascending order and NULL/MISSING first and a minus [-] stands for descending order and NULL/MISSING last. **Default:** ascending order and NULL/MISSING first.
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

Example 4: Sort by field include null value
===========================================

The example show sort employer field by default option (ascending order and null first), the result show that null value is in the first row.

PPL query::

    od> source=accounts | sort employer | fields employer;
    fetched rows / total rows = 4/4
    +------------+
    | employer   |
    |------------|
    | null       |
    | Netagy     |
    | Pyrami     |
    | Quility    |
    +------------+
