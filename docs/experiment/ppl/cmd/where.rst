=============
where
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| The ``where`` command bool-expression to filter the search result. The ``where`` command only return the result when bool-expression evaluated to true.


Syntax
============
where <boolean-expression>

* bool-expression: optional. any expression which could be evaluated to boolean value.

Example 1: Filter result set with condition
===========================================

The example show fetch all the document from accounts index with .

PPL query::

    od> source=accounts | where account_number=1 or gender="F" | fields account_number, gender;
    fetched rows / total rows = 2/2
    +------------------+----------+
    | account_number   | gender   |
    |------------------+----------|
    | 1                | M        |
    | 13               | F        |
    +------------------+----------+

