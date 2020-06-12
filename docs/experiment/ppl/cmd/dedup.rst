=============
dedup
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| Using ``dedup`` command to remove identical document defined by field from the search result.


Syntax
============
dedup [int] <field-list> [keepempty=<bool>] [consecutive=<bool>]


* int: optional. The ``dedup`` command retains multiple events for each combination when you specify <int>. The number for <int> must be greater than 0. If you do not specify a number, only the first occurring event is kept. All other duplicates are removed from the results. **Default:** 1
* keepempty: optional. if true, keep the document if the any field in the field-list has NULL value or field is MISSING. **Default:** false.
* consecutive: optional. If set to true, removes only events with duplicate combinations of values that are consecutive. **Default:** false.
* field-list: mandatory. The comma-delimited field list. At least one field is required.


Example 1: Dedup by one field
=============================

The example show dedup the document with gender field.

PPL query::

    od> source=accounts | dedup gender | fields account_number, gender;
    fetched rows / total rows = 2/2
    +------------------+----------+
    | account_number   | gender   |
    |------------------+----------|
    | 1                | M        |
    | 13               | F        |
    +------------------+----------+

Example 2: Keep 2 duplicates documents
======================================

The example show dedup the document with gender field keep 2 duplication.

PPL query::

    od> source=accounts | dedup 2 gender | fields account_number, gender;
    fetched rows / total rows = 3/3
    +------------------+----------+
    | account_number   | gender   |
    |------------------+----------|
    | 1                | M        |
    | 6                | M        |
    | 13               | F        |
    +------------------+----------+

Example 3: Keep or Ignore the empty field by default
============================================

The example show dedup the document by keep null value field.

PPL query::

    od> source=accounts | dedup email keepempty=true | fields account_number, email;
    fetched rows / total rows = 4/4
    +------------------+-----------------------+
    | account_number   | email                 |
    |------------------+-----------------------|
    | 1                | amberduke@pyrami.com  |
    | 6                | hattiebond@netagy.com |
    | 13               | null                  |
    | 18               | daleadams@boink.com   |
    +------------------+-----------------------+


The example show dedup the document by ignore the empty value field.

PPL query::

    od> source=accounts | dedup email | fields account_number, email;
    fetched rows / total rows = 3/3
    +------------------+-----------------------+
    | account_number   | email                 |
    |------------------+-----------------------|
    | 1                | amberduke@pyrami.com  |
    | 6                | hattiebond@netagy.com |
    | 18               | daleadams@boink.com   |
    +------------------+-----------------------+


Example 4: Dedup in consecutive document
=========================================

The example show dedup the consecutive document.

PPL query::

    od> source=accounts | dedup gender consecutive=true | fields account_number, gender;
    fetched rows / total rows = 3/3
    +------------------+----------+
    | account_number   | gender   |
    |------------------+----------|
    | 1                | M        |
    | 13               | F        |
    | 18               | M        |
    +------------------+----------+

