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
    +------------------+-------------+----------------------+-----------+----------+--------+------------+---------+-------+-----------------------+------------+
    | account_number   | firstname   | address              | balance   | gender   | city   | employer   | state   | age   | email                 | lastname   |
    |------------------+-------------+----------------------+-----------+----------+--------+------------+---------+-------+-----------------------+------------|
    | 1                | Amber       | 880 Holmes Lane      | 39225     | M        | Brogan | Pyrami     | IL      | 32    | amberduke@pyrami.com  | Duke       |
    | 6                | Hattie      | 671 Bristol Street   | 5686      | M        | Dante  | Netagy     | TN      | 36    | hattiebond@netagy.com | Bond       |
    | 13               | Nanette     | 789 Madison Street   | 32838     | F        | Nogal  | Quility    | VA      | 28    | null                  | Bates      |
    | 18               | Dale        | 467 Hutchinson Court | 4180      | M        | Orick  | null       | MD      | 33    | daleadams@boink.com   | Adams      |
    +------------------+-------------+----------------------+-----------+----------+--------+------------+---------+-------+-----------------------+------------+

Example 2: Fetch data with condition
====================================

The example show fetch all the document from accounts index with .

PPL query::

    od> source=accounts account_number=1 or gender="F";
    fetched rows / total rows = 2/2
    +------------------+-------------+--------------------+-----------+----------+--------+------------+---------+-------+----------------------+------------+
    | account_number   | firstname   | address            | balance   | gender   | city   | employer   | state   | age   | email                | lastname   |
    |------------------+-------------+--------------------+-----------+----------+--------+------------+---------+-------+----------------------+------------|
    | 1                | Amber       | 880 Holmes Lane    | 39225     | M        | Brogan | Pyrami     | IL      | 32    | amberduke@pyrami.com | Duke       |
    | 13               | Nanette     | 789 Madison Street | 32838     | F        | Nogal  | Quility    | VA      | 28    | null                 | Bates      |
    +------------------+-------------+--------------------+-----------+----------+--------+------------+---------+-------+----------------------+------------+

