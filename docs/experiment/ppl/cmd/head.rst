=============
head
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| The ``head`` command returns the first N number of specified results in search order.


Syntax
============
head [N]

* N: optional. number of results to return. **Default:** 10

Example 1: Get first 10 results
===========================================

The example show first 10 results from accounts index.

PPL query::

    od> source=accounts | fields firstname, age | head;
    fetched rows / total rows = 10/10
    +---------------+-----------+
    | firstname     | age       |
    |---------------+-----------|
    | Amber         | 32        |
    | Hattie        | 36        |
    | Nanette       | 28        |
    | Dale          | 33        |
    | Elinor        | 36        |
    | Virginia      | 39        |
    | Dillard       | 34        |
    | Mcgee         | 39        |
    | Aurelia       | 37        |
    | Fulton        | 23        |
    +---------------+-----------+

Example 2: Get first N results
===========================================

The example show first N results from accounts index.

PPL query::

    od> source=accounts | fields firstname, age | head 3;
    fetched rows / total rows = 3/3
    +---------------+-----------+
    | firstname     | age       |
    |---------------+-----------|
    | Amber         | 32        |
    | Hattie        | 36        |
    | Nanette       | 28        |
    +---------------+-----------+

