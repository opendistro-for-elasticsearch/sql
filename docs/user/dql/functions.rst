=============
SQL Functions
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2

Introduction
============

hello


Mathematical Functions
======================

hello


Date and Time Functions
=======================

ADDDATE
-------

Description
>>>>>>>>>>>

Usage: adddate(date, INTERVAL expr unit) adds the time interval of second argument to date; adddate(date, days) adds the second argument as integer number of days to date.

Argument type: DATE/DATETIME/TIMESTAMP, INTERVAL/LONG

Return type map:

(DATE/DATETIME/TIMESTAMP, INTERVAL) -> DATETIME
(DATE, LONG) -> DATE
(DATETIME/TIMESTAMP, LONG) -> DATETIME

Synonyms: `DATE_ADD`_

Example::

    >od SELECT ADDDATE(DATE('2020-08-26'), INTERVAL 1 HOUR), ADDDATE(DATE('2020-08-26'), 1)
    fetched rows / total rows = 1/1
    +------------------------------------------------+----------------------------------+
    | ADDDATE(DATE('2020-08-26'), INTERVAL 1 HOUR)   | ADDDATE(DATE('2020-08-26'), 1)   |
    |------------------------------------------------+----------------------------------|
    | DATETIME '2020-08-26 01:00:00'                 | DATE '2020-08-26'                |
    +------------------------------------------------+----------------------------------+


DATE_ADD
--------
