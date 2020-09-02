=============
rare
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| Using ``rare`` command to find the least common tuple of values of all fields in the field list.

A maximum of 10 results is returned for each distinct tuple of values of the group-by fields.

Syntax
============
rare <field-list> [by-clause]

* field-list: mandatory. comma-delimited list of field names.
* by-clause: optional. one or more fields to group the results by.


Example 1: Find the least common values in a field
===========================================

The example finds least common gender of all the accounts.

PPL query::

    od> source=accounts | rare gender;
    fetched rows / total rows = 2/2
    +------------+
    | gender     |
    |------------|
    | F          |
    |------------|
    | M          |
    +------------+


Example 2: Find the least common values organized by gender
====================================================

The example finds least common age of all the accounts group by gender.

PPL query::

    od> source=accounts | rare age by gender;
    fetched rows / total rows = 20/20
    +----------+----------+
    | gender   | age      |
    |----------+----------|
    | F        | 29       |
    | F        | 20       |
    | F        | 23       |
    | F        | 25       |
    | F        | 37       |
    | F        | 38       |
    | F        | 40       |
    | F        | 27       |
    | F        | 36       |
    | F        | 24       |
    | M        | 27       |
    | M        | 24       |
    | M        | 34       |
    | M        | 38       |
    | M        | 28       |
    | M        | 39       |
    | M        | 21       |
    | M        | 30       |
    | M        | 25       |
    | M        | 29       |
    +----------+----------+


