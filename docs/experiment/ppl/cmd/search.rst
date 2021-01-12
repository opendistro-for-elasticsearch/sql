=============
search
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| Using ``search`` command to retrieve document from the index. ``search`` command could be only used as the first command in the PPL query.


Syntax
============
search source=<index> [boolean-expression]

* search: search keywords, which could be ignore.
* index: mandatory. search command must specify which index to query from.
* bool-expression: optional. any expression which could be evaluated to boolean value.


Example 1: Fetch all the data
=============================

The example show fetch all the document from accounts index.

PPL query::

    od> source=accounts;
    fetched rows / total rows = 4/4
    +------------------+-------------+----------------------+----------+--------+------------+-----------+------------+---------+-------+-----------------------+
    | account_number   | firstname   | address              | gender   | city   | lastname   | balance   | employer   | state   | age   | email                 |
    |------------------+-------------+----------------------+----------+--------+------------+-----------+------------+---------+-------+-----------------------|
    | 1                | Amber       | 880 Holmes Lane      | M        | Brogan | Duke       | 39225     | Pyrami     | IL      | 32    | amberduke@pyrami.com  |
    | 6                | Hattie      | 671 Bristol Street   | M        | Dante  | Bond       | 5686      | Netagy     | TN      | 36    | hattiebond@netagy.com |
    | 13               | Nanette     | 789 Madison Street   | F        | Nogal  | Bates      | 32838     | Quility    | VA      | 28    | null                  |
    | 18               | Dale        | 467 Hutchinson Court | M        | Orick  | Adams      | 4180      | null       | MD      | 33    | daleadams@boink.com   |
    +------------------+-------------+----------------------+----------+--------+------------+-----------+------------+---------+-------+-----------------------+

Example 2: Fetch data with condition
====================================

The example show fetch all the document from accounts index with .

PPL query::

    od> source=accounts account_number=1 or gender="F";
    fetched rows / total rows = 2/2
    +------------------+-------------+--------------------+----------+--------+------------+-----------+------------+---------+-------+----------------------+
    | account_number   | firstname   | address            | gender   | city   | lastname   | balance   | employer   | state   | age   | email                |
    |------------------+-------------+--------------------+----------+--------+------------+-----------+------------+---------+-------+----------------------|
    | 1                | Amber       | 880 Holmes Lane    | M        | Brogan | Duke       | 39225     | Pyrami     | IL      | 32    | amberduke@pyrami.com |
    | 13               | Nanette     | 789 Madison Street | F        | Nogal  | Bates      | 32838     | Quility    | VA      | 28    | null                 |
    +------------------+-------------+--------------------+----------+--------+------------+-----------+------------+---------+-------+----------------------+

