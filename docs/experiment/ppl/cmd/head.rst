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
head [keeplast = (true | false)] [while "("<boolean-expression>")"] [N]

* keeplast: optional. use in conjunction with the while argument to determine whether the last result in the result set is retained. The last result returned is the result that caused the <boolean-expression> to evaluate to false or NULL. Set keeplast=true to retain the last result in the result set. Set keeplast=false to discard the last result. **Default:** true
* while: optional. expression that evaluates to either true or false. statistical functions can not be used in the expression.  **Default:** false
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

Example 3: Get first N results with while condition
===========================================================

The example show first N results from accounts index with while condition.

PPL query::

    od> source=accounts | fields firstname, age | sort age | head while(age < 21) 7;
    fetched rows / total rows = 4/4
    +---------------+-----------+
    | firstname     | age       |
    |---------------+-----------|
    | Claudia       | 20        |
    | Copeland      | 20        |
    | Cornelia      | 20        |
    | Schultz       | 20        |
    | Simpson       | 21        |
    +---------------+-----------+

Example 4: Get first N results with while condition and last result which failed condition
==========================================================================================

The example show first N results with while condition and last result which failed condition.

PPL query::

    od> source=accounts | fields firstname, age | sort age | head keeplast=false while(age < 21) 7;
    fetched rows / total rows = 4/4
    +---------------+-----------+
    | firstname     | age       |
    |---------------+-----------|
    | Claudia       | 20        |
    | Copeland      | 20        |
    | Cornelia      | 20        |
    | Schultz       | 20        |
    +---------------+-----------+

