=============
top
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| Using ``top`` command to find the most common tuple of values of all fields in the field list.


Syntax
============
top [N] <field-list> [by-clause]

* N: number of results to return. **Default**: 10
* field-list: mandatory. comma-delimited list of field names.
* by-clause: optional. one or more fields to group the results by.


Example 1: Find the most common values in a field
===========================================

The example finds most common gender of all the accounts.

PPL query::

    od> source=accounts | top gender;
    fetched rows / total rows = 2/2
    +------------+
    | gender     |
    |------------|
    | M          |
    |------------|
    | F          |
    +------------+

Example 2: Find the most common values in a field
===========================================

The example finds most common gender of all the accounts.

PPL query::

    od> source=accounts | top 1 gender;
    fetched rows / total rows = 1/1
    +------------+
    | gender     |
    |------------|
    | M          |
    +------------+

Example 2: Find the most common values organized by gender
====================================================

The example finds most common age of all the accounts group by gender.

PPL query::

    od> source=accounts | top 1 age by gender;
    fetched rows / total rows = 2/2
    +----------+----------+
    | gender   | age      |
    |----------+----------|
    | F        | 39       |
    | M        | 31       |
    +----------+----------+


