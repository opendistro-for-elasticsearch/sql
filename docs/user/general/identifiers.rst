===========
Identifiers
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

Identifiers are used for naming your database objects, such as index name, field name, alias etc. Basically there are two types of identifiers: regular identifiers and delimited identifiers.


Regular Identifiers
===================

Description
-----------

According to ANSI SQL standard, a regular identifier is a string of characters that must start with ASCII letter (lower or upper case). The subsequent character can be a combination of letter, digit, underscore (``_``). It cannot be a reversed key word. And whitespace and other special characters are not allowed. Additionally in our SQL parser, we make extension to the rule for Elasticsearch storage as shown in next sub-section.

Extensions
----------

For Elasticsearch, the following identifiers are supported extensionally by our SQL parser for convenience (without the need of being delimited as shown in next section):

1. Identifiers prefixed by dot ``.``: this is called hidden index in Elasticsearch, for example ``.kibana``.
2. Identifiers prefixed by at sign ``@``: this is common for meta fields generated in Logstash ingestion.
3. Identifiers with ``-`` in the middle: this is mostly the case for index name with date information.
4. Identifiers with star ``*`` present: this is mostly an index pattern for wildcard match.

Examples
--------

Here are examples for using index pattern directly without quotes::

    od> SELECT * FROM *cc*nt*;
    fetched rows / total rows = 4/4
    +------------------+-----------+-------------+------------+-------+----------+----------------------+------------+-----------------------+--------+---------+
    | account_number   | balance   | firstname   | lastname   | age   | gender   | address              | employer   | email                 | city   | state   |
    |------------------+-----------+-------------+------------+-------+----------+----------------------+------------+-----------------------+--------+---------|
    | 1                | 39225     | Amber       | Duke       | 32    | M        | 880 Holmes Lane      | Pyrami     | amberduke@pyrami.com  | Brogan | IL      |
    | 6                | 5686      | Hattie      | Bond       | 36    | M        | 671 Bristol Street   | Netagy     | hattiebond@netagy.com | Dante  | TN      |
    | 13               | 32838     | Nanette     | Bates      | 28    | F        | 789 Madison Street   | Quility    | null                  | Nogal  | VA      |
    | 18               | 4180      | Dale        | Adams      | 33    | M        | 467 Hutchinson Court | null       | daleadams@boink.com   | Orick  | MD      |
    +------------------+-----------+-------------+------------+-------+----------+----------------------+------------+-----------------------+--------+---------+


Delimited Identifiers
=====================

Description
-----------

A delimited identifier is an identifier enclosed in back ticks ````` or double quotation marks ``"``. In this case, the identifier enclosed is not necessarily a regular identifier. In other words, it can contain any special character not allowed by regular identifier.

Please note the difference between single quote and double quotes in SQL syntax. Single quote is used to enclose a string literal while double quotes have same purpose as back ticks to escape special characters in an identifier.

Use Cases
---------

Here are typical examples of the use of delimited identifiers:

1. Identifiers of reserved key word name
2. Identifiers with dot ``.`` present: similarly as ``-`` in index name to include date information, it is required to be quoted so parser can differentiate it from identifier with qualifiers.
3. Identifiers with other special character: Elasticsearch has its own rule which allows more special character, for example Unicode character is supported in index name.

Examples
--------

Here are examples for quoting an index name by back ticks::

    od> SELECT * FROM `accounts`;
    fetched rows / total rows = 4/4
    +------------------+-----------+-------------+------------+-------+----------+----------------------+------------+-----------------------+--------+---------+
    | account_number   | balance   | firstname   | lastname   | age   | gender   | address              | employer   | email                 | city   | state   |
    |------------------+-----------+-------------+------------+-------+----------+----------------------+------------+-----------------------+--------+---------|
    | 1                | 39225     | Amber       | Duke       | 32    | M        | 880 Holmes Lane      | Pyrami     | amberduke@pyrami.com  | Brogan | IL      |
    | 6                | 5686      | Hattie      | Bond       | 36    | M        | 671 Bristol Street   | Netagy     | hattiebond@netagy.com | Dante  | TN      |
    | 13               | 32838     | Nanette     | Bates      | 28    | F        | 789 Madison Street   | Quility    | null                  | Nogal  | VA      |
    | 18               | 4180      | Dale        | Adams      | 33    | M        | 467 Hutchinson Court | null       | daleadams@boink.com   | Orick  | MD      |
    +------------------+-----------+-------------+------------+-------+----------+----------------------+------------+-----------------------+--------+---------+


Case Sensitivity
================

Description
-----------

In SQL-92, regular identifiers are case insensitive and converted to upper case automatically just like key word. While characters in a delimited identifier appear as they are. However, in our SQL implementation, identifiers are treated in case sensitive manner. So it must be exactly same as what is stored in Elasticsearch which is different from ANSI standard.

Examples
--------

For example, if you run ``SELECT * FROM ACCOUNTS``, it will end up with an index not found exception from our plugin because the actual index name is under lower case.


Identifier Qualifiers
=====================

For now, we do not support using Elasticsearch cluster name as catalog name to qualify an index name, such as ``my-cluster.logs``.

TODO: field name qualifiers
