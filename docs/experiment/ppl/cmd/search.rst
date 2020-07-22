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
    +------------------+-----------+-------------+------------+-------+----------+----------------------+------------+-----------------------+--------+---------+
    | account_number   | balance   | firstname   | lastname   | age   | gender   | address              | employer   | email                 | city   | state   |
    |------------------+-----------+-------------+------------+-------+----------+----------------------+------------+-----------------------+--------+---------|
    | 1                | 39225     | Amber       | Duke       | 32    | M        | 880 Holmes Lane      | Pyrami     | amberduke@pyrami.com  | Brogan | IL      |
    | 6                | 5686      | Hattie      | Bond       | 36    | M        | 671 Bristol Street   | Netagy     | hattiebond@netagy.com | Dante  | TN      |
    | 13               | 32838     | Nanette     | Bates      | 28    | F        | 789 Madison Street   | Quility    | null                  | Nogal  | VA      |
    | 18               | 4180      | Dale        | Adams      | 33    | M        | 467 Hutchinson Court | null       | daleadams@boink.com   | Orick  | MD      |
    +------------------+-----------+-------------+------------+-------+----------+----------------------+------------+-----------------------+--------+---------+

Example 2: Fetch data with condition
====================================

The example show fetch all the document from accounts index with .

PPL query::

    od> source=accounts account_number=1 or gender="F";
    fetched rows / total rows = 2/2
    +------------------+-----------+-------------+------------+-------+----------+--------------------+------------+----------------------+--------+---------+
    | account_number   | balance   | firstname   | lastname   | age   | gender   | address            | employer   | email                | city   | state   |
    |------------------+-----------+-------------+------------+-------+----------+--------------------+------------+----------------------+--------+---------|
    | 1                | 39225     | Amber       | Duke       | 32    | M        | 880 Holmes Lane    | Pyrami     | amberduke@pyrami.com | Brogan | IL      |
    | 13               | 32838     | Nanette     | Bates      | 28    | F        | 789 Madison Street | Quility    | null                 | Nogal  | VA      |
    +------------------+-----------+-------------+------------+-------+----------+--------------------+------------+----------------------+--------+---------+

